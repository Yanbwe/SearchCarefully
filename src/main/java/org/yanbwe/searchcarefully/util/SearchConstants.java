package org.yanbwe.searchcarefully.util;

import net.minecraft.resources.ResourceLocation;
import org.yanbwe.searchcarefully.Config;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class SearchConstants {

    public static final String SEARCH_TIME_REMAINING = "SearchTimeRemaining";

    /**
     * Represents a configured rarity time entry parsed from the config string.
     *
     * @param rarity     The rarity level
     * @param baseTime   Base search time in ticks
     * @param randomTime Random time range in ticks (actual = baseTime +/- random(0, randomTime))
     * @param soundId    Optional sound event ID for completion sound (null = auto-map to 1-7)
     */
    public record RarityTimeRecord(int rarity, int baseTime, int randomTime, @Nullable ResourceLocation soundId) {}

    private static Map<Integer, RarityTimeRecord> rarityTimeCache = new HashMap<>();

    /**
     * Reloads the rarity time cache from the current config values.
     * Called on world load and when config changes.
     */
    public static void reloadCache() {
        rarityTimeCache.clear();
        for (String entry : Config.RARITY_SEARCH_TIMES.get()) {
            String[] parts = entry.split(":", 4);
            if (parts.length >= 3) {
                try {
                    int rarity = Integer.parseInt(parts[0]);
                    int baseTime = Integer.parseInt(parts[1]);
                    int randomTime = Integer.parseInt(parts[2]);
                    ResourceLocation soundId = null;
                    if (parts.length >= 4 && !parts[3].isEmpty()) {
                        soundId = new ResourceLocation(parts[3]);
                    }
                    rarityTimeCache.put(rarity, new RarityTimeRecord(rarity, baseTime, randomTime, soundId));
                } catch (NumberFormatException ignored) {
                    // Skip invalid entries
                }
            }
        }
    }

    /**
     * Gets the configured record for a rarity level.
     * If exact match not found, returns the closest lower configured rarity.
     *
     * @return The record, or null if no configuration exists at all
     */
    @Nullable
    public static RarityTimeRecord getRecord(int rarity) {
        if (rarityTimeCache.isEmpty()) {
            reloadCache();
        }

        RarityTimeRecord exact = rarityTimeCache.get(rarity);
        if (exact != null) return exact;

        return rarityTimeCache.entrySet().stream()
                .filter(e -> e.getKey() <= rarity)
                .max(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue)
                .orElse(null);
    }

    /**
     * Checks if a rarity has a configured entry (exact or fallback available).
     */
    public static boolean isValidRarity(int rarity) {
        return getRecord(rarity) != null;
    }

    /**
     * Gets the base search time for a rarity.
     */
    public static int getSearchTimeByRarity(int rarity) {
        RarityTimeRecord record = getRecord(rarity);
        return record != null ? record.baseTime() : 0;
    }

    /**
     * Gets the random time range for a rarity.
     */
    public static double getRarityRandomTime(int rarity) {
        RarityTimeRecord record = getRecord(rarity);
        return record != null ? record.randomTime() : 0.0;
    }

    /**
     * Gets the configured sound ResourceLocation for a rarity's completion sound.
     * Returns null if not specified in config (caller should use auto-mapping fallback).
     */
    @Nullable
    public static ResourceLocation getCompletionSoundId(int rarity) {
        RarityTimeRecord record = getRecord(rarity);
        return record != null ? record.soundId() : null;
    }

    public static double getSearchSpeedMultiplier() {
        return Config.SEARCH_SPEED_MULTIPLIER.get();
    }

    public static boolean isSearchSystemEnabled() {
        return Config.ENABLE_SEARCH_SYSTEM.get();
    }

    public static boolean isHotbarSearchEnabled() {
        return Config.ENABLE_HOTBAR_SEARCH.get();
    }

    public static boolean isSearchProgressSoundEnabled() {
        return Config.ENABLE_SEARCH_PROGRESS_SOUND.get();
    }
}
