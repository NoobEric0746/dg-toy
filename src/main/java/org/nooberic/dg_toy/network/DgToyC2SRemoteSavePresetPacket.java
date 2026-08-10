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

public record DgToyC2SRemoteSavePresetPacket() implements CustomPacketPayload {
    public static final Type<DgToyC2SRemoteSavePresetPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(Dg_toy.MODID, "c2s_remote_save_preset"));

    public static final StreamCodec<FriendlyByteBuf, DgToyC2SRemoteSavePresetPacket> STREAM_CODEC = StreamCodec.of(
            (buf, msg) -> {
            },
            buf -> new DgToyC2SRemoteSavePresetPacket()
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(DgToyC2SRemoteSavePresetPacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer sender)) {
                return;
            }

            ItemStack stack = getHeldRemote(sender);
            if (stack.isEmpty()) {
                return;
            }

            RemoteControlStateHelper.invalidateIfCleared(stack);
            RemoteControlItem.saveCurrentShockPreset(stack);
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
