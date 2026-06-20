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
import org.yanbwe.searchcarefully.util.SlotRenderCache;

@Mixin(value = Gui.class, priority = 500)
public class GuiHotbarRenderMixin {

    // 在热键栏渲染前收集需要渲染搜索遮罩的数据
    @Inject(
        method = "renderHotbar", 
        at = @At("HEAD")
    )
    private void collectHotbarSearchOverlayData(float partialTick, GuiGraphics guiGraphics, CallbackInfo ci) {
        // 当最高层级渲染模式启用时，跳过此处的遮罩渲染，改由 Gui.render() TAIL 处理
        if (org.yanbwe.searchcarefully.Config.MASK_RENDER_ON_TOP.get()) {
            return;
        }
        
        Minecraft mc = Minecraft.getInstance();
        
        if (mc.player != null) {
            Inventory inventory = mc.player.getInventory();
            
            // 遍历热键栏的所有槽位（索引 0-8）
            for (int i = 0; i < 9; i++) {
                ItemStack itemStack = inventory.getItem(i);
                            
                if (!itemStack.isEmpty() && ItemStackHelper.hasRemainingSearchTime(itemStack)) {
                    double searchTimeDouble = ItemStackHelper.getRemainingSearchTime(itemStack);
                    
                    if (searchTimeDouble > 0.0) {
                        int searchTime = (int) Math.ceil(searchTimeDouble); // 向上取整，确保小数部分不被忽略
                        // 计算热键栏中对应槽位的位置
                        int screenWidth = mc.getWindow().getGuiScaledWidth();
                        int screenHeight = mc.getWindow().getGuiScaledHeight();
                        int hotbarX = (screenWidth - 182) / 2; // 热键栏纹理的起始X坐标
                        int hotbarY = screenHeight - 22; // 热键栏Y坐标
                        int x = hotbarX + i * 20 + 3; // 每个槽位宽20像素，内部偏移3像素
                        int y = hotbarY + 3; // 槽位内部偏移
                        
                        // 将需要渲染遮罩的热键栏物品信息添加到缓存中
                        SlotRenderCache.addHotbarOverlay(x, y, itemStack, searchTime);
                    }
                }
            }
        }
    }

    // 在热键栏渲染完成后统一渲染搜索遮罩
    @Inject(
        method = "renderHotbar", 
        at = @At("TAIL")
    )
    private void renderHotbarSearchOverlays(float partialTick, GuiGraphics guiGraphics, CallbackInfo ci) {
        // 渲染缓存中所有的热键栏遮罩
        for (SlotRenderCache.HotbarOverlayInfo overlayInfo : SlotRenderCache.getPendingHotbarOverlays()) {
            SearchOverlayRenderer.renderOverlay(guiGraphics, overlayInfo.itemStack, overlayInfo.x, overlayInfo.y, false);
        }
        
        // 清理热键栏遮罩缓存
        SlotRenderCache.clearHotbarCache();
    }
}