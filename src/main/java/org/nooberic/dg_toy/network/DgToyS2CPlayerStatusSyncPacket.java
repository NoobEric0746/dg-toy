package org.nooberic.dg_toy.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import org.nooberic.dg_toy.client.PlayerStatusClientCache;

import java.util.UUID;
import java.util.function.Supplier;

public class DgToyS2CPlayerStatusSyncPacket {
    private final UUID playerId;
    private final boolean connected;
    private final boolean virtuallyBound;
    private final long pairingExpiresAtMs;
    private final long updatedAtMs;

    public DgToyS2CPlayerStatusSyncPacket(UUID playerId, boolean connected, boolean virtuallyBound, long pairingExpiresAtMs, long updatedAtMs) {
        this.playerId = playerId;
        this.connected = connected;
        this.virtuallyBound = virtuallyBound;
        this.pairingExpiresAtMs = pairingExpiresAtMs;
        this.updatedAtMs = updatedAtMs;
    }

    public static void encode(DgToyS2CPlayerStatusSyncPacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.playerId);
        buf.writeBoolean(msg.connected);
        buf.writeBoolean(msg.virtuallyBound);
        buf.writeLong(msg.pairingExpiresAtMs);
        buf.writeLong(msg.updatedAtMs);
    }

    public static DgToyS2CPlayerStatusSyncPacket decode(FriendlyByteBuf buf) {
        return new DgToyS2CPlayerStatusSyncPacket(
                buf.readUUID(),
                buf.readBoolean(),
                buf.readBoolean(),
                buf.readLong(),
                buf.readLong()
        );
    }

    public static void handle(DgToyS2CPlayerStatusSyncPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            if (Minecraft.getInstance().level == null) {
                return;
            }
            PlayerStatusClientCache.put(msg.playerId, msg.connected, msg.virtuallyBound, msg.pairingExpiresAtMs, msg.updatedAtMs);
        });
        ctx.setPacketHandled(true);
    }
}
