package org.nooberic.dg_toy.server;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class PairingClearSavedData extends SavedData {
    private static final String DATA_NAME = "dg_toy_pairing_clear";
    private static final String ENTRIES_TAG = "Entries";
    private static final String CLEAR_TIME_TAG = "ClearTime";
    private static final String CLEAR_FLAG_TAG = "ClearFlag";

    private final Map<UUID, Entry> entries = new HashMap<>();

    public static PairingClearSavedData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(PairingClearSavedData::load, PairingClearSavedData::new, DATA_NAME);
    }

    public static PairingClearSavedData get() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            throw new IllegalStateException("Server not available");
        }
        return get(server);
    }

    public static PairingClearSavedData load(CompoundTag tag) {
        PairingClearSavedData data = new PairingClearSavedData();
        CompoundTag entriesTag = tag.getCompound(ENTRIES_TAG);
        for (String key : entriesTag.getAllKeys()) {
            try {
                UUID uuid = UUID.fromString(key);
                CompoundTag entryTag = entriesTag.getCompound(key);
                data.entries.put(uuid, new Entry(entryTag.getLong(CLEAR_TIME_TAG), entryTag.getBoolean(CLEAR_FLAG_TAG)));
            } catch (IllegalArgumentException ignored) {
            }
        }
        return data;
    }

    public long getClearTime(UUID playerId) {
        Entry entry = entries.get(playerId);
        return entry == null ? 0L : entry.clearTime();
    }

    public boolean getClearFlag(UUID playerId) {
        Entry entry = entries.get(playerId);
        return entry != null && entry.clearFlag();
    }

    public void setClearTime(UUID playerId, long clearTime) {
        Entry entry = entries.get(playerId);
        boolean clearFlag = entry != null && entry.clearFlag();
        entries.put(playerId, new Entry(clearTime, clearFlag));
        setDirty();
    }

    public void setClearFlag(UUID playerId, boolean clearFlag) {
        Entry entry = entries.get(playerId);
        long clearTime = entry == null ? 0L : entry.clearTime();
        if (!clearFlag && clearTime == 0L) {
            entries.remove(playerId);
        } else {
            entries.put(playerId, new Entry(clearTime, clearFlag));
        }
        setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        CompoundTag entriesTag = new CompoundTag();
        for (Map.Entry<UUID, Entry> mapEntry : entries.entrySet()) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putLong(CLEAR_TIME_TAG, mapEntry.getValue().clearTime());
            entryTag.putBoolean(CLEAR_FLAG_TAG, mapEntry.getValue().clearFlag());
            entriesTag.put(mapEntry.getKey().toString(), entryTag);
        }
        tag.put(ENTRIES_TAG, entriesTag);
        return tag;
    }

    private record Entry(long clearTime, boolean clearFlag) {
    }
}