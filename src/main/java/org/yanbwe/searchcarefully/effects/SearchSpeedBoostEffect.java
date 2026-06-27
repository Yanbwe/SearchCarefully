package org.yanbwe.searchcarefully.effects;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.yanbwe.searchcarefully.SearchCarefully;

/**
 * 搜索速度提升效果
 * 每级 +20% 搜索速度
 */
public class SearchSpeedBoostEffect extends MobEffect {

    private static final ResourceLocation SEARCH_SPEED_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(SearchCarefully.MODID, "search_speed_boost");

    public SearchSpeedBoostEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x3498db);
        addAttributeModifier(
                SearchCarefully.SEARCH_SPEED,
                SEARCH_SPEED_MODIFIER_ID,
                0.2,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}
