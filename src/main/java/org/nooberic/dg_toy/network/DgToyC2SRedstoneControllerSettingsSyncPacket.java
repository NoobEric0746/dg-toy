package org.nooberic.dg_toy.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.nooberic.dg_toy.Dg_toy;
import org.nooberic.dg_toy.block.entity.RedstoneControllerBlockEntity;
import org.nooberic.dg_toy.item.RemoteControlItem;

public record DgToyC2SRedstoneControllerSettingsSyncPacket(
        BlockPos blockPos,
        int strength,
        boolean hardMode,
        String pulseId,
        int duration,
        int channel,
        int frequency
) implements CustomPacketPayload {
    public static final Type<DgToyC2SRedstoneControllerSettingsSyncPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(Dg_toy.MODID, "c2s_redstone_controller_settings_sync"));

    public static final StreamCodec<FriendlyByteBuf, DgToyC2SRedstoneControllerSettingsSyncPacket> STREAM_CODEC = StreamCodec.of(
            (buf, msg) -> {
                buf.writeBlockPos(msg.blockPos);
                buf.writeInt(msg.strength);
                buf.writeBoolean(msg.hardMode);
                buf.writeUtf(msg.pulseId);
                buf.writeInt(msg.duration);
                buf.writeInt(msg.channel);
                buf.writeInt(msg.frequency);
            },
            buf -> new DgToyC2SRedstoneControllerSettingsSyncPacket(
                    buf.readBlockPos(), buf.readInt(), buf.readBoolean(), buf.readUtf(), buf.readInt(), buf.readInt(), buf.readInt()
            )
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(DgToyC2SRedstoneControllerSettingsSyncPacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer sender)) {
                return;
            }
            if (sender.distanceToSqr(msg.blockPos.getX() + 0.5, msg.blockPos.getY() + 0.5, msg.blockPos.getZ() + 0.5) > 64.0
                    || !(sender.level().getBlockEntity(msg.blockPos) instanceof RedstoneControllerBlockEntity controller)) {
                return;
            }
            controller.setSettings(msg.strength, msg.hardMode, msg.pulseId, msg.duration, msg.channel, msg.frequency, RemoteControlItem.getAvailablePulseIds());
        });
    }
}
