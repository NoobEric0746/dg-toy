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
        if (tickCounter < 20) {
            return;
        }
        tickCounter = 0;

        for (ServerPlayer player : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()) {
            VirtualPairingManager.cleanupPlayerState(player);
            broadcastIfChanged(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        LAST_SENT.remove(event.getEntity().getUUID());
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            VirtualPairingManager.clearPlayer(serverPlayer.getUUID());
            PlayerConnectionSyncManager.clear(serverPlayer.getUUID());
        }
    }

    private static void broadcastIfChanged(ServerPlayer sourcePlayer) {
        boolean connected = PlayerConnectionSyncManager.isConnected(sourcePlayer.getUUID());
        boolean virtuallyBound = VirtualPairingManager.isVirtuallyBound(sourcePlayer.getUUID());
        long pairingExpiresAtMs = VirtualPairingManager.getPairingExpiresAt(sourcePlayer.getUUID());
        long updatedAtMs = Math.max(PlayerConnectionSyncManager.getUpdatedAtMs(sourcePlayer.getUUID()), pairingExpiresAtMs);
        SentStatus newStatus = new SentStatus(connected, virtuallyBound, pairingExpiresAtMs, updatedAtMs);
        SentStatus oldStatus = LAST_SENT.put(sourcePlayer.getUUID(), newStatus);
        if (newStatus.equals(oldStatus)) {
            return;
        }

        DgToyS2CPlayerStatusSyncPacket packet = new DgToyS2CPlayerStatusSyncPacket(
                sourcePlayer.getUUID(),
                connected,
                virtuallyBound,
                pairingExpiresAtMs,
                updatedAtMs
        );
        for (ServerPlayer target : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()) {
            DgToyNetworking.sendToPlayer(target, packet);
        }
    }

    private record SentStatus(boolean connected, boolean virtuallyBound, long pairingExpiresAtMs, long updatedAtMs) {
    }
}
