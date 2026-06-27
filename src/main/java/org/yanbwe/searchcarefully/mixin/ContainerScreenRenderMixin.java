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
 * 非容器场景的补充搜索遮罩渲染（创造模式、JEI等）
 */
@Mixin(value = GuiGraphics.class, priority = 500)
public abstract class ContainerScreenRenderMixin {

    private void renderSearchMask(ItemStack itemStack, int x, int y) {
        GuiGraphics guiGraphics = (GuiGraphics) (Object) this;
        SearchOverlayRenderer.renderOverlay(guiGraphics, itemStack, x, y, false);
    }

    @Inject(
        method = "renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;II)V",
        at = @At("TAIL"))
    private void renderItemDecorationsWithSearchMask(Font font, ItemStack itemStack, int x, int y, CallbackInfo ci) {
        renderSearchMask(itemStack, x, y);
    }

    @Inject(
        method = "renderFakeItem(Lnet/minecraft/world/item/ItemStack;II)V",
        at = @At("TAIL"))
    private void renderFakeItemWithSearchMask(ItemStack itemStack, int x, int y, CallbackInfo ci) {
        renderSearchMask(itemStack, x, y);
    }
}
