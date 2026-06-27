package org.yanbwe.searchcarefully.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.yanbwe.searchcarefully.SearchCarefully;
import org.yanbwe.searchcarefully.item.SearchPlaceholderItem;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, SearchCarefully.MODID);

    public static final DeferredHolder<Item, Item> SEARCH_PLACEHOLDER =
            ITEMS.register("search_placeholder", SearchPlaceholderItem::new);
}
