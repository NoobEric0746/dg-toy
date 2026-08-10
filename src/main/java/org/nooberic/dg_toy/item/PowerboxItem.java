package org.nooberic.dg_toy.item;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.nooberic.dg_toy.server.PairingClearSavedData;
import org.nooberic.dg_toy.server.PlayerConnectionSyncManager;
import org.nooberic.dg_toy.server.PlayerMessageCompat;
import org.nooberic.dg_toy.server.PlayerPairingData;
import org.nooberic.dg_toy.server.PlayerStatusBroadcaster;
import org.nooberic.dg_toy.server.RemoteControlStateHelper;
import org.nooberic.dg_toy.server.VirtualPairingManager;
import org.nooberic.dglib.client.screen.StrengthControlScreen;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;

public class PowerboxItem extends Item {
    public PowerboxItem(Item.Properties properties) {
        super(properties.stacksTo(1));
    }

    public static void registerCurioCapability(RegisterCapabilitiesEvent event) {
        event.registerItem(CuriosCapability.ITEM, (stack, context) -> new PowerboxCurio(stack), ModItems.POWERBOX.get());
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer && player.isShiftKeyDown()) {
            if (PlayerPairingData.isBound(serverPlayer)) {
                long clearTime = VirtualPairingManager.getCurrentLogicalTime(serverPlayer);
                PairingClearSavedData.get().setClearTime(serverPlayer.getUUID(), clearTime);
                RemoteControlStateHelper.clearPlayerPairing(serverPlayer);
                RemoteControlStateHelper.clearRemoteControlsForTarget(serverPlayer.getUUID());
                PlayerStatusBroadcaster.broadcastNow(serverPlayer);
                PlayerMessageCompat.displayClientMessage(serverPlayer, Component.translatable("item.dg_toy.powerbox.disconnect_all"), true);
                return InteractionResult.CONSUME;
            }

            if (!VirtualPairingManager.canEnterPairing(serverPlayer)) {
                PlayerMessageCompat.displayClientMessage(serverPlayer, Component.translatable("item.dg_toy.powerbox.pairing_unavailable"), true);
                return InteractionResult.CONSUME;
            }

            if (!PlayerConnectionSyncManager.isConnected(serverPlayer.getUUID())) {
                PlayerMessageCompat.displayClientMessage(serverPlayer, Component.translatable("item.dg_toy.powerbox.not_connected"), true);
                return InteractionResult.CONSUME;
            }

            VirtualPairingManager.startPairing(serverPlayer);
            PlayerStatusBroadcaster.broadcastNow(serverPlayer);
            PlayerMessageCompat.displayClientMessage(serverPlayer, Component.translatable("item.dg_toy.powerbox.waiting_remote"), true);
            return InteractionResult.CONSUME;
        }

        if (player.isShiftKeyDown()) {
            return InteractionResult.CONSUME;
        }

        if (level.isClientSide()) {
            openScreen();
        }
        return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
    }

    @OnlyIn(Dist.CLIENT)
    private void openScreen() {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.setScreen(new StrengthControlScreen());
    }

    private static final class PowerboxCurio implements ICurio {
        private final ItemStack stack;

        private PowerboxCurio(ItemStack stack) {
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
