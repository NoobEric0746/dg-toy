package org.nooberic.dg_toy.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import org.nooberic.dg_toy.client.PlayerStatusClientCache;
import org.nooberic.dglib.service.ConnectionState;

import java.util.UUID;
import java.util.function.Supplier;

public class DgToyS2CPlayerStatusSyncPacket {
    private final UUID playerId;
    private final ConnectionState connectionState;
    private final boolean paired;
    private final long updatedAtMs;

    public DgToyS2CPlayerStatusSyncPacket(UUID playerId, ConnectionState connectionState, boolean paired, long updatedAtMs) {
        this.playerId = playerId;
        this.connectionState = connectionState;
        this.paired = paired;
        this.updatedAtMs = updatedAtMs;
    }

    public static void encode(DgToyS2CPlayerStatusSyncPacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.playerId);
        buf.writeEnum(msg.connectionState);
        buf.writeBoolean(msg.paired);
        buf.writeLong(msg.updatedAtMs);
    }

    public static DgToyS2CPlayerStatusSyncPacket decode(FriendlyByteBuf buf) {
        return new DgToyS2CPlayerStatusSyncPacket(
                buf.readUUID(),
                buf.readEnum(ConnectionState.class),
                buf.readBoolean(),
                buf.readLong()
        );
    }

    public static void handle(DgToyS2CPlayerStatusSyncPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            if (Minecraft.getInstance().level == null) {
                return;
            }
            PlayerStatusClientCache.put(msg.playerId, msg.connectionState, msg.paired, msg.updatedAtMs);
        });
        ctx.setPacketHandled(true);
    }
}
