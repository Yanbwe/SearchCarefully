package org.yanbwe.searchcarefully.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/**
 * 搜索占位物品
 * 用于在搜索过程中替代原始物品，显示为锁定状态
 */
public class SearchPlaceholderItem extends Item {
    
    public SearchPlaceholderItem() {
        super(new Properties().stacksTo(1)); // 最大堆叠数为 1
    }
    
    /**
     * 检查物品堆叠是否为占位物品
     */
    public static boolean isPlaceholder(ItemStack stack) {
        return !stack.isEmpty() && 
               stack.getItem() instanceof SearchPlaceholderItem;
    }
    
    /**
     * 从占位物品中提取原始物品
     */
    public static ItemStack getOriginalItem(ItemStack placeholderStack) {
        if (!isPlaceholder(placeholderStack)) {
            return ItemStack.EMPTY;
        }
        
        CustomData customData = placeholderStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.getUnsafe();
        if (tag == null || !tag.contains("SearchItem")) {
            return ItemStack.EMPTY;
        }
        
        // 从 SearchItem 标签中读取原始物品
        CompoundTag originalTag = tag.getCompound("SearchItem");
        return ItemStack.parseOptional(null, originalTag);
    }
    
    /**
     * 创建占位物品并设置原始物品数据
     */
    public static ItemStack createPlaceholder(ItemStack originalItem, double searchTime) {
        ItemStack placeholder = new ItemStack(org.yanbwe.searchcarefully.registry.ModItems.SEARCH_PLACEHOLDER.get());
        
        CompoundTag tag = new CompoundTag();
        tag.putDouble("SearchTimeRemaining", searchTime);
        tag.put("SearchItem", originalItem.save(null, new CompoundTag()));
        
        CustomData.set(DataComponents.CUSTOM_DATA, placeholder, tag);
        
        return placeholder;
    }
}