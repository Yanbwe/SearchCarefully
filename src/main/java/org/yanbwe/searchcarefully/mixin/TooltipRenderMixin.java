package org.yanbwe.searchcarefully.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.yanbwe.searchcarefully.client.ClientOverlayRenderer;
import org.yanbwe.searchcarefully.util.ItemStackHelper;

import java.util.List;

/**
 * 底层工具提示渲染拦截Mixin
 * 直接拦截Minecraft原生的工具提示渲染方法，确保与Obscure Tooltips等模组的兼容性
 */
@Mixin(GuiGraphics.class)
public abstract class TooltipRenderMixin {
    
    // 槽位缓存优化
    @Unique
    private static Slot lastHoveredSlot = null;
    @Unique
    private static long lastCheckTime = 0;
    @Unique
    private static final long CACHE_DURATION = 50; // 50ms缓存
    
    // 全局拦截状态标记
    @Unique
    private static boolean tooltipBlockedThisFrame = false;
    
    // 缓存常用对象引用
    @Unique
    private static Minecraft cachedMc = null;
    @Unique
    private static AbstractContainerScreen<?> cachedScreen = null;

    /**
     * 在工具提示渲染的最早阶段进行拦截
     * 这个注入点与Obscure Tooltips在同一层级，但使用更早的执行时机
     */
    @Inject(
        method = "renderTooltipInternal",
        at = @At("HEAD"),
        cancellable = true,
        remap = true
    )
    private void onRenderTooltipInternal(
            Font font, 
            List<ClientTooltipComponent> components, 
            int mouseX, 
            int mouseY, 
            ClientTooltipPositioner positioner, 
            CallbackInfo ci) {
        
        // 渲染时机优化：检查游戏状态
        if (!shouldProcessTooltipRendering()) {
            return;
        }
        
        // 事件处理优化：早期退出机制
        if (tooltipBlockedThisFrame) {
            ci.cancel();
            return;
        }
        
        // 更新缓存引用
        updateCachedReferences();
        
        if (cachedScreen != null) {
            // 使用缓存优化的槽位检测
            Slot hoveredSlot = findSlotUnderMouseWithCache(cachedScreen, mouseX, mouseY);
            if (hoveredSlot != null && shouldBlockTooltipOptimized(hoveredSlot)) {
                tooltipBlockedThisFrame = true;
                ci.cancel();
            }
        }
    }

    /**
     * 使用缓存优化的槽位检测
     */
    @Unique
    private Slot findSlotUnderMouseWithCache(AbstractContainerScreen<?> screen, int mouseX, int mouseY) {
        long currentTime = System.currentTimeMillis();
        
        // 检查缓存是否有效
        if (lastHoveredSlot != null && 
            currentTime - lastCheckTime < CACHE_DURATION) {
            // 快速检查上次的槽位是否仍然匹配
            if (isMouseStillOverSlot(lastHoveredSlot, mouseX, mouseY)) {
                return lastHoveredSlot;
            }
        }
        
        // 执行完整搜索
        Slot foundSlot = findSlotUnderMouse(screen, mouseX, mouseY);
        
        // 更新缓存
        lastHoveredSlot = foundSlot;
        lastCheckTime = currentTime;
        
        return foundSlot;
    }
    
    /**
     * 检查鼠标是否仍在指定槽位上
     */
    @Unique
    private boolean isMouseStillOverSlot(Slot slot, int mouseX, int mouseY) {
        if (cachedScreen == null || cachedScreen.getMenu() == null) {
            return false;
        }
        
        int guiLeft = ((ContainerAccessMixin) cachedScreen).getLeftPos();
        int guiTop = ((ContainerAccessMixin) cachedScreen).getTopPos();
        
        int slotX = guiLeft + slot.x;
        int slotY = guiTop + slot.y;
        
        return mouseX >= slotX && mouseY >= slotY && 
               mouseX < slotX + 16 && mouseY < slotY + 16;
    }
    
    /**
     * 优化的条件检查
     */
    @Unique
    private boolean shouldBlockTooltipOptimized(Slot slot) {
        if (slot == null || !slot.hasItem()) return false;
        
        ItemStack stack = slot.getItem();
        
        // 快速检查：无标签直接返回
        if (!stack.hasTag()) return false;
        
        // 快速检查：检查特定标识而非完整解析
        if (!stack.getTag().contains("SearchTimeRemaining")) {
            return false;
        }
        
        // 只有通过快速检查才进行完整验证
        return ItemStackHelper.getRemainingSearchTime(stack) > 0;
    }
    
    /**
     * 渲染时机优化：检查是否应该处理工具提示渲染
     */
    @Unique
    private boolean shouldProcessTooltipRendering() {
        if (cachedMc == null) return false;
        
        // 只在游戏运行且有焦点时处理
        if (!cachedMc.isWindowActive() || cachedMc.getOverlay() != null) {
            return false;
        }
        
        // 只在容器界面中处理
        return cachedScreen != null;
    }
    
    /**
     * 更新缓存的对象引用
     */
    @Unique
    private void updateCachedReferences() {
        Minecraft mc = Minecraft.getInstance();
        if (cachedMc != mc) {
            cachedMc = mc;
            cachedScreen = mc.screen instanceof AbstractContainerScreen ? 
                          (AbstractContainerScreen<?>) mc.screen : null;
        }
    }
    
    /**
     * 原始的槽位查找方法（保留用于完整搜索）
     */
    @Unique
    private Slot findSlotUnderMouse(AbstractContainerScreen<?> screen, int mouseX, int mouseY) {
        if (screen.getMenu() == null) {
            return null;
        }

        // 使用ContainerAccessMixin获取GUI位置信息
        int guiLeft = ((ContainerAccessMixin) screen).getLeftPos();
        int guiTop = ((ContainerAccessMixin) screen).getTopPos();
        
        // 数据结构优化：对于大型容器使用空间分区
        if (screen.getMenu().slots.size() > 20) {
            return findSlotUsingSpatialPartition(screen, mouseX, mouseY, guiLeft, guiTop);
        }
        
        // 对于小型容器使用线性搜索
        for (Slot slot : screen.getMenu().slots) {
            int slotX = guiLeft + slot.x;
            int slotY = guiTop + slot.y;
            
            if (mouseX >= slotX && mouseY >= slotY && 
                mouseX < slotX + 16 && mouseY < slotY + 16) {
                return slot;
            }
        }
        
        return null;
    }
    
    /**
     * 数据结构优化：使用空间分区加速大型容器的槽位查找
     */
    @Unique
    private Slot findSlotUsingSpatialPartition(AbstractContainerScreen<?> screen, int mouseX, int mouseY, int guiLeft, int guiTop) {
        // 简化的空间分区实现
        // 计算鼠标所在的大概区域
        int relX = mouseX - guiLeft;
        int relY = mouseY - guiTop;
        
        // 只检查可能包含鼠标的槽位行
        int startRow = Math.max(0, (relY / 18) - 1); // 18是槽位间距
        int endRow = Math.min(screen.getMenu().slots.size() / 9 + 1, (relY / 18) + 2);
        
        // 在限定范围内搜索
        for (int i = startRow * 9; i < Math.min(endRow * 9, screen.getMenu().slots.size()); i++) {
            if (i >= 0 && i < screen.getMenu().slots.size()) {
                Slot slot = screen.getMenu().slots.get(i);
                int slotX = guiLeft + slot.x;
                int slotY = guiTop + slot.y;
                
                if (mouseX >= slotX && mouseY >= slotY && 
                    mouseX < slotX + 16 && mouseY < slotY + 16) {
                    return slot;
                }
            }
        }
        
        return null;
    }
    
    /**
     * 重置每帧的状态标记
     */
    @Unique
    private static void resetFrameState() {
        tooltipBlockedThisFrame = false;
    }
    
    /**
     * 公共静态方法供外部调用重置状态
     */
    public static void resetTooltipState() {
        resetFrameState();
    }
}