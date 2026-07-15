package org.nooberic.dg_toy.item;

import net.minecraft.core.Direction;
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
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraftforge.common.util.INBTSerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nooberic.dg_toy.server.PairingClearSavedData;
import org.nooberic.dg_toy.server.PlayerConnectionSyncManager;
import org.nooberic.dg_toy.server.PlayerPairingData;
import org.nooberic.dg_toy.server.PlayerStatusBroadcaster;
import org.nooberic.dg_toy.server.RemoteControlStateHelper;
import org.nooberic.dg_toy.server.VirtualPairingManager;
import org.nooberic.dglib.api.DgLibApi;
import org.nooberic.dglib.client.screen.StrengthControlScreen;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;

public class PowerboxItem extends Item {
    public PowerboxItem() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable net.minecraft.nbt.CompoundTag nbt) {
        return new PowerboxCurioProvider(stack);
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

            if (!PlayerConnectionSyncManager.isConnected(serverPlayer.getUUID())) {
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

    private static final class PowerboxCurioProvider implements ICapabilityProvider, INBTSerializable<net.minecraft.nbt.CompoundTag> {
        private final LazyOptional<ICurio> curio;

        private PowerboxCurioProvider(ItemStack stack) {
            this.curio = LazyOptional.of(() -> new PowerboxCurio(stack));
        }

        @Override
        public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
            return CuriosCapability.ITEM.orEmpty(cap, this.curio);
        }

        @Override
        public net.minecraft.nbt.CompoundTag serializeNBT() {
            return new net.minecraft.nbt.CompoundTag();
        }

        @Override
        public void deserializeNBT(net.minecraft.nbt.CompoundTag nbt) {
        }
    }
}