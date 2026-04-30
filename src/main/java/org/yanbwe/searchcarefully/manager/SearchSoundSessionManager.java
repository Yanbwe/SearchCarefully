package org.yanbwe.searchcarefully.manager;

import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.PacketDistributor;
import org.yanbwe.searchcarefully.Config;
import org.yanbwe.searchcarefully.network.NetworkHandler;
import org.yanbwe.searchcarefully.network.StartLoopSoundPacket;
import org.yanbwe.searchcarefully.sounds.SearchCompletionSound;
import org.yanbwe.searchcarefully.util.ItemStackHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 服务端音效会话管理器。
 * <p>
 * 追踪每个玩家的搜索音效播放状态，控制循环音效的启动和停止。
 * 当玩家打开容器且有待搜物品时启动循环音效，
 * 当所有物品搜索完成或玩家关闭容器时停止音效。
 * </p>
 */
public class SearchSoundSessionManager {

    private static final Map<UUID, Boolean> playerSoundActive = new HashMap<>();

    /**
     * 检查玩家当前打开的容器（或快捷栏）中是否还有待搜物品。
     *
     * @param player 玩家
     * @return 如果还有未完成的搜索项目返回 true
     */
    public static boolean hasAnySearchableItems(Player player) {
        // 检查容器菜单中的所有槽位
        if (player.containerMenu != null) {
            for (Slot slot : player.containerMenu.slots) {
                ItemStack stack = slot.getItem();
                if (stack.isEmpty()) continue;
                if (ItemStackHelper.hasRemainingSearchTime(stack)) {
                    double time = ItemStackHelper.getRemainingSearchTime(stack);
                    if (time > 0.0) return true;
                }
            }
        }

        // 如果启用了快捷栏搜索，检查快捷栏
        if (Config.ENABLE_HOTBAR_SEARCH.get()) {
            var inventory = player.getInventory();
            for (int i = 0; i < 9; i++) {
                ItemStack stack = inventory.getItem(i);
                if (stack.isEmpty()) continue;
                if (ItemStackHelper.hasRemainingSearchTime(stack)) {
                    double time = ItemStackHelper.getRemainingSearchTime(stack);
                    if (time > 0.0) return true;
                }
            }
        }

        return false;
    }

    /**
     * 更新音效播放状态。应在每次处理搜索进度后调用。
     * <p>
     * 状态转换：
     * <ul>
     *   <li>无物品→有物品：启动循环音效</li>
     *   <li>有物品→无物品：停止循环音效</li>
     *   <li>状态不变：不操作</li>
     * </ul>
     *
     * @param player 玩家
     */
    public static void updateSoundSession(Player player) {
        if (!Config.ENABLE_SEARCH_PROGRESS_SOUND.get()) {
            return;
        }

        UUID uuid = player.getUUID();
        boolean currentlyPlaying = playerSoundActive.getOrDefault(uuid, false);
        boolean hasItems = hasAnySearchableItems(player);

        if (hasItems && !currentlyPlaying) {
            // 从无到有 → 启动循环音效
            startLoopSound(player);
            playerSoundActive.put(uuid, true);
        } else if (!hasItems && currentlyPlaying) {
            // 从有到无 → 停止循环音效
            stopLoopSound(player);
            playerSoundActive.put(uuid, false);
        }
        // hasItems && currentlyPlaying → 不变，继续播放
        // !hasItems && !currentlyPlaying → 不变
    }

    /**
     * 强制停止玩家的音效（如关闭容器时）。
     *
     * @param player 玩家
     */
    public static void forceStopSound(Player player) {
        UUID uuid = player.getUUID();
        if (playerSoundActive.getOrDefault(uuid, false)) {
            stopLoopSound(player);
            playerSoundActive.put(uuid, false);
        }
    }

    private static void startLoopSound(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            NetworkHandler.INSTANCE.send(
                    PacketDistributor.PLAYER.with(() -> serverPlayer),
                    new StartLoopSoundPacket()
            );
        }
    }

    private static void stopLoopSound(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            // 使用 Minecraft 内置包停止音效
            serverPlayer.connection.send(
                    new ClientboundStopSoundPacket(
                            SearchCompletionSound.SEARCH_PROGRESS_SOUND_ID,
                            SoundSource.BLOCKS
                    )
            );
        }
    }

    /**
     * 清理玩家的音效会话（玩家退出时调用）。
     *
     * @param uuid 玩家 UUID
     */
    public static void removePlayer(UUID uuid) {
        playerSoundActive.remove(uuid);
    }

    /**
     * 重置所有状态（可用于 /reload 等场景）。
     */
    public static void resetAll() {
        playerSoundActive.clear();
    }
}
