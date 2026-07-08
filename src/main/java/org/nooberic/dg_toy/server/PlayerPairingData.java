package org.nooberic.dg_toy.server;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public final class PlayerPairingData {
    private static final String PERSISTED_ROOT_TAG = "PlayerPersisted";
    private static final String ROOT_TAG = "DgToyPairing";
    private static final String BOUND_TAG = "Bound";
    private static final String BOUND_REMOTE_ID_TAG = "BoundRemoteId";

    private PlayerPairingData() {
    }

    public static boolean isBound(ServerPlayer player) {
        CompoundTag data = getRootTag(player);
        return data != null && data.getBoolean(BOUND_TAG);
    }

    public static void setBound(ServerPlayer player, boolean bound) {
        CompoundTag data = getOrCreateRootTag(player);
        data.putBoolean(BOUND_TAG, bound);
        if (!bound) {
            data.remove(BOUND_REMOTE_ID_TAG);
        }
        cleanup(player, data);
    }

    public static void setBoundRemoteId(ServerPlayer player, UUID remoteId) {
        CompoundTag data = getOrCreateRootTag(player);
        data.putBoolean(BOUND_TAG, true);
        data.putUUID(BOUND_REMOTE_ID_TAG, remoteId);
    }

    public static UUID getBoundRemoteId(ServerPlayer player) {
        CompoundTag data = getRootTag(player);
        if (data == null || !data.hasUUID(BOUND_REMOTE_ID_TAG)) {
            return null;
        }
        return data.getUUID(BOUND_REMOTE_ID_TAG);
    }

    private static CompoundTag getRootTag(ServerPlayer player) {
        CompoundTag persisted = player.getPersistentData();
        if (!persisted.contains(PERSISTED_ROOT_TAG)) {
            return null;
        }
        CompoundTag forgeData = persisted.getCompound(PERSISTED_ROOT_TAG);
        if (!forgeData.contains(ROOT_TAG)) {
            return null;
        }
        return forgeData.getCompound(ROOT_TAG);
    }

    private static CompoundTag getOrCreateRootTag(ServerPlayer player) {
        CompoundTag persisted = player.getPersistentData();
        CompoundTag forgeData = persisted.getCompound(PERSISTED_ROOT_TAG);
        persisted.put(PERSISTED_ROOT_TAG, forgeData);
        if (!forgeData.contains(ROOT_TAG)) {
            forgeData.put(ROOT_TAG, new CompoundTag());
        }
        return forgeData.getCompound(ROOT_TAG);
    }

    private static void cleanup(ServerPlayer player, CompoundTag data) {
        if (!data.getBoolean(BOUND_TAG) && !data.hasUUID(BOUND_REMOTE_ID_TAG)) {
            CompoundTag persisted = player.getPersistentData();
            CompoundTag forgeData = persisted.getCompound(PERSISTED_ROOT_TAG);
            forgeData.remove(ROOT_TAG);
            if (forgeData.isEmpty()) {
                persisted.remove(PERSISTED_ROOT_TAG);
            } else {
                persisted.put(PERSISTED_ROOT_TAG, forgeData);
            }
        }
    }
}