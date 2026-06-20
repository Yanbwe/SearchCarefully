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
import org.yanbwe.searchcarefully.client.SearchOverlayRenderer;
import org.yanbwe.searchcarefully.util.ItemStackHelper;

/**
 * 热键栏最高层级遮罩渲染 Mixin
 * 当 maskRenderOnTop 配置启用时，在 Gui.render() 末尾（所有 HUD 元素之后）渲染热键栏搜索遮罩
 */
@Mixin(value = Gui.class, priority = 500)
public class GuiTopLayerMixin {

    /**
     * 在 Gui.render() 末尾渲染热键栏搜索遮罩
     * 此时所有 HUD 元素（包括玩家名称、物品提示等）已渲染完毕，遮罩将处于最顶层
     */
    @Inject(
        method = "render(Lnet/minecraft/client/gui/GuiGraphics;F)V",
        at = @At("TAIL")
    )
    private void renderHotbarTopLayerMasks(GuiGraphics guiGraphics, float partialTick, CallbackInfo ci) {
        if (!org.yanbwe.searchcarefully.Config.MASK_RENDER_ON_TOP.get()) {
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
