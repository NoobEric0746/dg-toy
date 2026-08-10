package org.nooberic.dg_toy.server;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.nooberic.dg_toy.Dg_toy;
import org.nooberic.dg_toy.item.PowerboxItem;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@EventBusSubscriber(modid = Dg_toy.MODID)
public final class VirtualPairingManager {
    public static final long PAIRING_WINDOW_TICKS = 200L;

    private static final Map<UUID, Long> ACTIVE_PAIRING = new ConcurrentHashMap<>();

    private VirtualPairingManager() {
    }

    public static boolean canEnterPairing(ServerPlayer player) {
        return !PlayerPairingData.isBound(player) && isHoldingPowerbox(player);
    }

    public static long startPairing(ServerPlayer player) {
        long expiresAt = getCurrentLogicalTime(player) + PAIRING_WINDOW_TICKS;
        ACTIVE_PAIRING.put(player.getUUID(), expiresAt);
        return expiresAt;
    }

    public static boolean isPairingActive(UUID playerId) {
        Long expiresAt = ACTIVE_PAIRING.get(playerId);
        return expiresAt != null && expiresAt > getCurrentLogicalTime();
    }

    public static long getPairingExpiresAt(UUID playerId) {
        Long expiresAt = ACTIVE_PAIRING.get(playerId);
        if (expiresAt == null || expiresAt <= getCurrentLogicalTime()) {
            ACTIVE_PAIRING.remove(playerId);
            return 0L;
        }
        return expiresAt;
    }

    public static boolean bind(ServerPlayer player, UUID remoteId) {
        UUID playerId = player.getUUID();
        if (PlayerPairingData.isBound(player) || !isPairingActive(playerId)) {
            return false;
        }
        PlayerPairingData.setBoundRemoteId(player, remoteId);
        ACTIVE_PAIRING.remove(playerId);
        return true;
    }

    public static void clearPairingWindow(UUID playerId) {
        ACTIVE_PAIRING.remove(playerId);
    }

    public static void cleanupPlayerState(ServerPlayer player) {
        UUID playerId = player.getUUID();
        if (!isHoldingPowerbox(player)) {
            ACTIVE_PAIRING.remove(playerId);
            PlayerStatusBroadcaster.broadcastNow(player);
            return;
        }

        Long expiresAt = ACTIVE_PAIRING.get(playerId);
        if (expiresAt != null && expiresAt <= getCurrentLogicalTime(player)) {
            ACTIVE_PAIRING.remove(playerId);
            if (!PlayerPairingData.isBound(player)) {
                PlayerStatusBroadcaster.broadcastNow(player);
            }
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (ServerLifecycleHooks.getCurrentServer() == null) {
            return;
        }

        for (ServerPlayer player : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()) {
            cleanupPlayerState(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            cleanupPlayerState(serverPlayer);
        }
    }

    public static long getCurrentLogicalTime(ServerPlayer player) {
        return player.level().getServer().overworld().getGameTime();
    }

    public static long getCurrentLogicalTime() {
        var server = net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();
        return server == null ? 0L : server.overworld().getGameTime();
    }

    private static boolean isHoldingPowerbox(ServerPlayer player) {
        return isPowerbox(player.getItemInHand(InteractionHand.MAIN_HAND))
                || isPowerbox(player.getItemInHand(InteractionHand.OFF_HAND));
    }

    private static boolean isPowerbox(ItemStack stack) {
        return stack.getItem() instanceof PowerboxItem;
    }
}
