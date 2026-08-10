package org.nooberic.dg_toy.pawprint.rule;

import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.nooberic.dg_toy.pawprint.BasePawprintRuleHandler;
import org.nooberic.dg_toy.pawprint.PawprintEffect;
import org.nooberic.dg_toy.pawprint.PawprintRuleContext;
import org.nooberic.dg_toy.pawprint.PawprintRuleNbtHelper;

public final class PlayerRightClickShockRule extends BasePawprintRuleHandler<PlayerInteractEvent> {
    private static final String RULE_ID = "prohibit_interaction";

    @Override
    public String getDisplayName() {
        return "禁止交互";
    }

    @Override
    public String getDescription() {
        return "右键交互时触发电击惩罚。";
    }

    @Override
    public String getRuleId() {
        return RULE_ID;
    }

    @Override
    public Class<PlayerInteractEvent> getEventType() {
        return PlayerInteractEvent.class;
    }

    @Override
    protected boolean matches(PawprintRuleContext<PlayerInteractEvent> context) {
        PlayerInteractEvent event = context.event();
        return event instanceof PlayerInteractEvent.RightClickBlock
                || event instanceof PlayerInteractEvent.RightClickEmpty
                || event instanceof PlayerInteractEvent.RightClickItem
                || event instanceof PlayerInteractEvent.EntityInteract
                || event instanceof PlayerInteractEvent.EntityInteractSpecific;
    }

    @Override
    protected PawprintEffect resolveEffect(PawprintRuleContext<PlayerInteractEvent> context) {
        return new PawprintEffect(PawprintRuleNbtHelper.getChannel(context.pawprintStack()), PawprintRuleNbtHelper.getStrength(context.pawprintStack()), PawprintRuleNbtHelper.isHardMode(context.pawprintStack()), "chaos", 1);
    }
}