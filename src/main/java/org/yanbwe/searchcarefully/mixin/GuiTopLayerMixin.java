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

/**
 * 热键栏最高层级遮罩渲染（1.21.1: render(GuiGraphics, float) → render(GuiGraphics, DeltaTracker)）
 */
@Mixin(value = Gui.class, priority = 500)
public class GuiTopLayerMixin {

    @Inject(
        method = "render(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/util/DeltaTracker;)V",
        at = @At("TAIL"))
    private void renderHotbarTopLayerMasks(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (!org.yanbwe.searchcarefully.Config.MASK_RENDER_ON_TOP.get()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        Inventory inventory = mc.player.getInventory();
        for (int i = 0; i < 9; i++) {
            ItemStack itemStack = inventory.getItem(i);
            if (!itemStack.isEmpty() && ItemStackHelper.hasRemainingSearchTime(itemStack)) {
                double searchTimeDouble = ItemStackHelper.getRemainingSearchTime(itemStack);
                if (searchTimeDouble > 0.0) {
                    int screenWidth = mc.getWindow().getGuiScaledWidth();
                    int screenHeight = mc.getWindow().getGuiScaledHeight();
                    int hotbarX = (screenWidth - 182) / 2;
                    int hotbarY = screenHeight - 22;
                    int x = hotbarX + i * 20 + 3;
                    int y = hotbarY + 3;
                    SearchOverlayRenderer.renderOverlay(guiGraphics, itemStack, x, y, true);
                }
            }
        }
    }
}
