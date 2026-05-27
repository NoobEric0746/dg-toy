package org.nooberic.dg_toy.client;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PlayerStatusClientCache {
    private static final Map<UUID, PlayerConnectionStatusSnapshot> CACHE = new ConcurrentHashMap<>();

    private PlayerStatusClientCache() {
    }

    public static PlayerConnectionStatusSnapshot get(UUID playerId) {
        return CACHE.getOrDefault(playerId, PlayerConnectionStatusSnapshot.empty());
    }

    public static void put(UUID playerId, boolean connected, boolean virtuallyBound, long pairingExpiresAtMs, long updatedAtMs) {
        CACHE.put(playerId, new PlayerConnectionStatusSnapshot(connected, virtuallyBound, pairingExpiresAtMs, updatedAtMs));
    }

    public static void remove(UUID playerId) {
        CACHE.remove(playerId);
    }

    public static void clear() {
        CACHE.clear();
    }
}
