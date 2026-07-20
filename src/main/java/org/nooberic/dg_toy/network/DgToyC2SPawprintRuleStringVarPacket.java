package org.nooberic.dg_toy.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import org.nooberic.dg_toy.pawprint.BasePawprintRuleHandler;
import org.nooberic.dg_toy.pawprint.PawprintRuleNbtHelper;
import org.nooberic.dg_toy.pawprint.PawprintRuleRegistry;
import org.nooberic.dg_toy.pawprint.PawprintRuleVariable;

import java.util.function.Supplier;

public class DgToyC2SPawprintRuleStringVarPacket {
    private final String ruleId;
    private final String variableKey;
    private final String value;

    public DgToyC2SPawprintRuleStringVarPacket(String ruleId, String variableKey, String value) {
        this.ruleId = ruleId;
        this.variableKey = variableKey;
        this.value = value;
    }

    public static void encode(DgToyC2SPawprintRuleStringVarPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.ruleId);
        buf.writeUtf(msg.variableKey);
        buf.writeUtf(msg.value);
    }

    public static DgToyC2SPawprintRuleStringVarPacket decode(FriendlyByteBuf buf) {
        return new DgToyC2SPawprintRuleStringVarPacket(buf.readUtf(), buf.readUtf(), buf.readUtf());
    }

    public static void handle(DgToyC2SPawprintRuleStringVarPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer sender = ctx.getSender();
            if (sender == null) {
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
                PawprintRuleNbtHelper.getOrCreateVariablesTag(mainHand, msg.ruleId).putString(msg.variableKey, nextValue);
                return;
            }
        });
        ctx.setPacketHandled(true);
    }
}