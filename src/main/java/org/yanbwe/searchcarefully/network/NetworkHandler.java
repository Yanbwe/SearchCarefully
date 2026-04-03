package org.yanbwe.searchcarefully.network;

import net.neoforged.neoforge.network.PacketDistributor;

public class NetworkHandler {
    public static void sendToServer(SearchProgressPacket packet) {
        PacketDistributor.sendToServer(packet);
    }
}