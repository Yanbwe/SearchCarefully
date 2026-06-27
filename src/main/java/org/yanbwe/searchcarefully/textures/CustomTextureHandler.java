package org.yanbwe.searchcarefully.textures;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CustomTextureHandler {

    public static final ResourceLocation SEARCH_MASK_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("searchcarefully", "textures/gui/search_mask.png");

    public static final ResourceLocation ROTATION_ANIMATION_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("searchcarefully", "textures/gui/rotation_animation.png");

    public static ResourceLocation getMaskTexture() {
        return SEARCH_MASK_TEXTURE;
    }

    public static ResourceLocation getRotationAnimationTexture() {
        return ROTATION_ANIMATION_TEXTURE;
    }
}
