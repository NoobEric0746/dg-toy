package org.nooberic.dg_toy.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class DgToyConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.IntValue BEACON_SIGNAL_RANGE;
    public static final ForgeConfigSpec.IntValue REDSTONE_CONTROLLER_RANGE;
    public static final ForgeConfigSpec.IntValue ELECTRIFIED_WIRE_NETTING_STRENGTH;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("beacon");
        BEACON_SIGNAL_RANGE = builder
                .comment("Maximum distance in blocks at which players receive Beacon controller signals.")
                .defineInRange("signalRange", 32, 1, 1024);
        builder.pop();

        builder.push("redstoneController");
        REDSTONE_CONTROLLER_RANGE = builder
            .comment("Maximum distance in blocks at which players receive Redstone Controller signals.")
            .defineInRange("controlRange", 64, 1, 1024);
        builder.pop();

        builder.push("electrifiedWireNetting");
        ELECTRIFIED_WIRE_NETTING_STRENGTH = builder
            .comment("Soft shock strength applied by electrified wire netting.")
            .defineInRange("strength", 66, 0, 200);
        builder.pop();

        SPEC = builder.build();
    }

    private DgToyConfig() {
    }
}