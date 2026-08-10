package org.nooberic.dg_toy.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.nooberic.dg_toy.Dg_toy;
import org.nooberic.dg_toy.pawprint.PawprintRuleNbtHelper;

public record DgToyC2SPawprintRuleTogglePacket(String ruleId, boolean enabled) implements CustomPacketPayload {
    public static final Type<DgToyC2SPawprintRuleTogglePacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(Dg_toy.MODID, "c2s_pawprint_rule_toggle"));

    public static final StreamCodec<FriendlyByteBuf, DgToyC2SPawprintRuleTogglePacket> STREAM_CODEC = StreamCodec.of(
            (buf, msg) -> {
                buf.writeUtf(msg.ruleId);
                buf.writeBoolean(msg.enabled);
            },
            buf -> new DgToyC2SPawprintRuleTogglePacket(buf.readUtf(), buf.readBoolean())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(DgToyC2SPawprintRuleTogglePacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer sender)) {
                return;
            }

            ItemStack mainHand = sender.getMainHandItem();
            if (!PawprintRuleNbtHelper.isRuleDevice(mainHand)) {
                return;
            }

            PawprintRuleNbtHelper.setRuleEnabled(mainHand, msg.ruleId, msg.enabled);
        });
    }
}
