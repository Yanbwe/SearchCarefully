package org.yanbwe.searchcarefully.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import org.yanbwe.searchcarefully.client.SearchSoundManager;

import java.util.function.Supplier;

/**
 * 服务端→客户端数据包。
 * 触发客户端开始播放搜索进度循环音效。
 */
public class StartLoopSoundPacket {

    public StartLoopSoundPacket() {}

    public StartLoopSoundPacket(FriendlyByteBuf buf) {}

    public void toBytes(FriendlyByteBuf buf) {}

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                Minecraft mc = Minecraft.getInstance();
                if (mc.player != null) {
                    SearchSoundManager.startSearchLoopSound(mc.player);
                }
            });
        });
        ctx.get().setPacketHandled(true);
        return true;
    }
}
