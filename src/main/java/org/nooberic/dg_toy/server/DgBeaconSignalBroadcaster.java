package org.nooberic.dg_toy.server;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.nooberic.dg_toy.Dg_toy;
import org.nooberic.dg_toy.event.DgBeaconSignalEvent;
import org.nooberic.dg_toy.item.DgBeaconItem;

import java.util.UUID;

@EventBusSubscriber(modid = Dg_toy.MODID)
public final class DgBeaconSignalBroadcaster {
    private static final int SIGNAL_INTERVAL_TICKS = 10;
    private static int tickCounter;

    private DgBeaconSignalBroadcaster() {
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (ServerLifecycleHooks.getCurrentServer() == null) {
            return;
        }

        tickCounter++;
        if (tickCounter < SIGNAL_INTERVAL_TICKS) {
            return;
        }
        tickCounter = 0;

        for (ServerPlayer player : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()) {
            broadcastPlayerHotbarSignals(player);
        }
    }

    private static void broadcastPlayerHotbarSignals(ServerPlayer player) {
        for (int hotbarSlot = 0; hotbarSlot < 9; hotbarSlot++) {
            ItemStack stack = player.getInventory().getItem(hotbarSlot);
            if (!(stack.getItem() instanceof DgBeaconItem)) {
                continue;
            }

            UUID collarUuid = DgBeaconItem.getCollarUuid(stack);
            if (collarUuid == null) {
                continue;
            }

            NeoForge.EVENT_BUS.post(new DgBeaconSignalEvent(
                    player.position().add(0.0D, player.getBbHeight() * 0.5D, 0.0D),
                    collarUuid,
                    DgBeaconItem.getMinimumDistanceLimit(stack),
                    DgBeaconItem.getMaximumDistanceLimit(stack)));
        }
    }
}