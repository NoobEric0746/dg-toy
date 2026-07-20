package org.nooberic.dg_toy.item;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import org.nooberic.dg_toy.client.screen.PawprintScreen;

import java.util.List;
import java.util.UUID;

public class DgCollarItem extends Item {
    private static final String COLLAR_UUID_TAG = "CollarUuid";

    public DgCollarItem() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResultHolder.pass(itemStack);
        }

        if (!hasCollarUuid(itemStack)) {
            if (!level.isClientSide) {
                UUID collarUuid = UUID.randomUUID();
                itemStack.getOrCreateTag().putUUID(COLLAR_UUID_TAG, collarUuid);
                player.displayClientMessage(Component.translatable("item.dg_toy.dg_collar.activated", collarUuid), true);
            }
            return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide);
        }

        if (level.isClientSide) {
            openScreen();
        }
        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        UUID collarUuid = getCollarUuid(stack);
        if (collarUuid == null) {
            tooltip.add(Component.translatable("item.dg_toy.dg_collar.activate"));
        } else {
            tooltip.add(Component.translatable("item.dg_toy.dg_collar.uuid", collarUuid));
        }
    }

    public static boolean hasCollarUuid(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.hasUUID(COLLAR_UUID_TAG);
    }

    @Nullable
    public static UUID getCollarUuid(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.hasUUID(COLLAR_UUID_TAG) ? tag.getUUID(COLLAR_UUID_TAG) : null;
    }

    @OnlyIn(Dist.CLIENT)
    private void openScreen() {
        Minecraft.getInstance().setScreen(new PawprintScreen(true));
    }
}