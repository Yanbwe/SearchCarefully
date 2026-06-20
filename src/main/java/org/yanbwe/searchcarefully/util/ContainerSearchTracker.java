package org.yanbwe.searchcarefully.util;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@OnlyIn(Dist.CLIENT)
public class ContainerSearchTracker {

    private static AbstractContainerScreen<?> lastScreen = null;
    private static final Map<Slot, TrackedSlotState> trackedContainerSlots = new IdentityHashMap<>();
    private static final Set<Integer> trackedHotbarSlots = new java.util.HashSet<>();
    private static boolean containerDirty = true;
    private static boolean hotbarDirty = true;
    private static Integer currentSearchingSlotIndex = null;

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
            // Reset current searching slot when screen changes or container is dirty
            currentSearchingSlotIndex = null;
        }

        // 检查是否启用混合搜索模式
        if (org.yanbwe.searchcarefully.Config.ENABLE_MOUSE_TARGET_SEARCH.get() && 
            org.yanbwe.searchcarefully.Config.ENABLE_SINGLE_SLOT_SEARCH.get()) {
            // 混合模式：优先使用HybridSearchManager的目标
            return getHybridTrackedSlots(screen);
        }
        
        // 检查是否启用单槽位搜索
        if (org.yanbwe.searchcarefully.Config.ENABLE_SINGLE_SLOT_SEARCH.get()) {
            return getSingleSlotTrackedSlots(screen);
        } else {
            // 默认行为：返回所有跟踪的槽位
            return new ArrayList<>(trackedContainerSlots.values());
        }
    }
    
    /**
     * 混合模式：获取跟踪的槽位
     * 优先使用HybridSearchManager的目标
     */
    private static List<TrackedSlotState> getHybridTrackedSlots(AbstractContainerScreen<?> screen) {
        List<TrackedSlotState> result = new ArrayList<>();
        
        // 获取所有跟踪的槽位（按索引排序）
        List<TrackedSlotState> allTrackedSlots = new ArrayList<>(trackedContainerSlots.values());
        allTrackedSlots.sort((a, b) -> Integer.compare(a.slotIndex, b.slotIndex));
        
        if (allTrackedSlots.isEmpty()) {
            currentSearchingSlotIndex = null;
            return result;
        }
        
        // 检查HybridSearchManager是否有鼠标目标
        boolean isMouseTargetMode = org.yanbwe.searchcarefully.manager.HybridSearchManager.isInMouseTargetMode();
        Integer hybridTarget = null;
        
        if (isMouseTargetMode) {
            hybridTarget = org.yanbwe.searchcarefully.manager.HybridSearchManager.getCurrentTargetSlot();
        }
        
        if (hybridTarget != null) {
            // 使用HybridSearchManager的目标
            currentSearchingSlotIndex = hybridTarget;
            
            // 验证目标是否有效
            boolean targetValid = false;
            for (TrackedSlotState state : allTrackedSlots) {
                if (state.slotIndex == hybridTarget) {
                    targetValid = true;
                    break;
                }
            }
            
            if (targetValid) {
                // 添加目标槽位到结果
                result.add(new TrackedSlotState(hybridTarget, false));
            } else {
                // 目标无效，回退到自动模式
                currentSearchingSlotIndex = null;
                return getSingleSlotTrackedSlots(screen);
            }
        } else {
            // 没有鼠标目标，使用自动模式
            return getSingleSlotTrackedSlots(screen);
        }
        
        return result;
    }
    
    /**
     * 单槽位搜索模式：获取跟踪的槽位
     */
    private static List<TrackedSlotState> getSingleSlotTrackedSlots(AbstractContainerScreen<?> screen) {
        List<TrackedSlotState> result = new ArrayList<>();
        
        // 获取所有跟踪的槽位（按索引排序）
        List<TrackedSlotState> allTrackedSlots = new ArrayList<>(trackedContainerSlots.values());
        allTrackedSlots.sort((a, b) -> Integer.compare(a.slotIndex, b.slotIndex));
        
        if (allTrackedSlots.isEmpty()) {
            currentSearchingSlotIndex = null;
            return result;
        }
        
        // 查找下一个要搜索的槽位
        if (currentSearchingSlotIndex == null) {
            // 从第一个槽位开始
            currentSearchingSlotIndex = allTrackedSlots.get(0).slotIndex;
        } else {
            // 检查当前槽位是否还有搜索时间
            boolean currentSlotHasSearchTime = false;
            for (TrackedSlotState state : allTrackedSlots) {
                if (state.slotIndex == currentSearchingSlotIndex) {
                    // 查找实际的槽位并检查其物品
                    if (screen.getMenu() != null && state.slotIndex < screen.getMenu().slots.size()) {
                        Slot slot = screen.getMenu().slots.get(state.slotIndex);
                        if (ItemStackHelper.hasRemainingSearchTime(slot.getItem())) {
                            currentSlotHasSearchTime = true;
                        }
                    }
                    break;
                }
            }
            
            if (!currentSlotHasSearchTime) {
                // 当前槽位不再有搜索时间，查找下一个
                for (TrackedSlotState state : allTrackedSlots) {
                    if (state.slotIndex > currentSearchingSlotIndex) {
                        currentSearchingSlotIndex = state.slotIndex;
                        currentSlotHasSearchTime = true;
                        break;
                    }
                }
                
                // 如果没找到下一个槽位，从开头开始
                if (!currentSlotHasSearchTime && !allTrackedSlots.isEmpty()) {
                    currentSearchingSlotIndex = allTrackedSlots.get(0).slotIndex;
                }
            }
        }
        
        // 只将当前搜索槽位添加到结果
        if (currentSearchingSlotIndex != null) {
            for (TrackedSlotState state : allTrackedSlots) {
                if (state.slotIndex == currentSearchingSlotIndex) {
                    result.add(state);
                    break;
                }
            }
        }
        
        return result;
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
        currentSearchingSlotIndex = null;
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