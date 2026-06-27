package org.yanbwe.searchcarefully.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.yanbwe.searchcarefully.mixin.ContainerAccessMixin;

/**
 * 鼠标目标检测器 — 仅在客户端运行
 */
@OnlyIn(Dist.CLIENT)
public class MouseTargetDetector {

    private static Integer lastHoveredSlotIndex = null;
    private static double switchDelayCounter = 0.0;
    private static Integer pendingTargetSlot = null;

    public static Slot getHoveredSlot() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof AbstractContainerScreen<?> screen) {
            return ((ContainerAccessMixin) screen).getHoveredSlot();
        }
        return null;
    }

    public static Integer getHoveredSlotIndex() {
        Slot slot = getHoveredSlot();
        return slot != null ? slot.index : null;
    }

    public static boolean hasSearchableItem(Slot slot) {
        if (slot == null || !slot.hasItem()) return false;
        ItemStack stack = slot.getItem();
        return ItemStackHelper.hasRemainingSearchTime(stack) &&
                ItemStackHelper.getRemainingSearchTime(stack) > 0.0;
    }

    public static boolean isHoveringSearchableItem() {
        return hasSearchableItem(getHoveredSlot());
    }

    public static void updateMouseTarget() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof AbstractContainerScreen) {
            Integer currentSlotIndex = getHoveredSlotIndex();
            boolean hasSearchable = isHoveringSearchableItem();

            if (currentSlotIndex != null && !currentSlotIndex.equals(lastHoveredSlotIndex)) {
                switchDelayCounter = 0.0;
                pendingTargetSlot = null;
            }

            lastHoveredSlotIndex = currentSlotIndex;

            if (hasSearchable && currentSlotIndex != null) {
                double switchDelay = org.yanbwe.searchcarefully.Config.MOUSE_TARGET_SWITCH_DELAY.get();
                if (switchDelay > 0) {
                    switchDelayCounter += 1.0;
                    if (switchDelayCounter >= switchDelay) {
                        pendingTargetSlot = currentSlotIndex;
                    }
                } else {
                    pendingTargetSlot = currentSlotIndex;
                }
            } else {
                switchDelayCounter = 0.0;
                pendingTargetSlot = null;
            }
        } else {
            lastHoveredSlotIndex = null;
            switchDelayCounter = 0.0;
            pendingTargetSlot = null;
        }
    }

    public static Integer getPendingTargetSlot() { return pendingTargetSlot; }
    public static void clearPendingTarget() {
        pendingTargetSlot = null;
        switchDelayCounter = 0.0;
    }

    public static boolean isMouseTargetEnabled() {
        return org.yanbwe.searchcarefully.Config.ENABLE_MOUSE_TARGET_SEARCH.get() &&
                org.yanbwe.searchcarefully.Config.ENABLE_SINGLE_SLOT_SEARCH.get();
    }

    public static Integer getCurrentMouseTarget() {
        return isMouseTargetEnabled() ? pendingTargetSlot : null;
    }
}
