package org.yanbwe.searchcarefully.manager;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.sounds.SoundSource;
import org.yanbwe.searchcarefully.Searchcarefully;
import org.yanbwe.searchcarefully.util.SearchConstants;
import org.yanbwe.searchcarefully.item.SearchPlaceholderItem;
import org.yanbwe.searchcarefully.sounds.SoundHandler;
import org.yanbwe.searchcarefully.sounds.SearchCompletionSound;
import org.yanbwe.searchcarefully.util.ItemStackHelper;
import org.yanbwe.raritycore.registry.RarityRegistry;

public class SearchManager {

    private static final double BASE_DECREMENT = 1.0;
    private static final double MIN_DECREMENT = 0.1;
    
    // 全局追踪上次搜索进度音效播放的时间（tick）
    private static long lastSearchProgressSoundTick = 0;

    public static double getPlayerSearchSpeed(Player player) {
        AttributeInstance attr = player.getAttribute(Searchcarefully.SEARCH_SPEED.get());
        if (attr != null) {
            return attr.getValue();
        }
        return 1.0;
    }

    public static void handleHotbarSearchProgress(Player player, int hotbarSlotIndex) {
        if (!SearchConstants.isSearchSystemEnabled() || !SearchConstants.isHotbarSearchEnabled()) {
            return;
        }

        if (hotbarSlotIndex < 0 || hotbarSlotIndex > 8) {
            return;
        }

        var inventory = player.getInventory();
        ItemStack stack = inventory.getItem(hotbarSlotIndex);

        if (SearchPlaceholderItem.isPlaceholder(stack)) {
            handleHotbarPlaceholderSearch(player, hotbarSlotIndex, stack);
        } else if (ItemStackHelper.hasRemainingSearchTime(stack)) {
            handleNormalSearchProgress(player, stack, () -> {
                inventory.setItem(hotbarSlotIndex, stack);
            });
        }
    }

    public static void handleSearchProgress(Player player, int slotIndex) {
        if (!SearchConstants.isSearchSystemEnabled()) {
            return;
        }

        if (player.containerMenu != null) {
            var slots = player.containerMenu.slots;
            if (slotIndex >= 0 && slotIndex < slots.size()) {
                var slot = slots.get(slotIndex);
                ItemStack stack = slot.getItem();

                if (SearchPlaceholderItem.isPlaceholder(stack)) {
                    handlePlaceholderSearch(player, slot, stack);
                } else if (ItemStackHelper.hasRemainingSearchTime(stack)) {
                    handleNormalSearchProgress(player, stack, () -> {
                        slot.set(stack);
                    });
                }
            }
        }
    }

    private static void handleNormalSearchProgress(Player player, ItemStack stack, Runnable onUpdate) {
        double configSpeed = SearchConstants.getSearchSpeedMultiplier();
        double playerSearchSpeed = getPlayerSearchSpeed(player);

        double actualDecrement = calculateActualDecrement(configSpeed, playerSearchSpeed);

        double remainingTime = ItemStackHelper.decrementSearchTime(stack, actualDecrement);

        onUpdate.run();
        
        // 播放搜索进度音效（全局统一控制，避免同时搜索多个物品时音效叠加）
        if (remainingTime > 0.0) {
            playSearchProgressSoundIfNeeded(player);
        }

        if (remainingTime <= 0.0) {
            playCompletionEffect(player, stack.getItem());
            ItemStackHelper.completeSearch(stack);
            onUpdate.run();
        }
    }

    private static void handleHotbarPlaceholderSearch(Player player, int slotIndex, ItemStack placeholderStack) {
        if (!placeholderStack.hasTag() || !placeholderStack.getTag().contains("SearchTimeRemaining")) {
            return;
        }

        double configSpeed = SearchConstants.getSearchSpeedMultiplier();
        double playerSearchSpeed = getPlayerSearchSpeed(player);

        double actualDecrement = calculateActualDecrement(configSpeed, playerSearchSpeed);

        double currentTime = placeholderStack.getTag().getDouble("SearchTimeRemaining");
        double newTime = Math.max(0.0, currentTime - actualDecrement);
        placeholderStack.getTag().putDouble("SearchTimeRemaining", newTime);
        
        // 播放搜索进度音效（全局统一控制）
        if (newTime > 0.0) {
            playSearchProgressSoundIfNeeded(player);
        }

        if (newTime <= 0.0) {
            ItemStack originalItem = SearchPlaceholderItem.getOriginalItem(placeholderStack);

            if (!originalItem.isEmpty()) {
                int rarity = RarityRegistry.getNormalizedRarity(originalItem.getItem());
                SoundHandler.playSearchCompletionSound(player.level(), player.getX(), player.getY(), player.getZ(), rarity);

                var inventory = player.getInventory();
                inventory.setItem(slotIndex, originalItem);
            }
        }
    }

    private static void handlePlaceholderSearch(Player player, Slot slot, ItemStack placeholderStack) {
        if (!placeholderStack.hasTag() || !placeholderStack.getTag().contains("SearchTimeRemaining")) {
            return;
        }

        double configSpeed = SearchConstants.getSearchSpeedMultiplier();
        double playerSearchSpeed = getPlayerSearchSpeed(player);

        double actualDecrement = calculateActualDecrement(configSpeed, playerSearchSpeed);

        double currentTime = placeholderStack.getTag().getDouble("SearchTimeRemaining");
        double newTime = Math.max(0.0, currentTime - actualDecrement);
        placeholderStack.getTag().putDouble("SearchTimeRemaining", newTime);

        slot.set(placeholderStack);
        
        // 播放搜索进度音效（全局统一控制）
        if (newTime > 0.0) {
            playSearchProgressSoundIfNeeded(player);
        }

        if (newTime <= 0.0) {
            ItemStack originalItem = SearchPlaceholderItem.getOriginalItem(placeholderStack);

            if (!originalItem.isEmpty()) {
                int rarity = RarityRegistry.getNormalizedRarity(originalItem.getItem());
                SoundHandler.playSearchCompletionSound(player.level(), player.getX(), player.getY(), player.getZ(), rarity);

                slot.set(originalItem);
            }
        }
    }

    private static double calculateActualDecrement(double configSpeed, double playerSearchSpeed) {
        double actualDecrement = BASE_DECREMENT * configSpeed * playerSearchSpeed;
        
        // 逐格搜索模式下搜索速度乘以三
        if (org.yanbwe.searchcarefully.Config.ENABLE_SINGLE_SLOT_SEARCH.get() && 
            org.yanbwe.searchcarefully.Config.SINGLE_SLOT_SEARCH_TIME_MULTIPLIER.get()) {
            actualDecrement *= 3.0;
        }
        
        return Math.max(MIN_DECREMENT, actualDecrement);
    }
    
    /**
     * 播放搜索进度音效（全局统一控制，避免同时搜索多个物品时音效叠加）
     * 使用统一的搜索进度音效，不区分稀有度
     * 
     * @param player 玩家（用于获取位置）
     */
    private static void playSearchProgressSoundIfNeeded(Player player) {
        if (!SearchConstants.isSearchProgressSoundEnabled()) {
            return;
        }
        
        // 获取当前tick
        long currentTick = player.level().getGameTime();
        long intervalTicks = (long) (SearchConstants.getSearchProgressSoundInterval() * 20); // 转换为tick
        
        // 检查是否满足播放间隔
        if (currentTick - lastSearchProgressSoundTick >= intervalTicks) {
            // 在世界中播放统一的搜索进度音效
            if (player.level() instanceof ServerLevel serverLevel) {
                serverLevel.playSound(
                    null, 
                    player.getX(), player.getY(), player.getZ(), 
                    SearchCompletionSound.SEARCH_PROGRESS_SOUND_EVENT, 
                    SoundSource.BLOCKS, 
                    0.5F, 
                    1.0F
                );
                lastSearchProgressSoundTick = currentTick;
            }
        }
    }
    
    private static void playCompletionEffect(Player player, net.minecraft.world.item.Item item) {
        int rarity = RarityRegistry.getNormalizedRarity(item);
        SoundHandler.playSearchCompletionSound(player.level(), player.getX(), player.getY(), player.getZ(), rarity);
    }
}