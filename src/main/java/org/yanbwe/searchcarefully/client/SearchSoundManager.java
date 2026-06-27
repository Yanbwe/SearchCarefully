package org.yanbwe.searchcarefully.client;

import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import org.yanbwe.searchcarefully.sounds.SearchCompletionSound;

/**
 * 客户端搜索音效管理器
 */
public class SearchSoundManager {

    private static SearchProgressLoopingSound activeSound = null;

    public static void startSearchLoopSound(Player player) {
        stopSearchLoopSound();
        SearchProgressLoopingSound sound = new SearchProgressLoopingSound(
                SearchCompletionSound.SEARCH_PROGRESS_SOUND_EVENT,
                SoundSource.BLOCKS,
                player);
        Minecraft.getInstance().getSoundManager().play(sound);
        activeSound = sound;
    }

    public static void stopSearchLoopSound() {
        if (activeSound != null) {
            activeSound.stop();
            activeSound = null;
        }
        Minecraft.getInstance().getSoundManager().stop(
                SearchCompletionSound.SEARCH_PROGRESS_SOUND_ID,
                SoundSource.BLOCKS);
    }
}
