package org.yanbwe.searchcarefully.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.yanbwe.searchcarefully.Searchcarefully;
import org.yanbwe.searchcarefully.manager.SearchManager;

public class SearchProgressPacket implements CustomPacketPayload {
    public static final Type<SearchProgressPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Searchcarefully.MODID, "search_progress"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SearchProgressPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            SearchProgressPacket::getSlotIndex,
            ByteBufCodecs.BOOL,
            SearchProgressPacket::isHotbarSlot,
            SearchProgressPacket::new
    );

    private final int slotIndex;
    private final boolean isHotbarSlot;

    public SearchProgressPacket(int slotIndex, boolean isHotbarSlot) {
        this.slotIndex = slotIndex;
        this.isHotbarSlot = isHotbarSlot;
    }

    public int getSlotIndex() {
        return slotIndex;
    }

    public boolean isHotbarSlot() {
        return isHotbarSlot;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (player != null) {
                if (isHotbarSlot) {
                    SearchManager.handleHotbarSearchProgress(player, slotIndex);
                } else {
                    SearchManager.handleSearchProgress(player, slotIndex);
                }
            }
        });
    }

    @Override
    public Type<SearchProgressPacket> type() {
        return TYPE;
    }
}