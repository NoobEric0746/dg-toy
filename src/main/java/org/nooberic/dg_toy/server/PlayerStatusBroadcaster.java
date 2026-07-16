package org.nooberic.dg_toy.server;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.nooberic.dg_toy.Dg_toy;
import org.nooberic.dg_toy.network.DgToyNetworking;
import org.nooberic.dg_toy.network.DgToyS2CPlayerStatusSyncPacket;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = Dg_toy.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class PlayerStatusBroadcaster {
    private static final int SYNC_INTERVAL_TICKS = 20;
    private static final Map<UUID, SentStatus> LAST_SENT = new ConcurrentHashMap<>();
    private static int tickCounter = 0;

    private PlayerStatusBroadcaster() {
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || ServerLifecycleHooks.getCurrentServer() == null) {
            return;
        }

        tickCounter++;
        if (tickCounter < SYNC_INTERVAL_TICKS) {
            return;
        }
        tickCounter = 0;

        for (ServerPlayer player : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()) {
            broadcastIfChanged(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            broadcastOffline(serverPlayer);
            LAST_SENT.remove(serverPlayer.getUUID());
            VirtualPairingManager.clearPairingWindow(serverPlayer.getUUID());
            PlayerConnectionSyncManager.clear(serverPlayer.getUUID());
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) {
            return;
        }

        PairingClearSavedData clearData = PairingClearSavedData.get(serverPlayer.server);
        if (clearData.getClearFlag(serverPlayer.getUUID())) {
            clearData.setClearFlag(serverPlayer.getUUID(), false);
            PlayerPairingData.setBound(serverPlayer, false);
            serverPlayer.displayClientMessage(net.minecraft.network.chat.Component.translatable("item.dg_toy.powerbox.disconnected_by_remote"), true);
        }
        broadcastNow(serverPlayer);
    }

    public static void broadcastNow(ServerPlayer sourcePlayer) {
        if (ServerLifecycleHooks.getCurrentServer() == null) {
            return;
        }
        sendStatusIfChanged(sourcePlayer, false);
    }

    private static void broadcastIfChanged(ServerPlayer sourcePlayer) {
        sendStatusIfChanged(sourcePlayer, true);
    }

    private static void sendStatusIfChanged(ServerPlayer sourcePlayer, boolean onlyIfChanged) {
        boolean connected = PlayerConnectionSyncManager.isConnected(sourcePlayer.getUUID());
        boolean virtuallyBound = PlayerPairingData.isBound(sourcePlayer);
        long pairingExpiresAtMs = toEpochMillisFromTicks(VirtualPairingManager.getPairingExpiresAt(sourcePlayer.getUUID()));
        long updatedAtMs = Math.max(PlayerConnectionSyncManager.getUpdatedAtMs(sourcePlayer.getUUID()), VirtualPairingManager.getCurrentLogicalTime(sourcePlayer));
        SentStatus newStatus = new SentStatus(connected, virtuallyBound, pairingExpiresAtMs, updatedAtMs);
        SentStatus oldStatus = LAST_SENT.put(sourcePlayer.getUUID(), newStatus);
        if (onlyIfChanged && newStatus.equals(oldStatus)) {
            return;
        }

        DgToyS2CPlayerStatusSyncPacket packet = new DgToyS2CPlayerStatusSyncPacket(sourcePlayer.getUUID(), connected, virtuallyBound, pairingExpiresAtMs, updatedAtMs);
        for (ServerPlayer target : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()) {
            DgToyNetworking.sendToPlayer(target, packet);
        }
    }

    private static void broadcastOffline(ServerPlayer sourcePlayer) {
        if (ServerLifecycleHooks.getCurrentServer() == null) {
            return;
        }

        long updatedAtMs = Math.max(PlayerConnectionSyncManager.getUpdatedAtMs(sourcePlayer.getUUID()), System.currentTimeMillis());
        DgToyS2CPlayerStatusSyncPacket packet = new DgToyS2CPlayerStatusSyncPacket(sourcePlayer.getUUID(), false, false, 0L, updatedAtMs);
        for (ServerPlayer target : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()) {
            if (!target.getUUID().equals(sourcePlayer.getUUID())) {
                DgToyNetworking.sendToPlayer(target, packet);
            }
        }
    }

    private static long toEpochMillisFromTicks(long expiresAtTick) {
        if (expiresAtTick <= 0L) {
            return 0L;
        }
        long nowTick = VirtualPairingManager.getCurrentLogicalTime();
        long remainingTicks = Math.max(0L, expiresAtTick - nowTick);
        return System.currentTimeMillis() + remainingTicks * 50L;
    }

    private record SentStatus(boolean connected, boolean virtuallyBound, long pairingExpiresAtMs, long updatedAtMs) {
    }
}
