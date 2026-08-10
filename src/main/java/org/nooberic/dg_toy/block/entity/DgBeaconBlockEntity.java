package org.nooberic.dg_toy.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.Nullable;
import org.nooberic.dg_toy.event.DgBeaconSignalEvent;
import org.nooberic.dg_toy.item.DgBeaconItem;

import java.util.UUID;

public class DgBeaconBlockEntity extends BlockEntity {
    private static final int SIGNAL_INTERVAL_TICKS = 20;

    private UUID collarUuid;
    private int minimumDistanceLimit;
    private int maximumDistanceLimit = 18;
    private int signalTicks;

    public DgBeaconBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DG_BEACON.get(), pos, state);
    }

    public void loadFromItem(ItemStack stack) {
        this.collarUuid = DgBeaconItem.getCollarUuid(stack);
        this.minimumDistanceLimit = DgBeaconItem.getMinimumDistanceLimit(stack);
        this.maximumDistanceLimit = DgBeaconItem.getMaximumDistanceLimit(stack);
        sync();
    }

    public void saveToItem(ItemStack stack) {
        DgBeaconItem.setCollarUuid(stack, this.collarUuid);
        DgBeaconItem.setDistanceLimits(stack, this.minimumDistanceLimit, this.maximumDistanceLimit);
    }

    @Nullable
    public UUID getCollarUuid() {
        return this.collarUuid;
    }

    public int getMinimumDistanceLimit() {
        return this.minimumDistanceLimit;
    }

    public int getMaximumDistanceLimit() {
        return this.maximumDistanceLimit;
    }

    public void setDistanceLimits(int minimumDistanceLimit, int maximumDistanceLimit) {
        this.minimumDistanceLimit = clampDistanceLimit(minimumDistanceLimit);
        this.maximumDistanceLimit = clampDistanceLimit(maximumDistanceLimit);
        sync();
    }

    @Nullable
    public static <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level) {
        return level.isClientSide() ? null : (tickerLevel, tickerPos, tickerState, blockEntity) -> {
            if (blockEntity instanceof DgBeaconBlockEntity beacon) {
                beacon.broadcastSignal();
            }
        };
    }

    private void broadcastSignal() {
        if (++this.signalTicks < SIGNAL_INTERVAL_TICKS) {
            return;
        }
        this.signalTicks = 0;

        if (this.collarUuid == null) {
            return;
        }

        if (this.level != null && this.level.hasNeighborSignal(this.worldPosition)) {
            return;
        }

        NeoForge.EVENT_BUS.post(new DgBeaconSignalEvent(
                Vec3.atCenterOf(this.worldPosition),
                this.collarUuid,
                this.minimumDistanceLimit,
                this.maximumDistanceLimit));
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        if (this.collarUuid != null) {
            output.putIntArray("CollarUuid", uuidToIntArray(this.collarUuid));
        }
        output.putInt("MinimumDistanceLimit", this.minimumDistanceLimit);
        output.putInt("MaximumDistanceLimit", this.maximumDistanceLimit);
    }

    @Override
    public void loadAdditional(ValueInput input) {
        this.collarUuid = input.getIntArray("CollarUuid")
                .filter(parts -> parts.length == 4)
                .map(DgBeaconBlockEntity::intArrayToUuid)
                .orElse(null);
        this.minimumDistanceLimit = clampDistanceLimit(input.getIntOr("MinimumDistanceLimit", 0));
        this.maximumDistanceLimit = clampDistanceLimit(input.getIntOr("MaximumDistanceLimit", 18));
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        return this.saveWithoutMetadata(provider);
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private void sync() {
        this.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    private static int clampDistanceLimit(int value) {
        return Math.max(0, Math.min(100, value));
    }

    private static int[] uuidToIntArray(UUID uuid) {
        return new int[]{
                (int) (uuid.getMostSignificantBits() >> 32),
                (int) uuid.getMostSignificantBits(),
                (int) (uuid.getLeastSignificantBits() >> 32),
                (int) uuid.getLeastSignificantBits()};
    }

    private static UUID intArrayToUuid(int[] parts) {
        return new UUID(
                ((long) parts[0] << 32) | (parts[1] & 0xFFFFFFFFL),
                ((long) parts[2] << 32) | (parts[3] & 0xFFFFFFFFL));
    }
}
