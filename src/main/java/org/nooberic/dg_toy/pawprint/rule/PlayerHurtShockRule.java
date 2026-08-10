package org.nooberic.dg_toy.pawprint.rule;

import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import org.nooberic.dglib.api.DgLibApi;
import org.nooberic.dg_toy.pawprint.BasePawprintRuleHandler;
import org.nooberic.dg_toy.pawprint.PawprintEffect;
import org.nooberic.dg_toy.pawprint.PawprintRuleContext;
import org.nooberic.dg_toy.pawprint.PawprintRuleNbtHelper;

public final class PlayerHurtShockRule extends BasePawprintRuleHandler<LivingIncomingDamageEvent> {
    private static final String RULE_ID = "player_hurt_shock";
    private static final float MAX_SCALING_DAMAGE = 20.0F;

    @Override
    public String getDisplayName() {
        return "受伤电击";
    }

    @Override
    public String getDescription() {
        return "受伤时按伤害量提高电击强度，最高为玩家设备强度上限。";
    }

    @Override
    public String getRuleId() {
        return RULE_ID;
    }

    @Override
    public Class<LivingIncomingDamageEvent> getEventType() {
        return LivingIncomingDamageEvent.class;
    }

    @Override
    protected boolean matches(PawprintRuleContext<LivingIncomingDamageEvent> context) {
        return context.event().getAmount() > 0.0F;
    }

    @Override
    protected PawprintEffect resolveEffect(PawprintRuleContext<LivingIncomingDamageEvent> context) {
        int configuredStrength = PawprintRuleNbtHelper.getStrength(context.pawprintStack());
        int channel = PawprintRuleNbtHelper.getChannel(context.pawprintStack());
        int strengthLimit = Math.max(0, Math.min(200, DgLibApi.get().getStrengthLimit(channel)));
        int upperStrength = Math.max(configuredStrength, strengthLimit);
        float damageRatio = Math.min(context.event().getAmount() / MAX_SCALING_DAMAGE, 1.0F);
        int strength = Math.round(configuredStrength + (upperStrength - configuredStrength) * damageRatio);

        return new PawprintEffect(PawprintRuleNbtHelper.getChannel(context.pawprintStack()), strength, PawprintRuleNbtHelper.isHardMode(context.pawprintStack()), "chaos", 1);
    }
}
