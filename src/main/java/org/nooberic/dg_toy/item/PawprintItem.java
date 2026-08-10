package org.nooberic.dg_toy.item;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.nooberic.dg_toy.client.screen.PawprintScreen;
import org.nooberic.dg_toy.util.ItemStackNbt;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;

public class PawprintItem extends Item {
    private static final String MODE_TAG = "PawprintMode";

    public enum PawprintMode {
        MOTION("motion", "item.dg_toy.pawprint.mode.motion"),
        SOUND("sound", "item.dg_toy.pawprint.mode.sound"),
        HURT("hurt", "item.dg_toy.pawprint.mode.hurt"),
        WIRELESS("wireless", "item.dg_toy.pawprint.mode.wireless"),
        COLLAR("collar", "item.dg_toy.dg_collar.mode");

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
            return switch (this) {
                case MOTION -> SOUND;
                case SOUND -> HURT;
                case HURT -> WIRELESS;
                case WIRELESS, COLLAR -> MOTION;
            };
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

    public PawprintItem(Item.Properties properties) {
        super(properties.stacksTo(1));
    }

    public static void registerCurioCapability(RegisterCapabilitiesEvent event) {
        event.registerItem(CuriosCapability.ITEM, (stack, context) -> new PawprintCurio(stack), ModItems.PAWPRINT.get());
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable("item.dg_toy.pawprint.with_mode", getMode(stack).displayName());
    }

    public static PawprintMode getMode(ItemStack stack) {
        String current = ItemStackNbt.getString(stack, MODE_TAG, "");
        PawprintMode mode = PawprintMode.fromId(current);
        if (!ItemStackNbt.contains(stack, MODE_TAG) || !mode.id().equals(current)) {
            ItemStackNbt.putString(stack, MODE_TAG, mode.id());
        }
        return mode;
    }

    public static void setMode(ItemStack stack, PawprintMode mode) {
        ItemStackNbt.putString(stack, MODE_TAG, mode.id());
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide()) {
            openScreen();
        }
        return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
    }

    @OnlyIn(Dist.CLIENT)
    private void openScreen() {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.setScreen(new PawprintScreen());
    }

    private static final class PawprintCurio implements ICurio {
        private final ItemStack stack;

        private PawprintCurio(ItemStack stack) {
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
