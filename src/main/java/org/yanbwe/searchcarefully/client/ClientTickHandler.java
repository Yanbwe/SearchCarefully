package org.yanbwe.searchcarefully.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.yanbwe.searchcarefully.manager.HybridSearchManager;
import org.yanbwe.searchcarefully.network.SearchProgressPacket;
import org.yanbwe.searchcarefully.util.ContainerSearchTracker;
import org.yanbwe.searchcarefully.util.ItemStackHelper;

@EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public class ClientTickHandler {

    private static int tickCounter = 0;
    private static final int SEND_INTERVAL = 1;
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

        if (mc.screen instanceof AbstractContainerScreen<?> screen) {
            HybridSearchManager.updateSearchTarget();
            ContainerSearchTracker.onScreenChanged(screen);

            rescanCounter++;
            if (rescanCounter >= RESCAN_INTERVAL) {
                rescanCounter = 0;
                ContainerSearchTracker.markContainerDirty();
            }

            for (var state : ContainerSearchTracker.getTrackedContainerSlots(screen)) {
                if (state.slotIndex >= 0 && state.slotIndex < screen.getMenu().slots.size()) {
                    var slot = screen.getMenu().slots.get(state.slotIndex);
                    if (hasSearchTime(slot.getItem())) {
                        PacketDistributor.sendToServer(
                                new SearchProgressPacket(state.slotIndex, false));
                    }
                }
            }
        } else {
            HybridSearchManager.reset();
            ContainerSearchTracker.onScreenChanged(null);
            ContainerSearchTracker.clearContainerTracking();

            if (org.yanbwe.searchcarefully.Config.ENABLE_HOTBAR_SEARCH.get() && mc.player != null) {
                var inventory = mc.player.getInventory();
                for (var state : ContainerSearchTracker.getTrackedHotbarSlots(inventory)) {
                    ItemStack itemStack = inventory.getItem(state.slotIndex);
                    if (hasSearchTime(itemStack)) {
                        PacketDistributor.sendToServer(
                                new SearchProgressPacket(state.slotIndex, true));
                    }
                }
            }
        }
    }

    private static boolean hasSearchTime(ItemStack stack) {
        return !stack.isEmpty() && ItemStackHelper.hasRemainingSearchTime(stack) &&
                ItemStackHelper.getRemainingSearchTime(stack) > 0;
    }
}
