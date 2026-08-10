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

public record DgToyS2CRemoteTargetOnlineResultPacket(UUID targetPlayerId, boolean online, boolean available) implements CustomPacketPayload {
    public static final Type<DgToyS2CRemoteTargetOnlineResultPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(Dg_toy.MODID, "s2c_remote_target_online_result"));

    public static final StreamCodec<FriendlyByteBuf, DgToyS2CRemoteTargetOnlineResultPacket> STREAM_CODEC = StreamCodec.of(
            (buf, msg) -> {
                buf.writeUUID(msg.targetPlayerId);
                buf.writeBoolean(msg.online);
                buf.writeBoolean(msg.available);
            },
            buf -> new DgToyS2CRemoteTargetOnlineResultPacket(buf.readUUID(), buf.readBoolean(), buf.readBoolean())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(DgToyS2CRemoteTargetOnlineResultPacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (Minecraft.getInstance().player == null) {
                return;
            }
            PlayerStatusClientCache.put(msg.targetPlayerId, true, msg.available, false, 0L, System.currentTimeMillis());
        });
    }
}
