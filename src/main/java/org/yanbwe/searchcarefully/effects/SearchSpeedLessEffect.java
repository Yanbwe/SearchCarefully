package org.yanbwe.searchcarefully.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import org.yanbwe.searchcarefully.Searchcarefully;

import java.util.UUID;

/**
 * 搜索速度降低效果
 * 每级减少 20% 的搜索速度（负面效果）
 */
public class SearchSpeedLessEffect extends MobEffect {

    private static final UUID SEARCH_SPEED_LESS_MODIFIER_ID = 
        UUID.fromString("a048c76a-bea4-438e-a494-d87eb3589420");

    public SearchSpeedLessEffect() {
        super(MobEffectCategory.HARMFUL, 0x8B4513);
    }
    
    /**
     * 添加属性修饰符
     * 每级减少 20% 搜索速度
     */
    @Override
    public void addAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        if (entity instanceof net.minecraft.world.entity.player.Player player) {
            double multiplier = -0.2 * (amplifier + 1);
            
            var modifier = new AttributeModifier(
                SEARCH_SPEED_LESS_MODIFIER_ID,
                "Search speed reduction",
                multiplier,
                AttributeModifier.Operation.MULTIPLY_TOTAL
            );
            
            var instance = attributeMap.getInstance(Searchcarefully.SEARCH_SPEED.get());
            if (instance != null) {
                instance.addTransientModifier(modifier);
            }
        }
    }

    /**
     * 移除属性修饰符
     */
    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        if (entity instanceof net.minecraft.world.entity.player.Player player) {
            var instance = attributeMap.getInstance(Searchcarefully.SEARCH_SPEED.get());
            if (instance != null) {
                instance.removeModifier(SEARCH_SPEED_LESS_MODIFIER_ID);
            }
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}
