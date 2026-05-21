package org.nooberic.dg_toy.client;

import org.nooberic.dglib.service.ConnectionState;

public record PlayerConnectionStatusSnapshot(
        ConnectionState connectionState,
        boolean paired,
        long updatedAtMs
) {
    public static PlayerConnectionStatusSnapshot empty() {
        return new PlayerConnectionStatusSnapshot(ConnectionState.DISCONNECTED, false, 0L);
    }
}
