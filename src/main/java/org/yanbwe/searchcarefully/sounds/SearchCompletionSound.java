package org.yanbwe.searchcarefully.sounds;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public class SearchCompletionSound {

    public static final int MAX_REGISTERED_RARITY_SOUNDS = 7;

    public static final ResourceLocation[] SEARCH_COMPLETION_IDS = new ResourceLocation[MAX_REGISTERED_RARITY_SOUNDS + 1];
    public static final SoundEvent[] SEARCH_COMPLETION_EVENTS = new SoundEvent[MAX_REGISTERED_RARITY_SOUNDS + 1];

    public static final ResourceLocation SEARCH_PROGRESS_SOUND_ID =
            ResourceLocation.fromNamespaceAndPath("searchcarefully", "search_progress");
    public static final SoundEvent SEARCH_PROGRESS_SOUND_EVENT =
            SoundEvent.createVariableRangeEvent(SEARCH_PROGRESS_SOUND_ID);

    static {
        for (int i = 1; i <= MAX_REGISTERED_RARITY_SOUNDS; i++) {
            SEARCH_COMPLETION_IDS[i] = ResourceLocation.fromNamespaceAndPath("searchcarefully", "search_completion_rarity_" + i);
            SEARCH_COMPLETION_EVENTS[i] = SoundEvent.createVariableRangeEvent(SEARCH_COMPLETION_IDS[i]);
        }
    }

    public static SoundEvent getClampedCompletionSound(int rarity) {
        int clamped = Math.max(1, Math.min(MAX_REGISTERED_RARITY_SOUNDS, rarity));
        return SEARCH_COMPLETION_EVENTS[clamped];
    }
}
