package org.yanbwe.searchcarefully.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.yanbwe.searchcarefully.animation.RotationAnimationHandler;
import org.yanbwe.searchcarefully.client.ClientOverlayRenderer;
import org.yanbwe.searchcarefully.textures.CustomTextureHandler;
import org.yanbwe.searchcarefully.util.ItemStackHelper;

/**
 * 在 renderSlot 的末尾渲染搜索遮罩，确保：
 * 1. 遮罩在所有物品级渲染（包括其他模组的 renderItem 注入）之后绘制
 * 2. 遮罩在提示框之前绘制（提示框在 render() 最后渲染）
 */
@Mixin(value = AbstractContainerScreen.class, priority = 500)
public class SlotRenderMixin {

    @Inject(
        method = "renderSlot(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/inventory/Slot;)V",
        at = @At("TAIL")
    )
    private void renderSearchOverlay(GuiGraphics guiGraphics, Slot slot, CallbackInfo ci) {
        // 当最高层级渲染模式启用时，跳过此处的遮罩渲染，改由 ScreenEvent.Render.Post 处理
        if (org.yanbwe.searchcarefully.Config.MASK_RENDER_ON_TOP.get()) {
            return;
        }
        
        ItemStack itemStack = slot.getItem();
        if (!itemStack.isEmpty() && ItemStackHelper.hasRemainingSearchTime(itemStack)) {
            double searchTime = ItemStackHelper.getRemainingSearchTime(itemStack);

            if (searchTime > 0.0) {
                int x = slot.x;
                int y = slot.y;

                // 渲染遮罩纹理
                try {
                    ResourceLocation maskTexture = CustomTextureHandler.getMaskTexture();
                    RenderSystem.disableDepthTest();
                    guiGraphics.blit(maskTexture, x, y, 0, 0, 16, 16, 16, 16);
                    RenderSystem.enableDepthTest();
                } catch (Exception e) {
                    // 如果纹理加载失败，回退到纯黑色填充
                    guiGraphics.fill(RenderType.guiOverlay(), x, y, x + 16, y + 16, 0xFF000000);
                }

                // 逐格搜索模式下，只渲染当前正在搜索物品的旋转动画
                if (org.yanbwe.searchcarefully.Config.ENABLE_SINGLE_SLOT_SEARCH.get()) {
                    if (!ClientOverlayRenderer.isItemBeingSearched(itemStack)) {
                        return;
                    }
                }

                // 渲染旋转动画纹理
                try {
                    ResourceLocation rotationTexture = CustomTextureHandler.getRotationAnimationTexture();
                    long currentTime = System.currentTimeMillis();
                    float[] position = RotationAnimationHandler.getRotatingPosition(
                            (int) (searchTime * 1000L),
                            currentTime
                    );
                    int animX = (int) (x + position[0]);
                    int animY = (int) (y + position[1]);
                    RenderSystem.disableDepthTest();
                    RenderSystem.enableBlend();
                    guiGraphics.blit(rotationTexture, animX, animY, 0, 0, 16, 16, 16, 16);
                    RenderSystem.enableDepthTest();
                } catch (Exception e) {
                    // 如果旋转纹理加载失败，忽略
                }
            }
        }
    }
}