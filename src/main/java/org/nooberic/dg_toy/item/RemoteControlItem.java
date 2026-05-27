package org.nooberic.dg_toy.item;

import net.minecraft.network.chat.Component;
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
import org.jetbrains.annotations.Nullable;
import org.nooberic.dg_toy.server.VirtualPairingManager;

import java.util.List;
import java.util.UUID;

public class RemoteControlItem extends Item {
    private static final String BOUND_PLAYER_UUID_TAG = "BoundPlayerUuid";
    private static final String BOUND_PLAYER_NAME_TAG = "BoundPlayerName";
    private static final String REMOTE_ID_TAG = "RemoteId";

    public RemoteControlItem() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            if (player.isShiftKeyDown() && isBound(itemStack)) {
                unbind(itemStack);
                return InteractionResultHolder.sidedSuccess(itemStack, false);
            }

            InteractionResult result = tryBindToTarget(serverPlayer, serverPlayer, itemStack);
            if (result.consumesAction()) {
                return InteractionResultHolder.sidedSuccess(itemStack, false);
            }
        }

        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity entity, InteractionHand hand) {
        if (!(entity instanceof ServerPlayer targetPlayer) || !(player instanceof ServerPlayer serverPlayer)) {
            return super.interactLivingEntity(stack, player, entity, hand);
        }

        return tryBindToTarget(serverPlayer, targetPlayer, stack);
    }

    private static InteractionResult tryBindToTarget(ServerPlayer actor, ServerPlayer targetPlayer, ItemStack stack) {
        boolean selfBind = actor.getUUID().equals(targetPlayer.getUUID());

        if (isBound(stack)) {
            return InteractionResult.PASS;
        }

        if (VirtualPairingManager.isVirtuallyBound(targetPlayer.getUUID())) {
            actor.displayClientMessage(Component.translatable("item.dg_toy.remote_control.target_already_bound"), true);
            return InteractionResult.CONSUME;
        }

        if (!VirtualPairingManager.isPairingActive(targetPlayer.getUUID())) {
            actor.displayClientMessage(Component.translatable(selfBind
                    ? "item.dg_toy.remote_control.self_not_pairing"
                    : "item.dg_toy.remote_control.target_not_pairing"), true);
            return InteractionResult.PASS;
        }

        if (!VirtualPairingManager.bind(targetPlayer.getUUID(), getOrCreateRemoteId(stack))) {
            actor.displayClientMessage(Component.translatable("item.dg_toy.remote_control.bind_failed"), true);
            return InteractionResult.CONSUME;
        }

        bindToPlayer(stack, targetPlayer.getUUID(), targetPlayer.getGameProfile().getName());
        actor.displayClientMessage(Component.translatable("item.dg_toy.remote_control.bind_success", targetPlayer.getDisplayName()), true);
        if (selfBind) {
            targetPlayer.displayClientMessage(Component.translatable("item.dg_toy.powerbox.bound_success_self"), true);
        } else {
            targetPlayer.displayClientMessage(Component.translatable("item.dg_toy.powerbox.bound_success", actor.getDisplayName()), true);
        }
        return InteractionResult.CONSUME;
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
        CompoundTag tag = stack.getOrCreateTag();
        tag.putUUID(BOUND_PLAYER_UUID_TAG, playerUuid);
        tag.putString(BOUND_PLAYER_NAME_TAG, playerName);
    }

    public static void unbind(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return;
        }
        tag.remove(BOUND_PLAYER_UUID_TAG);
        tag.remove(BOUND_PLAYER_NAME_TAG);
        tag.remove(REMOTE_ID_TAG);
        if (tag.isEmpty()) {
            stack.setTag(null);
        }
    }

    public static UUID getOrCreateRemoteId(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.hasUUID(REMOTE_ID_TAG)) {
            tag.putUUID(REMOTE_ID_TAG, UUID.randomUUID());
        }
        return tag.getUUID(REMOTE_ID_TAG);
    }
}