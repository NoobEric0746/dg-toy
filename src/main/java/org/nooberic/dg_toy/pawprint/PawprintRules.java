package org.nooberic.dg_toy.pawprint;

import org.nooberic.dg_toy.pawprint.rule.ChatContainsKeywordRule;

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
    }
}