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
import org.yanbwe.searchcarefully.util.ItemStackHelper;
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
        
        // 检查是否打开了容器界面
        if (mc.screen instanceof AbstractContainerScreen) {
            AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) mc.screen;
            
            if (screen.getMenu() != null) {
                // 遍历当前容器中的所有槽位，恢复原来的批量处理机制
                for (int i = 0; i < screen.getMenu().slots.size(); i++) {
                    Slot slot = screen.getMenu().slots.get(i);
                    
                    // 直接检查槽位中的物品是否有搜索时间
                    if (hasSearchTime(slot.getItem())) {
                        // 发送数据包到服务器以指示此槽位正在被搜索
                        SearchProgressPacket packet = new SearchProgressPacket(i, false); // false 表示容器槽位
                        NetworkHandler.INSTANCE.sendToServer(packet);
                    }
                }
            }
        }
        // 如果没有打开容器，且启用了热键栏搜索
        else if (org.yanbwe.searchcarefully.Config.ENABLE_HOTBAR_SEARCH.get() && mc.player != null) {
            // 遍历热键栏的所有槽位（索引 0-8）
            var inventory = mc.player.getInventory();
            for (int i = 0; i < 9; i++) {
                ItemStack itemStack = inventory.getItem(i);
                
                // 检查物品是否有搜索时间
                if (hasSearchTime(itemStack)) {
                    // 发送数据包到服务器以指示此热键栏槽位正在被搜索
                    SearchProgressPacket packet = new SearchProgressPacket(i, true); // true 表示热键栏槽位
                    NetworkHandler.INSTANCE.sendToServer(packet);
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