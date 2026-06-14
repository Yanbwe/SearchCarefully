package org.yanbwe.searchcarefully.sounds;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public class SearchCompletionSound {
    /** Maximum number of built-in registered rarity completion sounds (rarity 1-7). */
    public static final int MAX_REGISTERED_RARITY_SOUNDS = 7;

    // Completion sounds for rarity 1-7 (used as fallback bank when no custom sound is configured)
    public static final ResourceLocation[] SEARCH_COMPLETION_IDS = new ResourceLocation[MAX_REGISTERED_RARITY_SOUNDS + 1];
    public static final SoundEvent[] SEARCH_COMPLETION_EVENTS = new SoundEvent[MAX_REGISTERED_RARITY_SOUNDS + 1];

    // 搜索进度音效（单一音效，不按稀有度区分）
    public static final ResourceLocation SEARCH_PROGRESS_SOUND_ID = new ResourceLocation("searchcarefully", "search_progress");
    public static final SoundEvent SEARCH_PROGRESS_SOUND_EVENT = SoundEvent.createVariableRangeEvent(SEARCH_PROGRESS_SOUND_ID);

    static {
        for (int i = 1; i <= MAX_REGISTERED_RARITY_SOUNDS; i++) {
            SEARCH_COMPLETION_IDS[i] = new ResourceLocation("searchcarefully", "search_completion_rarity_" + i);
            SEARCH_COMPLETION_EVENTS[i] = SoundEvent.createVariableRangeEvent(SEARCH_COMPLETION_IDS[i]);
        }
    }

    /**
     * Gets a fallback completion sound by clamping the rarity to the available 1-7 range.
     * Used when no custom sound is configured for a rarity level.
     */
    public static SoundEvent getClampedCompletionSound(int rarity) {
        int clamped = Math.max(1, Math.min(MAX_REGISTERED_RARITY_SOUNDS, rarity));
        return SEARCH_COMPLETION_EVENTS[clamped];
    }
}