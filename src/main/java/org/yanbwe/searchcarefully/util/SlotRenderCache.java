package org.yanbwe.searchcarefully.util;

import net.minecraft.world.item.ItemStack;
import java.util.ArrayList;
import java.util.List;

/**
 * 热键栏搜索遮罩渲染缓存
 * 容器遮罩渲染已迁移到 SlotRenderMixin 直接渲染，不再使用缓存
 */
public class SlotRenderCache {

    // 热键栏遮罩渲染信息缓存
    private static final List<HotbarOverlayInfo> pendingHotbarOverlays = new ArrayList<>();

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
