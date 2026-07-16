package org.nooberic.dg_toy.client;

public record PlayerConnectionStatusSnapshot(
        boolean showNameplate,
        boolean connected,
        boolean virtuallyBound,
        long pairingExpiresAtMs,
        long updatedAtMs
) {
    public static PlayerConnectionStatusSnapshot empty() {
        return new PlayerConnectionStatusSnapshot(false, false, false, 0L, 0L);
    }
}
