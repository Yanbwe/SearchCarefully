package org.yanbwe.searchcarefully.util;

import net.minecraft.world.item.ItemStack;
import java.util.ArrayList;
import java.util.List;

/**
 * 热键栏搜索遮罩渲染缓存
 */
public class SlotRenderCache {

    private static final List<HotbarOverlayInfo> pendingHotbarOverlays = new ArrayList<>();

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

    public static void addHotbarOverlay(int x, int y, ItemStack itemStack, int searchTime) {
        pendingHotbarOverlays.add(new HotbarOverlayInfo(x, y, itemStack, searchTime));
    }

    public static List<HotbarOverlayInfo> getPendingHotbarOverlays() {
        return pendingHotbarOverlays;
    }

    public static void clearHotbarCache() {
        pendingHotbarOverlays.clear();
    }
}
