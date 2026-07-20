package org.nooberic.dg_toy.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import org.nooberic.dg_toy.pawprint.PawprintRuleNbtHelper;

import java.util.function.Supplier;

public class DgToyC2SPawprintRuleTogglePacket {
    private final String ruleId;
    private final boolean enabled;

    public DgToyC2SPawprintRuleTogglePacket(String ruleId, boolean enabled) {
        this.ruleId = ruleId;
        this.enabled = enabled;
    }

    public static void encode(DgToyC2SPawprintRuleTogglePacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.ruleId);
        buf.writeBoolean(msg.enabled);
    }

    public static DgToyC2SPawprintRuleTogglePacket decode(FriendlyByteBuf buf) {
        return new DgToyC2SPawprintRuleTogglePacket(buf.readUtf(), buf.readBoolean());
    }

    public static void handle(DgToyC2SPawprintRuleTogglePacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
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

            PawprintRuleNbtHelper.getOrCreateRuleTag(mainHand, msg.ruleId).putBoolean("enabled", msg.enabled);
        });
        ctx.setPacketHandled(true);
    }
}