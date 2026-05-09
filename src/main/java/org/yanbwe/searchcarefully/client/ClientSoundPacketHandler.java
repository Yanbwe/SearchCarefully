package org.yanbwe.searchcarefully.client;

import net.minecraft.client.Minecraft;

/**
 * 客户端网络数据包处理辅助类。
 * <p>
 * 仅在客户端加载，处理服务端发来的客户端专属数据包逻辑。
 * 设计为独立顶级类，避免在通用（common）网络数据包类中直接引用客户端类，
 * 从而防止在专用服务端（DEDICATED_SERVER）上触发 Forge dist 类加载检查错误。
 * </p>
 */
public class ClientSoundPacketHandler {

    /**
     * 处理开始播放搜索进度循环音效的请求。
     * 此方法通过 {@link net.minecraftforge.fml.DistExecutor#unsafeRunWhenOn} 延迟调用，
     * 仅在客户端环境中执行。
     */
    public static void handleStartLoopSound() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            SearchSoundManager.startSearchLoopSound(mc.player);
        }
    }
}
