package org.nooberic.dg_toy.mixin;

import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.nooberic.dg_toy.pawprint.PawprintEventDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin {
    private static final double HORIZONTAL_MOVEMENT_SQUARED_THRESHOLD = 0.000001D;

    @Shadow
    public ServerPlayer player;

    @Inject(method = "handleMovePlayer", at = @At("HEAD"))
    private void dgToy$handlePlayerMovement(ServerboundMovePlayerPacket packet, CallbackInfo callbackInfo) {
        if (!packet.hasPosition()) {
            return;
        }

        double deltaX = packet.getX(player.getX()) - player.getX();
        double deltaZ = packet.getZ(player.getZ()) - player.getZ();
        if (deltaX * deltaX + deltaZ * deltaZ > HORIZONTAL_MOVEMENT_SQUARED_THRESHOLD) {
            PawprintEventDispatcher.onPlayerMovement(player, packet);
        }
    }
}