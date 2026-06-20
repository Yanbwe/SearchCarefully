package org.yanbwe.searchcarefully.mixin;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.yanbwe.searchcarefully.client.SearchOverlayRenderer;

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
        SearchOverlayRenderer.renderOverlay(guiGraphics, itemStack, slot.x, slot.y, false);
    }
}