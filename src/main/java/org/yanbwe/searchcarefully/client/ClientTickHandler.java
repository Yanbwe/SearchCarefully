package org.yanbwe.searchcarefully.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.yanbwe.searchcarefully.network.NetworkHandler;
import org.yanbwe.searchcarefully.network.SearchProgressPacket;
import org.yanbwe.searchcarefully.util.SearchConstants;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientTickHandler {
    
    private static int tickCounter = 0;
    private static final int SEND_INTERVAL = 1; // 每1个tick发送一次，约50ms

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            tickCounter++;
            if (tickCounter >= SEND_INTERVAL) {
                tickCounter = 0; // 重置计数器
                sendSearchProgressPackets();
            }
        }
    }

    private static void sendSearchProgressPackets() {
        Minecraft mc = Minecraft.getInstance();
        
        // 仅在容器界面中发送数据包
        if (mc.screen instanceof AbstractContainerScreen) {
            AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) mc.screen;
            
            if (screen.getMenu() != null) {
                // 根据界面/菜单类型确定容器类型
                int containerType = getContainerType(screen);
                
                // 遍历当前容器中的所有槽位
                for (int i = 0; i < screen.getMenu().slots.size(); i++) {
                    Slot slot = screen.getMenu().slots.get(i);
                    
                    // 直接检查槽位中的物品是否有搜索时间（而不是依赖activeSearchSlots集合）
                    if (hasSearchTime(slot.getItem())) {
                        // 发送数据包到服务器以指示此槽位正在被搜索
                        SearchProgressPacket packet = new SearchProgressPacket(containerType, i);
                        NetworkHandler.INSTANCE.sendToServer(packet);
                    }
                }
            }
        }
    }
    
    private static boolean hasSearchTime(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTag()) {
            return false;
        }
        
        return stack.getTag().contains(SearchConstants.SEARCH_TIME_REMAINING) &&
               stack.getTag().getInt(SearchConstants.SEARCH_TIME_REMAINING) > 0;
    }
    
    private static int getContainerType(AbstractContainerScreen<?> screen) {
        // 将不同的容器界面类型映射到整数ID
        String screenClassName = screen.getClass().getSimpleName();
        
        // 由于潜在的导入问题，使用类名检查而不是instanceof
        if (screenClassName.contains("Chest")) {
            return 1; // 箱子
        } else if (screenClassName.contains("Dispenser") || screenClassName.contains("Hopper") || screenClassName.contains("ShulkerBox")) {
            return 2; // 发射器/漏斗/潜影盒
        } else if (screenClassName.contains("Furnace")) {
            return 3; // 熔炉
        } else if (screenClassName.contains("Crafting")) {
            return 4; // 合成台
        } else if (screenClassName.contains("Inventory") || screenClassName.contains("Player")) {
            return 0; // 玩家物品栏
        } else {
            return 0; // 默认为0，表示通用容器
        }
    }
}