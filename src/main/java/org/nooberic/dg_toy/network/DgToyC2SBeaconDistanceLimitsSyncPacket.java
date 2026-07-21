package org.nooberic.dg_toy.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;
import org.nooberic.dg_toy.block.entity.DgBeaconBlockEntity;
import org.nooberic.dg_toy.item.DgBeaconItem;
import org.nooberic.dg_toy.item.ModItems;

import java.util.function.Supplier;

public class DgToyC2SBeaconDistanceLimitsSyncPacket {
    private final int minimumDistanceLimit;
    private final int maximumDistanceLimit;
    @Nullable
    private final BlockPos blockPos;

    public DgToyC2SBeaconDistanceLimitsSyncPacket(int minimumDistanceLimit, int maximumDistanceLimit) {
        this(minimumDistanceLimit, maximumDistanceLimit, null);
    }

    public DgToyC2SBeaconDistanceLimitsSyncPacket(int minimumDistanceLimit, int maximumDistanceLimit, BlockPos blockPos) {
        this.minimumDistanceLimit = minimumDistanceLimit;
        this.maximumDistanceLimit = maximumDistanceLimit;
        this.blockPos = blockPos;
    }

    public static void encode(DgToyC2SBeaconDistanceLimitsSyncPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.minimumDistanceLimit);
        buf.writeInt(msg.maximumDistanceLimit);
        buf.writeBoolean(msg.blockPos != null);
        if (msg.blockPos != null) {
            buf.writeBlockPos(msg.blockPos);
        }
    }

    public static DgToyC2SBeaconDistanceLimitsSyncPacket decode(FriendlyByteBuf buf) {
        int minimumDistanceLimit = buf.readInt();
        int maximumDistanceLimit = buf.readInt();
        return buf.readBoolean()
                ? new DgToyC2SBeaconDistanceLimitsSyncPacket(minimumDistanceLimit, maximumDistanceLimit, buf.readBlockPos())
                : new DgToyC2SBeaconDistanceLimitsSyncPacket(minimumDistanceLimit, maximumDistanceLimit);
    }

    public static void handle(DgToyC2SBeaconDistanceLimitsSyncPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer sender = ctx.getSender();
            if (sender == null) {
                return;
            }

            if (msg.blockPos != null) {
                if (sender.distanceToSqr(msg.blockPos.getX() + 0.5, msg.blockPos.getY() + 0.5, msg.blockPos.getZ() + 0.5) > 64.0
                        || !(sender.level().getBlockEntity(msg.blockPos) instanceof DgBeaconBlockEntity beacon)) {
                    return;
                }
                beacon.setDistanceLimits(msg.minimumDistanceLimit, msg.maximumDistanceLimit);
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