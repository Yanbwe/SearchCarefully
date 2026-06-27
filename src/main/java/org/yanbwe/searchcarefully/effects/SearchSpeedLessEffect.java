package org.yanbwe.searchcarefully.effects;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.yanbwe.searchcarefully.SearchCarefully;

/**
 * 搜索速度降低效果
 * 每级 -20% 搜索速度（负面效果）
 */
public class SearchSpeedLessEffect extends MobEffect {

    private static final ResourceLocation SEARCH_SPEED_LESS_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(SearchCarefully.MODID, "search_speed_less");

    public SearchSpeedLessEffect() {
        super(MobEffectCategory.HARMFUL, 0x8B4513);
        addAttributeModifier(
                SearchCarefully.SEARCH_SPEED,
                SEARCH_SPEED_LESS_MODIFIER_ID,
                -0.2,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}
