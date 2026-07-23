package org.nooberic.dg_toy.pawprint.rule;

import org.nooberic.dg_toy.event.RedstoneControllerActivatedEvent;
import org.nooberic.dg_toy.pawprint.BasePawprintRuleHandler;
import org.nooberic.dg_toy.pawprint.PawprintEffect;
import org.nooberic.dg_toy.pawprint.PawprintRuleContext;
import org.nooberic.dg_toy.pawprint.PawprintRuleNbtHelper;
import org.nooberic.dg_toy.pawprint.PawprintRuleVariable;

import java.util.List;

public final class ReceiveChannelBRule extends BasePawprintRuleHandler<RedstoneControllerActivatedEvent> {
    private static final String RULE_ID = "receive_channel_b";
    private static final PawprintRuleVariable<String> CHANNEL_B = new PawprintRuleVariable<>(
            "frequency_b", "接收频道 B", String.class, "0", ReceiveChannelBRule::isValidFrequency);

    @Override
    public String getDisplayName() {
        return "接收频道 B";
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
        return List.of(CHANNEL_B);
    }

    @Override
    protected boolean matches(PawprintRuleContext<RedstoneControllerActivatedEvent> context) {
        String frequency = PawprintRuleNbtHelper.getStringVariable(context.pawprintStack(), RULE_ID, CHANNEL_B);
        int sharedChannel = context.event().getChannel() & PawprintRuleNbtHelper.getChannel(context.pawprintStack());
        return Integer.parseInt(frequency) == context.event().getFrequency() && sharedChannel != 0;
    }

    @Override
    protected PawprintEffect resolveEffect(PawprintRuleContext<RedstoneControllerActivatedEvent> context) {
        RedstoneControllerActivatedEvent event = context.event();
        int sharedChannel = event.getChannel() & PawprintRuleNbtHelper.getChannel(context.pawprintStack());
        return new PawprintEffect(sharedChannel, event.getStrength(), event.isHardMode(), event.getPulseId(), event.getDuration());
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