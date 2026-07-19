package org.nooberic.dg_toy.pawprint;

import org.nooberic.dg_toy.item.PawprintItem.PawprintMode;
import org.nooberic.dg_toy.pawprint.rule.ChatContainsKeywordRule;
import org.nooberic.dg_toy.pawprint.rule.ChatNumberEqualsVariablesSumRule;
import org.nooberic.dg_toy.pawprint.rule.PlayerCrouchRule;
import org.nooberic.dg_toy.pawprint.rule.PlayerHurtShockRule;

public final class PawprintRules {
    private static boolean initialized;

    private PawprintRules() {
    }

    public static void bootstrap() {
        if (initialized) {
            return;
        }
        initialized = true;
        PawprintRuleRegistry.register(PawprintMode.SOUND, new ChatContainsKeywordRule());
        PawprintRuleRegistry.register(PawprintMode.SOUND, new ChatNumberEqualsVariablesSumRule());
        PawprintRuleRegistry.register(PawprintMode.MOTION, new PlayerCrouchRule());
        PawprintRuleRegistry.register(PawprintMode.HURT, new PlayerHurtShockRule());
    }
}