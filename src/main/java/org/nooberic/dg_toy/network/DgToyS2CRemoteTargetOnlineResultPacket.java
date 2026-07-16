package org.nooberic.dg_toy.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import org.nooberic.dg_toy.client.PlayerStatusClientCache;

import java.util.UUID;
import java.util.function.Supplier;

public class DgToyS2CRemoteTargetOnlineResultPacket {
    private final UUID targetPlayerId;
    private final boolean online;

    public DgToyS2CRemoteTargetOnlineResultPacket(UUID targetPlayerId, boolean online) {
        this.targetPlayerId = targetPlayerId;
        this.online = online;
    }

    public static void encode(DgToyS2CRemoteTargetOnlineResultPacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.targetPlayerId);
        buf.writeBoolean(msg.online);
    }

    public static DgToyS2CRemoteTargetOnlineResultPacket decode(FriendlyByteBuf buf) {
        return new DgToyS2CRemoteTargetOnlineResultPacket(buf.readUUID(), buf.readBoolean());
    }

    public static void handle(DgToyS2CRemoteTargetOnlineResultPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            if (Minecraft.getInstance().player == null) {
                return;
            }
            PlayerStatusClientCache.put(msg.targetPlayerId, msg.online, false, 0L, System.currentTimeMillis());
        });
        ctx.setPacketHandled(true);
    }
}