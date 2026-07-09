package org.nooberic.dg_toy.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import org.nooberic.dg_toy.item.RemoteControlItem;
import org.nooberic.dg_toy.server.RemoteControlStateHelper;

import java.util.function.Supplier;

public class DgToyC2SRemoteSettingsSyncPacket {
    private final int strength;
    private final boolean hardMode;
    private final String pulseId;
    private final int duration;

    public DgToyC2SRemoteSettingsSyncPacket(int strength, boolean hardMode, String pulseId, int duration) {
        this.strength = strength;
        this.hardMode = hardMode;
        this.pulseId = pulseId;
        this.duration = duration;
    }

    public static void encode(DgToyC2SRemoteSettingsSyncPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.strength);
        buf.writeBoolean(msg.hardMode);
        buf.writeUtf(msg.pulseId);
        buf.writeInt(msg.duration);
    }

    public static DgToyC2SRemoteSettingsSyncPacket decode(FriendlyByteBuf buf) {
        return new DgToyC2SRemoteSettingsSyncPacket(buf.readInt(), buf.readBoolean(), buf.readUtf(), buf.readInt());
    }

    public static void handle(DgToyC2SRemoteSettingsSyncPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
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
            RemoteControlItem.setStrength(stack, msg.strength);
            RemoteControlItem.setHardMode(stack, msg.hardMode);
            RemoteControlItem.setPulseId(stack, msg.pulseId);
            RemoteControlItem.setDuration(stack, msg.duration);
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