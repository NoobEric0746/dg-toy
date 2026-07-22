package org.nooberic.dg_toy.pawprint.rule;

import org.nooberic.dg_toy.event.RedstoneControllerActivatedEvent;
import org.nooberic.dg_toy.pawprint.BasePawprintRuleHandler;
import org.nooberic.dg_toy.pawprint.PawprintEffect;
import org.nooberic.dg_toy.pawprint.PawprintRuleContext;
import org.nooberic.dg_toy.pawprint.PawprintRuleNbtHelper;
import org.nooberic.dg_toy.pawprint.PawprintRuleVariable;

import java.util.List;

public final class ReceiveChannelARule extends BasePawprintRuleHandler<RedstoneControllerActivatedEvent> {
    private static final String RULE_ID = "receive_channel_a";
    private static final PawprintRuleVariable<String> CHANNEL_A = new PawprintRuleVariable<>(
            "frequency_a", "接收频道 A", String.class, "0", ReceiveChannelARule::isValidFrequency);

    @Override
    public String getDisplayName() {
        return "接收频道 A";
    }

    @Override
    public String getDescription() {
        return "接收到相同频率的红石遥控器信号时，按信号设置执行惩罚。";
    }

    @Override
    public String getRuleId() {
        return RULE_ID;
    }

    @Override
    public Class<RedstoneControllerActivatedEvent> getEventType() {
        return RedstoneControllerActivatedEvent.class;
    }

    @Override
    public List<PawprintRuleVariable<?>> getVariables() {
        return List.of(CHANNEL_A);
    }

    @Override
    protected boolean matches(PawprintRuleContext<RedstoneControllerActivatedEvent> context) {
        String frequency = PawprintRuleNbtHelper.getStringVariable(context.pawprintStack(), RULE_ID, CHANNEL_A);
        return Integer.parseInt(frequency) == context.event().getFrequency();
    }

    @Override
    protected PawprintEffect resolveEffect(PawprintRuleContext<RedstoneControllerActivatedEvent> context) {
        RedstoneControllerActivatedEvent event = context.event();
        return new PawprintEffect(event.getChannel(), event.getStrength(), event.isHardMode(), event.getPulseId(), event.getDuration());
    }

    private static boolean isValidFrequency(String value) {
        try {
            int frequency = Integer.parseInt(value);
            return frequency >= 0 && frequency <= 65535;
        } catch (NumberFormatException exception) {
            return false;
        }
    }
}