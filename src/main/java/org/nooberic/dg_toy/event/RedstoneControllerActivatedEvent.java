package org.nooberic.dg_toy.event;

import net.minecraft.core.BlockPos;
import net.minecraftforge.eventbus.api.Event;

public final class RedstoneControllerActivatedEvent extends Event {
    private final BlockPos blockPos;
    private final int strength;
    private final boolean hardMode;
    private final String pulseId;
    private final int duration;
    private final int channel;
    private final int frequency;

    public RedstoneControllerActivatedEvent(BlockPos blockPos, int strength, boolean hardMode, String pulseId, int duration, int channel, int frequency) {
        this.blockPos = blockPos;
        this.strength = strength;
        this.hardMode = hardMode;
        this.pulseId = pulseId;
        this.duration = duration;
        this.channel = channel;
        this.frequency = frequency;
    }

    public BlockPos getBlockPos() {
        return this.blockPos;
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
}