package org.yanbwe.searchcarefully.registry;

import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.yanbwe.searchcarefully.Searchcarefully;
import org.yanbwe.searchcarefully.item.SearchPlaceholderItem;

/**
 * 模组物品注册表
 */
public class ModItems {
    
    public static final DeferredRegister<Item> ITEMS =
        DeferredRegister.create(ForgeRegistries.ITEMS, Searchcarefully.MODID);
    
    /**
     * 搜索占位物品
     */
    public static final RegistryObject<Item> SEARCH_PLACEHOLDER = 
        ITEMS.register("search_placeholder", SearchPlaceholderItem::new);
}
