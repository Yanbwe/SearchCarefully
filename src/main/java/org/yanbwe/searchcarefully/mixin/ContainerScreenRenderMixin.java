package org.yanbwe.searchcarefully.mixin;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
// ContainerScreenRenderMixin中不再需要直接调用RarityRegistry
import org.yanbwe.searchcarefully.animation.RotationAnimationHandler;
import org.yanbwe.searchcarefully.textures.CustomTextureHandler;
import org.yanbwe.searchcarefully.util.SearchConstants;
import org.yanbwe.searchcarefully.util.SlotRenderCache;

@Mixin(GuiGraphics.class)
public abstract class ContainerScreenRenderMixin {

    @Shadow public abstract void fill(int pMinX, int pMinY, int pMaxX, int pMaxY, int pBlitOffset, int pColor);

    /**
     * 统一的搜索遮罩渲染方法
     * 处理GUI中物品的搜索遮罩和旋转动画渲染
     */
    private void renderSearchMask(ItemStack itemStack, int x, int y) {
        if (!itemStack.isEmpty()) {
            CustomData customData = itemStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
            CompoundTag tag = customData.getUnsafe();
            if (tag != null && tag.contains(SearchConstants.SEARCH_TIME_REMAINING)) {
                int remainingTime = tag.getInt(SearchConstants.SEARCH_TIME_REMAINING);
                if (remainingTime > 0) {
                    GuiGraphics guiGraphics = (GuiGraphics)(Object)this;
                    
                    // 渲染遮罩 - 使用自定义纹理遮罩
                    var maskTexture = CustomTextureHandler.getMaskTexture();
                    try {
                        guiGraphics.blit(maskTexture, x, y, 400, 0, 0, 16, 16, 16, 16);
                    } catch (Exception e) {
                        // 如果纹理加载失败，回退到纯黑色填充
                        guiGraphics.fill(x, y, x + 16, y + 16, 400, 0xFF000000);
                    }
                    
                    // 渲染旋转动画纹理
                    try {
                        var rotationTexture = CustomTextureHandler.getRotationAnimationTexture();
                        long currentTime = System.currentTimeMillis();
                        
                        float[] position = RotationAnimationHandler.getRotatingPosition(
                            remainingTime * 1000L,
                            currentTime
                        );
                        
                        guiGraphics.blit(rotationTexture, 
                            (int)(x + position[0]), 
                            (int)(y + position[1]), 
                            450, 0, 0, 16, 16, 16, 16);
                    } catch (Exception e) {
                        // Ignore if rotation texture fails to load
                    }
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
}