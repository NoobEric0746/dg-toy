package org.nooberic.dg_toy.pawprint.rule;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import org.nooberic.dg_toy.pawprint.BasePawprintRuleHandler;
import org.nooberic.dg_toy.pawprint.PawprintEffect;
import org.nooberic.dg_toy.pawprint.PawprintRuleContext;
import org.nooberic.dg_toy.pawprint.PawprintRuleNbtHelper;

public final class PlayerAttackMobShockRule extends BasePawprintRuleHandler<AttackEntityEvent> {
    private static final String RULE_ID = "prohibit_attack";

    @Override
    public String getDisplayName() {
        return "禁止攻击";
    }

    @Override
    public String getDescription() {
        return "攻击生物时触发电击惩罚。";
    }

    @Override
    public String getRuleId() {
        return RULE_ID;
    }

    @Override
    public Class<AttackEntityEvent> getEventType() {
        return AttackEntityEvent.class;
    }

    @Override
    protected boolean matches(PawprintRuleContext<AttackEntityEvent> context) {
        return context.event().getTarget() instanceof LivingEntity;
    }

    @Override
    protected PawprintEffect resolveEffect(PawprintRuleContext<AttackEntityEvent> context) {
        return new PawprintEffect(PawprintRuleNbtHelper.getChannel(context.pawprintStack()), PawprintRuleNbtHelper.getStrength(context.pawprintStack()), PawprintRuleNbtHelper.isHardMode(context.pawprintStack()), "chaos", 1);
    }
}