package org.nooberic.dg_toy.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.nooberic.dg_toy.Dg_toy;
import org.nooberic.dg_toy.item.RemoteControlItem;
import org.nooberic.dg_toy.server.RemoteControlStateHelper;

public record DgToyC2SRemoteSettingsSyncPacket(
        int strength,
        boolean hardMode,
        String pulseId,
        int duration,
        int channel
) implements CustomPacketPayload {
    public static final Type<DgToyC2SRemoteSettingsSyncPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(Dg_toy.MODID, "c2s_remote_settings_sync"));

    public static final StreamCodec<FriendlyByteBuf, DgToyC2SRemoteSettingsSyncPacket> STREAM_CODEC = StreamCodec.of(
            (buf, msg) -> {
                buf.writeInt(msg.strength);
                buf.writeBoolean(msg.hardMode);
                buf.writeUtf(msg.pulseId);
                buf.writeInt(msg.duration);
                buf.writeInt(msg.channel);
            },
            buf -> new DgToyC2SRemoteSettingsSyncPacket(buf.readInt(), buf.readBoolean(), buf.readUtf(), buf.readInt(), buf.readInt())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(DgToyC2SRemoteSettingsSyncPacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer sender)) {
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
            RemoteControlItem.setChannel(stack, msg.channel);
        });
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
