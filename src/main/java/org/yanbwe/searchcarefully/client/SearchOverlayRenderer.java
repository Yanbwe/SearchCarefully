package org.yanbwe.searchcarefully.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.yanbwe.searchcarefully.animation.RotationAnimationHandler;
import org.yanbwe.searchcarefully.textures.CustomTextureHandler;
import org.yanbwe.searchcarefully.util.ItemStackHelper;

/**
 * 搜索遮罩渲染工具类
 * 提供统一的静态方法渲染搜索遮罩和旋转动画，消除 SlotRenderMixin、
 * ContainerScreenRenderMixin、ClientOverlayRenderer、GuiHotbarRenderMixin、
 * GuiTopLayerMixin 中重复的渲染代码。
 */
public class SearchOverlayRenderer {

    /**
     * 渲染搜索遮罩和旋转动画
     *
     * @param guiGraphics 渲染上下文
     * @param itemStack   要渲染的物品堆叠（需有剩余搜索时间）
     * @param x           渲染的 X 坐标（屏幕坐标）
     * @param y           渲染的 Y 坐标（屏幕坐标）
     * @param topLayer    是否为最高层级渲染。
     *                    为 true 时启用 depthMask(false/true) 确保遮罩在所有元素之上渲染
     */
    public static void renderOverlay(GuiGraphics guiGraphics, ItemStack itemStack, int x, int y, boolean topLayer) {
        if (itemStack.isEmpty() || !ItemStackHelper.hasRemainingSearchTime(itemStack)) {
            return;
        }

        double searchTime = ItemStackHelper.getRemainingSearchTime(itemStack);
        if (searchTime <= 0.0) {
            return;
        }

        // 渲染遮罩纹理
        try {
            ResourceLocation maskTexture = CustomTextureHandler.getMaskTexture();
            RenderSystem.disableDepthTest();
            if (topLayer) {
                RenderSystem.depthMask(false);
            }
            guiGraphics.blit(maskTexture, x, y, 0, 0, 16, 16, 16, 16);
            if (topLayer) {
                RenderSystem.depthMask(true);
            }
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
            if (topLayer) {
                RenderSystem.depthMask(false);
            }
            RenderSystem.enableBlend();
            guiGraphics.blit(rotationTexture, animX, animY, 0, 0, 16, 16, 16, 16);
            if (topLayer) {
                RenderSystem.depthMask(true);
            }
            RenderSystem.enableDepthTest();
        } catch (Exception e) {
            // 如果旋转纹理加载失败，忽略
        }
    }
}
