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
        }

        return new ArrayList<>(trackedContainerSlots.values());
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