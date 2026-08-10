package org.nooberic.dg_toy.item;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import org.nooberic.dg_toy.client.screen.PawprintScreen;
import org.nooberic.dg_toy.server.PlayerMessageCompat;
import org.nooberic.dg_toy.util.ItemStackNbt;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class DgCollarItem extends Item {
    private static final String COLLAR_UUID_TAG = "CollarUuid";

    public DgCollarItem(Item.Properties properties) {
        super(properties.stacksTo(1));
    }

    public static void registerCurioCapability(RegisterCapabilitiesEvent event) {
        event.registerItem(CuriosCapability.ITEM, (stack, context) -> new DgCollarCurio(stack), ModItems.DG_COLLAR.get());
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }

        if (!hasCollarUuid(itemStack)) {
            if (!level.isClientSide()) {
                UUID collarUuid = UUID.randomUUID();
                ItemStackNbt.putUUID(itemStack, COLLAR_UUID_TAG, collarUuid);
                PlayerMessageCompat.displayClientMessage((ServerPlayer) player, Component.translatableEscape("item.dg_toy.dg_collar.activated", collarUuid), true);
            }
            return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
        }

        if (level.isClientSide()) {
            openScreen();
        }
        return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltip, TooltipFlag flag) {
        UUID collarUuid = getCollarUuid(stack);
        if (collarUuid == null) {
            tooltip.accept(Component.translatable("item.dg_toy.dg_collar.activate"));
        } else {
            tooltip.accept(Component.translatableEscape("item.dg_toy.dg_collar.uuid", collarUuid));
        }
    }

    public static boolean hasCollarUuid(ItemStack stack) {
        return ItemStackNbt.getUUID(stack, COLLAR_UUID_TAG) != null;
    }

    @Nullable
    public static UUID getCollarUuid(ItemStack stack) {
        return ItemStackNbt.getUUID(stack, COLLAR_UUID_TAG);
    }

    @OnlyIn(Dist.CLIENT)
    private void openScreen() {
        Minecraft.getInstance().setScreen(new PawprintScreen(true));
    }

    private static final class DgCollarCurio implements ICurio {
        private final ItemStack stack;

        private DgCollarCurio(ItemStack stack) {
            this.stack = stack;
        }

        @Override
        public ItemStack getStack() {
            return this.stack;
        }

        @Override
        public boolean canEquip(SlotContext slotContext) {
            return true;
        }


        @Override
        public boolean canSync(SlotContext slotContext) {
            return true;
        }
    }
}
