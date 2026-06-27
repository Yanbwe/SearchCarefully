package org.yanbwe.searchcarefully.network;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.yanbwe.searchcarefully.SearchCarefully;

/**
 * 网络包注册中心
 * 替代 1.20.1 的 SimpleChannel 模式
 */
@EventBusSubscriber(modid = SearchCarefully.MODID, bus = EventBusSubscriber.Bus.MOD)
public class NetworkHandler {

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(SearchCarefully.MODID);

        // Client → Server: 搜索进度
        registrar.playToServer(
                SearchProgressPacket.TYPE,
                SearchProgressPacket.STREAM_CODEC,
                SearchProgressPacket::handle);

        // Server → Client: 循环音效
        registrar.playToClient(
                StartLoopSoundPacket.TYPE,
                StartLoopSoundPacket.STREAM_CODEC,
                StartLoopSoundPacket::handle);
    }
}
