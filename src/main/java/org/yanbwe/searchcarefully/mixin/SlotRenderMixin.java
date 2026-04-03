package org.yanbwe.searchcarefully.mixin;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.yanbwe.searchcarefully.util.ItemStackHelper;
import org.yanbwe.searchcarefully.util.SlotRenderCache;

@Mixin(AbstractContainerScreen.class)
public class SlotRenderMixin {

    // 在渲染物品槽时收集需要渲染搜索遮罩的槽位信息
    @Inject(
        method = "renderSlot(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/inventory/Slot;)V", 
        at = @At("TAIL")
    )
    private void collectSearchOverlayData(GuiGraphics guiGraphics, Slot slot, CallbackInfo ci) {
        // 检查槽位是否有物品且需要搜索
        ItemStack itemStack = slot.getItem();
        if (!itemStack.isEmpty() && ItemStackHelper.hasRemainingSearchTime(itemStack)) {
            int searchTime = (int) ItemStackHelper.getRemainingSearchTime(itemStack);
            
            if (searchTime > 0) {
                // 将需要渲染遮罩的槽位信息添加到缓存中
                SlotRenderCache.addSlotOverlay((AbstractContainerScreen<?>) (Object) this, slot);
            }
        }
    }
}