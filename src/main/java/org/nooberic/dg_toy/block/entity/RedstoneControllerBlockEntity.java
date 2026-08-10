package org.nooberic.dg_toy.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RedstoneControllerBlockEntity extends BlockEntity {
    private static final int DEFAULT_DURATION = 1;
    private static final int DEFAULT_CHANNEL = 3;

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
    protected void saveAdditional(ValueOutput output) {
        output.putInt("Strength", this.strength);
        output.putBoolean("HardMode", this.hardMode);
        output.putString("PulseId", this.pulseId);
        output.putInt("Duration", this.duration);
        output.putInt("Channel", this.channel);
        output.putInt("Frequency", this.frequency);
    }

    @Override
    public void loadAdditional(ValueInput input) {
        this.strength = clamp(input.getIntOr("Strength", 0), 0, 100);
        this.hardMode = input.getBooleanOr("HardMode", true);
        this.pulseId = input.getStringOr("PulseId", "");
        this.duration = clamp(input.getIntOr("Duration", DEFAULT_DURATION), 1, 10);
        this.channel = clamp(input.getIntOr("Channel", DEFAULT_CHANNEL), 1, 3);
        this.frequency = clamp(input.getIntOr("Frequency", 0), 0, 65535);
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

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
