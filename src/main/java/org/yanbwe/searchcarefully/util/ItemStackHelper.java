package org.yanbwe.searchcarefully.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class ItemStackHelper {
    
    /**
     * 检查物品堆叠是否有剩余搜索时间
     */
    public static boolean hasRemainingSearchTime(ItemStack stack) {
        return stack.hasTag() && 
               stack.getTag().contains(SearchConstants.SEARCH_TIME_REMAINING);
    }
    
    /**
     * 获取物品堆叠的剩余搜索时间
     */
    public static int getRemainingSearchTime(ItemStack stack) {
        if (hasRemainingSearchTime(stack)) {
            return stack.getTag().getInt(SearchConstants.SEARCH_TIME_REMAINING);
        }
        return 0;
    }
    
    /**
     * 设置物品堆叠的剩余搜索时间
     */
    public static void setRemainingSearchTime(ItemStack stack, int time) {
        stack.getOrCreateTag().putInt(SearchConstants.SEARCH_TIME_REMAINING, time);
    }
    
    /**
     * 检查物品堆叠的搜索是否已完成（时间 <= 0）
     */
    public static boolean isSearchComplete(ItemStack stack) {
        return !hasRemainingSearchTime(stack) || getRemainingSearchTime(stack) <= 0;
    }
}