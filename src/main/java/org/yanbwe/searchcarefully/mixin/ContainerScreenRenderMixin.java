package org.yanbwe.searchcarefully.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.yanbwe.searchcarefully.animation.RotationAnimationHandler;
import org.yanbwe.searchcarefully.textures.CustomTextureHandler;
import org.yanbwe.searchcarefully.util.SearchConstants;

@Mixin(value = GuiGraphics.class, priority = 500)
public abstract class ContainerScreenRenderMixin {

    @Shadow
    public PoseStack pose;

    /**
     * 使用 guiOverlay 渲染类型渲染纯色遮罩
     * guiOverlay 不进行深度测试且不写入深度缓冲区，确保不会遮挡提示框
     */
    private void fillWithOverlay(int x, int y, int width, int height, int color) {
        GuiGraphics guiGraphics = (GuiGraphics)(Object)this;
        guiGraphics.fill(RenderType.guiOverlay(), x, y, x + width, y + height, color);
    }

    /**
     * 使用 GuiGraphics.blit() 渲染纹理，禁用深度测试
     * 确保不会遮挡提示框
     */
    private void blitWithOverlay(GuiGraphics guiGraphics, ResourceLocation texture, int x, int y, int width, int height) {
        RenderSystem.disableDepthTest();
        guiGraphics.blit(texture, x, y, 0, 0, width, height, width, height);
        RenderSystem.enableDepthTest();
    }

    /**
     * 使用 GuiGraphics.blit() 渲染带透明度的纹理，禁用深度测试
     */
    private void blitWithOverlayAlpha(GuiGraphics guiGraphics, ResourceLocation texture, int x, int y, int width, int height, int alpha) {
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        guiGraphics.blit(texture, x, y, 0, 0, width, height, width, height);
        RenderSystem.enableDepthTest();
    }

    /**
     * 统一的搜索遮罩渲染方法
     * 处理GUI中物品的搜索遮罩和旋转动画渲染
     * 使用 guiOverlay 渲染类型，确保不会遮挡提示框
     */
    private void renderSearchMask(ItemStack itemStack, int x, int y) {
        if (!itemStack.isEmpty() && itemStack.hasTag() && itemStack.getTag().contains(SearchConstants.SEARCH_TIME_REMAINING)) {
            int remainingTime = itemStack.getTag().getInt(SearchConstants.SEARCH_TIME_REMAINING);
            if (remainingTime > 0) {
                GuiGraphics guiGraphics = (GuiGraphics)(Object)this;
                
                // 使用自定义渲染类型渲染纹理遮罩
                // guiOverlay 不进行深度测试且不写入深度缓冲区，确保不会遮挡提示框
                try {
                    var maskTexture = CustomTextureHandler.getMaskTexture();
                    blitWithOverlay(guiGraphics, maskTexture, x, y, 16, 16);
                } catch (Exception e) {
                    // 如果纹理加载失败，回退到纯黑色填充
                    fillWithOverlay(x, y, 16, 16, 0xFF000000);
                }
                
                // 逐格搜索模式下，只渲染当前正在搜索物品的旋转动画
                if (org.yanbwe.searchcarefully.Config.ENABLE_SINGLE_SLOT_SEARCH.get()) {
                    if (!org.yanbwe.searchcarefully.client.ClientOverlayRenderer.isItemBeingSearched(itemStack)) {
                        return;
                    }
                }
                
                // 渲染旋转动画纹理
                try {
                    var rotationTexture = CustomTextureHandler.getRotationAnimationTexture();
                    long currentTime = System.currentTimeMillis();
                    
                    float[] position = RotationAnimationHandler.getRotatingPosition(
                        remainingTime * 1000L,
                        currentTime
                    );
                    
                    // 旋转动画也使用自定义渲染类型
                    int animX = (int)(x + position[0]);
                    int animY = (int)(y + position[1]);
                    blitWithOverlayAlpha(guiGraphics, rotationTexture, animX, animY, 16, 16, 0x80);
                } catch (Exception e) {
                    // 如果旋转纹理加载失败，忽略
                }
            }
        }
    }

    /**
     * 在GUI中渲染物品时添加搜索遮罩和旋转动画
     * 这个方法用于GUI中的物品渲染，例如背包、创造模式物品栏等
     * 使用TAIL确保在物品渲染完成后执行
     */
    @Inject(method = "renderItem(Lnet/minecraft/world/item/ItemStack;II)V", 
            at = @At(value = "TAIL"))
    private void renderItemInGuiWithSearchMask(ItemStack itemStack, int x, int y, CallbackInfo ci) {
        renderSearchMask(itemStack, x, y);
    }

    /**
     * 渲染带Z偏移量的物品
     */
    @Inject(method = "renderItem(Lnet/minecraft/world/item/ItemStack;III)V", 
            at = @At(value = "TAIL"))
    private void renderItemWithZOffsetInGuiWithSearchMask(ItemStack itemStack, int x, int y, int z, CallbackInfo ci) {
        renderSearchMask(itemStack, x, y);
    }

    /**
     * 渲染带完整参数的物品
     */
    @Inject(method = "renderItem(Lnet/minecraft/world/item/ItemStack;IIII)V", 
            at = @At(value = "TAIL"))
    private void renderItemFullParamsInGuiWithSearchMask(ItemStack itemStack, int x, int y, int z, int w, CallbackInfo ci) {
        renderSearchMask(itemStack, x, y);
    }

    /**
     * 渲染物品装饰时添加搜索遮罩
     */
    @Inject(method = "renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;II)V", 
            at = @At(value = "TAIL"))
    private void renderItemDecorationsWithSearchMask(Font font, ItemStack itemStack, int x, int y, CallbackInfo ci) {
        renderSearchMask(itemStack, x, y);
    }
    
    /**
     * 渲染假物品时添加搜索遮罩
     */
    @Inject(method = "renderFakeItem(Lnet/minecraft/world/item/ItemStack;II)V", 
            at = @At(value = "TAIL"))
    private void renderFakeItemWithSearchMask(ItemStack itemStack, int x, int y, CallbackInfo ci) {
        renderSearchMask(itemStack, x, y);
    }
    
}