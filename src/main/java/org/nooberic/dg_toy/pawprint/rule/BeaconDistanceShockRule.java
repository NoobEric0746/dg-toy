package org.nooberic.dg_toy.pawprint.rule;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.nooberic.dg_toy.event.DgBeaconSignalEvent;
import org.nooberic.dg_toy.item.DgCollarItem;
import org.nooberic.dg_toy.pawprint.BasePawprintRuleHandler;
import org.nooberic.dg_toy.pawprint.PawprintEffect;
import org.nooberic.dg_toy.pawprint.PawprintRuleContext;
import org.nooberic.dg_toy.pawprint.PawprintRuleNbtHelper;

import java.util.UUID;

public final class BeaconDistanceShockRule extends BasePawprintRuleHandler<DgBeaconSignalEvent> {
    private static final String RULE_ID = "beacon_distance_shock";
    private static final double BOUNDARY_WARNING_DISTANCE = 1.0D;
    private static final double PARTICLE_SPACING = 0.25D;
    private static final DustParticleOptions WARNING_PARTICLE = new DustParticleOptions(0xFFFFFF00, 1.0F);
    private static final DustParticleOptions PENALTY_PARTICLE = new DustParticleOptions(0xFFFF0000, 1.0F);

    @Override
    public String getDisplayName() {
        return "信标距离惩罚";
    }

    @Override
    public String getDescription() {
        return "绑定信标发出的距离超出设定范围时触发电击惩罚。";
    }

    @Override
    public String getRuleId() {
        return RULE_ID;
    }

    @Override
    public Class<DgBeaconSignalEvent> getEventType() {
        return DgBeaconSignalEvent.class;
    }

    @Override
    protected int getCooldownTicks(PawprintRuleContext<DgBeaconSignalEvent> context) {
        return 10;
    }

    @Override
    protected boolean matches(PawprintRuleContext<DgBeaconSignalEvent> context) {
        UUID collarUuid = DgCollarItem.getCollarUuid(context.pawprintStack());
        if (!context.event().getCollarUuid().equals(collarUuid)) {
            return false;
        }

        double distance = context.owner().position().distanceTo(context.event().getSourcePosition());
        boolean outsideLimits = distance < context.event().getMinimumDistanceLimit()
                || distance > context.event().getMaximumDistanceLimit();
        boolean nearLimit = !outsideLimits && (distance - context.event().getMinimumDistanceLimit() < BOUNDARY_WARNING_DISTANCE
                || context.event().getMaximumDistanceLimit() - distance < BOUNDARY_WARNING_DISTANCE);
        if (outsideLimits || nearLimit) {
            showSignalLine(context, outsideLimits ? PENALTY_PARTICLE : WARNING_PARTICLE);
        }
        return outsideLimits;
    }

    private static void showSignalLine(PawprintRuleContext<DgBeaconSignalEvent> context, DustParticleOptions particle) {
        Vec3 start = context.owner().getEyePosition().add(0.0D, -0.25D, 0.0D);
        Vec3 end = context.event().getSourcePosition();
        Vec3 offset = end.subtract(start);
        double distance = offset.length();
        int particleCount = Math.max(1, (int) Math.ceil(distance / PARTICLE_SPACING));
        Vec3 step = offset.scale(1.0D / particleCount);
        ServerLevel level = context.owner().level();

        for (int index = 0; index <= particleCount; index++) {
            Vec3 particlePosition = start.add(step.scale(index));
            for (ServerPlayer player : context.owner().level().getServer().getPlayerList().getPlayers()) {
                level.sendParticles(player, particle, true, false,
                        particlePosition.x, particlePosition.y, particlePosition.z,
                        1, 0.0D, 0.0D, 0.0D, 0.0D);
            }
        }
    }

    @Override
    protected PawprintEffect resolveEffect(PawprintRuleContext<DgBeaconSignalEvent> context) {
        return new PawprintEffect(
            PawprintRuleNbtHelper.getChannel(context.pawprintStack()),
                PawprintRuleNbtHelper.getStrength(context.pawprintStack()),
                PawprintRuleNbtHelper.isHardMode(context.pawprintStack()),
                "const",
                1);
    }
}
