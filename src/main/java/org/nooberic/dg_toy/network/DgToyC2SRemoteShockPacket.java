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
import org.nooberic.dg_toy.server.ShockEffectHelper;

public record DgToyC2SRemoteShockPacket() implements CustomPacketPayload {
    public static final Type<DgToyC2SRemoteShockPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(Dg_toy.MODID, "c2s_remote_shock"));

    public static final StreamCodec<FriendlyByteBuf, DgToyC2SRemoteShockPacket> STREAM_CODEC = StreamCodec.of(
            (buf, msg) -> {
            },
            buf -> new DgToyC2SRemoteShockPacket()
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(DgToyC2SRemoteShockPacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer sender)) {
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
            ShockEffectHelper.apply(targetPlayer, hardMode, channel, strength, pulseId, duration);
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
