package org.nooberic.dg_toy.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.nooberic.dg_toy.Dg_toy;
import org.nooberic.dg_toy.server.RemoteControlStateHelper;

import java.util.UUID;

public record DgToyC2SRemoteTargetOnlineQueryPacket(UUID targetPlayerId) implements CustomPacketPayload {
    public static final Type<DgToyC2SRemoteTargetOnlineQueryPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(Dg_toy.MODID, "c2s_remote_target_online_query"));

    public static final StreamCodec<FriendlyByteBuf, DgToyC2SRemoteTargetOnlineQueryPacket> STREAM_CODEC = StreamCodec.of(
            (buf, msg) -> buf.writeUUID(msg.targetPlayerId),
            buf -> new DgToyC2SRemoteTargetOnlineQueryPacket(buf.readUUID())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(DgToyC2SRemoteTargetOnlineQueryPacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer sender)) {
                return;
            }

            ServerPlayer targetPlayer = RemoteControlStateHelper.findOnlinePlayer(msg.targetPlayerId);
            boolean online = targetPlayer != null;
            boolean available = targetPlayer != null && RemoteControlStateHelper.isShockAvailable(targetPlayer);
            DgToyNetworking.sendToPlayer(sender, new DgToyS2CRemoteTargetOnlineResultPacket(msg.targetPlayerId, online, available));
        });
    }
}
