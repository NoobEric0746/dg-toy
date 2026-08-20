package org.nooberic.dg_toy.client;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.nooberic.dg_toy.item.ModItems;

public final class ModItemTooltips {
    private ModItemTooltips() {
    }

    public static void register() {
        MinecraftForge.EVENT_BUS.register(ModItemTooltips.class);
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        Item item = event.getItemStack().getItem();
        String descriptionKey = getDescriptionKey(item);
        if (descriptionKey != null) {
            event.getToolTip().add(Component.translatable(descriptionKey).withStyle(ChatFormatting.GRAY));
        }
    }

    private static String getDescriptionKey(Item item) {
        if (item == ModItems.ELECTRIFIED_WIRE_NETTING.get()) {
            return "item.dg_toy.electrified_wire_netting.desc";
        }
        if (item == ModItems.DG_CORE.get()) {
            return "item.dg_toy.dg_core.desc";
        }
        if (item == ModItems.PAWPRINT.get()) {
            return "item.dg_toy.pawprint.desc";
        }
        if (item == ModItems.REDSTONE_CONTROLLER.get()) {
            return "item.dg_toy.redstone_controller.desc";
        }
        if (item == ModItems.DG_PRESSURE_PLATE.get()) {
            return "item.dg_toy.dg_pressure_plate.desc";
        }
        if (item == ModItems.REMOTE_CONTROL.get()) {
            return "item.dg_toy.remote_control.desc";
        }
        if (item == ModItems.DG_TOY_GUIDE.get()) {
            return "item.dg_toy.dg_toy_guide.desc";
        }
        return null;
    }
}
