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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import org.jetbrains.annotations.Nullable;
import org.nooberic.dglib.api.DgLibApi;
import org.nooberic.dg_toy.client.screen.RemoteControlScreen;
import org.nooberic.dg_toy.server.PairingClearSavedData;
import org.nooberic.dg_toy.server.PlayerConnectionSyncManager;
import org.nooberic.dg_toy.server.PlayerPairingData;
import org.nooberic.dg_toy.server.PlayerStatusBroadcaster;
import org.nooberic.dg_toy.server.RemoteControlStateHelper;
import org.nooberic.dg_toy.server.ShockEffectHelper;
import org.nooberic.dg_toy.server.VirtualPairingManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

    public RemoteControlItem() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance();
        ensureTag(stack);
        return stack;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResultHolder.pass(itemStack);
        }

        if (isTargetingOtherPlayer(level, player)) {
            return InteractionResultHolder.pass(itemStack);
        }

        if (level.isClientSide && player.isShiftKeyDown()) {
            openScreen();
            return InteractionResultHolder.sidedSuccess(itemStack, true);
        }

        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            RemoteControlStateHelper.invalidateIfCleared(itemStack);

            if (isBound(itemStack)) {
                if (player.isShiftKeyDown()) {
                    return InteractionResultHolder.sidedSuccess(itemStack, false);
                }

                if (triggerSavedShock(serverPlayer, itemStack)) {
                    return InteractionResultHolder.sidedSuccess(itemStack, false);
                }

                if (!player.isShiftKeyDown() && canSelfBind(player, itemStack)) {
                    serverPlayer.displayClientMessage(Component.translatable("item.dg_toy.remote_control.already_bound_disconnect_first"), true);
                    return InteractionResultHolder.sidedSuccess(itemStack, false);
                }

                return InteractionResultHolder.sidedSuccess(itemStack, false);
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

        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide);
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
        double reachDistance = player.getBlockReach();
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
        CompoundTag tag = ensureTag(stack);
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

    public static int getStrength(ItemStack stack) {
        CompoundTag tag = ensureTag(stack);
        return clampStrength(tag.getInt(STRENGTH_TAG));
    }

    public static void setStrength(ItemStack stack, int strength) {
        CompoundTag tag = ensureTag(stack);
        tag.putInt(STRENGTH_TAG, clampStrength(strength));
    }

    public static boolean isHardMode(ItemStack stack) {
        CompoundTag tag = ensureTag(stack);
        return tag.getBoolean(HARD_MODE_TAG);
    }

    public static void setHardMode(ItemStack stack, boolean hardMode) {
        CompoundTag tag = ensureTag(stack);
        tag.putBoolean(HARD_MODE_TAG, hardMode);
    }

    public static String getPulseId(ItemStack stack) {
        CompoundTag tag = ensureTag(stack);
        String pulseId = tag.getString(PULSE_ID_TAG);
        List<String> pulseIds = getAvailablePulseIds();
        if (pulseIds.isEmpty()) {
            if (tag.contains(PULSE_ID_TAG)) {
                tag.remove(PULSE_ID_TAG);
            }
            return "";
        }

        if (!pulseIds.contains(pulseId)) {
            pulseId = pulseIds.get(0);
            tag.putString(PULSE_ID_TAG, pulseId);
        }
        return pulseId;
    }

    public static void setPulseId(ItemStack stack, String pulseId) {
        CompoundTag tag = ensureTag(stack);
        List<String> pulseIds = getAvailablePulseIds();
        if (pulseIds.isEmpty()) {
            tag.remove(PULSE_ID_TAG);
            return;
        }

        String nextPulseId = pulseIds.contains(pulseId) ? pulseId : pulseIds.get(0);
        tag.putString(PULSE_ID_TAG, nextPulseId);
    }

    public static int getDuration(ItemStack stack) {
        CompoundTag tag = ensureTag(stack);
        return clampDuration(tag.getInt(DURATION_TAG));
    }

    public static void setDuration(ItemStack stack, int duration) {
        CompoundTag tag = ensureTag(stack);
        tag.putInt(DURATION_TAG, clampDuration(duration));
    }

    public static int getChannel(ItemStack stack) {
        CompoundTag tag = ensureTag(stack);
        return clampChannel(tag.getInt(CHANNEL_TAG));
    }

    public static void setChannel(ItemStack stack, int channel) {
        CompoundTag tag = ensureTag(stack);
        tag.putInt(CHANNEL_TAG, clampChannel(channel));
    }

    public static void saveCurrentShockPreset(ItemStack stack) {
        CompoundTag tag = ensureTag(stack);
        CompoundTag savedTag = new CompoundTag();
        savedTag.putInt(STRENGTH_TAG, getStrength(stack));
        savedTag.putBoolean(HARD_MODE_TAG, isHardMode(stack));
        savedTag.putString(PULSE_ID_TAG, getPulseId(stack));
        savedTag.putInt(DURATION_TAG, getDuration(stack));
        savedTag.putInt(CHANNEL_TAG, getChannel(stack));
        tag.put(SAVED_SHOCK_TAG, savedTag);
    }

    public static boolean hasSavedShockPreset(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains(SAVED_SHOCK_TAG, CompoundTag.TAG_COMPOUND);
    }

    @Nullable
    public static CompoundTag getSavedShockPreset(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(SAVED_SHOCK_TAG, CompoundTag.TAG_COMPOUND)) {
            return null;
        }
        return tag.getCompound(SAVED_SHOCK_TAG).copy();
    }

    public static int getSavedShockStrength(ItemStack stack) {
        CompoundTag preset = getSavedShockPreset(stack);
        return preset == null ? DEFAULT_STRENGTH : clampStrength(preset.getInt(STRENGTH_TAG));
    }

    public static boolean isSavedShockHardMode(ItemStack stack) {
        CompoundTag preset = getSavedShockPreset(stack);
        return preset == null ? DEFAULT_HARD_MODE : preset.getBoolean(HARD_MODE_TAG);
    }

    public static String getSavedShockPulseId(ItemStack stack) {
        CompoundTag preset = getSavedShockPreset(stack);
        if (preset == null) {
            return "";
        }
        String pulseId = preset.getString(PULSE_ID_TAG);
        List<String> pulseIds = getAvailablePulseIds();
        if (pulseIds.isEmpty()) {
            return "";
        }
        return pulseIds.contains(pulseId) ? pulseId : pulseIds.get(0);
    }

    public static int getSavedShockDuration(ItemStack stack) {
        CompoundTag preset = getSavedShockPreset(stack);
        return preset == null ? DEFAULT_DURATION : clampDuration(preset.getInt(DURATION_TAG));
    }

    public static int getSavedShockChannel(ItemStack stack) {
        CompoundTag preset = getSavedShockPreset(stack);
        return preset == null ? DEFAULT_CHANNEL : clampChannel(preset.getInt(CHANNEL_TAG));
    }

    public static List<String> getAvailablePulseIds() {
        Map<String, org.nooberic.dglib.pulse.RegisteredPulse> pulseMap = DgLibApi.get().getAllPulses();
        return new ArrayList<>(pulseMap.keySet());
    }

    public static UUID getOrCreateRemoteId(ItemStack stack) {
        CompoundTag tag = ensureTag(stack);
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

    private static boolean triggerSavedShock(ServerPlayer sender, ItemStack stack) {
        if (!hasSavedShockPreset(stack)) {
            return false;
        }

        UUID boundPlayerId = getBoundPlayerUuid(stack);
        if (boundPlayerId == null) {
            return false;
        }

        ServerPlayer targetPlayer = RemoteControlStateHelper.findOnlinePlayer(boundPlayerId);
        if (targetPlayer == null) {
            return false;
        }

        if (!RemoteControlStateHelper.isShockAvailable(targetPlayer)) {
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
        return true;
    }

    private static CompoundTag ensureTag(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            tag = new CompoundTag();
            writeDefaultSettings(tag);
            stack.setTag(tag);
            return tag;
        }

        boolean updated = false;
        if (!tag.contains(STRENGTH_TAG)) {
            tag.putInt(STRENGTH_TAG, DEFAULT_STRENGTH);
            updated = true;
        }
        if (!tag.contains(HARD_MODE_TAG)) {
            tag.putBoolean(HARD_MODE_TAG, DEFAULT_HARD_MODE);
            updated = true;
        }
        if (!tag.contains(PULSE_ID_TAG)) {
            String defaultPulseId = getDefaultPulseId();
            if (!defaultPulseId.isEmpty()) {
                tag.putString(PULSE_ID_TAG, defaultPulseId);
                updated = true;
            }
        }
        if (!tag.contains(DURATION_TAG)) {
            tag.putInt(DURATION_TAG, DEFAULT_DURATION);
            updated = true;
        }
        if (!tag.contains(CHANNEL_TAG)) {
            tag.putInt(CHANNEL_TAG, DEFAULT_CHANNEL);
            updated = true;
        }
        if (updated) {
            stack.setTag(tag);
        }
        return tag;
    }

    private static void writeDefaultSettings(CompoundTag tag) {
        tag.putInt(STRENGTH_TAG, DEFAULT_STRENGTH);
        tag.putBoolean(HARD_MODE_TAG, DEFAULT_HARD_MODE);
        String defaultPulseId = getDefaultPulseId();
        if (!defaultPulseId.isEmpty()) {
            tag.putString(PULSE_ID_TAG, defaultPulseId);
        }
        tag.putInt(DURATION_TAG, DEFAULT_DURATION);
        tag.putInt(CHANNEL_TAG, DEFAULT_CHANNEL);
    }

    private static String getDefaultPulseId() {
        List<String> pulseIds = getAvailablePulseIds();
        return pulseIds.isEmpty() ? "" : pulseIds.get(0);
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