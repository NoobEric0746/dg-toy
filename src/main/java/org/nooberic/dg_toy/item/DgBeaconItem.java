package org.nooberic.dg_toy.item;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.nooberic.dg_toy.client.screen.BeaconScreen;
import net.minecraft.world.level.block.Block;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.List;
import java.util.UUID;

public class DgBeaconItem extends BlockItem {
    private static final String COLLAR_UUID_TAG = "CollarUuid";
    private static final String MINIMUM_DISTANCE_LIMIT_TAG = "MinimumDistanceLimit";
    private static final String MAXIMUM_DISTANCE_LIMIT_TAG = "MaximumDistanceLimit";
    private static final String COLLAR_SLOT_ID = "collar";

    public DgBeaconItem(Block block) {
        super(block, new Properties().stacksTo(1));
    }

    public static int getMinimumDistanceLimit(ItemStack stack) {
        return getDistanceLimit(stack, MINIMUM_DISTANCE_LIMIT_TAG, 0);
    }

    public static int getMaximumDistanceLimit(ItemStack stack) {
        return getDistanceLimit(stack, MAXIMUM_DISTANCE_LIMIT_TAG, 18);
    }

    public static void setDistanceLimits(ItemStack stack, int minimumDistanceLimit, int maximumDistanceLimit) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt(MINIMUM_DISTANCE_LIMIT_TAG, clampDistanceLimit(minimumDistanceLimit));
        tag.putInt(MAXIMUM_DISTANCE_LIMIT_TAG, clampDistanceLimit(maximumDistanceLimit));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return context.getPlayer() != null && context.getPlayer().isShiftKeyDown()
                ? super.useOn(context)
                : InteractionResult.PASS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResultHolder.pass(stack);
        }

        if (level.isClientSide) {
            openScreen();
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity entity, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND || !(entity instanceof ServerPlayer targetPlayer) || !(player instanceof ServerPlayer serverPlayer) || targetPlayer == serverPlayer) {
            return InteractionResult.PASS;
        }

        UUID collarUuid = getEquippedCollarUuid(targetPlayer);
        if (collarUuid == null) {
            serverPlayer.displayClientMessage(Component.translatable("item.dg_toy.dg_beacon.target_not_activated"), true);
            return InteractionResult.CONSUME;
        }

        stack.getOrCreateTag().putUUID(COLLAR_UUID_TAG, collarUuid);
        serverPlayer.getInventory().setChanged();
        serverPlayer.setItemInHand(hand, stack);
        serverPlayer.connection.send(new ClientboundContainerSetSlotPacket(-2, 0, serverPlayer.getInventory().selected, stack.copy()));
        serverPlayer.displayClientMessage(Component.translatable("item.dg_toy.dg_beacon.bound", collarUuid), true);
        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        UUID collarUuid = getCollarUuid(stack);
        if (collarUuid == null) {
            tooltip.add(Component.translatable("item.dg_toy.dg_beacon.unbound"));
        } else {
            tooltip.add(Component.translatable("item.dg_toy.dg_beacon.bound_uuid", collarUuid));
        }
    }

    @Nullable
    private static UUID getEquippedCollarUuid(ServerPlayer player) {
        return CuriosApi.getCuriosInventory(player).resolve()
                .flatMap(handler -> handler.findCurios(stack -> stack.is(ModItems.DG_COLLAR.get())).stream()
                        .map(result -> DgCollarItem.getCollarUuid(result.stack()))
                        .filter(uuid -> uuid != null)
                        .findFirst())
                .orElse(null);
    }

    @Nullable
    public static UUID getCollarUuid(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.hasUUID(COLLAR_UUID_TAG) ? tag.getUUID(COLLAR_UUID_TAG) : null;
    }

    public static void setCollarUuid(ItemStack stack, @Nullable UUID collarUuid) {
        CompoundTag tag = stack.getOrCreateTag();
        if (collarUuid == null) {
            tag.remove(COLLAR_UUID_TAG);
        } else {
            tag.putUUID(COLLAR_UUID_TAG, collarUuid);
        }
    }

    private static int getDistanceLimit(ItemStack stack, String key, int defaultValue) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains(key) ? clampDistanceLimit(tag.getInt(key)) : defaultValue;
    }

    private static int clampDistanceLimit(int value) {
        return Math.max(0, Math.min(100, value));
    }

    @OnlyIn(Dist.CLIENT)
    private void openScreen() {
        Minecraft.getInstance().setScreen(new BeaconScreen());
    }
}