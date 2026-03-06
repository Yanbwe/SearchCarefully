package org.yanbwe.searchcarefully.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import org.yanbwe.searchcarefully.Searchcarefully;

import java.util.UUID;

/**
 * 搜索速度提升效果
 */
public class SearchSpeedBoostEffect extends MobEffect {

    private static final UUID SEARCH_SPEED_MODIFIER_ID = 
        UUID.fromString("738c21b5-3a67-4b98-be3d-b7df65c9dff8");

    public SearchSpeedBoostEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x3498db);
    }
    
    /**
     * 添加属性修饰符
     */
    @Override
    public void addAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        if (entity instanceof net.minecraft.world.entity.player.Player player) {
            // 每级+20%
            double multiplier = 0.2 * (amplifier + 1);
            
            var modifier = new AttributeModifier(
                SEARCH_SPEED_MODIFIER_ID,
                "Search speed boost",
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
                instance.removeModifier(SEARCH_SPEED_MODIFIER_ID);
            }
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}
