package org.nooberic.dg_toy.item;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import org.nooberic.dg_toy.client.screen.BeaconScreen;
import org.nooberic.dg_toy.server.PlayerMessageCompat;
import org.nooberic.dg_toy.util.ItemStackNbt;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class DgBeaconItem extends BlockItem {
    private static final String COLLAR_UUID_TAG = "CollarUuid";
    private static final String MINIMUM_DISTANCE_LIMIT_TAG = "MinimumDistanceLimit";
    private static final String MAXIMUM_DISTANCE_LIMIT_TAG = "MaximumDistanceLimit";
    private static final String COLLAR_SLOT_ID = "dg_collar";

    public DgBeaconItem(Block block, Item.Properties properties) {
        super(block, properties.stacksTo(1));
    }

    public static int getMinimumDistanceLimit(ItemStack stack) {
        return getDistanceLimit(stack, MINIMUM_DISTANCE_LIMIT_TAG, 0);
    }

    public static int getMaximumDistanceLimit(ItemStack stack) {
        return getDistanceLimit(stack, MAXIMUM_DISTANCE_LIMIT_TAG, 18);
    }

    public static void setDistanceLimits(ItemStack stack, int minimumDistanceLimit, int maximumDistanceLimit) {
        ItemStackNbt.update(stack, tag -> {
            tag.putInt(MINIMUM_DISTANCE_LIMIT_TAG, clampDistanceLimit(minimumDistanceLimit));
            tag.putInt(MAXIMUM_DISTANCE_LIMIT_TAG, clampDistanceLimit(maximumDistanceLimit));
        });
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return context.getPlayer() != null && context.getPlayer().isShiftKeyDown()
                ? super.useOn(context)
                : InteractionResult.PASS;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide()) {
            openScreen();
        }
        return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity entity, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND || !(entity instanceof ServerPlayer targetPlayer) || !(player instanceof ServerPlayer serverPlayer) || targetPlayer == serverPlayer) {
            return InteractionResult.PASS;
        }

        UUID collarUuid = getEquippedCollarUuid(targetPlayer);
        if (collarUuid == null) {
            PlayerMessageCompat.displayClientMessage(serverPlayer, Component.translatable("item.dg_toy.dg_beacon.target_not_activated"), true);
            return InteractionResult.CONSUME;
        }

        ItemStackNbt.putUUID(stack, COLLAR_UUID_TAG, collarUuid);
        serverPlayer.getInventory().setChanged();
        serverPlayer.setItemInHand(hand, stack);
        serverPlayer.connection.send(new ClientboundContainerSetSlotPacket(-2, 0, serverPlayer.getInventory().getSelectedSlot(), stack.copy()));
        PlayerMessageCompat.displayClientMessage(serverPlayer, Component.translatableEscape("item.dg_toy.dg_beacon.bound", collarUuid), true);
        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltip, TooltipFlag flag) {
        UUID collarUuid = getCollarUuid(stack);
        if (collarUuid == null) {
            tooltip.accept(Component.translatable("item.dg_toy.dg_beacon.unbound"));
        } else {
            tooltip.accept(Component.translatableEscape("item.dg_toy.dg_beacon.bound_uuid", collarUuid));
        }
    }

    @Nullable
    private static UUID getEquippedCollarUuid(ServerPlayer player) {
        return CuriosApi.getCuriosInventory(player)
                .flatMap(handler -> handler.findCurios(stack -> stack.is(ModItems.DG_COLLAR.get())).stream()
                        .map(result -> DgCollarItem.getCollarUuid(result.stack()))
                        .filter(uuid -> uuid != null)
                        .findFirst())
                .orElse(null);
    }

    @Nullable
    public static UUID getCollarUuid(ItemStack stack) {
        return ItemStackNbt.getUUID(stack, COLLAR_UUID_TAG);
    }

    public static void setCollarUuid(ItemStack stack, @Nullable UUID collarUuid) {
        ItemStackNbt.update(stack, tag -> {
            if (collarUuid == null) {
                tag.remove(COLLAR_UUID_TAG);
            } else {
                ItemStackNbt.putUUID(tag, COLLAR_UUID_TAG, collarUuid);
            }
        });
    }

    private static int getDistanceLimit(ItemStack stack, String key, int defaultValue) {
        return ItemStackNbt.getInt(stack, key, defaultValue);
    }

    private static int clampDistanceLimit(int value) {
        return Math.max(0, Math.min(100, value));
    }

    @OnlyIn(Dist.CLIENT)
    private void openScreen() {
        Minecraft.getInstance().setScreen(new BeaconScreen());
    }
}
