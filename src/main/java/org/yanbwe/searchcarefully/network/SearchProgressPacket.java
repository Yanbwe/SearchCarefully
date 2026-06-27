package org.yanbwe.searchcarefully.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.yanbwe.searchcarefully.SearchCarefully;
import org.yanbwe.searchcarefully.manager.SearchManager;

/**
 * 客户端→服务端：搜索进度数据包
 * 客户端每tick发送当前正在搜索的槽位信息
 */
public record SearchProgressPacket(int slotIndex, boolean isHotbarSlot) implements CustomPacketPayload {

    public static final Type<SearchProgressPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(SearchCarefully.MODID, "search_progress"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SearchProgressPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, SearchProgressPacket::slotIndex,
                    ByteBufCodecs.BOOL, SearchProgressPacket::isHotbarSlot,
                    SearchProgressPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * 服务端处理：根据槽位类型调用搜索管理器
     */
    public static void handle(SearchProgressPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (player != null) {
                if (packet.isHotbarSlot) {
                    SearchManager.handleHotbarSearchProgress(player, packet.slotIndex);
                } else {
                    SearchManager.handleSearchProgress(player, packet.slotIndex);
                }
            }
        });
    }
}
