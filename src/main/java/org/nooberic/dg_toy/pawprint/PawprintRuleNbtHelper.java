package org.nooberic.dg_toy.pawprint;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.nooberic.dg_toy.item.PawprintItem;

public final class PawprintRuleNbtHelper {
    private static final String RULES_TAG = "PawprintRules";
    private static final String ENABLED_TAG = "enabled";
    private static final String VARS_TAG = "vars";

    private PawprintRuleNbtHelper() {
    }

    public static ItemStack getOwnerPawprintStack(ServerPlayer player) {
        ItemStack mainHand = player.getMainHandItem();
        return mainHand.getItem() instanceof PawprintItem ? mainHand : ItemStack.EMPTY;
    }

    public static boolean hasOwnerPawprint(ServerPlayer player) {
        return !getOwnerPawprintStack(player).isEmpty();
    }

    public static CompoundTag getOrCreateRuleTag(ItemStack stack, String ruleId) {
        CompoundTag rootTag = stack.getOrCreateTag();
        CompoundTag rulesTag = rootTag.contains(RULES_TAG, CompoundTag.TAG_COMPOUND)
                ? rootTag.getCompound(RULES_TAG)
                : new CompoundTag();
        CompoundTag ruleTag = rulesTag.contains(ruleId, CompoundTag.TAG_COMPOUND)
                ? rulesTag.getCompound(ruleId)
                : new CompoundTag();

        if (!ruleTag.contains(ENABLED_TAG)) {
            ruleTag.putBoolean(ENABLED_TAG, true);
        }
        if (!ruleTag.contains(VARS_TAG, CompoundTag.TAG_COMPOUND)) {
            ruleTag.put(VARS_TAG, new CompoundTag());
        }

        rulesTag.put(ruleId, ruleTag);
        rootTag.put(RULES_TAG, rulesTag);
        return ruleTag;
    }

    public static boolean isRuleEnabled(ItemStack stack, String ruleId) {
        return getOrCreateRuleTag(stack, ruleId).getBoolean(ENABLED_TAG);
    }

    public static CompoundTag getOrCreateVariablesTag(ItemStack stack, String ruleId) {
        CompoundTag ruleTag = getOrCreateRuleTag(stack, ruleId);
        CompoundTag varsTag = ruleTag.getCompound(VARS_TAG);
        ruleTag.put(VARS_TAG, varsTag);
        return varsTag;
    }

    public static String getStringVariable(ItemStack stack, String ruleId, PawprintRuleVariable<String> variable) {
        CompoundTag varsTag = getOrCreateVariablesTag(stack, ruleId);
        String value = varsTag.contains(variable.key()) ? varsTag.getString(variable.key()) : variable.defaultValue();
        if (!variable.isValid(value)) {
            value = variable.defaultValue();
            varsTag.putString(variable.key(), value);
        }
        if (!varsTag.contains(variable.key())) {
            varsTag.putString(variable.key(), value);
        }
        return value;
    }
}