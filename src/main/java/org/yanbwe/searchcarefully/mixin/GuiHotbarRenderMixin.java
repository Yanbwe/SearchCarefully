package org.yanbwe.searchcarefully.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.yanbwe.searchcarefully.animation.RotationAnimationHandler;
import org.yanbwe.searchcarefully.textures.CustomTextureHandler;
import org.yanbwe.searchcarefully.util.ItemStackHelper;
import org.yanbwe.searchcarefully.util.SlotRenderCache;

@Mixin(Gui.class)
public class GuiHotbarRenderMixin {

    /**
     * 使用 guiOverlay 渲染类型渲染纯色遮罩
     * guiOverlay 不进行深度测试且不写入深度缓冲区，确保不会遮挡提示框
     */
    private void fillWithOverlay(GuiGraphics guiGraphics, int x, int y, int width, int height, int color) {
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
    private void blitWithOverlayAlpha(GuiGraphics guiGraphics, ResourceLocation texture, int x, int y, int width, int height) {
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        guiGraphics.blit(texture, x, y, 0, 0, width, height, width, height);
        RenderSystem.enableDepthTest();
    }

    // 在热键栏渲染前收集需要渲染搜索遮罩的数据
    @Inject(
        method = "renderHotbar", 
        at = @At("HEAD")
    )
    private void collectHotbarSearchOverlayData(float partialTick, GuiGraphics guiGraphics, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        
        if (mc.player != null) {
            Inventory inventory = mc.player.getInventory();
            
            // 遍历热键栏的所有槽位（索引 0-8）
            for (int i = 0; i < 9; i++) {
                ItemStack itemStack = inventory.getItem(i);
                            
                if (!itemStack.isEmpty() && ItemStackHelper.hasRemainingSearchTime(itemStack)) {
                    double searchTimeDouble = ItemStackHelper.getRemainingSearchTime(itemStack);
                    
                    if (searchTimeDouble > 0.0) {
                        int searchTime = (int) Math.ceil(searchTimeDouble); // 向上取整，确保小数部分不被忽略
                        // 计算热键栏中对应槽位的位置
                        int screenWidth = mc.getWindow().getGuiScaledWidth();
                        int screenHeight = mc.getWindow().getGuiScaledHeight();
                        int hotbarX = (screenWidth - 182) / 2; // 热键栏纹理的起始X坐标
                        int hotbarY = screenHeight - 22; // 热键栏Y坐标
                        int x = hotbarX + i * 20 + 3; // 每个槽位宽20像素，内部偏移3像素
                        int y = hotbarY + 3; // 槽位内部偏移
                        
                        // 将需要渲染遮罩的热键栏物品信息添加到缓存中
                        SlotRenderCache.addHotbarOverlay(x, y, itemStack, searchTime);
                    }
                }
            }
        }
    }

    // 在热键栏渲染完成后统一渲染搜索遮罩
    @Inject(
        method = "renderHotbar", 
        at = @At("TAIL")
    )
    private void renderHotbarSearchOverlays(float partialTick, GuiGraphics guiGraphics, CallbackInfo ci) {
        // 渲染缓存中所有的热键栏遮罩
        for (SlotRenderCache.HotbarOverlayInfo overlayInfo : SlotRenderCache.getPendingHotbarOverlays()) {
            // 使用自定义渲染类型渲染纹理遮罩
            // guiOverlay 不进行深度测试且不写入深度缓冲区，确保不会遮挡提示框
            try {
                var maskTexture = CustomTextureHandler.getMaskTexture();
                blitWithOverlay(guiGraphics, maskTexture, overlayInfo.x, overlayInfo.y, 16, 16);
            } catch (Exception e) {
                // 如果纹理加载失败，回退到纯黑色填充
                fillWithOverlay(guiGraphics, overlayInfo.x, overlayInfo.y, 16, 16, 0xFF000000);
            }
            
            // 渲染旋转动画
            try {
                var rotationTexture = CustomTextureHandler.getRotationAnimationTexture();
                long currentTime = System.currentTimeMillis();
                
                // 计算旋转动画的位置
                float[] position = RotationAnimationHandler.getRotatingPosition(
                    overlayInfo.searchTime * 1000L, // 使用剩余搜索时间作为起始时间的基础
                    currentTime
                );
                
                // 旋转动画也使用自定义渲染类型
                int animX = (int)(overlayInfo.x + position[0]);
                int animY = (int)(overlayInfo.y + position[1]);
                blitWithOverlayAlpha(guiGraphics, rotationTexture, animX, animY, 16, 16);
            } catch (Exception e) {
                // 如果旋转动画计算失败，忽略
            }
        }
        
        // 清理热键栏遮罩缓存
        SlotRenderCache.clearHotbarCache();
    }
}