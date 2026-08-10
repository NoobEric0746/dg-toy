package org.nooberic.dg_toy.util;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * 26.1.2 兼容层：ItemStack 不再提供 getTag/getOrCreateTag/setTag，
 * 任意 NBT 统一存入 CUSTOM_DATA 组件。本类提供语义等价的读写封装。
 */
public final class ItemStackNbt {
    private ItemStackNbt() {
    }

    /** 读取整份自定义 NBT（返回副本，改动不会写回）。 */
    public static CompoundTag getTag(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    /** 覆盖整份自定义 NBT。 */
    public static void setTag(ItemStack stack, CompoundTag tag) {
        CustomData.set(DataComponents.CUSTOM_DATA, stack, tag);
    }

    /** 读改写：应用修改并写回；结果为空时自动移除组件。 */
    public static void update(ItemStack stack, Consumer<CompoundTag> action) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, action);
    }

    public static boolean hasTag(ItemStack stack) {
        return stack.has(DataComponents.CUSTOM_DATA);
    }

    public static void putInt(ItemStack stack, String key, int value) {
        update(stack, tag -> tag.putInt(key, value));
    }

    public static void putBoolean(ItemStack stack, String key, boolean value) {
        update(stack, tag -> tag.putBoolean(key, value));
    }

    public static void putString(ItemStack stack, String key, String value) {
        update(stack, tag -> tag.putString(key, value));
    }

    public static void putLong(ItemStack stack, String key, long value) {
        update(stack, tag -> tag.putLong(key, value));
    }

    public static void putUUID(ItemStack stack, String key, UUID value) {
        update(stack, tag -> putUUID(tag, key, value));
    }

    public static void remove(ItemStack stack, String key) {
        update(stack, tag -> tag.remove(key));
    }

    public static int getInt(ItemStack stack, String key, int defaultValue) {
        return getTag(stack).getIntOr(key, defaultValue);
    }

    public static boolean getBoolean(ItemStack stack, String key, boolean defaultValue) {
        return getTag(stack).getBooleanOr(key, defaultValue);
    }

    public static String getString(ItemStack stack, String key, String defaultValue) {
        return getTag(stack).getStringOr(key, defaultValue);
    }

    public static long getLong(ItemStack stack, String key, long defaultValue) {
        return getTag(stack).getLongOr(key, defaultValue);
    }

    public static boolean contains(ItemStack stack, String key) {
        return getTag(stack).contains(key);
    }

    /** 读取 UUID；不存在或格式非法时返回 null。 */
    public static UUID getUUID(ItemStack stack, String key) {
        return getUUID(getTag(stack), key);
    }

    // ---- CompoundTag UUID 工具（26.1.2 已移除 putUUID/getUUID/hasUUID）----

    /** 以 4 个 int 存储 UUID，格式与旧版 putUUID 完全一致，可读回旧存档。 */
    public static void putUUID(CompoundTag tag, String key, UUID value) {
        tag.putIntArray(key, new int[]{
                (int) (value.getMostSignificantBits() >> 32),
                (int) value.getMostSignificantBits(),
                (int) (value.getLeastSignificantBits() >> 32),
                (int) value.getLeastSignificantBits()});
    }

    public static UUID getUUID(CompoundTag tag, String key) {
        if (!tag.contains(key)) {
            return null;
        }
        int[] parts = tag.getIntArray(key).orElse(null);
        if (parts == null || parts.length != 4) {
            return null;
        }
        return new UUID(
                ((long) parts[0] << 32) | (parts[1] & 0xFFFFFFFFL),
                ((long) parts[2] << 32) | (parts[3] & 0xFFFFFFFFL));
    }

    public static boolean hasUUID(CompoundTag tag, String key) {
        return tag.contains(key) && tag.getIntArray(key).isPresent();
    }
}
