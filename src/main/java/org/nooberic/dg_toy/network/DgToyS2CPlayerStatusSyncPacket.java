package org.nooberic.dg_toy.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.nooberic.dg_toy.Dg_toy;
import org.nooberic.dg_toy.client.PlayerStatusClientCache;

import java.util.UUID;

public record DgToyS2CPlayerStatusSyncPacket(
        UUID playerId,
        boolean showNameplate,
        boolean connected,
        boolean virtuallyBound,
        long pairingExpiresAtMs,
        long updatedAtMs
) implements CustomPacketPayload {
    public static final Type<DgToyS2CPlayerStatusSyncPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(Dg_toy.MODID, "s2c_player_status"));

    public static final StreamCodec<FriendlyByteBuf, DgToyS2CPlayerStatusSyncPacket> STREAM_CODEC = StreamCodec.of(
            (buf, msg) -> {
                buf.writeUUID(msg.playerId);
                buf.writeBoolean(msg.showNameplate);
                buf.writeBoolean(msg.connected);
                buf.writeBoolean(msg.virtuallyBound);
                buf.writeLong(msg.pairingExpiresAtMs);
                buf.writeLong(msg.updatedAtMs);
            },
            buf -> new DgToyS2CPlayerStatusSyncPacket(
                    buf.readUUID(),
                    buf.readBoolean(),
                    buf.readBoolean(),
                    buf.readBoolean(),
                    buf.readLong(),
                    buf.readLong()
            )
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(DgToyS2CPlayerStatusSyncPacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (Minecraft.getInstance().level == null) {
                return;
            }
            PlayerStatusClientCache.put(msg.playerId, msg.showNameplate, msg.connected, msg.virtuallyBound, msg.pairingExpiresAtMs, msg.updatedAtMs);
        });
    }
}
