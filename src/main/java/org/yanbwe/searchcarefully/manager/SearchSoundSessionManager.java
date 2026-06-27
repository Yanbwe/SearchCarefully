package org.yanbwe.searchcarefully.manager;

import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.yanbwe.searchcarefully.Config;
import org.yanbwe.searchcarefully.network.StartLoopSoundPacket;
import org.yanbwe.searchcarefully.sounds.SearchCompletionSound;
import org.yanbwe.searchcarefully.util.ItemStackHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 服务端音效会话管理器
 */
public class SearchSoundSessionManager {

    private static final Map<UUID, Boolean> playerSoundActive = new HashMap<>();

    public static boolean hasAnySearchableItems(Player player) {
        if (player.containerMenu != null) {
            for (Slot slot : player.containerMenu.slots) {
                ItemStack stack = slot.getItem();
                if (stack.isEmpty()) continue;
                if (ItemStackHelper.hasRemainingSearchTime(stack) &&
                        ItemStackHelper.getRemainingSearchTime(stack) > 0.0) return true;
            }
        }
        if (Config.ENABLE_HOTBAR_SEARCH.get()) {
            var inventory = player.getInventory();
            for (int i = 0; i < 9; i++) {
                ItemStack stack = inventory.getItem(i);
                if (stack.isEmpty()) continue;
                if (ItemStackHelper.hasRemainingSearchTime(stack) &&
                        ItemStackHelper.getRemainingSearchTime(stack) > 0.0) return true;
            }
        }
        return false;
    }

    public static void updateSoundSession(Player player) {
        if (!Config.ENABLE_SEARCH_PROGRESS_SOUND.get()) return;

        UUID uuid = player.getUUID();
        boolean currentlyPlaying = playerSoundActive.getOrDefault(uuid, false);
        boolean hasItems = hasAnySearchableItems(player);

        if (hasItems && !currentlyPlaying) {
            startLoopSound(player);
            playerSoundActive.put(uuid, true);
        } else if (!hasItems && currentlyPlaying) {
            stopLoopSound(player);
            playerSoundActive.put(uuid, false);
        }
    }

    public static void forceStopSound(Player player) {
        UUID uuid = player.getUUID();
        if (playerSoundActive.getOrDefault(uuid, false)) {
            stopLoopSound(player);
            playerSoundActive.put(uuid, false);
        }
    }

    private static void startLoopSound(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new StartLoopSoundPacket());
        }
    }

    private static void stopLoopSound(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.send(
                    new ClientboundStopSoundPacket(
                            SearchCompletionSound.SEARCH_PROGRESS_SOUND_ID,
                            SoundSource.BLOCKS));
        }
    }

    public static void removePlayer(UUID uuid) {
        playerSoundActive.remove(uuid);
    }

    public static void resetAll() {
        playerSoundActive.clear();
    }
}
