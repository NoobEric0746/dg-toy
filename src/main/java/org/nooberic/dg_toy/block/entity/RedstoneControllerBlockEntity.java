package org.nooberic.dg_toy.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RedstoneControllerBlockEntity extends BlockEntity {
    private static final int DEFAULT_DURATION = 1;
    private static final int DEFAULT_CHANNEL = 1;

    private int strength;
    private boolean hardMode = true;
    private String pulseId = "";
    private int duration = DEFAULT_DURATION;
    private int channel = DEFAULT_CHANNEL;
    private int frequency;

    public RedstoneControllerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.REDSTONE_CONTROLLER.get(), pos, state);
    }

    public int getStrength() {
        return this.strength;
    }

    public boolean isHardMode() {
        return this.hardMode;
    }

    public String getPulseId() {
        return this.pulseId;
    }

    public int getDuration() {
        return this.duration;
    }

    public int getChannel() {
        return this.channel;
    }

    public int getFrequency() {
        return this.frequency;
    }

    public void setSettings(int strength, boolean hardMode, String pulseId, int duration, int channel, int frequency, List<String> availablePulseIds) {
        this.strength = clamp(strength, 0, 100);
        this.hardMode = hardMode;
        this.pulseId = availablePulseIds.isEmpty() ? "" : availablePulseIds.contains(pulseId) ? pulseId : availablePulseIds.get(0);
        this.duration = clamp(duration, 1, 10);
        this.channel = clamp(channel, 1, 3);
        this.frequency = clamp(frequency, 0, 65535);
        sync();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("Strength", this.strength);
        tag.putBoolean("HardMode", this.hardMode);
        tag.putString("PulseId", this.pulseId);
        tag.putInt("Duration", this.duration);
        tag.putInt("Channel", this.channel);
        tag.putInt("Frequency", this.frequency);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.strength = clamp(tag.getInt("Strength"), 0, 100);
        this.hardMode = !tag.contains("HardMode") || tag.getBoolean("HardMode");
        this.pulseId = tag.getString("PulseId");
        this.duration = tag.contains("Duration") ? clamp(tag.getInt("Duration"), 1, 10) : DEFAULT_DURATION;
        this.channel = tag.contains("Channel") ? clamp(tag.getInt("Channel"), 1, 3) : DEFAULT_CHANNEL;
        this.frequency = clamp(tag.getInt("Frequency"), 0, 65535);
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

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}