package org.yanbwe.searchcarefully.sounds;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;

public class SoundHandler {
    
    /**
     * 在世界中播放搜索完成音效，附近的玩家都能听到
     * 
     * @param level 世界对象
     * @param x 音效播放的X坐标
     * @param y 音效播放的Y坐标
     * @param z 音效播放的Z坐标
     * @param rarity 物品稀有度 (1-7)
     */
    public static void playSearchCompletionSound(Level level, double x, double y, double z, int rarity) {
        if (level instanceof ServerLevel serverLevel) {
            // 确保稀有度在有效范围内
            if (rarity >= 1 && rarity <= 7) {
                // 在服务器端播放音效，这样附近的所有玩家都能听到
                serverLevel.playSound(null, x, y, z, SearchCompletionSound.SEARCH_COMPLETION_EVENTS[rarity], SoundSource.BLOCKS, 0.5F, 1.0F);
            } else {
                // 如果稀有度无效，播放默认音效
                serverLevel.playSound(null, x, y, z, SearchCompletionSound.SEARCH_COMPLETION_EVENTS[1], SoundSource.BLOCKS, 0.5F, 1.0F);
            }
        }
    }
    
    /**
     * 为特定玩家播放搜索完成音效
     * 
     * @param player 玩家对象
     * @param rarity 物品稀有度 (1-7)
     */
    public static void playSearchCompletionSoundForPlayer(ServerPlayer player, int rarity) {
        if (player.level() instanceof ServerLevel serverLevel) {
            if (rarity >= 1 && rarity <= 7) {
                serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(), 
                    SearchCompletionSound.SEARCH_COMPLETION_EVENTS[rarity], SoundSource.BLOCKS, 0.5F, 1.0F);
            } else {
                // 如果稀有度无效，播放默认音效
                serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(), 
                    SearchCompletionSound.SEARCH_COMPLETION_EVENTS[1], SoundSource.BLOCKS, 0.5F, 1.0F);
            }
        }
    }
}