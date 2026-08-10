package org.nooberic.dg_toy.server;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class PairingClearSavedData extends SavedData {
    private static final String DATA_NAME = "pairing_clear";

    private static final Codec<Entry> ENTRY_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.LONG.fieldOf("ClearTime").forGetter(Entry::clearTime),
            Codec.BOOL.fieldOf("ClearFlag").forGetter(Entry::clearFlag)
    ).apply(instance, Entry::new));

    private static final Codec<PairingClearSavedData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(Codec.STRING, ENTRY_CODEC)
                    .optionalFieldOf("Entries", Map.of())
                    .forGetter(data -> {
                        Map<String, Entry> stringMap = new HashMap<>();
                        data.entries.forEach((uuid, entry) -> stringMap.put(uuid.toString(), entry));
                        return stringMap;
                    })
    ).apply(instance, entries -> {
        PairingClearSavedData data = new PairingClearSavedData();
        entries.forEach((key, entry) -> {
            try {
                data.entries.put(UUID.fromString(key), entry);
            } catch (IllegalArgumentException ignored) {
            }
        });
        return data;
    }));

    public static final SavedDataType<PairingClearSavedData> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath("dg_toy", DATA_NAME),
            PairingClearSavedData::new,
            CODEC);

    private final Map<UUID, Entry> entries = new HashMap<>();

    public static PairingClearSavedData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    public static PairingClearSavedData get() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            throw new IllegalStateException("Server not available");
        }
        return get(server);
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

    private record Entry(long clearTime, boolean clearFlag) {
    }
}
