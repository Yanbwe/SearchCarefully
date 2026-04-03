package org.yanbwe.searchcarefully.registry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.yanbwe.searchcarefully.Searchcarefully;
import org.yanbwe.searchcarefully.item.SearchPlaceholderItem;

/**
 * 模组物品注册表
 */
public class ModItems {
    
    public static final DeferredRegister<Item> ITEMS =
        DeferredRegister.create(BuiltInRegistries.ITEM, Searchcarefully.MODID);
    
    /**
     * 搜索占位物品
     */
    public static final DeferredHolder<Item, Item> SEARCH_PLACEHOLDER = 
        ITEMS.register("search_placeholder", SearchPlaceholderItem::new);
}