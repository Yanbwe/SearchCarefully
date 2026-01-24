package org.yanbwe.searchcarefully.mixin;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.yanbwe.raritycore.registry.RarityRegistry;
import org.yanbwe.searchcarefully.animation.RotationAnimationHandler;
import org.yanbwe.searchcarefully.textures.CustomTextureHandler;
import org.yanbwe.searchcarefully.util.SlotRenderCache;

@Mixin(GuiGraphics.class)
public abstract class ContainerScreenRenderMixin {

    @Shadow public abstract void fill(int pMinX, int pMinY, int pMaxX, int pMaxY, int pBlitOffset, int pColor);

    /**
     * 在GUI中渲染物品时添加搜索遮罩和旋转动画
     * 这个方法用于GUI中的物品渲染，例如背包、创造模式物品栏等
     * 使用TAIL确保在物品渲染完成后执行
     */
    @Inject(method = "renderItem(Lnet/minecraft/world/item/ItemStack;II)V", 
            at = @At(value = "TAIL"))
    private void renderItemInGuiWithSearchMask(ItemStack itemStack, int x, int y, CallbackInfo ci) {
        if (!itemStack.isEmpty() && itemStack.hasTag() && itemStack.getTag().contains("SearchTimeRemaining")) {
            // 检查当前物品是否需要渲染搜索遮罩
            int remainingTime = itemStack.getTag().getInt("SearchTimeRemaining");
            if (remainingTime > 0) {
                GuiGraphics guiGraphics = (GuiGraphics)(Object)this;
                
                // 渲染遮罩 - 使用自定义纹理遮罩
                var maskTexture = CustomTextureHandler.getMaskTexture();
                try {
                    // 使用blit方法渲染自定义遮罩纹理，确保在物品上方显示，使用高Z值
                    guiGraphics.blit(maskTexture, x, y, 400, 0, 0, 16, 16, 16, 16);
                } catch (Exception e) {
                    // 如果纹理加载失败，回退到纯黑色填充
                    guiGraphics.fill(x, y, x + 16, y + 16, 400, 0xFF000000); // 纯黑色完全不透明，Z值设为400确保在顶层
                }
                
                // 渲染旋转动画纹理
                try {
                    var rotationTexture = CustomTextureHandler.getRotationAnimationTexture();
                    long currentTime = System.currentTimeMillis();
                    
                    // 计算旋转纹理的位置
                    float[] position = RotationAnimationHandler.getRotatingPosition(
                        remainingTime * 1000L, // 使用剩余搜索时间作为起始时间的基础
                        currentTime
                    );
                    
                    // 渲染旋转动画纹理，放在遮罩上方（更高Z值）
                    guiGraphics.blit(rotationTexture, 
                        (int)(x + position[0]), 
                        (int)(y + position[1]), 
                        450, 0, 0, 16, 16, 16, 16); // 使用更高Z值确保在顶层
                } catch (Exception e) {
                    // 如果旋转纹理加载失败，忽略
                }
            }
        }
    }

    /**
     * 另一个渲染物品的方法 - 帯Z偏移量
     */
    @Inject(method = "renderItem(Lnet/minecraft/world/item/ItemStack;III)V", 
            at = @At(value = "TAIL"))
    private void renderItemWithZOffsetInGuiWithSearchMask(ItemStack itemStack, int x, int y, int z, CallbackInfo ci) {
        if (!itemStack.isEmpty() && itemStack.hasTag() && itemStack.getTag().contains("SearchTimeRemaining")) {
            // 检查当前物品是否需要渲染搜索遮罩
            int remainingTime = itemStack.getTag().getInt("SearchTimeRemaining");
            if (remainingTime > 0) {
                GuiGraphics guiGraphics = (GuiGraphics)(Object)this;
                
                // 渲染遮罩 - 使用自定义纹理遮罩
                var maskTexture = CustomTextureHandler.getMaskTexture();
                try {
                    // 使用blit方法渲染自定义遮罩纹理，确保在物品上方显示，使用高Z值
                    guiGraphics.blit(maskTexture, x, y, 400, 0, 0, 16, 16, 16, 16);
                } catch (Exception e) {
                    // 如果纹理加载失败，回退到纯黑色填充
                    guiGraphics.fill(x, y, x + 16, y + 16, 400, 0xFF000000); // 纯黑色完全不透明，Z值设为400确保在顶层
                }
                
                // 渲染旋转动画纹理
                try {
                    var rotationTexture = CustomTextureHandler.getRotationAnimationTexture();
                    long currentTime = System.currentTimeMillis();
                    
                    // 计算旋转纹理的位置
                    float[] position = RotationAnimationHandler.getRotatingPosition(
                        remainingTime * 1000L, // 使用剩余搜索时间作为起始时间的基础
                        currentTime
                    );
                    
                    // 渲染旋转动画纹理，放在遮罩上方（更高Z值）
                    guiGraphics.blit(rotationTexture, 
                        (int)(x + position[0]), 
                        (int)(y + position[1]), 
                        450, 0, 0, 16, 16, 16, 16); // 使用更高Z值确保在顶层
                } catch (Exception e) {
                    // 如果旋转纹理加载失败，忽略
                }
            }
        }
    }

    /**
     * 另一个渲染物品的方法 - 帯完整参数
     */
    @Inject(method = "renderItem(Lnet/minecraft/world/item/ItemStack;IIII)V", 
            at = @At(value = "TAIL"))
    private void renderItemFullParamsInGuiWithSearchMask(ItemStack itemStack, int x, int y, int z, int w, CallbackInfo ci) {
        if (!itemStack.isEmpty() && itemStack.hasTag() && itemStack.getTag().contains("SearchTimeRemaining")) {
            // 检查当前物品是否需要渲染搜索遮罩
            int remainingTime = itemStack.getTag().getInt("SearchTimeRemaining");
            if (remainingTime > 0) {
                GuiGraphics guiGraphics = (GuiGraphics)(Object)this;
                
                // 渲染遮罩 - 使用自定义纹理遮罩
                var maskTexture = CustomTextureHandler.getMaskTexture();
                try {
                    // 使用blit方法渲染自定义遮罩纹理，确保在物品上方显示，使用高Z值
                    guiGraphics.blit(maskTexture, x, y, 400, 0, 0, 16, 16, 16, 16);
                } catch (Exception e) {
                    // 如果纹理加载失败，回退到纯黑色填充
                    guiGraphics.fill(x, y, x + 16, y + 16, 400, 0xFF000000); // 纯黑色完全不透明，Z值设为400确保在顶层
                }
                
                // 渲染旋转动画纹理
                try {
                    var rotationTexture = CustomTextureHandler.getRotationAnimationTexture();
                    long currentTime = System.currentTimeMillis();
                    
                    // 计算旋转纹理的位置
                    float[] position = RotationAnimationHandler.getRotatingPosition(
                        remainingTime * 1000L, // 使用剩余搜索时间作为起始时间的基础
                        currentTime
                    );
                    
                    // 渲染旋转动画纹理，放在遮罩上方（更高Z值）
                    guiGraphics.blit(rotationTexture, 
                        (int)(x + position[0]), 
                        (int)(y + position[1]), 
                        450, 0, 0, 16, 16, 16, 16); // 使用更高Z值确保在顶层
                } catch (Exception e) {
                    // 如果旋转纹理加载失败，忽略
                }
            }
        }
    }

    /**
     * 处理物品渲染的另一个方法，用于渲染物品并添加装饰（如数量）
     */
    @Inject(method = "renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;II)V", 
            at = @At(value = "TAIL"))
    private void renderItemDecorationsWithSearchMask(Font font, ItemStack itemStack, int x, int y, CallbackInfo ci) {
        if (!itemStack.isEmpty() && itemStack.hasTag() && itemStack.getTag().contains("SearchTimeRemaining")) {
            // 检查当前物品是否需要渲染搜索遮罩
            int remainingTime = itemStack.getTag().getInt("SearchTimeRemaining");
            if (remainingTime > 0) {
                GuiGraphics guiGraphics = (GuiGraphics)(Object)this;
                
                // 渲染遮罩 - 使用自定义纹理遮罩
                var maskTexture = CustomTextureHandler.getMaskTexture();
                try {
                    // 使用blit方法渲染自定义遮罩纹理，确保在物品上方显示，使用高Z值
                    guiGraphics.blit(maskTexture, x, y, 400, 0, 0, 16, 16, 16, 16);
                } catch (Exception e) {
                    // 如果纹理加载失败，回退到纯黑色填充
                    guiGraphics.fill(x, y, x + 16, y + 16, 400, 0xFF000000); // 纯黑色完全不透明，Z值设为400确保在顶层
                }
                
                // 渲染旋转动画纹理
                try {
                    var rotationTexture = CustomTextureHandler.getRotationAnimationTexture();
                    long currentTime = System.currentTimeMillis();
                    
                    // 计算旋转纹理的位置
                    float[] position = RotationAnimationHandler.getRotatingPosition(
                        remainingTime * 1000L, // 使用剩余搜索时间作为起始时间的基础
                        currentTime
                    );
                    
                    // 渲染旋转动画纹理，放在遮罩上方（更高Z值）
                    guiGraphics.blit(rotationTexture, 
                        (int)(x + position[0]), 
                        (int)(y + position[1]), 
                        450, 0, 0, 16, 16, 16, 16); // 使用更高Z值确保在顶层
                } catch (Exception e) {
                    // 如果旋转纹理加载失败，忽略
                }
            }
        }
    }
    
    /**
     * 渲染假物品的方法
     */
    @Inject(method = "renderFakeItem(Lnet/minecraft/world/item/ItemStack;II)V", 
            at = @At(value = "TAIL"))
    private void renderFakeItemWithSearchMask(ItemStack itemStack, int x, int y, CallbackInfo ci) {
        if (!itemStack.isEmpty() && itemStack.hasTag() && itemStack.getTag().contains("SearchTimeRemaining")) {
            // 检查当前物品是否需要渲染搜索遮罩
            int remainingTime = itemStack.getTag().getInt("SearchTimeRemaining");
            if (remainingTime > 0) {
                GuiGraphics guiGraphics = (GuiGraphics)(Object)this;
                
                // 渲染遮罩 - 使用自定义纹理遮罩
                var maskTexture = CustomTextureHandler.getMaskTexture();
                try {
                    // 使用blit方法渲染自定义遮罩纹理，确保在物品上方显示，使用高Z值
                    guiGraphics.blit(maskTexture, x, y, 400, 0, 0, 16, 16, 16, 16);
                } catch (Exception e) {
                    // 如果纹理加载失败，回退到纯黑色填充
                    guiGraphics.fill(x, y, x + 16, y + 16, 400, 0xFF000000); // 纯黑色完全不透明，Z值设为400确保在顶层
                }
                
                // 渲染旋转动画纹理
                try {
                    var rotationTexture = CustomTextureHandler.getRotationAnimationTexture();
                    long currentTime = System.currentTimeMillis();
                    
                    // 计算旋转纹理的位置
                    float[] position = RotationAnimationHandler.getRotatingPosition(
                        remainingTime * 1000L, // 使用剩余搜索时间作为起始时间的基础
                        currentTime
                    );
                    
                    // 渲染旋转动画纹理，放在遮罩上方（更高Z值）
                    guiGraphics.blit(rotationTexture, 
                        (int)(x + position[0]), 
                        (int)(y + position[1]), 
                        450, 0, 0, 16, 16, 16, 16); // 使用更高Z值确保在顶层
                } catch (Exception e) {
                    // 如果旋转纹理加载失败，忽略
                }
            }
        }
    }
}