package org.yanbwe.searchcarefully.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("searchcarefully", "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    public static void registerMessages() {
        INSTANCE.registerMessage(
                packetId++,
                SearchProgressPacket.class,
                SearchProgressPacket::toBytes,
                SearchProgressPacket::new,
                SearchProgressPacket::handle
        );
    }
}