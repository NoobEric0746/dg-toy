package org.nooberic.dg_toy.server;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.nooberic.dg_toy.item.PowerboxItem;
import org.nooberic.dg_toy.item.RemoteControlItem;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.UUID;
import java.util.function.Consumer;

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
        var server = net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return;
        }

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            forEachHeldStack(player, stack -> clearIfTargetsPlayer(stack, targetPlayerId));
        }
    }

    public static void clearPlayerPairing(ServerPlayer player) {
        PlayerPairingData.setBound(player, false);
    }

    public static boolean hasEquippedPowerbox(ServerPlayer player) {
        return CuriosApi.getCuriosInventory(player)
                .map(handler -> !handler.findCurios(RemoteControlStateHelper::isPowerbox).isEmpty())
                .orElse(false);
    }

    public static boolean isShockAvailable(ServerPlayer player) {
        return hasEquippedPowerbox(player);
    }

    @Nullable
    public static ServerPlayer findOnlinePlayer(UUID playerId) {
        var server = net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();
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
        var server = net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return;
        }

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            forEachHeldStack(player, held -> {
                if (held == stack) {
                    PlayerMessageCompat.displayClientMessage(player, RemoteControlItem.createRemoteDisconnectedMessage(boundPlayerId, boundPlayerName), true);
                }
            });
        }
    }

    /** 遍历玩家主背包 + 装备槽（含副手）中的所有物品。 */
    private static void forEachHeldStack(ServerPlayer player, Consumer<ItemStack> action) {
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            action.accept(inventory.getItem(i));
        }
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = player.getItemBySlot(slot);
            if (!stack.isEmpty()) {
                action.accept(stack);
            }
        }
    }

    private static boolean isPowerbox(ItemStack stack) {
        return stack.getItem() instanceof PowerboxItem;
    }
}
