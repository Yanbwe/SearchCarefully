package org.yanbwe.searchcarefully.util;

import net.minecraft.world.item.ItemStack;
import org.yanbwe.raritycore.registry.RarityRegistry;

import java.util.Random;

public class SearchTimeCalculator {

    private static final Random RANDOM = new Random();

    public static double calculateFinalSearchTime(ItemStack stack) {
        int rarity = RarityRegistry.getNormalizedRarity(stack.getItem());
        return calculateFinalSearchTimeByRarity(rarity);
    }

    public static double calculateFinalSearchTimeByRarity(int rarity) {
        if (!SearchConstants.isValidRarity(rarity)) {
            return 0.0;
        }

        double baseSearchTime = SearchConstants.getSearchTimeByRarity(rarity);
        if (baseSearchTime <= 0) {
            return 0.0;
        }

        double randomTimeRange = SearchConstants.getRarityRandomTime(rarity);
        double randomAddition = (RANDOM.nextDouble() * 2 - 1) * randomTimeRange;
        return Math.max(1.0, baseSearchTime + randomAddition);
    }

    public static void applySearchTimeToStack(ItemStack stack) {
        double finalSearchTime = calculateFinalSearchTime(stack);
        if (finalSearchTime > 0) {
            ItemStackHelper.setRemainingSearchTime(stack, finalSearchTime);
        }
    }
}