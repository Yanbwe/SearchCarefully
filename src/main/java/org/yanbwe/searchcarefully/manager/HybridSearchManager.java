package org.yanbwe.searchcarefully.manager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.yanbwe.searchcarefully.util.MouseTargetDetector;
import org.yanbwe.searchcarefully.util.ContainerSearchTracker;

/**
 * 混合搜索管理器
 * 管理鼠标指向模式和自动逐个模式的切换
 */
@OnlyIn(Dist.CLIENT)
public class HybridSearchManager {
    
    /**
     * 搜索模式枚举
     */
    public enum SearchMode {
        MOUSE_TARGET,    // 鼠标指向模式
        AUTO_SEQUENTIAL  // 自动逐个模式
    }
    
    private static SearchMode currentMode = SearchMode.AUTO_SEQUENTIAL;
    private static Integer currentTargetSlot = null;
    private static Integer autoModeCurrentSlot = null;
    private static boolean modeChangedThisTick = false;
    
    /**
     * 更新搜索模式和目标
     * 应该在每tick调用
     */
    public static void updateSearchTarget() {
        modeChangedThisTick = false;
        
        // 更新鼠标目标检测
        MouseTargetDetector.updateMouseTarget();
        
        // 检查鼠标目标模式是否启用
        if (!MouseTargetDetector.isMouseTargetEnabled()) {
            // 鼠标目标模式未启用，使用自动模式
            if (currentMode != SearchMode.AUTO_SEQUENTIAL) {
                currentMode = SearchMode.AUTO_SEQUENTIAL;
                currentTargetSlot = null;
                modeChangedThisTick = true;
            }
            updateAutoModeTarget();
            return;
        }
        
        // 获取当前鼠标目标
        Integer mouseTarget = MouseTargetDetector.getCurrentMouseTarget();
        
        if (mouseTarget != null) {
            // 有有效的鼠标目标，切换到鼠标指向模式
            if (currentMode != SearchMode.MOUSE_TARGET || 
                !mouseTarget.equals(currentTargetSlot)) {
                currentMode = SearchMode.MOUSE_TARGET;
                currentTargetSlot = mouseTarget;
                modeChangedThisTick = true;
            }
        } else {
            // 没有鼠标目标，切换到自动模式
            if (currentMode != SearchMode.AUTO_SEQUENTIAL) {
                currentMode = SearchMode.AUTO_SEQUENTIAL;
                currentTargetSlot = null;
                modeChangedThisTick = true;
            }
            updateAutoModeTarget();
        }
    }
    
    /**
     * 更新自动模式的当前目标
     */
    private static void updateAutoModeTarget() {
        Minecraft mc = Minecraft.getInstance();
        if (!(mc.screen instanceof AbstractContainerScreen screen) || screen.getMenu() == null) {
            autoModeCurrentSlot = null;
            return;
        }
        
        // 如果当前槽位无效或没有搜索时间，查找下一个
        if (autoModeCurrentSlot == null || !isSlotValidForSearch(screen, autoModeCurrentSlot)) {
            autoModeCurrentSlot = findNextSearchableSlot(screen);
        }
    }
    
    /**
     * 检查槽位是否有效可搜索
     */
    private static boolean isSlotValidForSearch(AbstractContainerScreen<?> screen, int slotIndex) {
        if (slotIndex < 0 || slotIndex >= screen.getMenu().slots.size()) {
            return false;
        }
        
        var slot = screen.getMenu().slots.get(slotIndex);
        if (!slot.hasItem()) {
            return false;
        }
        
        var stack = slot.getItem();
        if (!org.yanbwe.searchcarefully.util.ItemStackHelper.hasRemainingSearchTime(stack)) {
            return false;
        }
        
        double searchTime = org.yanbwe.searchcarefully.util.ItemStackHelper.getRemainingSearchTime(stack);
        return searchTime > 0.0;
    }
    
    /**
     * 查找下一个可搜索的槽位
     */
    private static Integer findNextSearchableSlot(AbstractContainerScreen<?> screen) {
        if (screen.getMenu() == null) {
            return null;
        }
        
        // 从当前槽位+1开始查找，如果当前槽位为null则从0开始
        int startIndex = (autoModeCurrentSlot != null) ? autoModeCurrentSlot + 1 : 0;
        
        // 先查找后面的槽位
        for (int i = startIndex; i < screen.getMenu().slots.size(); i++) {
            if (isSlotValidForSearch(screen, i)) {
                return i;
            }
        }
        
        // 如果后面没找到，从头开始查找
        for (int i = 0; i < startIndex; i++) {
            if (isSlotValidForSearch(screen, i)) {
                return i;
            }
        }
        
        return null;
    }
    
    /**
     * 获取当前搜索模式
     */
    public static SearchMode getCurrentMode() {
        return currentMode;
    }
    
    /**
     * 获取当前搜索目标槽位索引
     * 返回null表示没有特定目标（自动模式）
     */
    public static Integer getCurrentSearchSlot() {
        if (currentMode == SearchMode.MOUSE_TARGET) {
            return currentTargetSlot;
        } else {
            // 自动模式：返回当前自动模式的目标槽位
            return autoModeCurrentSlot;
        }
    }
    
    /**
     * 获取当前搜索目标槽位
     */
    public static Slot getCurrentSearchSlotObject() {
        Integer slotIndex = getCurrentSearchSlot();
        if (slotIndex == null) {
            return null;
        }
        
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof AbstractContainerScreen screen) {
            if (slotIndex >= 0 && slotIndex < screen.getMenu().slots.size()) {
                return screen.getMenu().slots.get(slotIndex);
            }
        }
        
        return null;
    }
    
    /**
     * 检查模式是否在本tick发生了变化
     */
    public static boolean isModeChangedThisTick() {
        return modeChangedThisTick;
    }
    
    /**
     * 检查当前是否处于鼠标指向模式
     */
    public static boolean isInMouseTargetMode() {
        return currentMode == SearchMode.MOUSE_TARGET;
    }
    
    /**
     * 检查当前是否处于自动模式
     */
    public static boolean isInAutoMode() {
        return currentMode == SearchMode.AUTO_SEQUENTIAL;
    }
    
    /**
     * 强制切换到指定模式
     */
    public static void forceSwitchToMode(SearchMode mode, Integer targetSlot) {
        if (currentMode != mode || 
            (targetSlot != null && !targetSlot.equals(currentTargetSlot))) {
            currentMode = mode;
            currentTargetSlot = targetSlot;
            modeChangedThisTick = true;
        }
    }
    
    /**
     * 强制切换到自动模式
     */
    public static void forceSwitchToAutoMode() {
        forceSwitchToMode(SearchMode.AUTO_SEQUENTIAL, null);
    }
    
    /**
     * 强制切换到鼠标指向模式并指定目标
     */
    public static void forceSwitchToMouseTargetMode(int targetSlot) {
        forceSwitchToMode(SearchMode.MOUSE_TARGET, targetSlot);
    }
    
    /**
     * 重置所有状态
     */
    public static void reset() {
        currentMode = SearchMode.AUTO_SEQUENTIAL;
        currentTargetSlot = null;
        autoModeCurrentSlot = null;
        modeChangedThisTick = false;
        MouseTargetDetector.clearPendingTarget();
    }
    
    /**
     * 获取当前模式的显示名称
     */
    public static String getModeDisplayName() {
        switch (currentMode) {
            case MOUSE_TARGET:
                return "鼠标指向模式";
            case AUTO_SEQUENTIAL:
                return "自动逐个模式";
            default:
                return "未知模式";
        }
    }
    
    /**
     * 检查当前搜索目标是否有效
     */
    public static boolean isCurrentTargetValid() {
        Integer slotIndex = getCurrentSearchSlot();
        if (slotIndex == null) {
            return false;
        }
        
        Slot slot = getCurrentSearchSlotObject();
        if (slot == null || !slot.hasItem()) {
            return false;
        }
        
        return MouseTargetDetector.hasSearchableItem(slot);
    }
}