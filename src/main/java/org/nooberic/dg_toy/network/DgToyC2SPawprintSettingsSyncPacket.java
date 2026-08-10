package org.nooberic.dg_toy.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.nooberic.dg_toy.Dg_toy;
import org.nooberic.dg_toy.item.PawprintItem;
import org.nooberic.dg_toy.item.PawprintItem.PawprintMode;
import org.nooberic.dg_toy.pawprint.PawprintRuleNbtHelper;

public record DgToyC2SPawprintSettingsSyncPacket(
        int strength,
        boolean hardMode,
        String pawprintModeId,
        int channel
) implements CustomPacketPayload {
    public static final Type<DgToyC2SPawprintSettingsSyncPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(Dg_toy.MODID, "c2s_pawprint_settings_sync"));

    public static final StreamCodec<FriendlyByteBuf, DgToyC2SPawprintSettingsSyncPacket> STREAM_CODEC = StreamCodec.of(
            (buf, msg) -> {
                buf.writeInt(msg.strength);
                buf.writeBoolean(msg.hardMode);
                buf.writeUtf(msg.pawprintModeId);
                buf.writeInt(msg.channel);
            },
            buf -> new DgToyC2SPawprintSettingsSyncPacket(buf.readInt(), buf.readBoolean(), buf.readUtf(), buf.readInt())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(DgToyC2SPawprintSettingsSyncPacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer sender)) {
                return;
            }

            ItemStack mainHand = sender.getMainHandItem();
            if (!PawprintRuleNbtHelper.isRuleDevice(mainHand)) {
                return;
            }

            PawprintRuleNbtHelper.setStrength(mainHand, msg.strength);
            PawprintRuleNbtHelper.setHardMode(mainHand, msg.hardMode);
            PawprintRuleNbtHelper.setChannel(mainHand, msg.channel);
            if (mainHand.getItem() instanceof PawprintItem) {
                PawprintItem.setMode(mainHand, PawprintMode.fromId(msg.pawprintModeId));
            }
        });
    }
}
