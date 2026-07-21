package org.nooberic.dg_toy.pawprint;

import org.nooberic.dg_toy.item.PawprintItem.PawprintMode;
import org.nooberic.dg_toy.pawprint.rule.ChatContainsKeywordRule;
import org.nooberic.dg_toy.pawprint.rule.ChatNumberEqualsVariablesSumRule;
import org.nooberic.dg_toy.pawprint.rule.BeaconDistanceShockRule;
import org.nooberic.dg_toy.pawprint.rule.PlayerCrouchRule;
import org.nooberic.dg_toy.pawprint.rule.PlayerAttackMobShockRule;
import org.nooberic.dg_toy.pawprint.rule.PlayerHurtShockRule;
import org.nooberic.dg_toy.pawprint.rule.PlayerMustCrouchRule;
import org.nooberic.dg_toy.pawprint.rule.PlayerMustNotSprintRule;
import org.nooberic.dg_toy.pawprint.rule.PlayerMustStayStillRule;
import org.nooberic.dg_toy.pawprint.rule.PlayerRightClickShockRule;

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
        PawprintRuleRegistry.register(PawprintMode.MOTION, new PlayerMustCrouchRule());
        PawprintRuleRegistry.register(PawprintMode.MOTION, new PlayerMustNotSprintRule());
        PawprintRuleRegistry.register(PawprintMode.MOTION, new PlayerAttackMobShockRule());
        PawprintRuleRegistry.register(PawprintMode.MOTION, new PlayerRightClickShockRule());
        PawprintRuleRegistry.register(PawprintMode.MOTION, new PlayerMustStayStillRule());
        PawprintRuleRegistry.register(PawprintMode.HURT, new PlayerHurtShockRule());
        PawprintRuleRegistry.register(PawprintMode.COLLAR, new BeaconDistanceShockRule());
    }
}