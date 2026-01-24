package org.yanbwe.searchcarefully.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;
import org.yanbwe.searchcarefully.Config;
import org.yanbwe.searchcarefully.util.SearchConstants;
import org.yanbwe.raritycore.registry.RarityRegistry;

import java.util.Random;

public class AddSearchTimeLootModifier extends LootModifier {
    public static final Codec<AddSearchTimeLootModifier> CODEC = RecordCodecBuilder.create(inst ->
        inst.group(
            LOOT_CONDITIONS_CODEC.fieldOf("conditions").forGetter(lm -> lm.conditions)
        ).apply(inst, AddSearchTimeLootModifier::new));

    public AddSearchTimeLootModifier(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        // 判断是否为箱子类战利品表（路径以 "chests/" 开头）
        String lootTablePath = context.getQueriedLootTableId().getPath();
        if (lootTablePath.startsWith("chests/")) {
            Random random = new Random();
            for (ItemStack stack : generatedLoot) {
                if (!stack.isEmpty()) {
                    // 获取物品的稀有度
                    int rarity = RarityRegistry.getRarity(stack.getItem());
                    
                    // 根据稀有度计算基础搜索时间
                    int baseSearchTime = SearchConstants.getSearchTimeByRarity(rarity);
                    
                    // 如果物品具有有效稀有度，则将搜索时间作为NBT标签添加
                    if (baseSearchTime > 0 && rarity >= 1 && rarity <= 7) {
                        // 获取该稀有度对应的随机时间
                        int randomTime = Config.RARITY_RANDOM_TIMES[rarity].get();
                        
                        // 添加随机时间，范围是 [-randomTime, +randomTime]
                        int randomAddition = random.nextInt(-randomTime, randomTime + 1); // nextInt(min, max) 是右开区间
                        int finalSearchTime = Math.max(1, baseSearchTime + randomAddition); // 确保至少为1
                        
                        var tag = stack.getOrCreateTag();
                        tag.putInt(SearchConstants.SEARCH_TIME_REMAINING, finalSearchTime);
                        stack.setTag(tag);
                    }
                }
            }
        }
        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}