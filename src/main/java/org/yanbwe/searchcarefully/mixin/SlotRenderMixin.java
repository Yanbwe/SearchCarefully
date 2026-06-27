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
 * 在 renderSlot 末尾渲染搜索遮罩
 */
@Mixin(value = AbstractContainerScreen.class, priority = 500)
public class SlotRenderMixin {

    @Inject(
        method = "renderSlot(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/inventory/Slot;)V",
        at = @At("TAIL")
    )
    private void renderSearchOverlay(GuiGraphics guiGraphics, Slot slot, CallbackInfo ci) {
        if (org.yanbwe.searchcarefully.Config.MASK_RENDER_ON_TOP.get()) {
            return;
        }
        ItemStack itemStack = slot.getItem();
        SearchOverlayRenderer.renderOverlay(guiGraphics, itemStack, slot.x, slot.y, false);
    }
}
