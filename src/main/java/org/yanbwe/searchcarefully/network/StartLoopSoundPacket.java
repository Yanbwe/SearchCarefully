package org.yanbwe.searchcarefully.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import org.yanbwe.searchcarefully.client.ClientSoundPacketHandler;

import java.util.function.Supplier;

/**
 * 服务端→客户端数据包。
 * 触发客户端开始播放搜索进度循环音效。
 * <p>
 * 注意：此类绝对不能直接引用任何客户端专用类（如 {@code Minecraft}、{@code LocalPlayer}），
 * 否则在专用服务端（DEDICATED_SERVER）启动时会因 Forge dist 类加载检查而崩溃。
 * 客户端逻辑通过 {@link ClientSoundPacketHandler} 延迟加载执行。
 * </p>
 */
public class StartLoopSoundPacket {

    public StartLoopSoundPacket() {}

    public StartLoopSoundPacket(FriendlyByteBuf buf) {}

    public void toBytes(FriendlyByteBuf buf) {}

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ClientSoundPacketHandler::handleStartLoopSound);
        });
        ctx.get().setPacketHandled(true);
        return true;
    }
}
