package org.nooberic.dg_toy.server;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.nooberic.dg_toy.item.PowerboxItem;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class VirtualPairingManager {
    public static final long PAIRING_WINDOW_MS = 10_000L;

    private static final Map<UUID, Long> ACTIVE_PAIRING = new ConcurrentHashMap<>();
    private static final Map<UUID, UUID> BOUND_REMOTE_BY_PLAYER = new ConcurrentHashMap<>();

    private VirtualPairingManager() {
    }

    public static boolean canEnterPairing(ServerPlayer player) {
        return !isVirtuallyBound(player.getUUID()) && isHoldingPowerbox(player);
    }

    public static long startPairing(ServerPlayer player) {
        long expiresAt = System.currentTimeMillis() + PAIRING_WINDOW_MS;
        ACTIVE_PAIRING.put(player.getUUID(), expiresAt);
        return expiresAt;
    }

    public static boolean isPairingActive(UUID playerId) {
        Long expiresAt = ACTIVE_PAIRING.get(playerId);
        return expiresAt != null && expiresAt > System.currentTimeMillis();
    }

    public static long getPairingExpiresAt(UUID playerId) {
        Long expiresAt = ACTIVE_PAIRING.get(playerId);
        if (expiresAt == null || expiresAt <= System.currentTimeMillis()) {
            ACTIVE_PAIRING.remove(playerId);
            return 0L;
        }
        return expiresAt;
    }

    public static boolean isVirtuallyBound(UUID playerId) {
        return BOUND_REMOTE_BY_PLAYER.containsKey(playerId);
    }

    public static UUID getBoundRemote(UUID playerId) {
        return BOUND_REMOTE_BY_PLAYER.get(playerId);
    }

    public static boolean bind(UUID playerId, UUID remoteId) {
        if (isVirtuallyBound(playerId) || !isPairingActive(playerId)) {
            return false;
        }
        BOUND_REMOTE_BY_PLAYER.put(playerId, remoteId);
        ACTIVE_PAIRING.remove(playerId);
        return true;
    }

    public static void clearPlayer(UUID playerId) {
        ACTIVE_PAIRING.remove(playerId);
        BOUND_REMOTE_BY_PLAYER.remove(playerId);
    }

    public static void cleanupPlayerState(ServerPlayer player) {
        UUID playerId = player.getUUID();
        if (!isHoldingPowerbox(player)) {
            ACTIVE_PAIRING.remove(playerId);
            return;
        }

        Long expiresAt = ACTIVE_PAIRING.get(playerId);
        if (expiresAt != null && expiresAt <= System.currentTimeMillis()) {
            ACTIVE_PAIRING.remove(playerId);
        }
    }

    private static boolean isHoldingPowerbox(ServerPlayer player) {
        return isPowerbox(player.getItemInHand(InteractionHand.MAIN_HAND))
                || isPowerbox(player.getItemInHand(InteractionHand.OFF_HAND));
    }

    private static boolean isPowerbox(ItemStack stack) {
        return stack.getItem() instanceof PowerboxItem;
    }
}