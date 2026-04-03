package org.yanbwe.searchcarefully.loot;

import com.mojang.serialization.Codec;
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
import org.yanbwe.searchcarefully.Searchcarefully;
import org.yanbwe.searchcarefully.util.SearchConstants;
import org.yanbwe.searchcarefully.util.SearchTimeCalculator;
import com.google.common.base.Suppliers;
import java.util.function.Supplier;

public class AddSearchTimeLootModifier extends LootModifier {
    public static final Supplier<MapCodec<AddSearchTimeLootModifier>> CODEC = Suppliers.memoize(() -> 
        RecordCodecBuilder.mapCodec(inst ->
            inst.group(
                LOOT_CONDITIONS_CODEC.fieldOf("conditions").forGetter(lm -> lm.conditions)
            ).apply(inst, AddSearchTimeLootModifier::new)));

    public AddSearchTimeLootModifier(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (!Config.ENABLE_SEARCH_SYSTEM.get()) {
            return generatedLoot;
        }

        if (context.getLevel() != null) {
            boolean lootModifierEnabled = context.getLevel().getGameRules().getRule(Searchcarefully.SEARCH_LOOT_MODIFIER_GAMERULE).get();
            if (!lootModifierEnabled) {
                return generatedLoot;
            }
        }

        String lootTablePath = context.getQueriedLootTableId().toString();
        String lootTablePathOnly = context.getQueriedLootTableId().getPath();

        boolean isStandardPrefix = lootTablePathOnly.startsWith("chest/") || lootTablePathOnly.startsWith("chests/");

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
            Searchcarefully.LOGGER.debug("Applying search time to loot table: {}", lootTablePath);
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
        return CODEC.get();
    }
}