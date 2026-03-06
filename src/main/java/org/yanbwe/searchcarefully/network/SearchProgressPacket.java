package org.yanbwe.searchcarefully.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SearchProgressPacket {
    private final int slotIndex;
    private final boolean isHotbarSlot; // true 表示热键栏槽位，false 表示容器槽位

    public SearchProgressPacket(int slotIndex, boolean isHotbarSlot) {
        this.slotIndex = slotIndex;
        this.isHotbarSlot = isHotbarSlot;
    }

    public SearchProgressPacket(FriendlyByteBuf buf) {
        this.slotIndex = buf.readInt();
        this.isHotbarSlot = buf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(slotIndex);
        buf.writeBoolean(isHotbarSlot);
    }

    public boolean handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            Player player = context.getSender(); // 发送数据包的玩家
            if (player != null) {
                // 在服务器端处理搜索进度，根据是否为热键栏槽位调用不同方法
                if (isHotbarSlot) {
                    org.yanbwe.searchcarefully.Searchcarefully.handleHotbarSearchProgress(player, slotIndex);
                } else {
                    org.yanbwe.searchcarefully.Searchcarefully.handleSearchProgress(player, slotIndex);
                }
            }
        });
        context.setPacketHandled(true);
        return true;
    }

    public int getSlotIndex() {
        return slotIndex;
    }
}