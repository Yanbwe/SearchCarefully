package org.yanbwe.searchcarefully.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.yanbwe.raritycore.registry.RarityRegistry;
import org.yanbwe.searchcarefully.animation.RotationAnimationHandler;
import org.yanbwe.searchcarefully.textures.CustomTextureHandler;
import org.yanbwe.searchcarefully.util.ItemStackHelper;
import org.yanbwe.searchcarefully.util.SlotRenderCache;

@Mixin(Gui.class)
public class GuiHotbarRenderMixin {

    // 在热键栏渲染前收集需要渲染搜索遮罩的数据
    @Inject(
        method = "renderHotbar", 
        at = @At("HEAD")
    )
    private void collectHotbarSearchOverlayData(float partialTick, GuiGraphics guiGraphics, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        
        if (mc.player != null) {
            Inventory inventory = mc.player.getInventory();
            
            // 遍历热键栏的所有槽位（索引0-8）
            for (int i = 0; i < 9; i++) {
                ItemStack itemStack = inventory.getItem(i);
                
                if (!itemStack.isEmpty() && ItemStackHelper.hasRemainingSearchTime(itemStack)) {
                    int searchTime = ItemStackHelper.getRemainingSearchTime(itemStack);
                    
                    if (searchTime > 0) {
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
            // 获取统一的遮罩纹理
            var maskTexture = CustomTextureHandler.getMaskTexture();
            
            // 尝试使用自定义纹理渲染遮罩，如果纹理不存在则回退到纯黑色
            try {
                // 使用blit方法渲染自定义遮罩纹理，确保在物品上方显示
                guiGraphics.blit(maskTexture, overlayInfo.x, overlayInfo.y, 0, 0, 16, 16, 16, 16);
            } catch (Exception e) {
                // 如果纹理加载失败，回退到纯黑色填充
                guiGraphics.fill(overlayInfo.x, overlayInfo.y, overlayInfo.x + 16, overlayInfo.y + 16, 400, 0xFF000000); // 纯黑色完全不透明，Z值设为400确保在顶层
            }
            
            // 渲染旋转动画纹理
            try {
                var rotationTexture = CustomTextureHandler.getRotationAnimationTexture();
                long currentTime = System.currentTimeMillis();
                
                // 计算旋转纹理的位置
                float[] position = RotationAnimationHandler.getRotatingPosition(
                    overlayInfo.searchTime * 1000L, // 使用剩余搜索时间作为起始时间的基础
                    currentTime
                );
                
                // 渲染旋转动画纹理，放在遮罩上方（更高Z值）
                guiGraphics.blit(rotationTexture, 
                    (int)(overlayInfo.x + position[0]), 
                    (int)(overlayInfo.y + position[1]), 
                    0, 0, 16, 16, 16, 16); // 16x16的纹理
            } catch (Exception e) {
                // 如果旋转纹理加载失败，忽略
            }
        }
        
        // 清理热键栏遮罩缓存
        SlotRenderCache.clearHotbarCache();
    }
}