package org.yanbwe.searchcarefully.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.yanbwe.searchcarefully.animation.RotationAnimationHandler;
import org.yanbwe.searchcarefully.textures.CustomTextureHandler;
import org.yanbwe.searchcarefully.util.SearchConstants;

/**
 * 注意：容器界面的遮罩渲染已迁移到 SlotRenderMixin（在 renderSlot TAIL 渲染）
 * 此 Mixin 仅保留 renderItemDecorations 和 renderFakeItem 的注入，
 * 用于非容器场景（如创造模式物品栏、JEI 预览等）的补充遮罩渲染。
 */
@Mixin(value = GuiGraphics.class, priority = 500)
public abstract class ContainerScreenRenderMixin {

    /**
     * 使用 guiOverlay 渲染类型渲染纯色遮罩
     */
    private void fillWithOverlay(int x, int y, int width, int height, int color) {
        GuiGraphics guiGraphics = (GuiGraphics)(Object)this;
        guiGraphics.fill(RenderType.guiOverlay(), x, y, x + width, y + height, color);
    }

    /**
     * 使用 GuiGraphics.blit() 渲染纹理，禁用深度测试
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
     */
    private void renderSearchMask(ItemStack itemStack, int x, int y) {
        if (!itemStack.isEmpty() && itemStack.hasTag() && itemStack.getTag().contains(SearchConstants.SEARCH_TIME_REMAINING)) {
            int remainingTime = itemStack.getTag().getInt(SearchConstants.SEARCH_TIME_REMAINING);
            if (remainingTime > 0) {
                GuiGraphics guiGraphics = (GuiGraphics)(Object)this;

                try {
                    var maskTexture = CustomTextureHandler.getMaskTexture();
                    blitWithOverlay(guiGraphics, maskTexture, x, y, 16, 16);
                } catch (Exception e) {
                    fillWithOverlay(x, y, 16, 16, 0xFF000000);
                }

                if (org.yanbwe.searchcarefully.Config.ENABLE_SINGLE_SLOT_SEARCH.get()) {
                    if (!org.yanbwe.searchcarefully.client.ClientOverlayRenderer.isItemBeingSearched(itemStack)) {
                        return;
                    }
                }

                try {
                    var rotationTexture = CustomTextureHandler.getRotationAnimationTexture();
                    long currentTime = System.currentTimeMillis();
                    float[] position = RotationAnimationHandler.getRotatingPosition(
                        remainingTime * 1000L,
                        currentTime
                    );
                    int animX = (int)(x + position[0]);
                    int animY = (int)(y + position[1]);
                    blitWithOverlayAlpha(guiGraphics, rotationTexture, animX, animY, 16, 16, 0x80);
                } catch (Exception e) {
                    // ignore
                }
            }
        }
    }

    /**
     * 渲染物品装饰时添加搜索遮罩（非容器场景的补充）
     */
    @Inject(method = "renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;II)V", 
            at = @At(value = "TAIL"))
    private void renderItemDecorationsWithSearchMask(Font font, ItemStack itemStack, int x, int y, CallbackInfo ci) {
        renderSearchMask(itemStack, x, y);
    }

    /**
     * 渲染假物品时添加搜索遮罩（创造模式等非容器场景的补充）
     */
    @Inject(method = "renderFakeItem(Lnet/minecraft/world/item/ItemStack;II)V", 
            at = @At(value = "TAIL"))
    private void renderFakeItemWithSearchMask(ItemStack itemStack, int x, int y, CallbackInfo ci) {
        renderSearchMask(itemStack, x, y);
    }

}
