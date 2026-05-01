package org.yanbwe.searchcarefully.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.yanbwe.searchcarefully.mixin.ContainerAccessMixin;

/**
 * 鼠标目标检测器
 * 负责检测鼠标悬停的槽位并判断是否可搜索
 */
@OnlyIn(Dist.CLIENT)
public class MouseTargetDetector {
    
    private static Integer lastHoveredSlotIndex = null;
    private static double switchDelayCounter = 0.0;
    private static Integer pendingTargetSlot = null;
    
    /**
     * 获取当前鼠标悬停的槽位
     */
    public static Slot getHoveredSlot() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof AbstractContainerScreen) {
            AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) mc.screen;
            return ((ContainerAccessMixin) screen).getHoveredSlot();
        }
        return null;
    }
    
    /**
     * 获取当前鼠标悬停的槽位索引
     */
    public static Integer getHoveredSlotIndex() {
        Slot slot = getHoveredSlot();
        return slot != null ? slot.index : null;
    }
    
    /**
     * 检查槽位是否有可搜索的物品
     */
    public static boolean hasSearchableItem(Slot slot) {
        if (slot == null || !slot.hasItem()) {
            return false;
        }
        
        ItemStack stack = slot.getItem();
        return ItemStackHelper.hasRemainingSearchTime(stack) && 
               ItemStackHelper.getRemainingSearchTime(stack) > 0.0;
    }
    
    /**
     * 检查当前鼠标悬停的槽位是否有可搜索的物品
     */
    public static boolean isHoveringSearchableItem() {
        Slot slot = getHoveredSlot();
        return hasSearchableItem(slot);
    }
    
    /**
     * 更新鼠标目标状态
     * 处理切换延迟逻辑
     */
    public static void updateMouseTarget() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof AbstractContainerScreen) {
            Integer currentSlotIndex = getHoveredSlotIndex();
            boolean hasSearchable = isHoveringSearchableItem();
            
            // 重置计数器如果鼠标移到了不同的槽位
            if (currentSlotIndex != null && !currentSlotIndex.equals(lastHoveredSlotIndex)) {
                switchDelayCounter = 0.0;
                pendingTargetSlot = null;
            }
            
            lastHoveredSlotIndex = currentSlotIndex;
            
            if (hasSearchable && currentSlotIndex != null) {
                // 增加延迟计数器
                double switchDelay = org.yanbwe.searchcarefully.Config.MOUSE_TARGET_SWITCH_DELAY.get();
                if (switchDelay > 0) {
                    switchDelayCounter += 1.0;
                    
                    if (switchDelayCounter >= switchDelay) {
                        // 延迟结束，设置待定目标
                        pendingTargetSlot = currentSlotIndex;
                    }
                } else {
                    // 无延迟，立即设置目标
                    pendingTargetSlot = currentSlotIndex;
                }
            } else {
                // 鼠标没有指向可搜索物品，重置状态
                switchDelayCounter = 0.0;
                pendingTargetSlot = null;
            }
        } else {
            // 不在容器界面，重置所有状态
            lastHoveredSlotIndex = null;
            switchDelayCounter = 0.0;
            pendingTargetSlot = null;
        }
    }
    
    /**
     * 获取待定的鼠标目标槽位索引
     */
    public static Integer getPendingTargetSlot() {
        return pendingTargetSlot;
    }
    
    /**
     * 清除待定的鼠标目标
     */
    public static void clearPendingTarget() {
        pendingTargetSlot = null;
        switchDelayCounter = 0.0;
    }
    
    /**
     * 检查鼠标目标模式是否启用
     */
    public static boolean isMouseTargetEnabled() {
        return org.yanbwe.searchcarefully.Config.ENABLE_MOUSE_TARGET_SEARCH.get() && 
               org.yanbwe.searchcarefully.Config.ENABLE_SINGLE_SLOT_SEARCH.get();
    }
    
    /**
     * 获取当前有效的鼠标目标槽位索引
     * 考虑延迟和启用状态
     */
    public static Integer getCurrentMouseTarget() {
        if (!isMouseTargetEnabled()) {
            return null;
        }
        
        return pendingTargetSlot;
    }
}