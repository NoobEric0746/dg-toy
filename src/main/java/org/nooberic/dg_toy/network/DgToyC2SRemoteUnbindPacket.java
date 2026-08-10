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

public record DgToyC2SRemoteUnbindPacket() implements CustomPacketPayload {
    public static final Type<DgToyC2SRemoteUnbindPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(Dg_toy.MODID, "c2s_remote_unbind"));

    public static final StreamCodec<FriendlyByteBuf, DgToyC2SRemoteUnbindPacket> STREAM_CODEC = StreamCodec.of(
            (buf, msg) -> {
            },
            buf -> new DgToyC2SRemoteUnbindPacket()
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(DgToyC2SRemoteUnbindPacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer sender)) {
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
    }
}
