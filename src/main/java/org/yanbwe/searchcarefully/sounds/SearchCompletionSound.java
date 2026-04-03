package org.yanbwe.searchcarefully.sounds;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public class SearchCompletionSound {
    // 为每个稀有度定义音效
    public static final ResourceLocation[] SEARCH_COMPLETION_IDS = new ResourceLocation[8]; // 索引0未使用，1-7对应稀有度
    public static final SoundEvent[] SEARCH_COMPLETION_EVENTS = new SoundEvent[8]; // 索引0未使用，1-7对应稀有度
    
    static {
        for (int i = 1; i <= 7; i++) {
            SEARCH_COMPLETION_IDS[i] = ResourceLocation.fromNamespaceAndPath("searchcarefully", "search_completion_rarity_" + i);
            SEARCH_COMPLETION_EVENTS[i] = SoundEvent.createVariableRangeEvent(SEARCH_COMPLETION_IDS[i]);
        }
    }
}