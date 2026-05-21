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
import org.nooberic.dglib.multiplayer.CoyoteStatusSnapshot;
import org.nooberic.dglib.multiplayer.DgServerCoyoteApi;
import org.nooberic.dglib.service.ConnectionState;

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
            DgServerCoyoteApi.get().requestStatus(player.getUUID())
                    .thenAccept(snapshot -> broadcastIfChanged(player, snapshot))
                    .exceptionally(ex -> null);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        LAST_SENT.remove(event.getEntity().getUUID());
    }

    private static void broadcastIfChanged(ServerPlayer sourcePlayer, CoyoteStatusSnapshot snapshot) {
        ConnectionState connectionState = snapshot.isPaired() ? ConnectionState.PAIRED : deriveConnectionState(snapshot);
        SentStatus newStatus = new SentStatus(connectionState, snapshot.isPaired());
        SentStatus oldStatus = LAST_SENT.put(sourcePlayer.getUUID(), newStatus);
        if (newStatus.equals(oldStatus)) {
            return;
        }

        DgToyS2CPlayerStatusSyncPacket packet = new DgToyS2CPlayerStatusSyncPacket(
                sourcePlayer.getUUID(),
                connectionState,
                snapshot.isPaired(),
                snapshot.getUpdatedAtMs()
        );
        for (ServerPlayer target : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()) {
            DgToyNetworking.sendToPlayer(target, packet);
        }
    }

    private static ConnectionState deriveConnectionState(CoyoteStatusSnapshot snapshot) {
        if (!snapshot.getError().isEmpty()) {
            return ConnectionState.DISCONNECTED;
        }
        if (snapshot.getUpdatedAtMs() > 0L) {
            return ConnectionState.CONNECTED;
        }
        return ConnectionState.DISCONNECTED;
    }

    private record SentStatus(ConnectionState connectionState, boolean paired) {
    }
}
