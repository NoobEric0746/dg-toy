package org.nooberic.dg_toy.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import org.nooberic.dglib.multiplayer.DgServerCoyoteApi;
import org.nooberic.dglib.multiplayer.ServerCoyote;
import org.nooberic.dg_toy.item.RemoteControlItem;
import org.nooberic.dg_toy.server.RemoteControlStateHelper;

import java.util.function.Supplier;

public class DgToyC2SRemoteShockPacket {
    public static void encode(DgToyC2SRemoteShockPacket msg, FriendlyByteBuf buf) {
    }

    public static DgToyC2SRemoteShockPacket decode(FriendlyByteBuf buf) {
        return new DgToyC2SRemoteShockPacket();
    }

    public static void handle(DgToyC2SRemoteShockPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
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

            if (RemoteControlStateHelper.invalidateIfCleared(stack) || !RemoteControlItem.isBound(stack)) {
                return;
            }

            String pulseId = RemoteControlItem.getPulseId(stack);
            if (pulseId.isEmpty()) {
                return;
            }

            ServerPlayer targetPlayer = RemoteControlStateHelper.findOnlinePlayer(RemoteControlItem.getBoundPlayerUuid(stack));
            if (targetPlayer == null) {
                return;
            }

            if (!RemoteControlStateHelper.isShockAvailable(targetPlayer)) {
                return;
            }

            int strength = RemoteControlItem.getStrength(stack);
            int duration = RemoteControlItem.getDuration(stack);
            int channel = RemoteControlItem.getChannel(stack);
            boolean hardMode = RemoteControlItem.isHardMode(stack);
            ServerCoyote coyote = DgServerCoyoteApi.get().getCoyote(targetPlayer);
            if (hardMode) {
                coyote.control(channel, strength, pulseId, duration);
            } else {
                coyote.controlSoft(channel, strength, pulseId, duration);
            }
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