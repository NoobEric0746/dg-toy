package org.nooberic.dg_toy.server;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PlayerConnectionSyncManager {
    private static final Map<UUID, ConnectionSyncState> CONNECTION_STATES = new ConcurrentHashMap<>();

    private PlayerConnectionSyncManager() {
    }

    public static void update(UUID playerId, boolean connected, long updatedAtMs) {
        CONNECTION_STATES.put(playerId, new ConnectionSyncState(connected, updatedAtMs));
    }

    public static boolean isConnected(UUID playerId) {
        ConnectionSyncState state = CONNECTION_STATES.get(playerId);
        return state != null && state.connected();
    }

    public static long getUpdatedAtMs(UUID playerId) {
        ConnectionSyncState state = CONNECTION_STATES.get(playerId);
        return state == null ? 0L : state.updatedAtMs();
    }

    public static void clear(UUID playerId) {
        CONNECTION_STATES.remove(playerId);
    }

    private record ConnectionSyncState(boolean connected, long updatedAtMs) {
    }
}