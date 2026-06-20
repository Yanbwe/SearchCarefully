package org.yanbwe.searchcarefully.mixin;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.yanbwe.searchcarefully.client.SearchOverlayRenderer;

/**
 * 注意：容器界面的遮罩渲染已迁移到 SlotRenderMixin（在 renderSlot TAIL 渲染）
 * 此 Mixin 仅保留 renderItemDecorations 和 renderFakeItem 的注入，
 * 用于非容器场景（如创造模式物品栏、JEI 预览等）的补充遮罩渲染。
 */
@Mixin(value = GuiGraphics.class, priority = 500)
public abstract class ContainerScreenRenderMixin {

    /**
     * 统一的搜索遮罩渲染方法
     */
    private void renderSearchMask(ItemStack itemStack, int x, int y) {
        GuiGraphics guiGraphics = (GuiGraphics)(Object)this;
        SearchOverlayRenderer.renderOverlay(guiGraphics, itemStack, x, y, false);
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
