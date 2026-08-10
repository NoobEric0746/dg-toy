package org.nooberic.dg_toy.server;

import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerPlayer;

/**
 * 替代已移除的 Player#displayClientMessage(Component, boolean)：
 * true -> 动作栏消息，false -> 聊天栏消息。
 */
public final class PlayerMessageCompat {
    private PlayerMessageCompat() {
    }

    public static void displayClientMessage(ServerPlayer player, Component message, boolean actionBar) {
        if (actionBar) {
            player.connection.send(new ClientboundSetActionBarTextPacket(message));
        } else {
            player.sendSystemMessage(message);
        }
    }
}
