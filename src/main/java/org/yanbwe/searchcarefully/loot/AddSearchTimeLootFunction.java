package org.yanbwe.searchcarefully.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.yanbwe.raritycore.registry.RarityRegistry;
import org.yanbwe.searchcarefully.Searchcarefully;
import org.yanbwe.searchcarefully.util.SearchConstants;

public class AddSearchTimeLootFunction extends LootItemConditionalFunction {

    public AddSearchTimeLootFunction(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        // 使用RarityRegistry获取物品的稀有度
        int rarity = RarityRegistry.getRarity(stack.getItem());
        
        // 根据稀有度计算搜索时间
        int searchTime = SearchConstants.getSearchTimeByRarity(rarity);
        
        // 如果物品具有有效稀有度，则将搜索时间作为NBT标签添加
        if (searchTime > 0) {
            stack.getOrCreateTag().putInt(SearchConstants.SEARCH_TIME_REMAINING, searchTime);
        }
        
        return stack;
    }

    private static LootItemFunctionType searchTimeLootFunction;

    @Override
    public LootItemFunctionType getType() {
        return searchTimeLootFunction;
    }

    public static void setLootFunction(LootItemFunctionType function) {
        searchTimeLootFunction = function;
    }

    public static LootItemFunctionType getLootFunction() {
        return searchTimeLootFunction;
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<AddSearchTimeLootFunction> {
        @Override
        public AddSearchTimeLootFunction deserialize(JsonObject object, JsonDeserializationContext context, LootItemCondition[] conditions) {
            return new AddSearchTimeLootFunction(conditions);
        }
    }
}