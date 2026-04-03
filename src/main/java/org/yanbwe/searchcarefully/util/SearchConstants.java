package org.yanbwe.searchcarefully.util;

import org.yanbwe.searchcarefully.Config;

public class SearchConstants {
    
    public static final String SEARCH_TIME_REMAINING = "SearchTimeRemaining";
    
    public static final int MIN_RARITY = 1;
    public static final int MAX_RARITY = 7;
    
    public static boolean isValidRarity(int rarity) {
        return rarity >= MIN_RARITY && rarity <= MAX_RARITY;
    }
    
    public static int getSearchTimeByRarity(int rarity) {
        if (isValidRarity(rarity)) {
            return Config.RARITY_BASE_TIMES[rarity].get();
        }
        return 0;
    }
    
    public static double getRarityRandomTime(int rarity) {
        if (isValidRarity(rarity)) {
            return Config.RARITY_RANDOM_TIMES[rarity].get();
        }
        return 0.0;
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
}