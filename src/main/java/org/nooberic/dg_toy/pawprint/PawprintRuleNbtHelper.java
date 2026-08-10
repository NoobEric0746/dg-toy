package org.nooberic.dg_toy.pawprint;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.nooberic.dg_toy.item.ModItems;
import org.nooberic.dg_toy.item.PawprintItem;
import org.nooberic.dg_toy.util.ItemStackNbt;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

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
        CuriosApi.getCuriosInventory(player)
                .flatMap(handler -> handler.getStacksHandler(PAWPRINT_SLOT_ID))
                .ifPresent(handler -> ruleDevices.addAll(getRuleDevicesFromSlots(player, handler, PAWPRINT_SLOT_ID)));
        CuriosApi.getCuriosInventory(player)
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
        return clampStrength(ItemStackNbt.getInt(stack, STRENGTH_TAG, DEFAULT_STRENGTH));
    }

    public static void setStrength(ItemStack stack, int strength) {
        ItemStackNbt.putInt(stack, STRENGTH_TAG, clampStrength(strength));
    }

    public static boolean isHardMode(ItemStack stack) {
        return ItemStackNbt.getBoolean(stack, HARD_MODE_TAG, DEFAULT_HARD_MODE);
    }

    public static void setHardMode(ItemStack stack, boolean hardMode) {
        ItemStackNbt.putBoolean(stack, HARD_MODE_TAG, hardMode);
    }

    public static int getChannel(ItemStack stack) {
        return clampChannel(ItemStackNbt.getInt(stack, CHANNEL_TAG, DEFAULT_CHANNEL));
    }

    public static void setChannel(ItemStack stack, int channel) {
        ItemStackNbt.putInt(stack, CHANNEL_TAG, clampChannel(channel));
    }

    /** 对指定规则的 NBT 子节点做读改写。 */
    private static void updateRuleTag(ItemStack stack, String ruleId, Consumer<CompoundTag> action) {
        ItemStackNbt.update(stack, root -> {
            CompoundTag rulesTag = root.contains(RULES_TAG) ? root.getCompoundOrEmpty(RULES_TAG) : new CompoundTag();
            CompoundTag ruleTag = rulesTag.contains(ruleId) ? rulesTag.getCompoundOrEmpty(ruleId) : new CompoundTag();
            if (!ruleTag.contains(ENABLED_TAG)) {
                ruleTag.putBoolean(ENABLED_TAG, false);
            }
            if (!ruleTag.contains(VARS_TAG)) {
                ruleTag.put(VARS_TAG, new CompoundTag());
            }
            action.accept(ruleTag);
            rulesTag.put(ruleId, ruleTag);
            root.put(RULES_TAG, rulesTag);
        });
    }

    /** 兼容旧调用：返回规则的 NBT 副本（只读语义，修改不会写回）。 */
    public static CompoundTag getOrCreateRuleTag(ItemStack stack, String ruleId) {
        CompoundTag root = ItemStackNbt.getTag(stack);
        if (!root.contains(RULES_TAG)) {
            return new CompoundTag();
        }
        CompoundTag rulesTag = root.getCompoundOrEmpty(RULES_TAG);
        if (!rulesTag.contains(ruleId)) {
            return new CompoundTag();
        }
        return rulesTag.getCompoundOrEmpty(ruleId).copy();
    }

    public static boolean isRuleEnabled(ItemStack stack, String ruleId) {
        CompoundTag root = ItemStackNbt.getTag(stack);
        if (!root.contains(RULES_TAG)) {
            return false;
        }
        CompoundTag rulesTag = root.getCompoundOrEmpty(RULES_TAG);
        if (!rulesTag.contains(ruleId)) {
            return false;
        }
        return rulesTag.getCompoundOrEmpty(ruleId).getBooleanOr(ENABLED_TAG, false);
    }

    public static void setRuleEnabled(ItemStack stack, String ruleId, boolean enabled) {
        updateRuleTag(stack, ruleId, ruleTag -> ruleTag.putBoolean(ENABLED_TAG, enabled));
    }

    /** 兼容旧调用：返回变量子节点的 NBT 副本（只读语义）。 */
    public static CompoundTag getOrCreateVariablesTag(ItemStack stack, String ruleId) {
        CompoundTag ruleTag = getOrCreateRuleTag(stack, ruleId);
        return ruleTag.contains(VARS_TAG) ? ruleTag.getCompoundOrEmpty(VARS_TAG).copy() : new CompoundTag();
    }

    public static String getStringVariable(ItemStack stack, String ruleId, PawprintRuleVariable<String> variable) {
        CompoundTag varsTag = getOrCreateVariablesTag(stack, ruleId);
        String value = varsTag.contains(variable.key())
                ? varsTag.getStringOr(variable.key(), variable.defaultValue())
                : variable.defaultValue();
        if (!variable.isValid(value)) {
            value = variable.defaultValue();
            setStringVariable(stack, ruleId, variable.key(), value);
        } else if (!varsTag.contains(variable.key())) {
            setStringVariable(stack, ruleId, variable.key(), value);
        }
        return value;
    }

    public static void setStringVariable(ItemStack stack, String ruleId, String key, String value) {
        updateRuleTag(stack, ruleId, ruleTag -> {
            CompoundTag varsTag = ruleTag.contains(VARS_TAG) ? ruleTag.getCompoundOrEmpty(VARS_TAG) : new CompoundTag();
            varsTag.putString(key, value);
            ruleTag.put(VARS_TAG, varsTag);
        });
    }

    private static int clampStrength(int strength) {
        return Math.max(0, Math.min(100, strength));
    }

    private static int clampChannel(int channel) {
        return channel >= 1 && channel <= 3 ? channel : DEFAULT_CHANNEL;
    }
}
