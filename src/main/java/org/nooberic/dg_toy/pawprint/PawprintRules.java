package org.nooberic.dg_toy.pawprint;

import org.nooberic.dg_toy.pawprint.rule.ChatContainsKeywordRule;
import org.nooberic.dg_toy.pawprint.rule.ChatNumberEqualsVariablesSumRule;
import org.nooberic.dg_toy.pawprint.rule.PlayerCrouchRule;

public final class PawprintRules {
    private static boolean initialized;

    private PawprintRules() {
    }

    public static void bootstrap() {
        if (initialized) {
            return;
        }
        initialized = true;
        PawprintRuleRegistry.register(new ChatContainsKeywordRule());
        PawprintRuleRegistry.register(new ChatNumberEqualsVariablesSumRule());
        PawprintRuleRegistry.register(new PlayerCrouchRule());
    }
}