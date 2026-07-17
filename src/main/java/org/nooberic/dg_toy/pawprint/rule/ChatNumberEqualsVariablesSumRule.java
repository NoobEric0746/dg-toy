package org.nooberic.dg_toy.pawprint.rule;

import net.minecraftforge.event.ServerChatEvent;
import org.nooberic.dg_toy.pawprint.BasePawprintRuleHandler;
import org.nooberic.dg_toy.pawprint.PawprintEffect;
import org.nooberic.dg_toy.pawprint.PawprintRuleContext;
import org.nooberic.dg_toy.pawprint.PawprintRuleNbtHelper;
import org.nooberic.dg_toy.pawprint.PawprintRuleVariable;

import java.util.List;

public final class ChatNumberEqualsVariablesSumRule extends BasePawprintRuleHandler<ServerChatEvent> {
    private static final String RULE_ID = "chat_number_equals_variables_sum";
    private static final List<PawprintRuleVariable<String>> VARIABLES = List.of(
            integerVariable("variable_1", "变量1"),
            integerVariable("variable_2", "变量2"),
            integerVariable("variable_3", "变量3"),
            integerVariable("variable_4", "变量4"),
            integerVariable("variable_5", "变量5"),
            integerVariable("variable_6", "变量6")
    );

    @Override
    public String getDisplayName() {
        return "聊天数字等于变量之和";
    }

    @Override
    public String getDescription() {
        return "聊天文本为数字且等于变量1至变量6之和时触发。";
    }

    @Override
    public String getRuleId() {
        return RULE_ID;
    }

    @Override
    public Class<ServerChatEvent> getEventType() {
        return ServerChatEvent.class;
    }

    @Override
    public List<PawprintRuleVariable<?>> getVariables() {
        return List.copyOf(VARIABLES);
    }

    @Override
    protected boolean matches(PawprintRuleContext<ServerChatEvent> context) {
        int messageNumber;
        try {
            messageNumber = Integer.parseInt(context.event().getMessage().getString());
        } catch (NumberFormatException ex) {
            return false;
        }

        long sum = 0;
        for (PawprintRuleVariable<String> variable : VARIABLES) {
            String value = PawprintRuleNbtHelper.getStringVariable(context.pawprintStack(), RULE_ID, variable);
            sum += Integer.parseInt(value);
        }
        return messageNumber == sum;
    }

    @Override
    protected PawprintEffect resolveEffect(PawprintRuleContext<ServerChatEvent> context) {
        return new PawprintEffect(1, 40, false, "chaos", 1);
    }

    private static PawprintRuleVariable<String> integerVariable(String key, String displayName) {
        return new PawprintRuleVariable<>(key, displayName, String.class, "0", ChatNumberEqualsVariablesSumRule::isInteger);
    }

    private static boolean isInteger(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }
}