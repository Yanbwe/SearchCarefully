package org.yanbwe.searchcarefully.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SearchProgressPacket {
    private final int containerType;
    private final int slotIndex;

    public SearchProgressPacket(int containerType, int slotIndex) {
        this.containerType = containerType;
        this.slotIndex = slotIndex;
    }

    public SearchProgressPacket(FriendlyByteBuf buf) {
        this.containerType = buf.readInt();
        this.slotIndex = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(containerType);
        buf.writeInt(slotIndex);
    }

    public boolean handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            Player player = context.getSender(); // 发送数据包的玩家
            if (player != null) {
                // 在服务器端处理搜索进度
                org.yanbwe.searchcarefully.Searchcarefully.handleSearchProgress(player, containerType, slotIndex);
            }
        });
        context.setPacketHandled(true);
        return true;
    }

    public int getContainerType() {
        return containerType;
    }

    public int getSlotIndex() {
        return slotIndex;
    }
}