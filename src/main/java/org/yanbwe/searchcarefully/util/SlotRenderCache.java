package org.yanbwe.searchcarefully.util;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import java.util.ArrayList;
import java.util.List;

public class SlotRenderCache {
    // 存储需要渲染搜索遮罩的槽位信息
    private static final List<SlotOverlayInfo> pendingOverlays = new ArrayList<>();
    
    // 存储热键栏需要渲染搜索遮罩的信息
    private static final List<HotbarOverlayInfo> pendingHotbarOverlays = new ArrayList<>();

    // 槽位遮罩信息类
    public static class SlotOverlayInfo {
        public final int x;
        public final int y;
        public final ItemStack itemStack;
        public final int searchTime;
        
        public SlotOverlayInfo(int x, int y, ItemStack itemStack, int searchTime) {
            this.x = x;
            this.y = y;
            this.itemStack = itemStack;
            this.searchTime = searchTime;
        }
    }
    
    // 热键栏遮罩信息类
    public static class HotbarOverlayInfo {
        public final int x;
        public final int y;
        public final ItemStack itemStack;
        public final int searchTime;
        
        public HotbarOverlayInfo(int x, int y, ItemStack itemStack, int searchTime) {
            this.x = x;
            this.y = y;
            this.itemStack = itemStack;
            this.searchTime = searchTime;
        }
    }

    public static void addSlotOverlay(AbstractContainerScreen<?> screen, Slot slot) {
        ItemStack itemStack = slot.getItem();
        if (!itemStack.isEmpty() && ItemStackHelper.hasRemainingSearchTime(itemStack)) {
            int searchTime = (int) ItemStackHelper.getRemainingSearchTime(itemStack);
            if (searchTime > 0) {
                int x = screen.getGuiLeft() + slot.x;
                int y = screen.getGuiTop() + slot.y;
                pendingOverlays.add(new SlotOverlayInfo(x, y, itemStack, searchTime));
            }
        }
    }

    public static void addHotbarOverlay(int x, int y, ItemStack itemStack, int searchTime) {
        pendingHotbarOverlays.add(new HotbarOverlayInfo(x, y, itemStack, searchTime));
    }

    public static List<SlotOverlayInfo> getPendingOverlays() {
        return pendingOverlays;
    }

    public static List<HotbarOverlayInfo> getPendingHotbarOverlays() {
        return pendingHotbarOverlays;
    }

    public static void clearCache() {
        pendingOverlays.clear();
        pendingHotbarOverlays.clear();
    }
    
    public static void clearContainerCache() {
        pendingOverlays.clear();
    }
    
    public static void clearHotbarCache() {
        pendingHotbarOverlays.clear();
    }
}