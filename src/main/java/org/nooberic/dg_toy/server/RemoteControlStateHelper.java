package org.nooberic.dg_toy.server;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.nooberic.dg_toy.item.RemoteControlItem;

import java.util.UUID;

public final class RemoteControlStateHelper {
    private RemoteControlStateHelper() {
    }

    public static boolean invalidateIfCleared(ItemStack stack) {
        UUID boundPlayerId = RemoteControlItem.getBoundPlayerUuid(stack);
        if (boundPlayerId == null) {
            return false;
        }

        String boundPlayerName = RemoteControlItem.getBoundPlayerName(stack);

        long boundTime = RemoteControlItem.getBoundTime(stack);
        long clearTime = PairingClearSavedData.get().getClearTime(boundPlayerId);
        if (boundTime < clearTime) {
            notifyRemoteDisconnected(stack, boundPlayerId, boundPlayerName);
            RemoteControlItem.unbind(stack);
            return true;
        }
        return false;
    }

    public static void clearRemoteControlsForTarget(UUID targetPlayerId) {
        var server = net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return;
        }

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            for (ItemStack stack : player.getInventory().items) {
                clearIfTargetsPlayer(stack, targetPlayerId);
            }
            for (ItemStack stack : player.getInventory().armor) {
                clearIfTargetsPlayer(stack, targetPlayerId);
            }
            for (ItemStack stack : player.getInventory().offhand) {
                clearIfTargetsPlayer(stack, targetPlayerId);
            }
        }
    }

    public static void clearPlayerPairing(ServerPlayer player) {
        PlayerPairingData.setBound(player, false);
    }

    @Nullable
    public static ServerPlayer findOnlinePlayer(UUID playerId) {
        var server = net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();
        return server == null ? null : server.getPlayerList().getPlayer(playerId);
    }

    private static void clearIfTargetsPlayer(ItemStack stack, UUID targetPlayerId) {
        if (!(stack.getItem() instanceof RemoteControlItem)) {
            return;
        }
        UUID boundPlayerId = RemoteControlItem.getBoundPlayerUuid(stack);
        if (targetPlayerId.equals(boundPlayerId)) {
            invalidateIfCleared(stack);
        }
    }

    private static void notifyRemoteDisconnected(ItemStack stack, UUID boundPlayerId, @Nullable String boundPlayerName) {
        var server = net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return;
        }

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            notifyIfHoldingStack(player, stack, boundPlayerId, boundPlayerName, player.getInventory().items);
            notifyIfHoldingStack(player, stack, boundPlayerId, boundPlayerName, player.getInventory().armor);
            notifyIfHoldingStack(player, stack, boundPlayerId, boundPlayerName, player.getInventory().offhand);
        }
    }

    private static void notifyIfHoldingStack(ServerPlayer holder, ItemStack targetStack, UUID boundPlayerId, @Nullable String boundPlayerName, Iterable<ItemStack> stacks) {
        for (ItemStack stack : stacks) {
            if (stack == targetStack) {
                holder.displayClientMessage(RemoteControlItem.createRemoteDisconnectedMessage(boundPlayerId, boundPlayerName), true);
                return;
            }
        }
    }
}