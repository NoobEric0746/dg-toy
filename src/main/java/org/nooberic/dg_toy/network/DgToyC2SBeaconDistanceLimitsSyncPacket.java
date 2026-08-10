package org.nooberic.dg_toy.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.Nullable;
import org.nooberic.dg_toy.Dg_toy;
import org.nooberic.dg_toy.block.entity.DgBeaconBlockEntity;
import org.nooberic.dg_toy.item.DgBeaconItem;
import org.nooberic.dg_toy.item.ModItems;

public record DgToyC2SBeaconDistanceLimitsSyncPacket(
        int minimumDistanceLimit,
        int maximumDistanceLimit,
        @Nullable BlockPos blockPos
) implements CustomPacketPayload {
    public static final Type<DgToyC2SBeaconDistanceLimitsSyncPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(Dg_toy.MODID, "c2s_beacon_distance_limits_sync"));

    public static final StreamCodec<FriendlyByteBuf, DgToyC2SBeaconDistanceLimitsSyncPacket> STREAM_CODEC = StreamCodec.of(
            (buf, msg) -> {
                buf.writeInt(msg.minimumDistanceLimit);
                buf.writeInt(msg.maximumDistanceLimit);
                buf.writeBoolean(msg.blockPos != null);
                if (msg.blockPos != null) {
                    buf.writeBlockPos(msg.blockPos);
                }
            },
            buf -> {
                int minimumDistanceLimit = buf.readInt();
                int maximumDistanceLimit = buf.readInt();
                return buf.readBoolean()
                        ? new DgToyC2SBeaconDistanceLimitsSyncPacket(minimumDistanceLimit, maximumDistanceLimit, buf.readBlockPos())
                        : new DgToyC2SBeaconDistanceLimitsSyncPacket(minimumDistanceLimit, maximumDistanceLimit, null);
            }
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(DgToyC2SBeaconDistanceLimitsSyncPacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer sender)) {
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
    }
}
