package org.yanbwe.searchcarefully.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.yanbwe.searchcarefully.Searchcarefully;
import org.yanbwe.searchcarefully.mixin.ContainerAccessMixin;
import org.yanbwe.searchcarefully.util.SearchConstants;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientOverlayRenderer {

    // 跟踪当前正在渲染的容器界面
    private static AbstractContainerScreen<?> currentScreen = null;
    
    // 跟踪当前客户端正在搜索的槽位
    private static final java.util.Set<Slot> activeSearchSlots = new java.util.HashSet();

    @SubscribeEvent
    public static void onScreenRenderPre(ScreenEvent.Render.Pre event) {
        if (event.getScreen() instanceof AbstractContainerScreen) {
            currentScreen = (AbstractContainerScreen<?>) event.getScreen();
        }
    }

    @SubscribeEvent
    public static void onScreenRenderPost(ScreenEvent.Render.Post event) {
        // 遮罩渲染现在通过mixin处理，以避免与RarityCore的边框渲染冲突
    }

    // 添加工具提示事件处理，用于隐藏正在搜索的物品的工具提示
    @SubscribeEvent
    public static void onRenderTooltip(RenderTooltipEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof AbstractContainerScreen) {
            AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) mc.screen;
            
            // 使用mixin获取悬停的槽位
            Slot hoveredSlot = ((ContainerAccessMixin) screen).getHoveredSlot();
            
            if (hoveredSlot != null && isSlotBeingSearched(hoveredSlot)) {
                // 如果槽位正在搜索中，取消工具提示的渲染
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            // 根据当前界面更新活跃的搜索槽位
            updateActiveSearchSlots();
        }
    }

    private static void updateActiveSearchSlots() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof AbstractContainerScreen) {
            AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) mc.screen;
            if (screen.getMenu() != null) {
                activeSearchSlots.clear();
                
                // 添加所有有剩余搜索时间物品的槽位
                for (Slot slot : screen.getMenu().slots) {
                    if (slot.hasItem()) {
                        ItemStack stack = slot.getItem();
                        if (stack.hasTag() && stack.getTag().contains(SearchConstants.SEARCH_TIME_REMAINING)) {
                            int searchTime = stack.getTag().getInt(SearchConstants.SEARCH_TIME_REMAINING);
                            if (searchTime > 0) {
                                activeSearchSlots.add(slot);
                            }
                        }
                    }
                }
            }
        } else {
            activeSearchSlots.clear();
        }
    }

    private static Slot getSlotUnderMouse(AbstractContainerScreen<?> screen, int mouseX, int mouseY) {
        if (screen.getMenu() != null) {
            // 使用mixin访问器获取正确的字段值
            int guiLeft = ((ContainerAccessMixin) screen).getLeftPos();
            int guiTop = ((ContainerAccessMixin) screen).getTopPos();
            
            // 遍历菜单中的所有槽位
            for (Slot slot : screen.getMenu().slots) {
                int slotX = guiLeft + slot.x;
                int slotY = guiTop + slot.y;
                
                // 检查鼠标是否悬停在槽位上
                if (mouseX >= slotX && mouseY >= slotY && mouseX < slotX + 16 && mouseY < slotY + 16) {
                    return slot;
                }
            }
        }
        return null;
    }

    private static Slot findSlotUnderMouse(AbstractContainerScreen<?> screen, int mouseX, int mouseY) {
        if (screen.getMenu() != null) {
            // 使用mixin访问器获取正确的字段值
            int guiLeft = ((ContainerAccessMixin) screen).getLeftPos();
            int guiTop = ((ContainerAccessMixin) screen).getTopPos();
            
            for (Slot slot : screen.getMenu().slots) {
                if (isMouseOverSlot(guiLeft, guiTop, slot, mouseX, mouseY)) {
                    return slot;
                }
            }
        }
        return null;
    }

    private static boolean isMouseOverSlot(int guiLeft, int guiTop, Slot slot, int mouseX, int mouseY) {
        int slotX = guiLeft + slot.x;
        int slotY = guiTop + slot.y;
        return mouseX >= slotX && mouseY >= slotY && mouseX < slotX + 16 && mouseY < slotY + 16;
    }

    public static boolean isSlotBeingSearched(Slot slot) {
        return activeSearchSlots.contains(slot);
    }

    public static AbstractContainerScreen<?> getCurrentScreen() {
        return currentScreen;
    }
}