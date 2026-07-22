package org.nooberic.dg_toy.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import org.nooberic.dg_toy.block.entity.RedstoneControllerBlockEntity;
import org.nooberic.dg_toy.item.RemoteControlItem;

import java.util.function.Supplier;

public class DgToyC2SRedstoneControllerSettingsSyncPacket {
    private final BlockPos blockPos;
    private final int strength;
    private final boolean hardMode;
    private final String pulseId;
    private final int duration;
    private final int channel;
    private final int frequency;

    public DgToyC2SRedstoneControllerSettingsSyncPacket(BlockPos blockPos, int strength, boolean hardMode, String pulseId, int duration, int channel, int frequency) {
        this.blockPos = blockPos;
        this.strength = strength;
        this.hardMode = hardMode;
        this.pulseId = pulseId;
        this.duration = duration;
        this.channel = channel;
        this.frequency = frequency;
    }

    public static void encode(DgToyC2SRedstoneControllerSettingsSyncPacket msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.blockPos);
        buf.writeInt(msg.strength);
        buf.writeBoolean(msg.hardMode);
        buf.writeUtf(msg.pulseId);
        buf.writeInt(msg.duration);
        buf.writeInt(msg.channel);
        buf.writeInt(msg.frequency);
    }

    public static DgToyC2SRedstoneControllerSettingsSyncPacket decode(FriendlyByteBuf buf) {
        return new DgToyC2SRedstoneControllerSettingsSyncPacket(buf.readBlockPos(), buf.readInt(), buf.readBoolean(), buf.readUtf(), buf.readInt(), buf.readInt(), buf.readInt());
    }

    public static void handle(DgToyC2SRedstoneControllerSettingsSyncPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer sender = ctx.getSender();
            if (sender == null
                    || sender.distanceToSqr(msg.blockPos.getX() + 0.5, msg.blockPos.getY() + 0.5, msg.blockPos.getZ() + 0.5) > 64.0
                    || !(sender.level().getBlockEntity(msg.blockPos) instanceof RedstoneControllerBlockEntity controller)) {
                return;
            }
            controller.setSettings(msg.strength, msg.hardMode, msg.pulseId, msg.duration, msg.channel, msg.frequency, RemoteControlItem.getAvailablePulseIds());
        });
        ctx.setPacketHandled(true);
    }
}