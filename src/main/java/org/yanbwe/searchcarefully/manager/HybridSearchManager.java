package org.yanbwe.searchcarefully.manager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.yanbwe.searchcarefully.util.MouseTargetDetector;

/**
 * 混合搜索管理器 — 仅在客户端运行
 */
@OnlyIn(Dist.CLIENT)
public class HybridSearchManager {

    public enum SearchMode {
        MOUSE_TARGET,
        AUTO_SEQUENTIAL
    }

    private static SearchMode currentMode = SearchMode.AUTO_SEQUENTIAL;
    private static Integer currentTargetSlot = null;
    private static Integer autoModeCurrentSlot = null;
    private static boolean modeChangedThisTick = false;

    public static void updateSearchTarget() {
        modeChangedThisTick = false;
        MouseTargetDetector.updateMouseTarget();

        if (!MouseTargetDetector.isMouseTargetEnabled()) {
            if (currentMode != SearchMode.AUTO_SEQUENTIAL) {
                currentMode = SearchMode.AUTO_SEQUENTIAL;
                currentTargetSlot = null;
                modeChangedThisTick = true;
            }
            updateAutoModeTarget();
            return;
        }

        Integer mouseTarget = MouseTargetDetector.getCurrentMouseTarget();
        if (mouseTarget != null) {
            if (currentMode != SearchMode.MOUSE_TARGET || !mouseTarget.equals(currentTargetSlot)) {
                currentMode = SearchMode.MOUSE_TARGET;
                currentTargetSlot = mouseTarget;
                modeChangedThisTick = true;
            }
        } else {
            if (currentMode != SearchMode.AUTO_SEQUENTIAL) {
                currentMode = SearchMode.AUTO_SEQUENTIAL;
                currentTargetSlot = null;
                modeChangedThisTick = true;
            }
            updateAutoModeTarget();
        }
    }

    private static void updateAutoModeTarget() {
        Minecraft mc = Minecraft.getInstance();
        if (!(mc.screen instanceof AbstractContainerScreen<?> screen) || screen.getMenu() == null) {
            autoModeCurrentSlot = null;
            return;
        }
        if (autoModeCurrentSlot == null || !isSlotValidForSearch(screen, autoModeCurrentSlot)) {
            autoModeCurrentSlot = findNextSearchableSlot(screen);
        }
    }

    private static boolean isSlotValidForSearch(AbstractContainerScreen<?> screen, int slotIndex) {
        if (slotIndex < 0 || slotIndex >= screen.getMenu().slots.size()) return false;
        Slot slot = screen.getMenu().slots.get(slotIndex);
        if (!slot.hasItem()) return false;
        return org.yanbwe.searchcarefully.util.ItemStackHelper.hasRemainingSearchTime(slot.getItem()) &&
                org.yanbwe.searchcarefully.util.ItemStackHelper.getRemainingSearchTime(slot.getItem()) > 0.0;
    }

    private static Integer findNextSearchableSlot(AbstractContainerScreen<?> screen) {
        if (screen.getMenu() == null) return null;
        int startIndex = (autoModeCurrentSlot != null) ? autoModeCurrentSlot + 1 : 0;
        for (int i = startIndex; i < screen.getMenu().slots.size(); i++) {
            if (isSlotValidForSearch(screen, i)) return i;
        }
        for (int i = 0; i < startIndex; i++) {
            if (isSlotValidForSearch(screen, i)) return i;
        }
        return null;
    }

    public static SearchMode getCurrentMode() { return currentMode; }

    public static Integer getCurrentSearchSlot() {
        return currentMode == SearchMode.MOUSE_TARGET ? currentTargetSlot : autoModeCurrentSlot;
    }

    public static Integer getCurrentTargetSlot() { return currentTargetSlot; }

    public static Slot getCurrentSearchSlotObject() {
        Integer idx = getCurrentSearchSlot();
        if (idx == null) return null;
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof AbstractContainerScreen<?> screen) {
            if (idx >= 0 && idx < screen.getMenu().slots.size()) {
                return screen.getMenu().slots.get(idx);
            }
        }
        return null;
    }

    public static boolean isModeChangedThisTick() { return modeChangedThisTick; }
    public static boolean isInMouseTargetMode() { return currentMode == SearchMode.MOUSE_TARGET; }
    public static boolean isInAutoMode() { return currentMode == SearchMode.AUTO_SEQUENTIAL; }

    public static void forceSwitchToMode(SearchMode mode, Integer targetSlot) {
        if (currentMode != mode || (targetSlot != null && !targetSlot.equals(currentTargetSlot))) {
            currentMode = mode;
            currentTargetSlot = targetSlot;
            modeChangedThisTick = true;
        }
    }

    public static void forceSwitchToAutoMode() { forceSwitchToMode(SearchMode.AUTO_SEQUENTIAL, null); }
    public static void forceSwitchToMouseTargetMode(int targetSlot) { forceSwitchToMode(SearchMode.MOUSE_TARGET, targetSlot); }

    public static void reset() {
        currentMode = SearchMode.AUTO_SEQUENTIAL;
        currentTargetSlot = null;
        autoModeCurrentSlot = null;
        modeChangedThisTick = false;
        MouseTargetDetector.clearPendingTarget();
    }

    public static boolean isCurrentTargetValid() {
        Integer idx = getCurrentSearchSlot();
        if (idx == null) return false;
        Slot slot = getCurrentSearchSlotObject();
        return slot != null && slot.hasItem() && MouseTargetDetector.hasSearchableItem(slot);
    }
}
