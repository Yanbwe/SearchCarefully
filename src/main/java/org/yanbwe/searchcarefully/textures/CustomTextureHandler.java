package org.yanbwe.searchcarefully.textures;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CustomTextureHandler {
    
    // 自定义遮罩纹理的位置
    public static final ResourceLocation SEARCH_MASK_TEXTURE = ResourceLocation.fromNamespaceAndPath("searchcarefully", "textures/gui/search_mask.png");
    
    // 旋转动画纹理的位置
    public static final ResourceLocation ROTATION_ANIMATION_TEXTURE = ResourceLocation.fromNamespaceAndPath("searchcarefully", "textures/gui/rotation_animation.png");
    
    /**
     * 获取遮罩纹理
     * 
     * @return 统一的遮罩纹理资源位置
     */
    public static ResourceLocation getMaskTexture() {
        return SEARCH_MASK_TEXTURE;
    }
    
    /**
     * 获取旋转动画纹理
     * 
     * @return 旋转动画纹理资源位置
     */
    public static ResourceLocation getRotationAnimationTexture() {
        return ROTATION_ANIMATION_TEXTURE;
    }
}