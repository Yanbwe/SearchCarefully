package org.yanbwe.searchcarefully.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.yanbwe.searchcarefully.SearchCarefully;
import org.yanbwe.searchcarefully.client.ClientSoundPacketHandler;

/**
 * 服务端→客户端：开始播放搜索进度循环音效
 * 空数据包，仅用作信号触发
 */
public record StartLoopSoundPacket() implements CustomPacketPayload {

    public static final Type<StartLoopSoundPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(SearchCarefully.MODID, "start_loop_sound"));

    public static final StreamCodec<RegistryFriendlyByteBuf, StartLoopSoundPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, packet) -> {},
                    buf -> new StartLoopSoundPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * 客户端处理：触发循环音效
     */
    public static void handle(StartLoopSoundPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            // 委托给客户端专用处理器，避免在专用服务端加载客户端类
            ClientSoundPacketHandler.handleStartLoopSound();
        });
    }
}
