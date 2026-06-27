package org.yanbwe.searchcarefully.util;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ItemStackHelper {

    public static boolean hasRemainingSearchTime(ItemStack stack) {
        CompoundTag tag = stack.get(DataComponents.CUSTOM_DATA);
        return tag != null && tag.contains(SearchConstants.SEARCH_TIME_REMAINING);
    }

    public static double getRemainingSearchTime(ItemStack stack) {
        CompoundTag tag = stack.get(DataComponents.CUSTOM_DATA);
        if (tag == null) return 0.0;

        if (tag.contains(SearchConstants.SEARCH_TIME_REMAINING, Tag.TAG_DOUBLE)) {
            return tag.getDouble(SearchConstants.SEARCH_TIME_REMAINING);
        }
        if (tag.contains(SearchConstants.SEARCH_TIME_REMAINING, Tag.TAG_INT)) {
            int oldValue = tag.getInt(SearchConstants.SEARCH_TIME_REMAINING);
            tag.putDouble(SearchConstants.SEARCH_TIME_REMAINING, (double) oldValue);
            return oldValue;
        }
        return 0.0;
    }

    public static void setRemainingSearchTime(ItemStack stack, double time) {
        CompoundTag tag = stack.get(DataComponents.CUSTOM_DATA);
        if (tag == null) tag = new CompoundTag();
        tag.putDouble(SearchConstants.SEARCH_TIME_REMAINING, time);
        stack.set(DataComponents.CUSTOM_DATA, tag);
    }

    public static boolean isSearchComplete(ItemStack stack) {
        return !hasRemainingSearchTime(stack) || getRemainingSearchTime(stack) <= 0;
    }

    public static void cleanEmptyTags(ItemStack stack) {
        if (stack == null) return;
        CompoundTag tag = stack.get(DataComponents.CUSTOM_DATA);
        if (tag != null && tag.isEmpty()) {
            stack.remove(DataComponents.CUSTOM_DATA);
        }
    }

    public static double decrementSearchTime(ItemStack stack, double amount) {
        if (!hasRemainingSearchTime(stack)) return 0.0;
        double currentTime = getRemainingSearchTime(stack);
        double newTime = Math.max(0.0, currentTime - amount);
        setRemainingSearchTime(stack, newTime);
        return newTime;
    }

    public static void completeSearch(ItemStack stack) {
        if (!hasRemainingSearchTime(stack)) return;
        CompoundTag tag = stack.get(DataComponents.CUSTOM_DATA);
        if (tag != null) {
            tag.remove(SearchConstants.SEARCH_TIME_REMAINING);
            if (tag.isEmpty()) {
                stack.remove(DataComponents.CUSTOM_DATA);
            }
        }
    }

    public static List<ItemStack> findAllItemsWithSearchTime(List<ItemStack> items) {
        if (items == null || items.isEmpty()) return new ArrayList<>();
        List<ItemStack> result = new ArrayList<>();
        for (ItemStack stack : items) {
            if (!stack.isEmpty() && hasRemainingSearchTime(stack)) {
                result.add(stack);
            }
        }
        return result;
    }

    public static int clearAllSearchTags(List<ItemStack> items) {
        if (items == null || items.isEmpty()) return 0;
        int count = 0;
        for (ItemStack stack : items) {
            if (!stack.isEmpty() && hasRemainingSearchTime(stack)) {
                completeSearch(stack);
                count++;
            }
        }
        return count;
    }
}
