package org.yanbwe.searchcarefully.manager;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.yanbwe.searchcarefully.SearchCarefully;
import org.yanbwe.searchcarefully.util.SearchConstants;
import org.yanbwe.searchcarefully.item.SearchPlaceholderItem;
import org.yanbwe.searchcarefully.sounds.SoundHandler;
import org.yanbwe.searchcarefully.util.ItemStackHelper;
import org.yanbwe.raritycore.registry.RarityRegistry;

public class SearchManager {

    private static final double BASE_DECREMENT = 1.0;
    private static final double MIN_DECREMENT = 0.1;

    public static double getPlayerSearchSpeed(Player player) {
        AttributeInstance attr = player.getAttribute(SearchCarefully.SEARCH_SPEED);
        return attr != null ? attr.getValue() : 1.0;
    }

    public static void handleHotbarSearchProgress(Player player, int hotbarSlotIndex) {
        if (!SearchConstants.isSearchSystemEnabled() || !SearchConstants.isHotbarSearchEnabled()) return;
        if (hotbarSlotIndex < 0 || hotbarSlotIndex > 8) return;

        var inventory = player.getInventory();
        ItemStack stack = inventory.getItem(hotbarSlotIndex);

        if (SearchPlaceholderItem.isPlaceholder(stack)) {
            handleHotbarPlaceholderSearch(player, hotbarSlotIndex, stack);
        } else if (ItemStackHelper.hasRemainingSearchTime(stack)) {
            handleNormalSearchProgress(player, stack, () -> inventory.setItem(hotbarSlotIndex, stack));
        }
        SearchSoundSessionManager.updateSoundSession(player);
    }

    public static void handleSearchProgress(Player player, int slotIndex) {
        if (!SearchConstants.isSearchSystemEnabled()) return;
        if (player.containerMenu != null) {
            var slots = player.containerMenu.slots;
            if (slotIndex >= 0 && slotIndex < slots.size()) {
                var slot = slots.get(slotIndex);
                ItemStack stack = slot.getItem();

                if (SearchPlaceholderItem.isPlaceholder(stack)) {
                    handlePlaceholderSearch(player, slot, stack);
                } else if (ItemStackHelper.hasRemainingSearchTime(stack)) {
                    handleNormalSearchProgress(player, stack, () -> slot.set(stack));
                }
            }
        }
        SearchSoundSessionManager.updateSoundSession(player);
    }

    private static void handleNormalSearchProgress(Player player, ItemStack stack, Runnable onUpdate) {
        double configSpeed = SearchConstants.getSearchSpeedMultiplier();
        double playerSearchSpeed = getPlayerSearchSpeed(player);
        double actualDecrement = calculateActualDecrement(configSpeed, playerSearchSpeed);
        double remainingTime = ItemStackHelper.decrementSearchTime(stack, actualDecrement);
        onUpdate.run();

        if (remainingTime <= 0.0) {
            playCompletionEffect(player, stack.getItem());
            ItemStackHelper.completeSearch(stack);
            onUpdate.run();
        }
    }

    private static void handleHotbarPlaceholderSearch(Player player, int slotIndex, ItemStack placeholderStack) {
        CompoundTag tag = placeholderStack.get(DataComponents.CUSTOM_DATA);
        if (tag == null || !tag.contains("SearchTimeRemaining")) return;

        double configSpeed = SearchConstants.getSearchSpeedMultiplier();
        double playerSearchSpeed = getPlayerSearchSpeed(player);
        double actualDecrement = calculateActualDecrement(configSpeed, playerSearchSpeed);
        double currentTime = tag.getDouble("SearchTimeRemaining");
        double newTime = Math.max(0.0, currentTime - actualDecrement);
        tag.putDouble("SearchTimeRemaining", newTime);

        if (newTime <= 0.0) {
            ItemStack originalItem = SearchPlaceholderItem.getSearchData(placeholderStack).contains("SearchItem")
                    ? ItemStack.parseOptional(player.registryAccess(),
                            placeholderStack.getOrDefault(DataComponents.CUSTOM_DATA, CompoundTag.EMPTY)
                                    .getCompound("SearchItem")).orElse(ItemStack.EMPTY)
                    : ItemStack.EMPTY;

            if (!originalItem.isEmpty()) {
                int rarity = RarityRegistry.getNormalizedRarity(originalItem.getItem());
                SoundHandler.playSearchCompletionSound(player.level(), player.getX(), player.getY(), player.getZ(), rarity);
                player.getInventory().setItem(slotIndex, originalItem);
            }
        }
    }

    private static void handlePlaceholderSearch(Player player, Slot slot, ItemStack placeholderStack) {
        CompoundTag tag = placeholderStack.get(DataComponents.CUSTOM_DATA);
        if (tag == null || !tag.contains("SearchTimeRemaining")) return;

        double configSpeed = SearchConstants.getSearchSpeedMultiplier();
        double playerSearchSpeed = getPlayerSearchSpeed(player);
        double actualDecrement = calculateActualDecrement(configSpeed, playerSearchSpeed);
        double currentTime = tag.getDouble("SearchTimeRemaining");
        double newTime = Math.max(0.0, currentTime - actualDecrement);
        tag.putDouble("SearchTimeRemaining", newTime);
        slot.set(placeholderStack);

        if (newTime <= 0.0) {
            ItemStack originalItem = placeholderStack.getOrDefault(DataComponents.CUSTOM_DATA, CompoundTag.EMPTY)
                    .contains("SearchItem")
                    ? ItemStack.parseOptional(player.registryAccess(),
                            placeholderStack.getOrDefault(DataComponents.CUSTOM_DATA, CompoundTag.EMPTY)
                                    .getCompound("SearchItem")).orElse(ItemStack.EMPTY)
                    : ItemStack.EMPTY;

            if (!originalItem.isEmpty()) {
                int rarity = RarityRegistry.getNormalizedRarity(originalItem.getItem());
                SoundHandler.playSearchCompletionSound(player.level(), player.getX(), player.getY(), player.getZ(), rarity);
                slot.set(originalItem);
            }
        }
    }

    private static double calculateActualDecrement(double configSpeed, double playerSearchSpeed) {
        double actualDecrement = BASE_DECREMENT * configSpeed * playerSearchSpeed;
        if (org.yanbwe.searchcarefully.Config.ENABLE_SINGLE_SLOT_SEARCH.get() &&
            org.yanbwe.searchcarefully.Config.SINGLE_SLOT_SEARCH_TIME_MULTIPLIER.get()) {
            actualDecrement *= SearchConstants.SINGLE_SLOT_SPEED_MULTIPLIER;
        }
        return Math.max(MIN_DECREMENT, actualDecrement);
    }

    private static void playCompletionEffect(Player player, net.minecraft.world.item.Item item) {
        int rarity = RarityRegistry.getNormalizedRarity(item);
        SoundHandler.playSearchCompletionSound(player.level(), player.getX(), player.getY(), player.getZ(), rarity);
    }
}
