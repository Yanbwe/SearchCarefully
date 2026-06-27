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
 * 搜索遮罩渲染工具 — 统一静态渲染方法
 */
public class SearchOverlayRenderer {

    public static void renderOverlay(GuiGraphics guiGraphics, ItemStack itemStack, int x, int y, boolean topLayer) {
        if (itemStack.isEmpty() || !ItemStackHelper.hasRemainingSearchTime(itemStack)) return;

        double searchTime = ItemStackHelper.getRemainingSearchTime(itemStack);
        if (searchTime <= 0.0) return;

        // Render mask texture
        try {
            ResourceLocation maskTexture = CustomTextureHandler.getMaskTexture();
            RenderSystem.disableDepthTest();
            if (topLayer) RenderSystem.depthMask(false);
            guiGraphics.blit(maskTexture, x, y, 0, 0, 16, 16, 16, 16);
            if (topLayer) RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
        } catch (Exception e) {
            guiGraphics.fill(RenderType.guiOverlay(), x, y, x + 16, y + 16, 0xFF000000);
        }

        // In single-slot mode only render animation for current item
        if (org.yanbwe.searchcarefully.Config.ENABLE_SINGLE_SLOT_SEARCH.get()) {
            if (!ClientOverlayRenderer.isItemBeingSearched(itemStack)) return;
        }

        // Render rotation animation
        try {
            ResourceLocation rotationTexture = CustomTextureHandler.getRotationAnimationTexture();
            long currentTime = System.currentTimeMillis();
            float[] position = RotationAnimationHandler.getRotatingPosition(
                    (int) (searchTime * 1000L), currentTime);
            int animX = (int) (x + position[0]);
            int animY = (int) (y + position[1]);
            RenderSystem.disableDepthTest();
            if (topLayer) RenderSystem.depthMask(false);
            RenderSystem.enableBlend();
            guiGraphics.blit(rotationTexture, animX, animY, 0, 0, 16, 16, 16, 16);
            if (topLayer) RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
        } catch (Exception e) {
            // Ignore if rotation texture fails
        }
    }
}
