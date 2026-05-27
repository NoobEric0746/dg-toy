package org.nooberic.dg_toy.client;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.nooberic.dg_toy.Dg_toy;
import org.nooberic.dg_toy.network.DgToyC2SConnectionStatusPacket;
import org.nooberic.dg_toy.network.DgToyNetworking;
import org.nooberic.dglib.api.DgLibApi;

@Mod.EventBusSubscriber(modid = Dg_toy.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class DgToyClientConnectionReporter {
    private static final long SYNC_INTERVAL_MS = 30_000L;

    private static boolean lastConnected = false;
    private static long lastSyncAtMs = 0L;

    private DgToyClientConnectionReporter() {
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
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.getConnection() == null) {
            return;
        }

        boolean connected = DgLibApi.get().isPaired();
        long now = System.currentTimeMillis();
        if (connected != lastConnected || now - lastSyncAtMs >= SYNC_INTERVAL_MS) {
            syncState(connected);
        }
    }

    public static void syncNow() {
        syncState(DgLibApi.get().isPaired());
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