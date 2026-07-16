package org.nooberic.dg_toy.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import org.nooberic.dg_toy.Dg_toy;

public final class DgToyNetworking {
    private static final String PROTOCOL_VERSION = "1";
    private static boolean registered = false;

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(Dg_toy.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private DgToyNetworking() {
    }

    public static void register() {
        if (registered) {
            return;
        }

        CHANNEL.messageBuilder(DgToyS2CPlayerStatusSyncPacket.class, 0, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(DgToyS2CPlayerStatusSyncPacket::encode)
                .decoder(DgToyS2CPlayerStatusSyncPacket::decode)
                .consumerMainThread(DgToyS2CPlayerStatusSyncPacket::handle)
                .add();

        CHANNEL.messageBuilder(DgToyC2SConnectionStatusPacket.class, 1, NetworkDirection.PLAY_TO_SERVER)
                .encoder(DgToyC2SConnectionStatusPacket::encode)
                .decoder(DgToyC2SConnectionStatusPacket::decode)
                .consumerMainThread(DgToyC2SConnectionStatusPacket::handle)
                .add();

        CHANNEL.messageBuilder(DgToyC2SRemoteUnbindPacket.class, 2, NetworkDirection.PLAY_TO_SERVER)
            .encoder(DgToyC2SRemoteUnbindPacket::encode)
            .decoder(DgToyC2SRemoteUnbindPacket::decode)
            .consumerMainThread(DgToyC2SRemoteUnbindPacket::handle)
            .add();

        CHANNEL.messageBuilder(DgToyC2SRemoteShockPacket.class, 3, NetworkDirection.PLAY_TO_SERVER)
            .encoder(DgToyC2SRemoteShockPacket::encode)
            .decoder(DgToyC2SRemoteShockPacket::decode)
            .consumerMainThread(DgToyC2SRemoteShockPacket::handle)
            .add();

        CHANNEL.messageBuilder(DgToyC2SRemoteSettingsSyncPacket.class, 4, NetworkDirection.PLAY_TO_SERVER)
            .encoder(DgToyC2SRemoteSettingsSyncPacket::encode)
            .decoder(DgToyC2SRemoteSettingsSyncPacket::decode)
            .consumerMainThread(DgToyC2SRemoteSettingsSyncPacket::handle)
            .add();

        CHANNEL.messageBuilder(DgToyC2SRemoteSavePresetPacket.class, 5, NetworkDirection.PLAY_TO_SERVER)
            .encoder(DgToyC2SRemoteSavePresetPacket::encode)
            .decoder(DgToyC2SRemoteSavePresetPacket::decode)
            .consumerMainThread(DgToyC2SRemoteSavePresetPacket::handle)
            .add();

        CHANNEL.messageBuilder(DgToyC2SRemoteTargetOnlineQueryPacket.class, 6, NetworkDirection.PLAY_TO_SERVER)
            .encoder(DgToyC2SRemoteTargetOnlineQueryPacket::encode)
            .decoder(DgToyC2SRemoteTargetOnlineQueryPacket::decode)
            .consumerMainThread(DgToyC2SRemoteTargetOnlineQueryPacket::handle)
            .add();

        CHANNEL.messageBuilder(DgToyS2CRemoteTargetOnlineResultPacket.class, 7, NetworkDirection.PLAY_TO_CLIENT)
            .encoder(DgToyS2CRemoteTargetOnlineResultPacket::encode)
            .decoder(DgToyS2CRemoteTargetOnlineResultPacket::decode)
            .consumerMainThread(DgToyS2CRemoteTargetOnlineResultPacket::handle)
            .add();

        CHANNEL.messageBuilder(DgToyC2SPawprintRuleTogglePacket.class, 8, NetworkDirection.PLAY_TO_SERVER)
            .encoder(DgToyC2SPawprintRuleTogglePacket::encode)
            .decoder(DgToyC2SPawprintRuleTogglePacket::decode)
            .consumerMainThread(DgToyC2SPawprintRuleTogglePacket::handle)
            .add();

        CHANNEL.messageBuilder(DgToyC2SPawprintRuleStringVarPacket.class, 9, NetworkDirection.PLAY_TO_SERVER)
            .encoder(DgToyC2SPawprintRuleStringVarPacket::encode)
            .decoder(DgToyC2SPawprintRuleStringVarPacket::decode)
            .consumerMainThread(DgToyC2SPawprintRuleStringVarPacket::handle)
            .add();

        registered = true;
    }

    public static void sendToPlayer(ServerPlayer player, Object packet) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public static void sendToServer(Object packet) {
        CHANNEL.sendToServer(packet);
    }
}
