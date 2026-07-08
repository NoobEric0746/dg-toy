package org.nooberic.dg_toy.item;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import org.nooberic.dg_toy.server.PairingClearSavedData;
import org.nooberic.dg_toy.server.PlayerPairingData;
import org.nooberic.dg_toy.server.PlayerStatusBroadcaster;
import org.nooberic.dg_toy.server.RemoteControlStateHelper;
import org.nooberic.dg_toy.server.VirtualPairingManager;
import org.nooberic.dglib.api.DgLibApi;
import org.nooberic.dglib.client.screen.StrengthControlScreen;

public class PowerboxItem extends Item {
    public PowerboxItem() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResultHolder.pass(itemStack);
        }

        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer && player.isShiftKeyDown()) {
            if (PlayerPairingData.isBound(serverPlayer)) {
                long clearTime = VirtualPairingManager.getCurrentLogicalTime(serverPlayer);
                PairingClearSavedData.get().setClearTime(serverPlayer.getUUID(), clearTime);
                RemoteControlStateHelper.clearPlayerPairing(serverPlayer);
                RemoteControlStateHelper.clearRemoteControlsForTarget(serverPlayer.getUUID());
                PlayerStatusBroadcaster.broadcastNow(serverPlayer);
                serverPlayer.displayClientMessage(Component.translatable("item.dg_toy.powerbox.disconnect_all"), true);
                return new InteractionResultHolder<>(InteractionResult.CONSUME, itemStack);
            }

            if (!VirtualPairingManager.canEnterPairing(serverPlayer)) {
                serverPlayer.displayClientMessage(Component.translatable("item.dg_toy.powerbox.pairing_unavailable"), true);
                return new InteractionResultHolder<>(InteractionResult.CONSUME, itemStack);
            }

            if (!DgLibApi.get().isPaired()) {
                serverPlayer.displayClientMessage(Component.translatable("item.dg_toy.powerbox.not_connected"), true);
                return new InteractionResultHolder<>(InteractionResult.CONSUME, itemStack);
            }

            VirtualPairingManager.startPairing(serverPlayer);
            PlayerStatusBroadcaster.broadcastNow(serverPlayer);
            serverPlayer.displayClientMessage(Component.translatable("item.dg_toy.powerbox.waiting_remote"), true);
            return new InteractionResultHolder<>(InteractionResult.CONSUME, itemStack);
        }

        if (player.isShiftKeyDown()) {
            return new InteractionResultHolder<>(InteractionResult.CONSUME, itemStack);
        }

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