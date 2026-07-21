package org.nooberic.dg_toy.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import org.nooberic.dg_toy.item.DgBeaconItem;
import org.nooberic.dg_toy.item.ModItems;

import java.util.function.Supplier;

public class DgToyC2SBeaconDistanceLimitsSyncPacket {
    private final int minimumDistanceLimit;
    private final int maximumDistanceLimit;

    public DgToyC2SBeaconDistanceLimitsSyncPacket(int minimumDistanceLimit, int maximumDistanceLimit) {
        this.minimumDistanceLimit = minimumDistanceLimit;
        this.maximumDistanceLimit = maximumDistanceLimit;
    }

    public static void encode(DgToyC2SBeaconDistanceLimitsSyncPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.minimumDistanceLimit);
        buf.writeInt(msg.maximumDistanceLimit);
    }

    public static DgToyC2SBeaconDistanceLimitsSyncPacket decode(FriendlyByteBuf buf) {
        return new DgToyC2SBeaconDistanceLimitsSyncPacket(buf.readInt(), buf.readInt());
    }

    public static void handle(DgToyC2SBeaconDistanceLimitsSyncPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer sender = ctx.getSender();
            if (sender == null) {
                return;
            }

            ItemStack mainHand = sender.getMainHandItem();
            if (!mainHand.is(ModItems.DG_BEACON.get())) {
                return;
            }

            DgBeaconItem.setDistanceLimits(mainHand, msg.minimumDistanceLimit, msg.maximumDistanceLimit);
            sender.getInventory().setChanged();
        });
        ctx.setPacketHandled(true);
    }
}