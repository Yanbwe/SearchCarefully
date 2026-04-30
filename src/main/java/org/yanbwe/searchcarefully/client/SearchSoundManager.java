package org.yanbwe.searchcarefully.client;

import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import org.yanbwe.searchcarefully.sounds.SearchCompletionSound;

/**
 * 客户端搜索音效管理器。
 * 维护当前活跃的循环音效引用，支持启动和停止。
 */
public class SearchSoundManager {

    private static SearchProgressLoopingSound activeSound = null;

    /**
     * 启动搜索进度循环音效。
     * 如果已有活跃音效，会先停止再重新启动。
     */
    public static void startSearchLoopSound(Player player) {
        stopSearchLoopSound();

        SearchProgressLoopingSound sound = new SearchProgressLoopingSound(
                SearchCompletionSound.SEARCH_PROGRESS_SOUND_EVENT,
                SoundSource.BLOCKS,
                player
        );
        Minecraft.getInstance().getSoundManager().play(sound);
        activeSound = sound;
    }

    /**
     * 停止搜索进度循环音效。
     * 同时通过 SoundManager 停止以确保清理干净。
     */
    public static void stopSearchLoopSound() {
        if (activeSound != null) {
            activeSound.stop();
            activeSound = null;
        }
        Minecraft.getInstance().getSoundManager().stop(
                SearchCompletionSound.SEARCH_PROGRESS_SOUND_ID,
                SoundSource.BLOCKS
        );
    }
}
