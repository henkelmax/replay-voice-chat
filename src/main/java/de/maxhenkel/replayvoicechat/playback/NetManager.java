package de.maxhenkel.replayvoicechat.playback;

import de.maxhenkel.replayvoicechat.net.EntitySoundPacket;
import de.maxhenkel.replayvoicechat.net.LocationalSoundPacket;
import de.maxhenkel.replayvoicechat.net.Packet;
import de.maxhenkel.replayvoicechat.net.StaticSoundPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class NetManager {


    public static void init() {
        registerPacket(EntitySoundPacket.class);
        registerPacket(LocationalSoundPacket.class);
        registerPacket(StaticSoundPacket.class);
    }

    public static <T extends Packet<?>> void registerPacket(Class<T> packetClass) {
        try {
            T dummyPacket = packetClass.getDeclaredConstructor().newInstance();
            ClientPlayNetworking.registerGlobalReceiver(dummyPacket.getIdentifier(), (client, handler, buf, responseSender) -> {
                try {
                    T packet = packetClass.getDeclaredConstructor().newInstance();
                    packet.fromBytes(buf);
                    client.execute(packet::onPacket);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}