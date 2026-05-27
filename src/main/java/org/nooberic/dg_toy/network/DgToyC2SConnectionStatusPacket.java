package org.nooberic.dg_toy.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import org.nooberic.dg_toy.server.PlayerConnectionSyncManager;

import java.util.function.Supplier;

public class DgToyC2SConnectionStatusPacket {
    private final boolean connected;
    private final long updatedAtMs;

    public DgToyC2SConnectionStatusPacket(boolean connected, long updatedAtMs) {
        this.connected = connected;
        this.updatedAtMs = updatedAtMs;
    }

    public static void encode(DgToyC2SConnectionStatusPacket msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.connected);
        buf.writeLong(msg.updatedAtMs);
    }

    public static DgToyC2SConnectionStatusPacket decode(FriendlyByteBuf buf) {
        return new DgToyC2SConnectionStatusPacket(buf.readBoolean(), buf.readLong());
    }

    public static void handle(DgToyC2SConnectionStatusPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer sender = ctx.getSender();
            if (sender == null) {
                return;
            }
            PlayerConnectionSyncManager.update(sender.getUUID(), msg.connected, msg.updatedAtMs);
        });
        ctx.setPacketHandled(true);
    }
}