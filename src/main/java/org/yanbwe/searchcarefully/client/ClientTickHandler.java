package org.yanbwe.searchcarefully.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.yanbwe.searchcarefully.Searchcarefully;
import org.yanbwe.searchcarefully.network.NetworkHandler;
import org.yanbwe.searchcarefully.network.SearchProgressPacket;
import org.yanbwe.searchcarefully.util.ContainerSearchTracker;
import org.yanbwe.searchcarefully.util.ItemStackHelper;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

@EventBusSubscriber(value = Dist.CLIENT, modid = Searchcarefully.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ClientTickHandler {

    private static int tickCounter = 0;
    private static final int SEND_INTERVAL = 1;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        tickCounter++;
        if (tickCounter >= SEND_INTERVAL) {
            tickCounter = 0;
            sendSearchProgressPackets();
        }
    }

    private static void sendSearchProgressPackets() {
        Minecraft mc = Minecraft.getInstance();

        if (mc.screen instanceof AbstractContainerScreen screen) {
            ContainerSearchTracker.onScreenChanged(screen);

            for (var state : ContainerSearchTracker.getTrackedContainerSlots(screen)) {
                if (state.slotIndex >= 0 && state.slotIndex < screen.getMenu().slots.size()) {
                    var slot = screen.getMenu().slots.get(state.slotIndex);
                    if (hasSearchTime(slot.getItem())) {
                        SearchProgressPacket packet = new SearchProgressPacket(state.slotIndex, false);
                        NetworkHandler.sendToServer(packet);
                    }
                }
            }
        } else {
            ContainerSearchTracker.onScreenChanged(null);
            ContainerSearchTracker.clearContainerTracking();

            if (org.yanbwe.searchcarefully.Config.ENABLE_HOTBAR_SEARCH.get() && mc.player != null) {
                var inventory = mc.player.getInventory();

                for (var state : ContainerSearchTracker.getTrackedHotbarSlots(inventory)) {
                    ItemStack itemStack = inventory.getItem(state.slotIndex);
                    if (hasSearchTime(itemStack)) {
                        SearchProgressPacket packet = new SearchProgressPacket(state.slotIndex, true);
                        NetworkHandler.sendToServer(packet);
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