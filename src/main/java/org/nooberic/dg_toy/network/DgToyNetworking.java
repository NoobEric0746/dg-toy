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

        registered = true;
    }

    public static void sendToPlayer(ServerPlayer player, Object packet) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public static void sendToServer(Object packet) {
        CHANNEL.sendToServer(packet);
    }
}
