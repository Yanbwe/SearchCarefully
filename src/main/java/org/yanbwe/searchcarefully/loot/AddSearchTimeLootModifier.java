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
        String lootTablePath = context.getQueriedLootTableId().toString(); // 获取完整资源位置
        String lootTablePathOnly = context.getQueriedLootTableId().getPath(); // 获取路径部分
        
        // 检查是否匹配标准前缀
        boolean isStandardPrefix = lootTablePathOnly.startsWith("chest/") || lootTablePathOnly.startsWith("chests/");
        
        // 检查是否匹配中间路径片段（动态配置驱动）
        boolean hasChestSegment = false;
        if (Config.CHEST_PATH_SEGMENTS != null && Config.CHEST_PATH_SEGMENTS.get() != null) {
            String[] pathSegments = lootTablePathOnly.split("/");
            for (String segment : Config.CHEST_PATH_SEGMENTS.get()) {
                for (String pathSegment : pathSegments) {
                    if (pathSegment.equals(segment)) {
                        hasChestSegment = true;
                        break;
                    }
                }
                if (hasChestSegment) break;
            }
        }
        
        // 检查是否匹配自定义路径
        boolean isCustomPath = false;
        if (Config.CUSTOM_LOOT_TABLE_PATHS != null && Config.CUSTOM_LOOT_TABLE_PATHS.get() != null) {
            for (String customPath : Config.CUSTOM_LOOT_TABLE_PATHS.get()) {
                if (lootTablePath.equals(customPath)) {
                    isCustomPath = true;
                    break;
                }
            }
        }
        
        // 如果匹配标准前缀、中间路径片段或自定义路径，则应用搜索时间
        if (isStandardPrefix || hasChestSegment || isCustomPath) {
            Random random = new Random();
            for (ItemStack stack : generatedLoot) {
                if (!stack.isEmpty()) {
                    // 获取物品的稀有度
                    int rarity = RarityRegistry.getRarity(stack.getItem());
                    
                    // 根据稀有度计算基础搜索时间
                    int baseSearchTime = SearchConstants.getSearchTimeByRarity(rarity);
                    
                    // 如果物品具有有效稀有度，则将搜索时间作为NBT标签添加
                    if (baseSearchTime > 0 && rarity >= 1 && rarity <= 7) {
                        // 获取该稀有度对应的随机时间，确保非负值
                        int randomTime = Math.max(0, Config.RARITY_RANDOM_TIMES[rarity].get());
                        
                        // 添加随机时间，范围是 [-randomTime, +randomTime]，但确保最终结果至少为1
                        int randomAddition = random.nextInt(Math.max(1, randomTime * 2 + 1)) - randomTime;
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