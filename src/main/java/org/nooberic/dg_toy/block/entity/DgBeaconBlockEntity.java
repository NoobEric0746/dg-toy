package org.nooberic.dg_toy.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.nooberic.dg_toy.item.DgBeaconItem;

import java.util.UUID;

public class DgBeaconBlockEntity extends BlockEntity {
    private UUID collarUuid;
    private int minimumDistanceLimit;
    private int maximumDistanceLimit = 18;

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

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (this.collarUuid != null) {
            tag.putUUID("CollarUuid", this.collarUuid);
        }
        tag.putInt("MinimumDistanceLimit", this.minimumDistanceLimit);
        tag.putInt("MaximumDistanceLimit", this.maximumDistanceLimit);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.collarUuid = tag.hasUUID("CollarUuid") ? tag.getUUID("CollarUuid") : null;
        this.minimumDistanceLimit = clampDistanceLimit(tag.getInt("MinimumDistanceLimit"));
        this.maximumDistanceLimit = tag.contains("MaximumDistanceLimit") ? clampDistanceLimit(tag.getInt("MaximumDistanceLimit")) : 18;
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private void sync() {
        this.setChanged();
        if (this.level != null && !this.level.isClientSide) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    private static int clampDistanceLimit(int value) {
        return Math.max(0, Math.min(100, value));
    }
}