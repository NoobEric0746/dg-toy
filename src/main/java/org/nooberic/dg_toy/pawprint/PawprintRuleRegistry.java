package org.nooberic.dg_toy.pawprint;

import org.nooberic.dg_toy.item.PawprintItem.PawprintMode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public final class PawprintRuleRegistry {
    private static final Map<Class<?>, List<BasePawprintRuleHandler<?>>> EVENT_RULES = new HashMap<>();
    private static final Map<BasePawprintRuleHandler<?>, PawprintMode> RULE_MODES = new IdentityHashMap<>();

    private PawprintRuleRegistry() {
    }

    public static <TEvent> void register(PawprintMode mode, BasePawprintRuleHandler<TEvent> handler) {
        EVENT_RULES.computeIfAbsent(handler.getEventType(), ignored -> new ArrayList<>()).add(handler);
        RULE_MODES.put(handler, mode);
    }

    @SuppressWarnings("unchecked")
    public static <TEvent> List<BasePawprintRuleHandler<TEvent>> getHandlers(Class<TEvent> eventType) {
        return (List<BasePawprintRuleHandler<TEvent>>) (List<?>) EVENT_RULES.getOrDefault(eventType, List.of());
    }

    public static Collection<BasePawprintRuleHandler<?>> allHandlers() {
        List<BasePawprintRuleHandler<?>> handlers = new ArrayList<>();
        for (List<BasePawprintRuleHandler<?>> value : EVENT_RULES.values()) {
            handlers.addAll(value);
        }
        return List.copyOf(handlers);
    }

    public static Collection<BasePawprintRuleHandler<?>> getHandlersForMode(PawprintMode mode) {
        List<BasePawprintRuleHandler<?>> handlers = new ArrayList<>();
        for (BasePawprintRuleHandler<?> handler : allHandlers()) {
            if (RULE_MODES.get(handler) == mode) {
                handlers.add(handler);
            }
        }
        return List.copyOf(handlers);
    }

    public static PawprintMode getMode(BasePawprintRuleHandler<?> handler) {
        return RULE_MODES.get(handler);
    }

    public static BasePawprintRuleHandler<?> getById(String ruleId) {
        for (BasePawprintRuleHandler<?> handler : allHandlers()) {
            if (handler.getRuleId().equals(ruleId)) {
                return handler;
            }
        }
        return null;
    }
}