package de.maxhenkel.replayvoicechat.playback;

import de.maxhenkel.replayvoicechat.ReplayVoicechat;
import de.maxhenkel.replayvoicechat.net.*;
import de.maxhenkel.replayvoicechat.rendering.VoicechatVoiceRenderer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import xyz.breadloaf.replaymodinterface.ReplayInterface;

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

                    if (ReplayInterface.INSTANCE.isRendering) {
                        if (Minecraft.getInstance().cameraEntity != null) {
                            VoicechatVoiceRenderer.onRecordingPacket((AbstractSoundPacket<?>) packet);
                        }
                        return;
                    }

                    if (ReplayInterface.INSTANCE.skipping) {
                        return;
                    }
                    client.execute(packet::onPacket);
                } catch (VersionCompatibilityException e) {
                    ReplayVoicechat.LOGGER.warn("Failed to read packet: {}", e.getMessage());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
