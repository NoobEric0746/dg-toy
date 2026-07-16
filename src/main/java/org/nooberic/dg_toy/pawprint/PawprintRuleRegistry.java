package org.nooberic.dg_toy.pawprint;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class PawprintRuleRegistry {
    private static final Map<Class<?>, List<BasePawprintRuleHandler<?>>> EVENT_RULES = new HashMap<>();

    private PawprintRuleRegistry() {
    }

    public static <TEvent> void register(BasePawprintRuleHandler<TEvent> handler) {
        EVENT_RULES.computeIfAbsent(handler.getEventType(), ignored -> new ArrayList<>()).add(handler);
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

    public static BasePawprintRuleHandler<?> getById(String ruleId) {
        for (BasePawprintRuleHandler<?> handler : allHandlers()) {
            if (handler.getRuleId().equals(ruleId)) {
                return handler;
            }
        }
        return null;
    }
}