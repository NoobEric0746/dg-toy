package org.nooberic.dg_toy.client;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.nooberic.dg_toy.Dg_toy;
import org.nooberic.dg_toy.network.DgToyC2SConnectionStatusPacket;
import org.nooberic.dg_toy.network.DgToyNetworking;
import org.nooberic.dglib.api.DgLibApi;
import org.nooberic.dglib.service.DeviceStatus;

@EventBusSubscriber(modid = Dg_toy.MODID, value = Dist.CLIENT)
public final class DgToyClientConnectionReporter {
    private static final long SYNC_INTERVAL_MS = 30_000L;

    private static boolean lastConnected = false;
    private static long lastSyncAtMs = 0L;

    private DgToyClientConnectionReporter() {
    }

    private static boolean isConnectedToLocalCoyoteHost() {
        DeviceStatus status = DgLibApi.get().getStatus();
        return status != null
                && status.getClientId() != null
                && !status.getClientId().isEmpty()
                && status.getTargetId() != null
                && !status.getTargetId().isEmpty();
    }

    @SubscribeEvent
    public static void onClientLogin(ClientPlayerNetworkEvent.LoggingIn event) {
        lastConnected = false;
        lastSyncAtMs = 0L;
        syncNow();
    }

    @SubscribeEvent
    public static void onClientLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        lastConnected = false;
        lastSyncAtMs = 0L;
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.getConnection() == null) {
            return;
        }

        boolean connected = isConnectedToLocalCoyoteHost();
        long now = System.currentTimeMillis();
        if (connected != lastConnected || now - lastSyncAtMs >= SYNC_INTERVAL_MS) {
            syncState(connected);
        }
    }

    public static void syncNow() {
        syncState(isConnectedToLocalCoyoteHost());
    }

    public static void syncState(boolean connected) {
        Minecraft minecraft = Minecraft.getInstance();
        lastConnected = connected;
        lastSyncAtMs = System.currentTimeMillis();
        if (minecraft.getConnection() == null) {
            return;
        }
        DgToyNetworking.sendToServer(new DgToyC2SConnectionStatusPacket(connected, lastSyncAtMs));
    }
}