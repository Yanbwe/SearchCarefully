package org.yanbwe.searchcarefully.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * 搜索占位物品
 * 用于在搜索过程中替代原始物品，显示为锁定状态
 */
public class SearchPlaceholderItem extends Item {

    public SearchPlaceholderItem() {
        super(new Properties().stacksTo(1));
    }

    /**
     * 检查物品堆叠是否为占位物品
     */
    public static boolean isPlaceholder(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof SearchPlaceholderItem;
    }

    /**
     * 从占位物品中提取存储的自定义数据
     */
    public static CompoundTag getSearchData(ItemStack placeholderStack) {
        if (!isPlaceholder(placeholderStack)) {
            return new CompoundTag();
        }
        return placeholderStack.getOrDefault(DataComponents.CUSTOM_DATA, CompoundTag.EMPTY);
    }

    /**
     * 设置占位物品的搜索数据
     */
    public static ItemStack setSearchData(ItemStack placeholderStack, CompoundTag data) {
        placeholderStack.set(DataComponents.CUSTOM_DATA, data);
        return placeholderStack;
    }
}
