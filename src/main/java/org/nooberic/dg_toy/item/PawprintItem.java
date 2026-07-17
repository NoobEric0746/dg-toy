package org.nooberic.dg_toy.item;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.nooberic.dg_toy.client.screen.PawprintScreen;

public class PawprintItem extends Item {
    private static final String MODE_TAG = "PawprintMode";

    public enum PawprintMode {
        MOTION("motion", "item.dg_toy.pawprint.mode.motion"),
        SOUND("sound", "item.dg_toy.pawprint.mode.sound"),
        HURT("hurt", "item.dg_toy.pawprint.mode.hurt");

        private final String id;
        private final String displayNameKey;

        PawprintMode(String id, String displayNameKey) {
            this.id = id;
            this.displayNameKey = displayNameKey;
        }

        public String id() {
            return this.id;
        }

        public Component displayName() {
            return Component.translatable(this.displayNameKey);
        }

        public PawprintMode next() {
            PawprintMode[] modes = values();
            return modes[(this.ordinal() + 1) % modes.length];
        }

        public static PawprintMode fromId(String id) {
            for (PawprintMode mode : values()) {
                if (mode.id.equals(id)) {
                    return mode;
                }
            }
            return MOTION;
        }
    }

    public PawprintItem() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable("item.dg_toy.pawprint.with_mode", getMode(stack).displayName());
    }

    public static PawprintMode getMode(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        PawprintMode mode = PawprintMode.fromId(tag.getString(MODE_TAG));
        if (!tag.contains(MODE_TAG) || !mode.id().equals(tag.getString(MODE_TAG))) {
            tag.putString(MODE_TAG, mode.id());
        }
        return mode;
    }

    public static void setMode(ItemStack stack, PawprintMode mode) {
        stack.getOrCreateTag().putString(MODE_TAG, mode.id());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResultHolder.pass(itemStack);
        }

        if (level.isClientSide) {
            openScreen();
        }
        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide);
    }

    @OnlyIn(Dist.CLIENT)
    private void openScreen() {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.setScreen(new PawprintScreen());
    }
}