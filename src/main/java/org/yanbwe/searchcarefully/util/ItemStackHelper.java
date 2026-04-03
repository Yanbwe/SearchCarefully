package org.yanbwe.searchcarefully.util;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.ArrayList;
import java.util.List;

public class ItemStackHelper {
    
    /**
     * 检查物品堆叠是否有剩余搜索时间
     */
    public static boolean hasRemainingSearchTime(ItemStack stack) {
        if (stack.isEmpty()) return false;
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        return customData.getUnsafe().contains(SearchConstants.SEARCH_TIME_REMAINING);
    }
    
    /**
     * 获取物品堆叠的剩余搜索时间
     * 
     * @param stack 物品堆叠
     * @return 剩余搜索时间（double 类型）
     */
    public static double getRemainingSearchTime(ItemStack stack) {
        if (hasRemainingSearchTime(stack)) {
            CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).getUnsafe();
            
            // 优先读取 double (NBT 类型 6)
            if (tag.contains(SearchConstants.SEARCH_TIME_REMAINING, 6)) {
                return tag.getDouble(SearchConstants.SEARCH_TIME_REMAINING);
            }
            
            // 兼容旧的 int 格式 (NBT 类型 3)
            if (tag.contains(SearchConstants.SEARCH_TIME_REMAINING, 3)) {
                int oldValue = tag.getInt(SearchConstants.SEARCH_TIME_REMAINING);
                // 升级为 double 并写回
                tag.putDouble(SearchConstants.SEARCH_TIME_REMAINING, (double) oldValue);
                CustomData.set(DataComponents.CUSTOM_DATA, stack, tag);
                return (double) oldValue;
            }
            
            return 0.0;
        }
        return 0.0;
    }
    
    /**
     * 设置物品堆叠的剩余搜索时间
     * 
     * @param stack 物品堆叠
     * @param time 剩余搜索时间（double 类型）
     */
    public static void setRemainingSearchTime(ItemStack stack, double time) {
        if (stack.isEmpty()) return;
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).getUnsafe().copy();
        tag.putDouble(SearchConstants.SEARCH_TIME_REMAINING, time);
        CustomData.set(DataComponents.CUSTOM_DATA, stack, tag);
    }
    
    /**
     * 检查物品堆叠的搜索是否已完成（时间 <= 0）
     */
    public static boolean isSearchComplete(ItemStack stack) {
        return !hasRemainingSearchTime(stack) || getRemainingSearchTime(stack) <= 0;
    }
    
    /**
     * 清理空的 NBT 标签，优化物品堆叠性能
     * 当 NBT 标签为空时，完全移除标签以允许物品正常堆叠
     */
    public static void cleanEmptyTags(ItemStack stack) {
        if (stack.isEmpty()) return;
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        if (customData.getUnsafe().isEmpty()) {
            stack.remove(DataComponents.CUSTOM_DATA);
        }
    }
    
    /**
     * 减少物品的搜索时间
     * 
     * @param stack 物品堆叠
     * @param amount 要减少的时间量（double 类型）
     * @return 减少后的剩余时间，如果没有搜索时间则返回 0.0
     */
    public static double decrementSearchTime(ItemStack stack, double amount) {
        if (!hasRemainingSearchTime(stack)) return 0.0;
        
        double currentTime = getRemainingSearchTime(stack);
        double newTime = Math.max(0.0, currentTime - amount);
        setRemainingSearchTime(stack, newTime);
        return newTime;
    }
    
    /**
     * 完成物品的搜索，清理所有搜索相关的 NBT 标签
     * 此方法会自动调用 cleanEmptyTags 来优化物品数据
     */
    public static void completeSearch(ItemStack stack) {
        if (!hasRemainingSearchTime(stack)) return;
        
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).getUnsafe().copy();
        tag.remove(SearchConstants.SEARCH_TIME_REMAINING);
        CustomData.set(DataComponents.CUSTOM_DATA, stack, tag);
        cleanEmptyTags(stack);
    }
    
    /**
     * 从物品列表中查找所有具有搜索时间的物品
     * 
     * @param items 物品列表
     * @return 包含所有有搜索时间的物品的列表
     */
    public static List<ItemStack> findAllItemsWithSearchTime(List<ItemStack> items) {
        if (items == null || items.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<ItemStack> result = new ArrayList<>();
        for (ItemStack stack : items) {
            if (!stack.isEmpty() && hasRemainingSearchTime(stack)) {
                result.add(stack);
            }
        }
        return result;
    }
    
    /**
     * 清除物品列表中所有物品的搜索标签
     * 
     * @param items 物品列表
     * @return 被清除搜索标签的物品数量
     */
    public static int clearAllSearchTags(List<ItemStack> items) {
        if (items == null || items.isEmpty()) {
            return 0;
        }
        
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