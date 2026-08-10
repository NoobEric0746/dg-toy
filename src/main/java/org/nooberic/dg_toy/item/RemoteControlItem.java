package org.nooberic.dg_toy.item;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import org.jetbrains.annotations.Nullable;
import org.nooberic.dglib.api.DgLibApi;
import org.nooberic.dg_toy.client.screen.RemoteControlScreen;
import org.nooberic.dg_toy.server.PairingClearSavedData;
import org.nooberic.dg_toy.server.PlayerConnectionSyncManager;
import org.nooberic.dg_toy.server.PlayerMessageCompat;
import org.nooberic.dg_toy.server.PlayerPairingData;
import org.nooberic.dg_toy.server.PlayerStatusBroadcaster;
import org.nooberic.dg_toy.server.RemoteControlStateHelper;
import org.nooberic.dg_toy.server.ShockEffectHelper;
import org.nooberic.dg_toy.server.VirtualPairingManager;
import org.nooberic.dg_toy.util.ItemStackNbt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import static net.minecraft.ChatFormatting.YELLOW;

public class RemoteControlItem extends Item {
    private static final String BOUND_PLAYER_UUID_TAG = "BoundPlayerUuid";
    private static final String BOUND_PLAYER_NAME_TAG = "BoundPlayerName";
    private static final String REMOTE_ID_TAG = "RemoteId";
    private static final String BOUND_TIME_TAG = "BoundTime";
    private static final String STRENGTH_TAG = "Strength";
    private static final String HARD_MODE_TAG = "HardMode";
    private static final String PULSE_ID_TAG = "PulseId";
    private static final String DURATION_TAG = "Duration";
    private static final String CHANNEL_TAG = "Channel";
    private static final String SAVED_SHOCK_TAG = "SavedShock";
    private static final int DEFAULT_STRENGTH = 0;
    private static final boolean DEFAULT_HARD_MODE = true;
    private static final int DEFAULT_DURATION = 1;
    private static final int DEFAULT_CHANNEL = 1;

    public RemoteControlItem(Item.Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }

        if (isTargetingOtherPlayer(level, player)) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide() && player.isShiftKeyDown()) {
            openScreen();
            return InteractionResult.SUCCESS;
        }

        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            RemoteControlStateHelper.invalidateIfCleared(itemStack);

            if (!player.isShiftKeyDown() && canSelfBind(player, itemStack)) {
                if (isBound(itemStack)) {
                    PlayerMessageCompat.displayClientMessage(serverPlayer, Component.translatable("item.dg_toy.remote_control.already_bound_disconnect_first"), true);
                    return InteractionResult.CONSUME;
                }

                if (PlayerPairingData.isBound(serverPlayer)) {
                    PlayerMessageCompat.displayClientMessage(serverPlayer, Component.translatable("item.dg_toy.remote_control.target_already_bound"), true);
                    return InteractionResult.CONSUME;
                }

                if (!PlayerConnectionSyncManager.isConnected(serverPlayer.getUUID())) {
                    PlayerMessageCompat.displayClientMessage(serverPlayer, Component.translatable("item.dg_toy.powerbox.not_connected"), true);
                    return InteractionResult.CONSUME;
                }

                directSelfBind(serverPlayer, itemStack);
                return InteractionResult.CONSUME;
            }

            if (isBound(itemStack)) {
                if (player.isShiftKeyDown()) {
                    return InteractionResult.CONSUME;
                }

                if (triggerSavedShock(serverPlayer, itemStack)) {
                    return InteractionResult.CONSUME;
                }

                return InteractionResult.CONSUME;
            }

            if (!player.isShiftKeyDown() && hasSavedShockPreset(itemStack)) {
                PlayerMessageCompat.displayClientMessage(serverPlayer, Component.translatable("item.dg_toy.remote_control.shock_unbound"), true);
                return InteractionResult.CONSUME;
            }
        }

        return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
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

    private static boolean isTargetingOtherPlayer(Level level, Player player) {
        Vec3 eyePosition = player.getEyePosition();
        Vec3 viewVector = player.getViewVector(1.0F);
        double reachDistance = player.blockInteractionRange();
        Vec3 reachPosition = eyePosition.add(viewVector.scale(reachDistance));

        HitResult blockHitResult = level.clip(new ClipContext(eyePosition, reachPosition, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
        double maxDistanceSqr = reachDistance * reachDistance;
        if (blockHitResult.getType() != HitResult.Type.MISS) {
            maxDistanceSqr = blockHitResult.getLocation().distanceToSqr(eyePosition);
        }

        AABB searchBox = player.getBoundingBox().expandTowards(viewVector.scale(reachDistance)).inflate(1.0D);
        EntityHitResult entityHitResult = ProjectileUtil.getEntityHitResult(player, eyePosition, reachPosition, searchBox,
                entity -> entity instanceof Player && entity != player && !entity.isSpectator() && entity.isPickable(),
                maxDistanceSqr);

        return entityHitResult != null && entityHitResult.getEntity() instanceof Player;
    }

    private static InteractionResult tryBindToTarget(ServerPlayer actor, ServerPlayer targetPlayer, ItemStack stack) {
        boolean selfBind = actor.getUUID().equals(targetPlayer.getUUID());

        if (isBound(stack)) {
            PlayerMessageCompat.displayClientMessage(actor, Component.translatable("item.dg_toy.remote_control.already_bound_disconnect_first"), true);
            return InteractionResult.CONSUME;
        }

        if (PlayerPairingData.isBound(targetPlayer)) {
            if (!selfBind) {
                PlayerMessageCompat.displayClientMessage(actor, Component.translatable("item.dg_toy.remote_control.target_already_bound"), true);
            }
            return InteractionResult.CONSUME;
        }

        if (!VirtualPairingManager.isPairingActive(targetPlayer.getUUID())) {
            if (!selfBind) {
                PlayerMessageCompat.displayClientMessage(actor, Component.translatable("item.dg_toy.remote_control.target_not_pairing"), true);
            }
            return InteractionResult.PASS;
        }

        UUID remoteId = getOrCreateRemoteId(stack);
        if (!VirtualPairingManager.bind(targetPlayer, remoteId)) {
            PlayerMessageCompat.displayClientMessage(actor, Component.translatable("item.dg_toy.remote_control.bind_failed"), true);
            return InteractionResult.CONSUME;
        }

        bindToPlayer(stack, targetPlayer.getUUID(), targetPlayer.getGameProfile().name(), VirtualPairingManager.getCurrentLogicalTime(targetPlayer));
        PairingClearSavedData.get().setClearFlag(targetPlayer.getUUID(), false);
        PlayerStatusBroadcaster.broadcastNow(targetPlayer);
        if (!selfBind) {
            PlayerStatusBroadcaster.broadcastNow(actor);
        }
        PlayerMessageCompat.displayClientMessage(actor, Component.translatable("item.dg_toy.remote_control.connect_success", coloredPlayerName(targetPlayer)), true);
        if (selfBind) {
            PlayerMessageCompat.displayClientMessage(targetPlayer, Component.translatable("item.dg_toy.powerbox.connect_success_self"), true);
        } else {
            PlayerMessageCompat.displayClientMessage(targetPlayer, Component.translatable("item.dg_toy.powerbox.connected_by_remote", coloredPlayerName(actor)), true);
        }
        return InteractionResult.CONSUME;
    }

    private static boolean canSelfBind(Player player, ItemStack mainHandStack) {
        return mainHandStack.getItem() instanceof RemoteControlItem
                && player.getOffhandItem().getItem() instanceof PowerboxItem;
    }

    private static void directSelfBind(ServerPlayer player, ItemStack stack) {
        UUID remoteId = getOrCreateRemoteId(stack);
        PlayerPairingData.setBoundRemoteId(player, remoteId);
        VirtualPairingManager.clearPairingWindow(player.getUUID());
        bindToPlayer(stack, player.getUUID(), player.getGameProfile().name(), VirtualPairingManager.getCurrentLogicalTime(player));
        PairingClearSavedData.get().setClearFlag(player.getUUID(), false);
        PlayerStatusBroadcaster.broadcastNow(player);
        PlayerMessageCompat.displayClientMessage(player, Component.translatable("item.dg_toy.remote_control.connect_success", coloredPlayerName(player)), true);
        PlayerMessageCompat.displayClientMessage(player, Component.translatable("item.dg_toy.powerbox.connect_success_self"), true);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable("item.dg_toy.remote_control.desc"));
        String boundPlayerName = getBoundPlayerName(stack);
        if (boundPlayerName != null) {
            tooltip.accept(Component.translatable("item.dg_toy.remote_control.bound_player", Component.literal(boundPlayerName)));
        }
    }

    @Nullable
    public static String getBoundPlayerName(ItemStack stack) {
        CompoundTag tag = ItemStackNbt.getTag(stack);
        if (tag.contains(BOUND_PLAYER_NAME_TAG)) {
            String playerName = tag.getStringOr(BOUND_PLAYER_NAME_TAG, "");
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
        return ItemStackNbt.getUUID(stack, BOUND_PLAYER_UUID_TAG);
    }

    public static void bindToPlayer(ItemStack stack, UUID playerUuid, String playerName) {
        bindToPlayer(stack, playerUuid, playerName, 0L);
    }

    public static void bindToPlayer(ItemStack stack, UUID playerUuid, String playerName, long boundTime) {
        ItemStackNbt.update(stack, tag -> {
            ItemStackNbt.putUUID(tag, BOUND_PLAYER_UUID_TAG, playerUuid);
            tag.putString(BOUND_PLAYER_NAME_TAG, playerName);
            tag.putLong(BOUND_TIME_TAG, boundTime);
        });
    }

    public static void unbind(ItemStack stack) {
        ItemStackNbt.update(stack, tag -> {
            tag.remove(BOUND_PLAYER_UUID_TAG);
            tag.remove(BOUND_PLAYER_NAME_TAG);
            tag.remove(REMOTE_ID_TAG);
            tag.remove(BOUND_TIME_TAG);
        });
    }

    public static long getBoundTime(ItemStack stack) {
        return ItemStackNbt.getLong(stack, BOUND_TIME_TAG, 0L);
    }

    public static int getStrength(ItemStack stack) {
        return clampStrength(ItemStackNbt.getInt(stack, STRENGTH_TAG, DEFAULT_STRENGTH));
    }

    public static void setStrength(ItemStack stack, int strength) {
        ItemStackNbt.putInt(stack, STRENGTH_TAG, clampStrength(strength));
    }

    public static boolean isHardMode(ItemStack stack) {
        return ItemStackNbt.getBoolean(stack, HARD_MODE_TAG, DEFAULT_HARD_MODE);
    }

    public static void setHardMode(ItemStack stack, boolean hardMode) {
        ItemStackNbt.putBoolean(stack, HARD_MODE_TAG, hardMode);
    }

    public static String getPulseId(ItemStack stack) {
        String pulseId = ItemStackNbt.getString(stack, PULSE_ID_TAG, "");
        List<String> pulseIds = getAvailablePulseIds();
        if (pulseIds.isEmpty()) {
            if (ItemStackNbt.contains(stack, PULSE_ID_TAG)) {
                ItemStackNbt.remove(stack, PULSE_ID_TAG);
            }
            return "";
        }

        if (!pulseIds.contains(pulseId)) {
            pulseId = pulseIds.get(0);
            ItemStackNbt.putString(stack, PULSE_ID_TAG, pulseId);
        }
        return pulseId;
    }

    public static void setPulseId(ItemStack stack, String pulseId) {
        List<String> pulseIds = getAvailablePulseIds();
        if (pulseIds.isEmpty()) {
            ItemStackNbt.remove(stack, PULSE_ID_TAG);
            return;
        }

        String nextPulseId = pulseIds.contains(pulseId) ? pulseId : pulseIds.get(0);
        ItemStackNbt.putString(stack, PULSE_ID_TAG, nextPulseId);
    }

    public static int getDuration(ItemStack stack) {
        return clampDuration(ItemStackNbt.getInt(stack, DURATION_TAG, DEFAULT_DURATION));
    }

    public static void setDuration(ItemStack stack, int duration) {
        ItemStackNbt.putInt(stack, DURATION_TAG, clampDuration(duration));
    }

    public static int getChannel(ItemStack stack) {
        return clampChannel(ItemStackNbt.getInt(stack, CHANNEL_TAG, DEFAULT_CHANNEL));
    }

    public static void setChannel(ItemStack stack, int channel) {
        ItemStackNbt.putInt(stack, CHANNEL_TAG, clampChannel(channel));
    }

    public static void saveCurrentShockPreset(ItemStack stack) {
        CompoundTag savedTag = new CompoundTag();
        savedTag.putInt(STRENGTH_TAG, getStrength(stack));
        savedTag.putBoolean(HARD_MODE_TAG, isHardMode(stack));
        savedTag.putString(PULSE_ID_TAG, getPulseId(stack));
        savedTag.putInt(DURATION_TAG, getDuration(stack));
        savedTag.putInt(CHANNEL_TAG, getChannel(stack));
        ItemStackNbt.update(stack, tag -> tag.put(SAVED_SHOCK_TAG, savedTag));
    }

    public static boolean hasSavedShockPreset(ItemStack stack) {
        return ItemStackNbt.getTag(stack).contains(SAVED_SHOCK_TAG);
    }

    @Nullable
    public static CompoundTag getSavedShockPreset(ItemStack stack) {
        CompoundTag tag = ItemStackNbt.getTag(stack);
        if (!tag.contains(SAVED_SHOCK_TAG)) {
            return null;
        }
        return tag.getCompoundOrEmpty(SAVED_SHOCK_TAG).copy();
    }

    public static int getSavedShockStrength(ItemStack stack) {
        CompoundTag preset = getSavedShockPreset(stack);
        return preset == null ? DEFAULT_STRENGTH : clampStrength(preset.getIntOr(STRENGTH_TAG, DEFAULT_STRENGTH));
    }

    public static boolean isSavedShockHardMode(ItemStack stack) {
        CompoundTag preset = getSavedShockPreset(stack);
        return preset == null ? DEFAULT_HARD_MODE : preset.getBooleanOr(HARD_MODE_TAG, DEFAULT_HARD_MODE);
    }

    public static String getSavedShockPulseId(ItemStack stack) {
        CompoundTag preset = getSavedShockPreset(stack);
        if (preset == null) {
            return "";
        }
        String pulseId = preset.getStringOr(PULSE_ID_TAG, "");
        List<String> pulseIds = getAvailablePulseIds();
        if (pulseIds.isEmpty()) {
            return "";
        }
        return pulseIds.contains(pulseId) ? pulseId : pulseIds.get(0);
    }

    public static int getSavedShockDuration(ItemStack stack) {
        CompoundTag preset = getSavedShockPreset(stack);
        return preset == null ? DEFAULT_DURATION : clampDuration(preset.getIntOr(DURATION_TAG, DEFAULT_DURATION));
    }

    public static int getSavedShockChannel(ItemStack stack) {
        CompoundTag preset = getSavedShockPreset(stack);
        return preset == null ? DEFAULT_CHANNEL : clampChannel(preset.getIntOr(CHANNEL_TAG, DEFAULT_CHANNEL));
    }

    public static List<String> getAvailablePulseIds() {
        Map<String, org.nooberic.dglib.pulse.RegisteredPulse> pulseMap = DgLibApi.get().getAllPulses();
        return new ArrayList<>(pulseMap.keySet());
    }

    public static UUID getOrCreateRemoteId(ItemStack stack) {
        UUID existing = ItemStackNbt.getUUID(stack, REMOTE_ID_TAG);
        if (existing != null) {
            return existing;
        }
        UUID newId = UUID.randomUUID();
        ItemStackNbt.putUUID(stack, REMOTE_ID_TAG, newId);
        return newId;
    }

    private static MutableComponent coloredPlayerName(ServerPlayer player) {
        return Component.literal(" " + player.getDisplayName().getString() + " ").setStyle(Style.EMPTY.withColor(YELLOW));
    }

    public static MutableComponent createRemoteDisconnectedMessage(UUID playerId, @Nullable String playerName) {
        return Component.translatable("item.dg_toy.remote_control.unbound_by_remote", coloredPlayerNameFromNbt(playerName));
    }

    public static void unbindFromPlayer(ServerPlayer actor, ItemStack stack) {
        UUID boundPlayerId = getBoundPlayerUuid(stack);
        if (boundPlayerId == null) {
            unbind(stack);
            return;
        }

        String boundPlayerName = getBoundPlayerName(stack);

        ServerPlayer targetPlayer = RemoteControlStateHelper.findOnlinePlayer(boundPlayerId);
        if (targetPlayer != null) {
            RemoteControlStateHelper.clearPlayerPairing(targetPlayer);
            PlayerMessageCompat.displayClientMessage(targetPlayer, Component.translatable("item.dg_toy.powerbox.disconnected_by_remote", coloredPlayerName(actor)), true);
            PlayerStatusBroadcaster.broadcastNow(targetPlayer);
        } else {
            PairingClearSavedData.get().setClearFlag(boundPlayerId, true);
        }

        unbind(stack);
        PlayerMessageCompat.displayClientMessage(actor, Component.translatable("item.dg_toy.remote_control.disconnect_success", coloredPlayerNameFromNbt(boundPlayerName)), true);
    }

    private static MutableComponent coloredPlayerNameFromNbt(@Nullable String playerName) {
        String name = playerName;
        if (name == null || name.isBlank()) {
            name = "玩家";
            return Component.literal(name).setStyle(Style.EMPTY.withColor(YELLOW));
        }
        return Component.literal(" " + name + " ").setStyle(Style.EMPTY.withColor(YELLOW));
    }

    private static boolean triggerSavedShock(ServerPlayer sender, ItemStack stack) {
        if (!hasSavedShockPreset(stack)) {
            return false;
        }

        UUID boundPlayerId = getBoundPlayerUuid(stack);
        if (boundPlayerId == null) {
            PlayerMessageCompat.displayClientMessage(sender, Component.translatable("item.dg_toy.remote_control.shock_unbound"), true);
            return false;
        }

        ServerPlayer targetPlayer = RemoteControlStateHelper.findOnlinePlayer(boundPlayerId);
        if (targetPlayer == null) {
            PlayerMessageCompat.displayClientMessage(sender, Component.translatable("item.dg_toy.remote_control.shock_target_offline"), true);
            return false;
        }

        if (!RemoteControlStateHelper.isShockAvailable(targetPlayer)
                || !PlayerConnectionSyncManager.isConnected(targetPlayer.getUUID())) {
            PlayerMessageCompat.displayClientMessage(sender, Component.translatable("item.dg_toy.remote_control.shock_powerbox_offline"), true);
            return false;
        }

        String pulseId = getSavedShockPulseId(stack);
        if (pulseId.isEmpty()) {
            return false;
        }

        int channel = getSavedShockChannel(stack);
        int strength = getSavedShockStrength(stack);
        int duration = getSavedShockDuration(stack);
        ShockEffectHelper.apply(targetPlayer, isSavedShockHardMode(stack), channel, strength, pulseId, duration);
        PlayerMessageCompat.displayClientMessage(sender, Component.translatable("item.dg_toy.remote_control.shock_success"), true);
        return true;
    }

    private static int clampStrength(int strength) {
        return Math.max(0, Math.min(100, strength));
    }

    private static int clampDuration(int duration) {
        return Math.max(1, Math.min(10, duration));
    }

    private static int clampChannel(int channel) {
        return switch (channel) {
            case 2, 3 -> channel;
            default -> 1;
        };
    }
}
