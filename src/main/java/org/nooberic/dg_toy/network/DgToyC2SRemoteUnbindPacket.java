package org.nooberic.dg_toy.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import org.nooberic.dg_toy.item.RemoteControlItem;

import java.util.function.Supplier;

public class DgToyC2SRemoteUnbindPacket {
    public static void encode(DgToyC2SRemoteUnbindPacket msg, FriendlyByteBuf buf) {
    }

    public static DgToyC2SRemoteUnbindPacket decode(FriendlyByteBuf buf) {
        return new DgToyC2SRemoteUnbindPacket();
    }

    public static void handle(DgToyC2SRemoteUnbindPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer sender = ctx.getSender();
            if (sender == null) {
                return;
            }

            ItemStack mainHand = sender.getMainHandItem();
            if (mainHand.getItem() instanceof RemoteControlItem) {
                RemoteControlItem.unbindFromPlayer(sender, mainHand);
                return;
            }

            ItemStack offHand = sender.getOffhandItem();
            if (offHand.getItem() instanceof RemoteControlItem) {
                RemoteControlItem.unbindFromPlayer(sender, offHand);
            }
        });
        ctx.setPacketHandled(true);
    }
}