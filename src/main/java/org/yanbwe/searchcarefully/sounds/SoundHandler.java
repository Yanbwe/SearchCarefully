package org.yanbwe.searchcarefully.sounds;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import org.yanbwe.searchcarefully.util.SearchConstants;

public class SoundHandler {

    public static void playSearchCompletionSound(Level level, double x, double y, double z, int rarity) {
        if (level instanceof ServerLevel serverLevel) {
            SoundEvent soundEvent = resolveCompletionSound(rarity);
            serverLevel.playSound(null, x, y, z, soundEvent, SoundSource.BLOCKS, 0.5F, 1.0F);
        }
    }

    public static void playSearchCompletionSoundForPlayer(ServerPlayer player, int rarity) {
        if (player.level() instanceof ServerLevel serverLevel) {
            SoundEvent soundEvent = resolveCompletionSound(rarity);
            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                    soundEvent, SoundSource.BLOCKS, 0.5F, 1.0F);
        }
    }

    private static SoundEvent resolveCompletionSound(int rarity) {
        ResourceLocation configSoundId = SearchConstants.getCompletionSoundId(rarity);
        if (configSoundId != null) {
            SoundEvent customSound = BuiltInRegistries.SOUND_EVENT.get(configSoundId);
            if (customSound != null) return customSound;
        }
        return SearchCompletionSound.getClampedCompletionSound(rarity);
    }
}
