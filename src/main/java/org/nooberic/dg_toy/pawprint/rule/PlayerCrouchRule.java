package org.nooberic.dg_toy.pawprint.rule;

import net.minecraftforge.event.TickEvent;
import org.nooberic.dg_toy.pawprint.BasePawprintRuleHandler;
import org.nooberic.dg_toy.pawprint.PawprintEffect;
import org.nooberic.dg_toy.pawprint.PawprintRuleContext;
import org.nooberic.dg_toy.pawprint.PawprintRuleNbtHelper;

public final class PlayerCrouchRule extends BasePawprintRuleHandler<TickEvent.PlayerTickEvent> {
    private static final String RULE_ID = "player_crouch";

    @Override
    public String getDisplayName() {
        return "禁止潜行";
    }

    @Override
    public String getDescription() {
        return "玩家保持下蹲时，按冷却间隔持续触发。";
    }

    @Override
    public String getRuleId() {
        return RULE_ID;
    }

    @Override
    public Class<TickEvent.PlayerTickEvent> getEventType() {
        return TickEvent.PlayerTickEvent.class;
    }

    @Override
    protected int getCooldownTicks(PawprintRuleContext<TickEvent.PlayerTickEvent> context) {
        return 25;
    }

    @Override
    protected boolean matches(PawprintRuleContext<TickEvent.PlayerTickEvent> context) {
        return context.owner().isShiftKeyDown();
    }

    @Override
    protected PawprintEffect resolveEffect(PawprintRuleContext<TickEvent.PlayerTickEvent> context) {
        return new PawprintEffect(PawprintRuleNbtHelper.getChannel(context.pawprintStack()), PawprintRuleNbtHelper.getStrength(context.pawprintStack()), PawprintRuleNbtHelper.isHardMode(context.pawprintStack()), "const", 1);
    }
}