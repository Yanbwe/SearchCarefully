package org.yanbwe.searchcarefully.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;
import org.yanbwe.searchcarefully.Config;
import org.yanbwe.searchcarefully.SearchCarefully;
import org.yanbwe.searchcarefully.util.SearchTimeCalculator;

public class AddSearchTimeLootModifier extends LootModifier {

    public static final MapCodec<AddSearchTimeLootModifier> CODEC = RecordCodecBuilder.mapCodec(inst ->
            inst.group(IGlobalLootModifier.LOOT_CONDITIONS_CODEC.fieldOf("conditions").forGetter(lm -> lm.conditions))
                    .apply(inst, AddSearchTimeLootModifier::new));

    public AddSearchTimeLootModifier(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (!Config.ENABLE_SEARCH_SYSTEM.get()) return generatedLoot;

        if (context.getLevel() != null) {
            boolean lootModifierEnabled = context.getLevel().getGameRules()
                    .getRule(SearchCarefully.SEARCH_LOOT_MODIFIER_GAMERULE).get();
            if (!lootModifierEnabled) return generatedLoot;
        }

        String lootTablePath = context.getQueriedLootTableId().toString();
        String lootTablePathOnly = context.getQueriedLootTableId().getPath();

        boolean isStandardPrefix = lootTablePathOnly.startsWith("chest/") || lootTablePathOnly.startsWith("chests/");

        boolean hasChestSegment = false;
        if (Config.CHEST_PATH_SEGMENTS != null && Config.CHEST_PATH_SEGMENTS.get() != null) {
            for (String segment : Config.CHEST_PATH_SEGMENTS.get()) {
                for (String pathSegment : lootTablePathOnly.split("/")) {
                    if (pathSegment.equals(segment)) {
                        hasChestSegment = true;
                        break;
                    }
                }
                if (hasChestSegment) break;
            }
        }

        boolean isCustomPath = false;
        if (Config.CUSTOM_LOOT_TABLE_PATHS != null && Config.CUSTOM_LOOT_TABLE_PATHS.get() != null) {
            for (String customPath : Config.CUSTOM_LOOT_TABLE_PATHS.get()) {
                if (lootTablePath.equals(customPath)) {
                    isCustomPath = true;
                    break;
                }
            }
        }

        if (isStandardPrefix || hasChestSegment || isCustomPath) {
            for (ItemStack stack : generatedLoot) {
                if (!stack.isEmpty()) {
                    SearchTimeCalculator.applySearchTimeToStack(stack);
                }
            }
        }
        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
