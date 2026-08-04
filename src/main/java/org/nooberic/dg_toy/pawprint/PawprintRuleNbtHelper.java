package org.nooberic.dg_toy.pawprint;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.nooberic.dg_toy.item.ModItems;
import org.nooberic.dg_toy.item.PawprintItem;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.ArrayList;
import java.util.List;

public final class PawprintRuleNbtHelper {
    private static final String RULES_TAG = "PawprintRules";
    private static final String ENABLED_TAG = "enabled";
    private static final String VARS_TAG = "vars";
    private static final String STRENGTH_TAG = "Strength";
    private static final String HARD_MODE_TAG = "HardMode";
    private static final String CHANNEL_TAG = "Channel";
    private static final String PAWPRINT_SLOT_ID = "pawprint";
    private static final String COLLAR_SLOT_ID = "dg_collar";
    private static final int DEFAULT_STRENGTH = 0;
    private static final boolean DEFAULT_HARD_MODE = true;
    public static final int DEFAULT_CHANNEL = 3;

    private PawprintRuleNbtHelper() {
    }

    public static List<SlotResult> getEquippedPawprints(ServerPlayer player) {
        List<SlotResult> ruleDevices = new ArrayList<>();
        CuriosApi.getCuriosInventory(player).resolve()
                .flatMap(handler -> handler.getStacksHandler(PAWPRINT_SLOT_ID))
                .ifPresent(handler -> ruleDevices.addAll(getRuleDevicesFromSlots(player, handler, PAWPRINT_SLOT_ID)));
        CuriosApi.getCuriosInventory(player).resolve()
                .flatMap(handler -> handler.getStacksHandler(COLLAR_SLOT_ID))
                .ifPresent(handler -> ruleDevices.addAll(getRuleDevicesFromSlots(player, handler, COLLAR_SLOT_ID)));
        return List.copyOf(ruleDevices);
    }

    private static List<SlotResult> getRuleDevicesFromSlots(ServerPlayer player, ICurioStacksHandler handler, String slotId) {
        List<SlotResult> ruleDevices = new ArrayList<>();
        for (int index = 0; index < handler.getSlots(); index++) {
            ItemStack stack = handler.getStacks().getStackInSlot(index);
            if (isRuleDevice(stack)) {
                boolean visible = handler.getRenders().size() > index && handler.getRenders().get(index);
                ruleDevices.add(new SlotResult(new SlotContext(slotId, player, index, false, visible), stack));
            }
        }
        return ruleDevices;
    }

    public static boolean hasEquippedPawprint(ServerPlayer player) {
        return !getEquippedPawprints(player).isEmpty();
    }

    public static boolean isRuleDevice(ItemStack stack) {
        return stack.getItem() instanceof PawprintItem || stack.is(ModItems.DG_COLLAR.get());
    }

    public static boolean isCollar(ItemStack stack) {
        return stack.is(ModItems.DG_COLLAR.get());
    }

    public static PawprintItem.PawprintMode getMode(ItemStack stack) {
        return isCollar(stack) ? PawprintItem.PawprintMode.COLLAR : PawprintItem.getMode(stack);
    }

    public static int getStrength(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains(STRENGTH_TAG)) {
            tag.putInt(STRENGTH_TAG, DEFAULT_STRENGTH);
        }
        int strength = clampStrength(tag.getInt(STRENGTH_TAG));
        tag.putInt(STRENGTH_TAG, strength);
        return strength;
    }

    public static void setStrength(ItemStack stack, int strength) {
        stack.getOrCreateTag().putInt(STRENGTH_TAG, clampStrength(strength));
    }

    public static boolean isHardMode(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains(HARD_MODE_TAG)) {
            tag.putBoolean(HARD_MODE_TAG, DEFAULT_HARD_MODE);
        }
        return tag.getBoolean(HARD_MODE_TAG);
    }

    public static void setHardMode(ItemStack stack, boolean hardMode) {
        stack.getOrCreateTag().putBoolean(HARD_MODE_TAG, hardMode);
    }

    public static int getChannel(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        int channel = tag.contains(CHANNEL_TAG) ? clampChannel(tag.getInt(CHANNEL_TAG)) : DEFAULT_CHANNEL;
        tag.putInt(CHANNEL_TAG, channel);
        return channel;
    }

    public static void setChannel(ItemStack stack, int channel) {
        stack.getOrCreateTag().putInt(CHANNEL_TAG, clampChannel(channel));
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
            ruleTag.putBoolean(ENABLED_TAG, false);
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

    private static int clampStrength(int strength) {
        return Math.max(0, Math.min(100, strength));
    }

    private static int clampChannel(int channel) {
        return channel >= 1 && channel <= 3 ? channel : DEFAULT_CHANNEL;
    }
}