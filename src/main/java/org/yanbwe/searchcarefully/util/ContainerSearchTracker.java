package org.yanbwe.searchcarefully.util;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ContainerSearchTracker {

    private static AbstractContainerScreen<?> lastScreen = null;
    private static final Map<Slot, TrackedSlotState> trackedContainerSlots = new IdentityHashMap<>();
    private static final Set<Integer> trackedHotbarSlots = new java.util.HashSet<>();
    private static boolean containerDirty = true;
    private static boolean hotbarDirty = true;
    private static int currentSequentialSlotIndex = -1;

    public static class TrackedSlotState {
        public final int slotIndex;
        public final boolean isHotbar;

        public TrackedSlotState(int slotIndex, boolean isHotbar) {
            this.slotIndex = slotIndex;
            this.isHotbar = isHotbar;
        }
    }

    public static void onScreenChanged(AbstractContainerScreen<?> newScreen) {
        if (lastScreen != newScreen) {
            lastScreen = newScreen;
            containerDirty = true;
            trackedContainerSlots.clear();
        }
    }

    public static void markContainerDirty() {
        containerDirty = true;
    }

    public static void markHotbarDirty() {
        hotbarDirty = true;
    }

    public static void scanAndMarkContainer(AbstractContainerScreen<?> screen) {
        if (screen == null || screen.getMenu() == null) {
            trackedContainerSlots.clear();
            return;
        }

        trackedContainerSlots.clear();

        var slots = screen.getMenu().slots;
        for (int i = 0; i < slots.size(); i++) {
            Slot slot = slots.get(i);
            if (ItemStackHelper.hasRemainingSearchTime(slot.getItem())) {
                trackedContainerSlots.put(slot, new TrackedSlotState(i, false));
            }
        }

        containerDirty = false;
        lastScreen = screen;
    }

    public static void scanAndMarkHotbar(net.minecraft.world.entity.player.Inventory inventory) {
        trackedHotbarSlots.clear();

        for (int i = 0; i < 9; i++) {
            ItemStack stack = inventory.getItem(i);
            if (ItemStackHelper.hasRemainingSearchTime(stack)) {
                trackedHotbarSlots.add(i);
            }
        }

        hotbarDirty = false;
    }

    public static List<TrackedSlotState> getTrackedContainerSlots(AbstractContainerScreen<?> screen) {
        if (containerDirty || lastScreen != screen) {
            scanAndMarkContainer(screen);
            // 重置顺序搜索索引
            currentSequentialSlotIndex = -1;
        }

        if (org.yanbwe.searchcarefully.util.SearchConstants.isSequentialSearchEnabled()) {
            // 顺序搜索模式
            List<TrackedSlotState> allSlots = new ArrayList<>(trackedContainerSlots.values());
            // 按槽位索引排序
            allSlots.sort((a, b) -> Integer.compare(a.slotIndex, b.slotIndex));
            
            // 找到下一个需要搜索的槽位
            if (currentSequentialSlotIndex < 0) {
                // 从第一个槽位开始
                if (!allSlots.isEmpty()) {
                    currentSequentialSlotIndex = allSlots.get(0).slotIndex;
                }
            } else {
                // 检查当前槽位是否已搜索完成
                boolean currentSlotDone = true;
                for (TrackedSlotState state : allSlots) {
                    if (state.slotIndex == currentSequentialSlotIndex) {
                        Slot slot = screen.getMenu().slots.get(state.slotIndex);
                        if (ItemStackHelper.hasRemainingSearchTime(slot.getItem()) && 
                            ItemStackHelper.getRemainingSearchTime(slot.getItem()) > 0) {
                            currentSlotDone = false;
                        }
                        break;
                    }
                }
                
                // 如果当前槽位已完成，移动到下一个
                if (currentSlotDone) {
                    int nextIndex = -1;
                    boolean foundCurrent = false;
                    for (TrackedSlotState state : allSlots) {
                        if (foundCurrent && state.slotIndex > currentSequentialSlotIndex) {
                            nextIndex = state.slotIndex;
                            break;
                        }
                        if (state.slotIndex == currentSequentialSlotIndex) {
                            foundCurrent = true;
                        }
                    }
                    // 如果没有下一个，回到第一个
                    if (nextIndex == -1 && !allSlots.isEmpty()) {
                        nextIndex = allSlots.get(0).slotIndex;
                    }
                    currentSequentialSlotIndex = nextIndex;
                }
            }
            
            // 只返回当前槽位
            List<TrackedSlotState> result = new ArrayList<>();
            if (currentSequentialSlotIndex >= 0) {
                for (TrackedSlotState state : allSlots) {
                    if (state.slotIndex == currentSequentialSlotIndex) {
                        result.add(state);
                        break;
                    }
                }
            }
            return result;
        } else {
            // 正常模式，返回所有槽位
            return new ArrayList<>(trackedContainerSlots.values());
        }
    }

    public static List<TrackedSlotState> getTrackedHotbarSlots(net.minecraft.world.entity.player.Inventory inventory) {
        if (hotbarDirty) {
            scanAndMarkHotbar(inventory);
        }

        List<TrackedSlotState> result = new ArrayList<>();
        for (Integer slotIndex : trackedHotbarSlots) {
            result.add(new TrackedSlotState(slotIndex, true));
        }
        return result;
    }

    public static void clearContainerTracking() {
        trackedContainerSlots.clear();
        containerDirty = true;
        currentSequentialSlotIndex = -1;
    }

    public static void clearHotbarTracking() {
        trackedHotbarSlots.clear();
        hotbarDirty = true;
    }

    public static void clearAll() {
        clearContainerTracking();
        clearHotbarTracking();
    }

    public static AbstractContainerScreen<?> getLastScreen() {
        return lastScreen;
    }

    public static boolean isContainerDirty() {
        return containerDirty;
    }

    public static boolean isHotbarDirty() {
        return hotbarDirty;
    }
}