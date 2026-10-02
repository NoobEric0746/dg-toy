package org.nooberic.dg_toy.pawprint.rule;

import net.minecraftforge.event.entity.living.LivingDeathEvent;
import org.nooberic.dg_toy.pawprint.BasePawprintRuleHandler;
import org.nooberic.dg_toy.pawprint.PawprintEffect;
import org.nooberic.dg_toy.pawprint.PawprintRuleContext;
import org.nooberic.dg_toy.pawprint.PawprintRuleNbtHelper;

public final class PlayerDeathShockRule extends BasePawprintRuleHandler<LivingDeathEvent> {
    private static final String RULE_ID = "player_death_shock";
    private static final int DEATH_STRENGTH = 100;
    private static final int DEATH_DURATION_SECONDS = 2;

    @Override
    public String getDisplayName() {
        return "死亡电击";
    }

    @Override
    public String getDescription() {
        return "玩家死亡时以 100 强度（软模式）电击 2 秒，波形 const。";
    }

    @Override
    public String getRuleId() {
        return RULE_ID;
    }

    @Override
    public Class<LivingDeathEvent> getEventType() {
        return LivingDeathEvent.class;
    }

    @Override
    protected boolean matches(PawprintRuleContext<LivingDeathEvent> context) {
        return true;
    }

    @Override
    protected PawprintEffect resolveEffect(PawprintRuleContext<LivingDeathEvent> context) {
        return new PawprintEffect(
                PawprintRuleNbtHelper.getChannel(context.pawprintStack()),
                DEATH_STRENGTH,
                false,
                "const",
                DEATH_DURATION_SECONDS);
    }
}
