package org.nooberic.dg_toy.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.nooberic.dg_toy.Dg_toy;
import vazkii.patchouli.api.PatchouliAPI;

public class ModGuideBookItem extends Item {
    public static final ResourceLocation BOOK_ID = new ResourceLocation(Dg_toy.MODID, "dg_toy_guide");

    public ModGuideBookItem() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            PatchouliAPI.get().openBookGUI(BOOK_ID);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
