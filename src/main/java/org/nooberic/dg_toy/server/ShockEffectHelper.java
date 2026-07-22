package org.nooberic.dg_toy.server;

import net.minecraft.server.level.ServerPlayer;
import org.nooberic.dglib.multiplayer.DgServerCoyoteApi;
import org.nooberic.dglib.multiplayer.ServerCoyote;
import org.nooberic.dglib.util.ElectricityParticleUtil;

public final class ShockEffectHelper {
    private ShockEffectHelper() {
    }

    public static void apply(ServerPlayer player, boolean hardMode, int channel, int strength, String pulseId, int duration) {
        ServerCoyote coyote = DgServerCoyoteApi.get().getCoyote(player);
        if (hardMode) {
            coyote.control(channel, strength, pulseId, duration);
        } else {
            coyote.controlSoft(channel, strength, pulseId, duration);
        }
        ElectricityParticleUtil.spawnAroundPlayer(player);
    }
}