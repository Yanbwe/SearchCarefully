package org.yanbwe.searchcarefully.client;

import net.minecraft.client.Minecraft;

/**
 * 客户端网络数据包处理辅助类。
 * 仅在客户端加载，处理服务端发来的客户端专属数据包逻辑。
 */
public class ClientSoundPacketHandler {

    /**
     * 处理开始播放搜索进度循环音效的请求。
     * 通过 {@link StartLoopSoundPacket#handle} 调用，仅在客户端环境中执行。
     */
    public static void handleStartLoopSound() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            SearchSoundManager.startSearchLoopSound(mc.player);
        }
    }
}
