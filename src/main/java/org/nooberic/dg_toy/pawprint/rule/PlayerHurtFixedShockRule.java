package org.nooberic.dg_toy.pawprint.rule;

import net.minecraftforge.event.entity.living.LivingHurtEvent;
import org.nooberic.dg_toy.pawprint.BasePawprintRuleHandler;
import org.nooberic.dg_toy.pawprint.PawprintEffect;
import org.nooberic.dg_toy.pawprint.PawprintRuleContext;
import org.nooberic.dg_toy.pawprint.PawprintRuleNbtHelper;

public final class PlayerHurtFixedShockRule extends BasePawprintRuleHandler<LivingHurtEvent> {
    private static final String RULE_ID = "player_hurt_fixed_shock";

    @Override
    public String getDisplayName() {
        return "受伤固定电击";
    }

    @Override
    public String getDescription() {
        return "受到任意伤害后施加固定强度的电击 1 秒，与伤害量无关。";
    }

    @Override
    public String getRuleId() {
        return RULE_ID;
    }

    @Override
    public Class<LivingHurtEvent> getEventType() {
        return LivingHurtEvent.class;
    }

    @Override
    protected boolean matches(PawprintRuleContext<LivingHurtEvent> context) {
        return context.event().getAmount() > 0.0F;
    }

    @Override
    protected PawprintEffect resolveEffect(PawprintRuleContext<LivingHurtEvent> context) {
        return new PawprintEffect(
                PawprintRuleNbtHelper.getChannel(context.pawprintStack()),
                PawprintRuleNbtHelper.getStrength(context.pawprintStack()),
                PawprintRuleNbtHelper.isHardMode(context.pawprintStack()),
                "chaos",
                1);
    }
}
