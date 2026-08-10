package org.nooberic.dg_toy.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.nooberic.dg_toy.Dg_toy;
import org.nooberic.dg_toy.server.PlayerConnectionSyncManager;

public record DgToyC2SConnectionStatusPacket(boolean connected, long updatedAtMs) implements CustomPacketPayload {
    public static final Type<DgToyC2SConnectionStatusPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(Dg_toy.MODID, "c2s_connection_status"));

    public static final StreamCodec<FriendlyByteBuf, DgToyC2SConnectionStatusPacket> STREAM_CODEC = StreamCodec.of(
            (buf, msg) -> {
                buf.writeBoolean(msg.connected);
                buf.writeLong(msg.updatedAtMs);
            },
            buf -> new DgToyC2SConnectionStatusPacket(buf.readBoolean(), buf.readLong())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(DgToyC2SConnectionStatusPacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer sender)) {
                return;
            }
            PlayerConnectionSyncManager.update(sender.getUUID(), msg.connected, msg.updatedAtMs);
        });
    }
}
