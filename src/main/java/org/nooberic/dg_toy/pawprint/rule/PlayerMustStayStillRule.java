package org.nooberic.dg_toy.pawprint.rule;

import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import org.nooberic.dg_toy.pawprint.BasePawprintRuleHandler;
import org.nooberic.dg_toy.pawprint.PawprintEffect;
import org.nooberic.dg_toy.pawprint.PawprintRuleContext;
import org.nooberic.dg_toy.pawprint.PawprintRuleNbtHelper;

public final class PlayerMustStayStillRule extends BasePawprintRuleHandler<ServerboundMovePlayerPacket> {
    private static final String RULE_ID = "prohibit_movement";

    @Override
    public String getDisplayName() {
        return "禁止移动";
    }

    @Override
    public String getDescription() {
        return "移动时按冷却间隔持续触发电击惩罚。";
    }

    @Override
    public String getRuleId() {
        return RULE_ID;
    }

    @Override
    public Class<ServerboundMovePlayerPacket> getEventType() {
        return ServerboundMovePlayerPacket.class;
    }

    @Override
    protected int getCooldownTicks(PawprintRuleContext<ServerboundMovePlayerPacket> context) {
        return 25;
    }

    @Override
    protected boolean matches(PawprintRuleContext<ServerboundMovePlayerPacket> context) {
        return true;
    }

    @Override
    protected PawprintEffect resolveEffect(PawprintRuleContext<ServerboundMovePlayerPacket> context) {
        return new PawprintEffect(PawprintRuleNbtHelper.getChannel(context.pawprintStack()), PawprintRuleNbtHelper.getStrength(context.pawprintStack()), PawprintRuleNbtHelper.isHardMode(context.pawprintStack()), "const", 1);
    }
}