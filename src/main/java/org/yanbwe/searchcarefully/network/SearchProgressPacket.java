package org.yanbwe.searchcarefully.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SearchProgressPacket {
    private final int slotIndex;

    public SearchProgressPacket(int slotIndex) {
        this.slotIndex = slotIndex;
    }

    public SearchProgressPacket(FriendlyByteBuf buf) {
        this.slotIndex = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(slotIndex);
    }

    public boolean handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            Player player = context.getSender(); // 发送数据包的玩家
            if (player != null) {
                // 在服务器端处理搜索进度，直接传入槽位索引
                org.yanbwe.searchcarefully.Searchcarefully.handleSearchProgress(player, slotIndex);
            }
        });
        context.setPacketHandled(true);
        return true;
    }

    public int getSlotIndex() {
        return slotIndex;
    }
}