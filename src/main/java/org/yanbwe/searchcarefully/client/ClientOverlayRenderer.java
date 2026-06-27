package org.yanbwe.searchcarefully.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.event.TickEvent;
import org.yanbwe.searchcarefully.SearchCarefully;
import org.yanbwe.searchcarefully.mixin.ContainerAccessMixin;
import org.yanbwe.searchcarefully.util.ItemStackHelper;

import java.util.HashSet;
import java.util.Set;

@EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public class ClientOverlayRenderer {

    private static AbstractContainerScreen<?> currentScreen = null;
    private static final Set<Slot> activeSearchSlots = new HashSet<>();
    private static boolean tooltipBlockedThisFrame = false;

    @SubscribeEvent
    public static void onScreenRenderPre(ScreenEvent.Render.Pre event) {
        if (event.getScreen() instanceof AbstractContainerScreen<?> screen) {
            currentScreen = screen;
        }
    }

    @SubscribeEvent
    public static void onScreenRenderPost(ScreenEvent.Render.Post event) {
        if (org.yanbwe.searchcarefully.Config.MASK_RENDER_ON_TOP.get()) {
            if (event.getScreen() instanceof AbstractContainerScreen<?> screen) {
                renderTopLayerMasks(event.getGuiGraphics(), screen);
            }
        }
    }

    private static void renderTopLayerMasks(GuiGraphics guiGraphics, AbstractContainerScreen<?> screen) {
        if (screen.getMenu() == null) return;
        int guiLeft = ((ContainerAccessMixin) screen).getLeftPos();
        int guiTop = ((ContainerAccessMixin) screen).getTopPos();
        for (Slot slot : screen.getMenu().slots) {
            ItemStack itemStack = slot.getItem();
            if (itemStack.isEmpty() || !ItemStackHelper.hasRemainingSearchTime(itemStack)) continue;
            double searchTime = ItemStackHelper.getRemainingSearchTime(itemStack);
            if (searchTime <= 0.0) continue;
            int x = guiLeft + slot.x;
            int y = guiTop + slot.y;
            SearchOverlayRenderer.renderOverlay(guiGraphics, itemStack, x, y, true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRenderTooltip(RenderTooltipEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof AbstractContainerScreen<?> screen) {
            Slot hoveredSlot = ((ContainerAccessMixin) screen).getHoveredSlot();
            if (hoveredSlot != null) {
                if (isSlotBeingSearched(hoveredSlot)) {
                    if (event.isCancelable()) event.setCanceled(true);
                    return;
                }
                if (org.yanbwe.searchcarefully.Config.ENABLE_SINGLE_SLOT_SEARCH.get()) {
                    if (hoveredSlot.hasItem()) {
                        ItemStack stack = hoveredSlot.getItem();
                        if (ItemStackHelper.hasRemainingSearchTime(stack)) {
                            if (ItemStackHelper.getRemainingSearchTime(stack) > 0.0) {
                                if (event.isCancelable()) event.setCanceled(true);
                            }
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) resetTooltipFrameState();
        if (event.phase == TickEvent.Phase.END) updateActiveSearchSlots();
    }

    private static void updateActiveSearchSlots() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof AbstractContainerScreen<?> screen && screen.getMenu() != null) {
            activeSearchSlots.clear();
            boolean singleSlotSearch = org.yanbwe.searchcarefully.Config.ENABLE_SINGLE_SLOT_SEARCH.get();
            if (singleSlotSearch) {
                var trackedSlots = org.yanbwe.searchcarefully.util.ContainerSearchTracker.getTrackedContainerSlots(screen);
                if (!trackedSlots.isEmpty()) {
                    int idx = trackedSlots.get(0).slotIndex;
                    if (idx < screen.getMenu().slots.size()) {
                        Slot slot = screen.getMenu().slots.get(idx);
                        if (slot.hasItem()) {
                            ItemStack stack = slot.getItem();
                            if (ItemStackHelper.hasRemainingSearchTime(stack) &&
                                    ItemStackHelper.getRemainingSearchTime(stack) > 0.0) {
                                activeSearchSlots.add(slot);
                            }
                        }
                    }
                }
            } else {
                for (Slot slot : screen.getMenu().slots) {
                    if (slot.hasItem()) {
                        ItemStack stack = slot.getItem();
                        if (ItemStackHelper.hasRemainingSearchTime(stack) &&
                                ItemStackHelper.getRemainingSearchTime(stack) > 0.0) {
                            activeSearchSlots.add(slot);
                        }
                    }
                }
            }
        } else {
            activeSearchSlots.clear();
        }
    }

    public static boolean isSlotBeingSearched(Slot slot) { return activeSearchSlots.contains(slot); }

    public static boolean isItemBeingSearched(ItemStack itemStack) {
        return !itemStack.isEmpty() && activeSearchSlots.stream()
                .anyMatch(s -> s.hasItem() && s.getItem() == itemStack);
    }

    public static AbstractContainerScreen<?> getCurrentScreen() { return currentScreen; }

    private static void resetTooltipFrameState() { tooltipBlockedThisFrame = false; }
    public static void setTooltipBlocked(boolean blocked) { tooltipBlockedThisFrame = blocked; }
    public static boolean isTooltipBlocked() { return tooltipBlockedThisFrame; }
}
