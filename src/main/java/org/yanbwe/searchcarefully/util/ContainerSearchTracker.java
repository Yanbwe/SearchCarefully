package org.yanbwe.searchcarefully.util;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.*;

@OnlyIn(Dist.CLIENT)
public class ContainerSearchTracker {

    private static AbstractContainerScreen<?> lastScreen = null;
    private static final Map<Slot, TrackedSlotState> trackedContainerSlots = new IdentityHashMap<>();
    private static final Set<Integer> trackedHotbarSlots = new HashSet<>();
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

    public static void markContainerDirty() { containerDirty = true; }
    public static void markHotbarDirty() { hotbarDirty = true; }

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
            if (ItemStackHelper.hasRemainingSearchTime(inventory.getItem(i))) {
                trackedHotbarSlots.add(i);
            }
        }
        hotbarDirty = false;
    }

    public static List<TrackedSlotState> getTrackedContainerSlots(AbstractContainerScreen<?> screen) {
        if (containerDirty || lastScreen != screen) {
            scanAndMarkContainer(screen);
            currentSearchingSlotIndex = null;
        }
        if (org.yanbwe.searchcarefully.Config.ENABLE_MOUSE_TARGET_SEARCH.get() &&
            org.yanbwe.searchcarefully.Config.ENABLE_SINGLE_SLOT_SEARCH.get()) {
            return getHybridTrackedSlots(screen);
        }
        if (org.yanbwe.searchcarefully.Config.ENABLE_SINGLE_SLOT_SEARCH.get()) {
            return getSingleSlotTrackedSlots(screen);
        }
        return new ArrayList<>(trackedContainerSlots.values());
    }

    private static List<TrackedSlotState> getHybridTrackedSlots(AbstractContainerScreen<?> screen) {
        List<TrackedSlotState> result = new ArrayList<>();
        List<TrackedSlotState> allTrackedSlots = new ArrayList<>(trackedContainerSlots.values());
        allTrackedSlots.sort(Comparator.comparingInt(a -> a.slotIndex));
        if (allTrackedSlots.isEmpty()) {
            currentSearchingSlotIndex = null;
            return result;
        }
        boolean isMouseTargetMode = org.yanbwe.searchcarefully.manager.HybridSearchManager.isInMouseTargetMode();
        Integer hybridTarget = isMouseTargetMode
                ? org.yanbwe.searchcarefully.manager.HybridSearchManager.getCurrentTargetSlot()
                : null;
        if (hybridTarget != null) {
            currentSearchingSlotIndex = hybridTarget;
            boolean targetValid = allTrackedSlots.stream().anyMatch(s -> s.slotIndex == hybridTarget);
            if (targetValid) {
                result.add(new TrackedSlotState(hybridTarget, false));
            } else {
                currentSearchingSlotIndex = null;
                return getSingleSlotTrackedSlots(screen);
            }
        } else {
            return getSingleSlotTrackedSlots(screen);
        }
        return result;
    }

    private static List<TrackedSlotState> getSingleSlotTrackedSlots(AbstractContainerScreen<?> screen) {
        List<TrackedSlotState> result = new ArrayList<>();
        List<TrackedSlotState> allTrackedSlots = new ArrayList<>(trackedContainerSlots.values());
        allTrackedSlots.sort(Comparator.comparingInt(a -> a.slotIndex));
        if (allTrackedSlots.isEmpty()) {
            currentSearchingSlotIndex = null;
            return result;
        }
        if (currentSearchingSlotIndex == null) {
            currentSearchingSlotIndex = allTrackedSlots.get(0).slotIndex;
        } else {
            boolean currentSlotHasSearchTime = false;
            for (TrackedSlotState state : allTrackedSlots) {
                if (state.slotIndex == currentSearchingSlotIndex) {
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
                Optional<TrackedSlotState> next = allTrackedSlots.stream()
                        .filter(s -> s.slotIndex > currentSearchingSlotIndex).findFirst();
                currentSearchingSlotIndex = next.map(s -> s.slotIndex)
                        .orElse(allTrackedSlots.isEmpty() ? null : allTrackedSlots.get(0).slotIndex);
            }
        }
        if (currentSearchingSlotIndex != null) {
            allTrackedSlots.stream()
                    .filter(s -> s.slotIndex == currentSearchingSlotIndex)
                    .findFirst().ifPresent(result::add);
        }
        return result;
    }

    public static List<TrackedSlotState> getTrackedHotbarSlots(net.minecraft.world.entity.player.Inventory inventory) {
        if (hotbarDirty) scanAndMarkHotbar(inventory);
        List<TrackedSlotState> result = new ArrayList<>();
        trackedHotbarSlots.forEach(i -> result.add(new TrackedSlotState(i, true)));
        return result;
    }

    public static void clearContainerTracking() {
        trackedContainerSlots.clear();
        containerDirty = true;
        currentSearchingSlotIndex = null;
    }

    public static void clearHotbarTracking() { trackedHotbarSlots.clear(); hotbarDirty = true; }
    public static void clearAll() { clearContainerTracking(); clearHotbarTracking(); }
    public static AbstractContainerScreen<?> getLastScreen() { return lastScreen; }
    public static boolean isContainerDirty() { return containerDirty; }
    public static boolean isHotbarDirty() { return hotbarDirty; }
}
