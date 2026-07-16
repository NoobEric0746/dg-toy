package org.nooberic.dg_toy.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import org.nooberic.dg_toy.server.RemoteControlStateHelper;

import java.util.UUID;
import java.util.function.Supplier;

public class DgToyC2SRemoteTargetOnlineQueryPacket {
    private final UUID targetPlayerId;

    public DgToyC2SRemoteTargetOnlineQueryPacket(UUID targetPlayerId) {
        this.targetPlayerId = targetPlayerId;
    }

    public static void encode(DgToyC2SRemoteTargetOnlineQueryPacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.targetPlayerId);
    }

    public static DgToyC2SRemoteTargetOnlineQueryPacket decode(FriendlyByteBuf buf) {
        return new DgToyC2SRemoteTargetOnlineQueryPacket(buf.readUUID());
    }

    public static void handle(DgToyC2SRemoteTargetOnlineQueryPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer sender = ctx.getSender();
            if (sender == null) {
                return;
            }

            ServerPlayer targetPlayer = RemoteControlStateHelper.findOnlinePlayer(msg.targetPlayerId);
            boolean online = targetPlayer != null;
            boolean available = targetPlayer != null && RemoteControlStateHelper.isShockAvailable(targetPlayer);
            DgToyNetworking.sendToPlayer(sender, new DgToyS2CRemoteTargetOnlineResultPacket(msg.targetPlayerId, online, available));
        });
        ctx.setPacketHandled(true);
    }
}