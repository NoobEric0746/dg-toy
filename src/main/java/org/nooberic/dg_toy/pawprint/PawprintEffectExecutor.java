package org.nooberic.dg_toy.pawprint;

import net.minecraft.server.level.ServerPlayer;
import org.nooberic.dg_toy.server.RemoteControlStateHelper;

public final class PawprintEffectExecutor {
    private PawprintEffectExecutor() {
    }

    public static boolean apply(ServerPlayer player, int channel, PawprintEffect effect) {
        if (!RemoteControlStateHelper.isShockAvailable(player) || effect.pulseId().isBlank()) {
            return false;
        }

        org.nooberic.dglib.multiplayer.ServerCoyote coyote = org.nooberic.dglib.multiplayer.DgServerCoyoteApi.get().getCoyote(player);
        if (effect.hardMode()) {
            coyote.control(channel, effect.strength(), effect.pulseId(), effect.duration());
        } else {
            coyote.controlSoft(channel, effect.strength(), effect.pulseId(), effect.duration());
        }
        return true;
    }
}