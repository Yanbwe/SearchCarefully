package org.yanbwe.searchcarefully.sounds;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.yanbwe.searchcarefully.util.SearchConstants;

public class SoundHandler {

    /**
     * 在世界中播放搜索完成音效，附近的玩家都能听到
     *
     * @param level  世界对象
     * @param x      音效播放的X坐标
     * @param y      音效播放的Y坐标
     * @param z      音效播放的Z坐标
     * @param rarity 物品稀有度（任意整数，支持超出1-7的范围）
     */
    public static void playSearchCompletionSound(Level level, double x, double y, double z, int rarity) {
        if (level instanceof ServerLevel serverLevel) {
            SoundEvent soundEvent = resolveCompletionSound(rarity);
            serverLevel.playSound(null, x, y, z, soundEvent, SoundSource.BLOCKS, 0.5F, 1.0F);
        }
    }

    /**
     * 为特定玩家播放搜索完成音效
     *
     * @param player 玩家对象
     * @param rarity 物品稀有度（任意整数，支持超出1-7的范围）
     */
    public static void playSearchCompletionSoundForPlayer(ServerPlayer player, int rarity) {
        if (player.level() instanceof ServerLevel serverLevel) {
            SoundEvent soundEvent = resolveCompletionSound(rarity);
            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                    soundEvent, SoundSource.BLOCKS, 0.5F, 1.0F);
        }
    }

    /**
     * Resolves the completion sound for a given rarity.
     * Checks the config for a custom sound ID first, falls back to clamped 1-7 built-in sounds.
     */
    private static SoundEvent resolveCompletionSound(int rarity) {
        // Try config-specified sound first
        ResourceLocation configSoundId = SearchConstants.getCompletionSoundId(rarity);
        if (configSoundId != null) {
            SoundEvent customSound = ForgeRegistries.SOUND_EVENTS.getValue(configSoundId);
            if (customSound != null) {
                return customSound;
            }
        }
        // Fallback: clamp rarity to 1-7 and use built-in completion sound
        return SearchCompletionSound.getClampedCompletionSound(rarity);
    }
}
