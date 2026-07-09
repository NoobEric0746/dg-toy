package org.nooberic.dg_toy.item;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import org.nooberic.dg_toy.client.screen.RemoteControlScreen;
import org.nooberic.dg_toy.server.PairingClearSavedData;
import org.nooberic.dg_toy.server.PlayerConnectionSyncManager;
import org.nooberic.dg_toy.server.PlayerPairingData;
import org.nooberic.dg_toy.server.PlayerStatusBroadcaster;
import org.nooberic.dg_toy.server.RemoteControlStateHelper;
import org.nooberic.dg_toy.server.VirtualPairingManager;

import java.util.List;
import java.util.UUID;

import static net.minecraft.ChatFormatting.YELLOW;

public class RemoteControlItem extends Item {
    private static final String BOUND_PLAYER_UUID_TAG = "BoundPlayerUuid";
    private static final String BOUND_PLAYER_NAME_TAG = "BoundPlayerName";
    private static final String REMOTE_ID_TAG = "RemoteId";
    private static final String BOUND_TIME_TAG = "BoundTime";

    public RemoteControlItem() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResultHolder.pass(itemStack);
        }

        if (level.isClientSide && !player.isShiftKeyDown() && isBound(itemStack)) {
            openScreen();
            return InteractionResultHolder.sidedSuccess(itemStack, true);
        }

        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            RemoteControlStateHelper.invalidateIfCleared(itemStack);

            if (isBound(itemStack)) {
                if (player.isShiftKeyDown()) {
                    unbindFromPlayer(serverPlayer, itemStack);
                    return InteractionResultHolder.sidedSuccess(itemStack, false);
                }

                if (!player.isShiftKeyDown() && canSelfBind(player, itemStack)) {
                    serverPlayer.displayClientMessage(Component.translatable("item.dg_toy.remote_control.already_bound_disconnect_first"), true);
                    return InteractionResultHolder.sidedSuccess(itemStack, false);
                }

                return InteractionResultHolder.pass(itemStack);
            }

            if (!player.isShiftKeyDown() && canSelfBind(player, itemStack) && PlayerPairingData.isBound(serverPlayer)) {
                serverPlayer.displayClientMessage(Component.translatable("item.dg_toy.remote_control.target_already_bound"), true);
                return InteractionResultHolder.sidedSuccess(itemStack, false);
            }

            if (!player.isShiftKeyDown() && canSelfBind(player, itemStack) && !PlayerConnectionSyncManager.isConnected(serverPlayer.getUUID())) {
                serverPlayer.displayClientMessage(Component.translatable("item.dg_toy.powerbox.not_connected"), true);
                return InteractionResultHolder.sidedSuccess(itemStack, false);
            }

            if (!player.isShiftKeyDown() && canDirectSelfBind(serverPlayer, hand, itemStack)) {
                directSelfBind(serverPlayer, itemStack);
                return InteractionResultHolder.sidedSuccess(itemStack, false);
            }
        }

        return InteractionResultHolder.pass(itemStack);
    }

    @OnlyIn(Dist.CLIENT)
    private void openScreen() {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.setScreen(new RemoteControlScreen());
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity entity, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }

        if (!(entity instanceof ServerPlayer targetPlayer) || !(player instanceof ServerPlayer serverPlayer)) {
            return super.interactLivingEntity(stack, player, entity, hand);
        }

        if (hand == InteractionHand.MAIN_HAND && canSelfBind(player, stack)) {
            return InteractionResult.PASS;
        }

        ItemStack actualStack = player.getItemInHand(hand);
        RemoteControlStateHelper.invalidateIfCleared(actualStack);

        return tryBindToTarget(serverPlayer, targetPlayer, actualStack);
    }

    private static InteractionResult tryBindToTarget(ServerPlayer actor, ServerPlayer targetPlayer, ItemStack stack) {
        boolean selfBind = actor.getUUID().equals(targetPlayer.getUUID());

        if (isBound(stack)) {
            actor.displayClientMessage(Component.translatable("item.dg_toy.remote_control.already_bound_disconnect_first"), true);
            return InteractionResult.CONSUME;
        }

        if (PlayerPairingData.isBound(targetPlayer)) {
            if (!selfBind) {
                actor.displayClientMessage(Component.translatable("item.dg_toy.remote_control.target_already_bound"), true);
            }
            return InteractionResult.CONSUME;
        }

        if (!VirtualPairingManager.isPairingActive(targetPlayer.getUUID())) {
            if (!selfBind) {
                actor.displayClientMessage(Component.translatable("item.dg_toy.remote_control.target_not_pairing"), true);
            }
            return InteractionResult.PASS;
        }

        UUID remoteId = getOrCreateRemoteId(stack);
        if (!VirtualPairingManager.bind(targetPlayer, remoteId)) {
            actor.displayClientMessage(Component.translatable("item.dg_toy.remote_control.bind_failed"), true);
            return InteractionResult.CONSUME;
        }

        bindToPlayer(stack, targetPlayer.getUUID(), targetPlayer.getGameProfile().getName(), VirtualPairingManager.getCurrentLogicalTime(targetPlayer));
        PairingClearSavedData.get().setClearFlag(targetPlayer.getUUID(), false);
        PlayerStatusBroadcaster.broadcastNow(targetPlayer);
        if (!selfBind) {
            PlayerStatusBroadcaster.broadcastNow(actor);
        }
        actor.displayClientMessage(Component.translatable("item.dg_toy.remote_control.connect_success", coloredPlayerName(targetPlayer)), true);
        if (selfBind) {
            targetPlayer.displayClientMessage(Component.translatable("item.dg_toy.powerbox.connect_success_self"), true);
        } else {
            targetPlayer.displayClientMessage(Component.translatable("item.dg_toy.powerbox.connected_by_remote", coloredPlayerName(actor)), true);
        }
        return InteractionResult.CONSUME;
    }

    private static boolean canSelfBind(Player player, ItemStack mainHandStack) {
        return mainHandStack.getItem() instanceof RemoteControlItem
                && player.getOffhandItem().getItem() instanceof PowerboxItem;
    }

    private static boolean canDirectSelfBind(ServerPlayer player, InteractionHand hand, ItemStack mainHandStack) {
        return hand == InteractionHand.MAIN_HAND
                && !player.isShiftKeyDown()
                && canSelfBind(player, mainHandStack)
                && PlayerConnectionSyncManager.isConnected(player.getUUID())
                && !PlayerPairingData.isBound(player)
                && !isBound(mainHandStack);
    }

    private static void directSelfBind(ServerPlayer player, ItemStack stack) {
        UUID remoteId = getOrCreateRemoteId(stack);
        PlayerPairingData.setBoundRemoteId(player, remoteId);
        VirtualPairingManager.clearPairingWindow(player.getUUID());
        bindToPlayer(stack, player.getUUID(), player.getGameProfile().getName(), VirtualPairingManager.getCurrentLogicalTime(player));
        PairingClearSavedData.get().setClearFlag(player.getUUID(), false);
        PlayerStatusBroadcaster.broadcastNow(player);
        player.displayClientMessage(Component.translatable("item.dg_toy.remote_control.connect_success", coloredPlayerName(player)), true);
        player.displayClientMessage(Component.translatable("item.dg_toy.powerbox.connect_success_self"), true);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.dg_toy.remote_control.desc"));
        String boundPlayerName = getBoundPlayerName(stack);
        if (boundPlayerName != null) {
            tooltip.add(Component.translatable("item.dg_toy.remote_control.bound_player", Component.literal(boundPlayerName)));
        }
    }

    @Nullable
    public static String getBoundPlayerName(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(BOUND_PLAYER_NAME_TAG)) {
            String playerName = tag.getString(BOUND_PLAYER_NAME_TAG);
            if (!playerName.isBlank()) {
                return playerName;
            }
        }
        UUID playerUuid = getBoundPlayerUuid(stack);
        return playerUuid == null ? null : playerUuid.toString();
    }

    public static boolean isBound(ItemStack stack) {
        return getBoundPlayerUuid(stack) != null;
    }

    @Nullable
    public static UUID getBoundPlayerUuid(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.hasUUID(BOUND_PLAYER_UUID_TAG)) {
            return null;
        }
        return tag.getUUID(BOUND_PLAYER_UUID_TAG);
    }

    public static void bindToPlayer(ItemStack stack, UUID playerUuid, String playerName) {
        bindToPlayer(stack, playerUuid, playerName, 0L);
    }

    public static void bindToPlayer(ItemStack stack, UUID playerUuid, String playerName, long boundTime) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putUUID(BOUND_PLAYER_UUID_TAG, playerUuid);
        tag.putString(BOUND_PLAYER_NAME_TAG, playerName);
        tag.putLong(BOUND_TIME_TAG, boundTime);
    }

    public static void unbind(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return;
        }
        tag.remove(BOUND_PLAYER_UUID_TAG);
        tag.remove(BOUND_PLAYER_NAME_TAG);
        tag.remove(REMOTE_ID_TAG);
        tag.remove(BOUND_TIME_TAG);
        if (tag.isEmpty()) {
            stack.setTag(null);
        }
    }

    public static long getBoundTime(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(BOUND_TIME_TAG)) {
            return 0L;
        }
        return tag.getLong(BOUND_TIME_TAG);
    }

    public static UUID getOrCreateRemoteId(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.hasUUID(REMOTE_ID_TAG)) {
            tag.putUUID(REMOTE_ID_TAG, UUID.randomUUID());
        }
        return tag.getUUID(REMOTE_ID_TAG);
    }

    private static MutableComponent coloredPlayerName(ServerPlayer player) {
        return Component.literal(" " + player.getDisplayName().getString() + " ").setStyle(Style.EMPTY.withColor(YELLOW));
    }

    public static MutableComponent createRemoteDisconnectedMessage(UUID playerId, @Nullable String playerName) {
        return Component.translatable("item.dg_toy.remote_control.unbound_by_remote", coloredPlayerNameFromNbt(playerName));
    }

    private static void unbindFromPlayer(ServerPlayer actor, ItemStack stack) {
        UUID boundPlayerId = getBoundPlayerUuid(stack);
        if (boundPlayerId == null) {
            unbind(stack);
            return;
        }

        String boundPlayerName = getBoundPlayerName(stack);

        ServerPlayer targetPlayer = RemoteControlStateHelper.findOnlinePlayer(boundPlayerId);
        if (targetPlayer != null) {
            RemoteControlStateHelper.clearPlayerPairing(targetPlayer);
            targetPlayer.displayClientMessage(Component.translatable("item.dg_toy.powerbox.disconnected_by_remote", coloredPlayerName(actor)), true);
            PlayerStatusBroadcaster.broadcastNow(targetPlayer);
        } else {
            PairingClearSavedData.get().setClearFlag(boundPlayerId, true);
        }

        unbind(stack);
        actor.displayClientMessage(Component.translatable("item.dg_toy.remote_control.disconnect_success", coloredPlayerNameFromNbt(boundPlayerName)), true);
    }

    private static MutableComponent coloredPlayerNameFromNbt(@Nullable String playerName) {
        String name = playerName;
        if (name == null || name.isBlank()) {
            name = " 玩家 ";
            return Component.literal(name).setStyle(Style.EMPTY.withColor(YELLOW));
        }
        return Component.literal(" " + name + " ").setStyle(Style.EMPTY.withColor(YELLOW));
    }
}