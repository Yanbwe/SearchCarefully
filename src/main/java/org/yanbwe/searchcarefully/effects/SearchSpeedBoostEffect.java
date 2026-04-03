package org.yanbwe.searchcarefully.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.resources.ResourceLocation;
import org.yanbwe.searchcarefully.Searchcarefully;

import java.util.UUID;

public class SearchSpeedBoostEffect extends MobEffect {

    private static final UUID SEARCH_SPEED_MODIFIER_ID =
        UUID.fromString("738c21b5-3a67-4b98-be3d-b7df65c9dff8");
    private static final ResourceLocation ATTRIBUTE_ID =
        ResourceLocation.fromNamespaceAndPath("searchcarefully", "search_speed");

    public SearchSpeedBoostEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x3498db);
    }

    @Override
    public void addAttributeModifiers(AttributeMap map, int amplifier) {
        AttributeInstance instance = map.getInstance(Searchcarefully.SEARCH_SPEED);
        if (instance != null) {
            double multiplier = 0.2 * (amplifier + 1);
            AttributeModifier modifier = new AttributeModifier(
                ATTRIBUTE_ID,
                multiplier,
                Operation.ADD_MULTIPLIED_TOTAL
            );
            instance.addTransientModifier(modifier);
        }
    }

    @Override
    public void removeAttributeModifiers(AttributeMap map) {
        AttributeInstance instance = map.getInstance(Searchcarefully.SEARCH_SPEED);
        if (instance != null) {
            AttributeModifier modifier = instance.getModifier(ATTRIBUTE_ID);
            if (modifier != null) {
                instance.removeModifier(modifier);
            }
        }
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }
}