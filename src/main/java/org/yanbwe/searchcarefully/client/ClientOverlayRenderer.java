package org.yanbwe.searchcarefully.client;

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
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.yanbwe.searchcarefully.Searchcarefully;
import org.yanbwe.searchcarefully.mixin.ContainerAccessMixin;
import org.yanbwe.searchcarefully.util.ItemStackHelper;
import org.yanbwe.searchcarefully.util.SearchConstants;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientOverlayRenderer {

    // 跟踪当前正在渲染的容器界面
    private static AbstractContainerScreen<?> currentScreen = null;
    
    // 跟踪当前客户端正在搜索的槽位
    private static final java.util.Set<Slot> activeSearchSlots = new java.util.HashSet();
    
    // 跟踪工具提示拦截状态
    private static boolean tooltipBlockedThisFrame = false;

    @SubscribeEvent
    public static void onScreenRenderPre(ScreenEvent.Render.Pre event) {
        if (event.getScreen() instanceof AbstractContainerScreen) {
            currentScreen = (AbstractContainerScreen<?>) event.getScreen();
        }
    }

    @SubscribeEvent
    public static void onScreenRenderPost(ScreenEvent.Render.Post event) {
        // 最高层级遮罩渲染模式：在所有元素（包括提示框）之上渲染遮罩
        if (org.yanbwe.searchcarefully.Config.MASK_RENDER_ON_TOP.get()) {
            if (event.getScreen() instanceof AbstractContainerScreen<?> screen) {
                renderTopLayerMasks(event.getGuiGraphics(), screen);
            }
        }
    }
    
    /**
     * 在最高层级渲染所有搜索遮罩
     * 此方法在 ScreenEvent.Render.Post 中调用，确保遮罩在所有元素之上
     */
    private static void renderTopLayerMasks(GuiGraphics guiGraphics, AbstractContainerScreen<?> screen) {
        if (screen.getMenu() == null) return;
        
        int guiLeft = ((ContainerAccessMixin) screen).getLeftPos();
        int guiTop = ((ContainerAccessMixin) screen).getTopPos();
        
        for (Slot slot : screen.getMenu().slots) {
            ItemStack itemStack = slot.getItem();
            if (itemStack.isEmpty() || !ItemStackHelper.hasRemainingSearchTime(itemStack)) {
                continue;
            }
            
            double searchTime = ItemStackHelper.getRemainingSearchTime(itemStack);
            if (searchTime <= 0.0) {
                continue;
            }
            
            // 在 Post 事件中坐标系为屏幕坐标，需要加上 GUI 偏移
            int x = guiLeft + slot.x;
            int y = guiTop + slot.y;
            
            SearchOverlayRenderer.renderOverlay(guiGraphics, itemStack, x, y, true);
        }
    }

    // 添加工具提示事件处理，用于隐藏正在搜索的物品的工具提示
    // 使用最高优先级确保在Obscure Tooltips之前执行
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRenderTooltip(RenderTooltipEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof AbstractContainerScreen) {
            AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) mc.screen;
            
            // 使用mixin获取悬停的槽位
            Slot hoveredSlot = ((ContainerAccessMixin) screen).getHoveredSlot();
            
            if (hoveredSlot != null) {
                // 检查槽位是否正在搜索
                if (isSlotBeingSearched(hoveredSlot)) {
                    // 只取消可取消的事件类型（如RenderTooltipEvent.Pre）
                    if (event.isCancelable()) {
                        event.setCanceled(true);
                    }
                    return;
                }
                
                // 逐格搜索模式下，检查所有待搜物品
                if (org.yanbwe.searchcarefully.Config.ENABLE_SINGLE_SLOT_SEARCH.get()) {
                    if (hoveredSlot.hasItem()) {
                        ItemStack stack = hoveredSlot.getItem();
                        if (ItemStackHelper.hasRemainingSearchTime(stack)) {
                            double searchTime = ItemStackHelper.getRemainingSearchTime(stack);
                            if (searchTime > 0.0) {
                                // 只取消可取消的事件类型（如RenderTooltipEvent.Pre）
                                if (event.isCancelable()) {
                                    event.setCanceled(true);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            // 重置每帧的状态标记
            resetTooltipFrameState();
        }
        
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
                
                boolean singleSlotSearch = org.yanbwe.searchcarefully.Config.ENABLE_SINGLE_SLOT_SEARCH.get();
                
                if (singleSlotSearch) {
                    // 逐格搜索模式：只收集当前正在搜索的槽位
                    var trackedSlots = org.yanbwe.searchcarefully.util.ContainerSearchTracker.getTrackedContainerSlots(screen);
                    if (!trackedSlots.isEmpty()) {
                        int currentSlotIndex = trackedSlots.get(0).slotIndex;
                        if (currentSlotIndex < screen.getMenu().slots.size()) {
                            Slot slot = screen.getMenu().slots.get(currentSlotIndex);
                            if (slot.hasItem()) {
                                ItemStack stack = slot.getItem();
                                if (ItemStackHelper.hasRemainingSearchTime(stack)) {
                                    double searchTime = ItemStackHelper.getRemainingSearchTime(stack);
                                    if (searchTime > 0.0) {
                                        activeSearchSlots.add(slot);
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // 默认模式：添加所有有剩余搜索时间物品的槽位
                    for (Slot slot : screen.getMenu().slots) {
                        if (slot.hasItem()) {
                            ItemStack stack = slot.getItem();
                            if (ItemStackHelper.hasRemainingSearchTime(stack)) {
                                double searchTime = ItemStackHelper.getRemainingSearchTime(stack);
                                if (searchTime > 0.0) {
                                    activeSearchSlots.add(slot);
                                }
                            }
                        }
                    }
                }
            }
        } else {
            activeSearchSlots.clear();
        }
    }

    /**
     * 根据鼠标位置找到对应的槽位
     * 使用GUI坐标和槽位位置进行精确匹配
     */
    private static Slot getSlotUnderMouse(AbstractContainerScreen<?> screen, int mouseX, int mouseY) {
        if (screen.getMenu() != null) {
            // 获取GUI位置信息
            Point guiPosition = getGuiPosition(screen);
                
            // 遍历菜单中的所有槽位
            for (Slot slot : screen.getMenu().slots) {
                if (isMouseOverSlot(guiPosition.x, guiPosition.y, slot, mouseX, mouseY)) {
                    return slot;
                }
            }
        }
        return null;
    }
        
    /**
     * 获取容器界面的GUI位置坐标
     * 通过Mixin访问器获取准确的界面位置信息
     */
    private static Point getGuiPosition(AbstractContainerScreen<?> screen) {
        int guiLeft = ((ContainerAccessMixin) screen).getLeftPos();
        int guiTop = ((ContainerAccessMixin) screen).getTopPos();
        return new Point(guiLeft, guiTop);
    }
        
    /**
     * 表示GUI坐标的封装类
     * 用于存储界面左上角坐标信息
     */
    private static class Point {
        final int x, y;
        Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
        
    private static boolean isMouseOverSlot(int guiLeft, int guiTop, Slot slot, int mouseX, int mouseY) {
        int slotX = guiLeft + slot.x;
        int slotY = guiTop + slot.y;
        return mouseX >= slotX && mouseY >= slotY && mouseX < slotX + 16 && mouseY < slotY + 16;
    }

    public static boolean isSlotBeingSearched(Slot slot) {
        return activeSearchSlots.contains(slot);
    }

    public static boolean isItemBeingSearched(ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return false;
        }
        for (Slot slot : activeSearchSlots) {
            if (slot.hasItem() && slot.getItem() == itemStack) {
                return true;
            }
        }
        return false;
    }

    public static AbstractContainerScreen<?> getCurrentScreen() {
        return currentScreen;
    }
    
    /**
     * 重置工具提示帧状态
     */
    private static void resetTooltipFrameState() {
        tooltipBlockedThisFrame = false;
    }
    
    /**
     * 设置工具提示拦截状态
     */
    public static void setTooltipBlocked(boolean blocked) {
        tooltipBlockedThisFrame = blocked;
    }
    
    /**
     * 检查是否应该拦截工具提示
     */
    public static boolean isTooltipBlocked() {
        return tooltipBlockedThisFrame;
    }
}