package org.nooberic.dg_toy.pawprint.rule;

import net.minecraftforge.event.ServerChatEvent;
import org.nooberic.dg_toy.pawprint.BasePawprintRuleHandler;
import org.nooberic.dg_toy.pawprint.PawprintEffect;
import org.nooberic.dg_toy.pawprint.PawprintRuleContext;
import org.nooberic.dg_toy.pawprint.PawprintRuleNbtHelper;
import org.nooberic.dg_toy.pawprint.PawprintRuleVariable;

import java.util.List;

public final class ChatContainsKeywordRule extends BasePawprintRuleHandler<ServerChatEvent> {
    private static final String RULE_ID = "chat_contains_keyword";
    private static final PawprintRuleVariable<String> KEYWORD = new PawprintRuleVariable<>("keyword", "关键词", String.class, "", value -> !value.isBlank());

    @Override
    public String getDisplayName() {
        return "禁止包含关键词";
    }

    @Override
    public String getDescription() {
        return "聊天内容包含设置的关键词时触发。";
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
        return List.of(KEYWORD);
    }

    @Override
    protected int getCooldownTicks(PawprintRuleContext<ServerChatEvent> context) {
        return 20;
    }

    @Override
    protected boolean matches(PawprintRuleContext<ServerChatEvent> context) {
        String keyword = PawprintRuleNbtHelper.getStringVariable(context.pawprintStack(), RULE_ID, KEYWORD);
        String message = context.event().getMessage().getString();
        return !keyword.isBlank() && message.contains(keyword);
    }

    @Override
    protected PawprintEffect resolveEffect(PawprintRuleContext<ServerChatEvent> context) {
        return new PawprintEffect(PawprintRuleNbtHelper.getChannel(context.pawprintStack()), PawprintRuleNbtHelper.getStrength(context.pawprintStack()), PawprintRuleNbtHelper.isHardMode(context.pawprintStack()), "chaos", 5);
    }
}