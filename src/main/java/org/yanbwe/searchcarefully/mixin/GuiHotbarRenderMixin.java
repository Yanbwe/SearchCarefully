package org.yanbwe.searchcarefully.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.DeltaTracker;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.yanbwe.searchcarefully.client.SearchOverlayRenderer;
import org.yanbwe.searchcarefully.util.ItemStackHelper;
import org.yanbwe.searchcarefully.util.SlotRenderCache;

/**
 * 热键栏搜索遮罩渲染（1.21.1: renderHotbar → renderItemHotbar）
 */
@Mixin(value = Gui.class, priority = 500)
public class GuiHotbarRenderMixin {

    @Inject(method = "renderItemHotbar", at = @At("HEAD"))
    private void collectHotbarSearchOverlayData(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (org.yanbwe.searchcarefully.Config.MASK_RENDER_ON_TOP.get()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        Inventory inventory = mc.player.getInventory();
        for (int i = 0; i < 9; i++) {
            ItemStack itemStack = inventory.getItem(i);
            if (!itemStack.isEmpty() && ItemStackHelper.hasRemainingSearchTime(itemStack)) {
                double searchTimeDouble = ItemStackHelper.getRemainingSearchTime(itemStack);
                if (searchTimeDouble > 0.0) {
                    int searchTime = (int) Math.ceil(searchTimeDouble);
                    int screenWidth = mc.getWindow().getGuiScaledWidth();
                    int screenHeight = mc.getWindow().getGuiScaledHeight();
                    int hotbarX = (screenWidth - 182) / 2;
                    int hotbarY = screenHeight - 22;
                    int x = hotbarX + i * 20 + 3;
                    int y = hotbarY + 3;
                    SlotRenderCache.addHotbarOverlay(x, y, itemStack, searchTime);
                }
            }
        }
    }

    @Inject(method = "renderItemHotbar", at = @At("TAIL"))
    private void renderHotbarSearchOverlays(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        for (SlotRenderCache.HotbarOverlayInfo overlayInfo : SlotRenderCache.getPendingHotbarOverlays()) {
            SearchOverlayRenderer.renderOverlay(guiGraphics, overlayInfo.itemStack, overlayInfo.x, overlayInfo.y, false);
        }
        SlotRenderCache.clearHotbarCache();
    }
}
