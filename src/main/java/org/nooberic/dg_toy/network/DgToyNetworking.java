package org.nooberic.dg_toy.network;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class DgToyNetworking {
    private static final String PROTOCOL_VERSION = "1";

    private DgToyNetworking() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);

        registrar.playToClient(DgToyS2CPlayerStatusSyncPacket.TYPE, DgToyS2CPlayerStatusSyncPacket.STREAM_CODEC, DgToyS2CPlayerStatusSyncPacket::handle);
        registrar.playToServer(DgToyC2SConnectionStatusPacket.TYPE, DgToyC2SConnectionStatusPacket.STREAM_CODEC, DgToyC2SConnectionStatusPacket::handle);
        registrar.playToServer(DgToyC2SRemoteUnbindPacket.TYPE, DgToyC2SRemoteUnbindPacket.STREAM_CODEC, DgToyC2SRemoteUnbindPacket::handle);
        registrar.playToServer(DgToyC2SRemoteShockPacket.TYPE, DgToyC2SRemoteShockPacket.STREAM_CODEC, DgToyC2SRemoteShockPacket::handle);
        registrar.playToServer(DgToyC2SRemoteSettingsSyncPacket.TYPE, DgToyC2SRemoteSettingsSyncPacket.STREAM_CODEC, DgToyC2SRemoteSettingsSyncPacket::handle);
        registrar.playToServer(DgToyC2SRemoteSavePresetPacket.TYPE, DgToyC2SRemoteSavePresetPacket.STREAM_CODEC, DgToyC2SRemoteSavePresetPacket::handle);
        registrar.playToServer(DgToyC2SRemoteTargetOnlineQueryPacket.TYPE, DgToyC2SRemoteTargetOnlineQueryPacket.STREAM_CODEC, DgToyC2SRemoteTargetOnlineQueryPacket::handle);
        registrar.playToClient(DgToyS2CRemoteTargetOnlineResultPacket.TYPE, DgToyS2CRemoteTargetOnlineResultPacket.STREAM_CODEC, DgToyS2CRemoteTargetOnlineResultPacket::handle);
        registrar.playToServer(DgToyC2SPawprintRuleTogglePacket.TYPE, DgToyC2SPawprintRuleTogglePacket.STREAM_CODEC, DgToyC2SPawprintRuleTogglePacket::handle);
        registrar.playToServer(DgToyC2SPawprintRuleStringVarPacket.TYPE, DgToyC2SPawprintRuleStringVarPacket.STREAM_CODEC, DgToyC2SPawprintRuleStringVarPacket::handle);
        registrar.playToServer(DgToyC2SPawprintSettingsSyncPacket.TYPE, DgToyC2SPawprintSettingsSyncPacket.STREAM_CODEC, DgToyC2SPawprintSettingsSyncPacket::handle);
        registrar.playToServer(DgToyC2SBeaconDistanceLimitsSyncPacket.TYPE, DgToyC2SBeaconDistanceLimitsSyncPacket.STREAM_CODEC, DgToyC2SBeaconDistanceLimitsSyncPacket::handle);
        registrar.playToServer(DgToyC2SRedstoneControllerSettingsSyncPacket.TYPE, DgToyC2SRedstoneControllerSettingsSyncPacket.STREAM_CODEC, DgToyC2SRedstoneControllerSettingsSyncPacket::handle);
    }

    public static void sendToPlayer(ServerPlayer player, Object packet) {
        PacketDistributor.sendToPlayer(player, (CustomPacketPayload) packet);
    }

    public static void sendToServer(Object packet) {
        ClientPacketDistributor.sendToServer((CustomPacketPayload) packet);
    }
}
