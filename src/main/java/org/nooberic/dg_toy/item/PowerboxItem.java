package org.nooberic.dg_toy.item;

import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.nooberic.dglib.client.screen.StrengthControlScreen;

public class PowerboxItem extends Item {
    public PowerboxItem() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (level.isClientSide) {
            openScreen();
        }
        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide);
    }

    @OnlyIn(Dist.CLIENT)
    private void openScreen() {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.setScreen(new StrengthControlScreen());
    }
}