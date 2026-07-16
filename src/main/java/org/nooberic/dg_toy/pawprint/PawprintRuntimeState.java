package org.nooberic.dg_toy.pawprint;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PawprintRuntimeState {
    private static final Map<UUID, Map<String, Long>> LAST_TRIGGER_TICKS = new ConcurrentHashMap<>();

    private PawprintRuntimeState() {
    }

    public static boolean isOnCooldown(UUID playerId, String ruleId, long currentTick, int cooldownTicks) {
        if (cooldownTicks <= 0) {
            return false;
        }
        Long lastTick = LAST_TRIGGER_TICKS.getOrDefault(playerId, Map.of()).get(ruleId);
        return lastTick != null && currentTick - lastTick < cooldownTicks;
    }

    public static void markTriggered(UUID playerId, String ruleId, long currentTick) {
        LAST_TRIGGER_TICKS.computeIfAbsent(playerId, ignored -> new ConcurrentHashMap<>()).put(ruleId, currentTick);
    }
}