package org.nooberic.dg_toy.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import org.nooberic.dg_toy.item.RemoteControlItem;
import org.nooberic.dg_toy.server.RemoteControlStateHelper;

import java.util.function.Supplier;

public class DgToyC2SRemoteSavePresetPacket {
    public static void encode(DgToyC2SRemoteSavePresetPacket msg, FriendlyByteBuf buf) {
    }

    public static DgToyC2SRemoteSavePresetPacket decode(FriendlyByteBuf buf) {
        return new DgToyC2SRemoteSavePresetPacket();
    }

    public static void handle(DgToyC2SRemoteSavePresetPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer sender = ctx.getSender();
            if (sender == null) {
                return;
            }

            ItemStack stack = getHeldRemote(sender);
            if (stack.isEmpty()) {
                return;
            }

            RemoteControlStateHelper.invalidateIfCleared(stack);
            RemoteControlItem.saveCurrentShockPreset(stack);
        });
        ctx.setPacketHandled(true);
    }

    private static ItemStack getHeldRemote(ServerPlayer player) {
        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.getItem() instanceof RemoteControlItem) {
            return mainHand;
        }

        ItemStack offHand = player.getOffhandItem();
        if (offHand.getItem() instanceof RemoteControlItem) {
            return offHand;
        }

        return ItemStack.EMPTY;
    }
}