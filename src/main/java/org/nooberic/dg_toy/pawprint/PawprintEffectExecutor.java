package org.nooberic.dg_toy.pawprint;

import net.minecraft.server.level.ServerPlayer;
import org.nooberic.dg_toy.server.RemoteControlStateHelper;
import org.nooberic.dg_toy.server.ShockEffectHelper;

public final class PawprintEffectExecutor {
    private PawprintEffectExecutor() {
    }

    public static boolean apply(ServerPlayer player, int channel, PawprintEffect effect) {
        if (!RemoteControlStateHelper.isShockAvailable(player) || effect.pulseId().isBlank()) {
            return false;
        }

        ShockEffectHelper.apply(player, effect.hardMode(), channel, effect.strength(), effect.pulseId(), effect.duration());
        return true;
    }
}