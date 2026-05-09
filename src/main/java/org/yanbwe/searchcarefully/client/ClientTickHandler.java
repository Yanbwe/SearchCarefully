package org.yanbwe.searchcarefully.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.yanbwe.searchcarefully.network.NetworkHandler;
import org.yanbwe.searchcarefully.network.SearchProgressPacket;
import org.yanbwe.searchcarefully.util.ContainerSearchTracker;
import org.yanbwe.searchcarefully.util.ItemStackHelper;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientTickHandler {

    private static int tickCounter = 0;
    private static final int SEND_INTERVAL = 1;

    // 定期重新扫描容器的计数器，防止因初始扫描时机过早而漏掉物品
    // 每 10 tick（0.5 秒）强制刷新一次 ContainerSearchTracker 缓存
    private static int rescanCounter = 0;
    private static final int RESCAN_INTERVAL = 10;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            tickCounter++;
            if (tickCounter >= SEND_INTERVAL) {
                tickCounter = 0;
                sendSearchProgressPackets();
            }
        }
    }

    private static void sendSearchProgressPackets() {
        Minecraft mc = Minecraft.getInstance();

        if (mc.screen instanceof AbstractContainerScreen screen) {
            // 更新混合搜索管理器
            org.yanbwe.searchcarefully.manager.HybridSearchManager.updateSearchTarget();
            
            ContainerSearchTracker.onScreenChanged(screen);
            
            // 定期强制重新扫描容器，确保后续到达的物品也能被检测到
            // 避免因初始扫描时机过早而漏掉未同步完成的物品
            rescanCounter++;
            if (rescanCounter >= RESCAN_INTERVAL) {
                rescanCounter = 0;
                ContainerSearchTracker.markContainerDirty();
            }

            for (var state : ContainerSearchTracker.getTrackedContainerSlots(screen)) {
                if (state.slotIndex >= 0 && state.slotIndex < screen.getMenu().slots.size()) {
                    var slot = screen.getMenu().slots.get(state.slotIndex);
                    if (hasSearchTime(slot.getItem())) {
                        SearchProgressPacket packet = new SearchProgressPacket(state.slotIndex, false);
                        NetworkHandler.INSTANCE.sendToServer(packet);
                    }
                }
            }
        } else {
            // 不在容器界面，重置混合搜索管理器
            org.yanbwe.searchcarefully.manager.HybridSearchManager.reset();
            
            ContainerSearchTracker.onScreenChanged(null);
            ContainerSearchTracker.clearContainerTracking();

            if (org.yanbwe.searchcarefully.Config.ENABLE_HOTBAR_SEARCH.get() && mc.player != null) {
                var inventory = mc.player.getInventory();

                for (var state : ContainerSearchTracker.getTrackedHotbarSlots(inventory)) {
                    ItemStack itemStack = inventory.getItem(state.slotIndex);
                    if (hasSearchTime(itemStack)) {
                        SearchProgressPacket packet = new SearchProgressPacket(state.slotIndex, true);
                        NetworkHandler.INSTANCE.sendToServer(packet);
                    }
                }
            }
        }
    }

    private static boolean hasSearchTime(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        return ItemStackHelper.hasRemainingSearchTime(stack) &&
               ItemStackHelper.getRemainingSearchTime(stack) > 0;
    }
}