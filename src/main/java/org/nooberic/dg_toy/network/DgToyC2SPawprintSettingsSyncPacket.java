package org.nooberic.dg_toy.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import org.nooberic.dg_toy.item.PawprintItem;
import org.nooberic.dg_toy.item.PawprintItem.PawprintMode;
import org.nooberic.dg_toy.pawprint.PawprintRuleNbtHelper;

import java.util.function.Supplier;

public class DgToyC2SPawprintSettingsSyncPacket {
    private final int strength;
    private final boolean hardMode;
    private final String pawprintModeId;

    public DgToyC2SPawprintSettingsSyncPacket(int strength, boolean hardMode, String pawprintModeId) {
        this.strength = strength;
        this.hardMode = hardMode;
        this.pawprintModeId = pawprintModeId;
    }

    public static void encode(DgToyC2SPawprintSettingsSyncPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.strength);
        buf.writeBoolean(msg.hardMode);
        buf.writeUtf(msg.pawprintModeId);
    }

    public static DgToyC2SPawprintSettingsSyncPacket decode(FriendlyByteBuf buf) {
        return new DgToyC2SPawprintSettingsSyncPacket(buf.readInt(), buf.readBoolean(), buf.readUtf());
    }

    public static void handle(DgToyC2SPawprintSettingsSyncPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer sender = ctx.getSender();
            if (sender == null) {
                return;
            }

            ItemStack mainHand = sender.getMainHandItem();
            if (!(mainHand.getItem() instanceof PawprintItem)) {
                return;
            }

            PawprintRuleNbtHelper.setStrength(mainHand, msg.strength);
            PawprintRuleNbtHelper.setHardMode(mainHand, msg.hardMode);
            PawprintItem.setMode(mainHand, PawprintMode.fromId(msg.pawprintModeId));
        });
        ctx.setPacketHandled(true);
    }
}