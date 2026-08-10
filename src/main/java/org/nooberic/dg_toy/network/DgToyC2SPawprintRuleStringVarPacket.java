package org.nooberic.dg_toy.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.nooberic.dg_toy.Dg_toy;
import org.nooberic.dg_toy.pawprint.BasePawprintRuleHandler;
import org.nooberic.dg_toy.pawprint.PawprintRuleNbtHelper;
import org.nooberic.dg_toy.pawprint.PawprintRuleRegistry;
import org.nooberic.dg_toy.pawprint.PawprintRuleVariable;

public record DgToyC2SPawprintRuleStringVarPacket(String ruleId, String variableKey, String value) implements CustomPacketPayload {
    public static final Type<DgToyC2SPawprintRuleStringVarPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(Dg_toy.MODID, "c2s_pawprint_rule_string_var"));

    public static final StreamCodec<FriendlyByteBuf, DgToyC2SPawprintRuleStringVarPacket> STREAM_CODEC = StreamCodec.of(
            (buf, msg) -> {
                buf.writeUtf(msg.ruleId);
                buf.writeUtf(msg.variableKey);
                buf.writeUtf(msg.value);
            },
            buf -> new DgToyC2SPawprintRuleStringVarPacket(buf.readUtf(), buf.readUtf(), buf.readUtf())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(DgToyC2SPawprintRuleStringVarPacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer sender)) {
                return;
            }

            ItemStack mainHand = sender.getMainHandItem();
            if (!PawprintRuleNbtHelper.isRuleDevice(mainHand)) {
                return;
            }

            BasePawprintRuleHandler<?> handler = PawprintRuleRegistry.getById(msg.ruleId);
            if (handler == null) {
                return;
            }

            for (PawprintRuleVariable<?> variable : handler.getVariables()) {
                if (!variable.key().equals(msg.variableKey) || variable.type() != String.class) {
                    continue;
                }

                @SuppressWarnings("unchecked")
                PawprintRuleVariable<String> stringVariable = (PawprintRuleVariable<String>) variable;
                String nextValue = stringVariable.isValid(msg.value) ? msg.value : stringVariable.defaultValue();
                PawprintRuleNbtHelper.setStringVariable(mainHand, msg.ruleId, msg.variableKey, nextValue);
                return;
            }
        });
    }
}
