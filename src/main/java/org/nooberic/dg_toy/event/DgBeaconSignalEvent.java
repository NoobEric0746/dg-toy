package org.nooberic.dg_toy.event;

import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.Event;

import java.util.UUID;

public final class DgBeaconSignalEvent extends Event {
    private final Vec3 sourcePosition;
    private final UUID collarUuid;
    private final int minimumDistanceLimit;
    private final int maximumDistanceLimit;

    public DgBeaconSignalEvent(Vec3 sourcePosition, UUID collarUuid, int minimumDistanceLimit, int maximumDistanceLimit) {
        this.sourcePosition = sourcePosition;
        this.collarUuid = collarUuid;
        this.minimumDistanceLimit = minimumDistanceLimit;
        this.maximumDistanceLimit = maximumDistanceLimit;
    }

    public Vec3 getSourcePosition() {
        return sourcePosition;
    }

    public UUID getCollarUuid() {
        return collarUuid;
    }

    public int getMinimumDistanceLimit() {
        return minimumDistanceLimit;
    }

    public int getMaximumDistanceLimit() {
        return maximumDistanceLimit;
    }
}