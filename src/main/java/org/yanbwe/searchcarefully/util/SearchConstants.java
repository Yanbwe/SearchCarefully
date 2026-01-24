package org.yanbwe.searchcarefully.util;

import org.yanbwe.searchcarefully.Config;

public class SearchConstants {
    public static final String SEARCH_TIME_REMAINING = "SearchTimeRemaining";
    
    // 根据稀有度设置搜索时间 (单位: ticks)
    public static int getSearchTimeByRarity(int rarity) {
        if (rarity >= 1 && rarity <= 7) {
            return Config.RARITY_BASE_TIMES[rarity].get();
        }
        return 0;   // 无效稀有度
    }
}