package de.maxhenkel.replayvoicechat.playback;

import de.maxhenkel.replayvoicechat.ReplayVoicechat;
import de.maxhenkel.replayvoicechat.net.*;
import de.maxhenkel.replayvoicechat.rendering.VoicechatVoiceRenderer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
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

            StreamCodec<RegistryFriendlyByteBuf, T> codec = new StreamCodec<>() {

                @Override
                public void encode(RegistryFriendlyByteBuf buf, T packet) {
                    packet.toBytes(buf);
                }

                @Override
                public T decode(RegistryFriendlyByteBuf buf) {
                    try {
                        T packet = packetClass.getDeclaredConstructor().newInstance();
                        packet.fromBytes(buf);
                        return packet;
                    } catch (VersionCompatibilityException e) {
                        ReplayVoicechat.LOGGER.warn("Failed to read packet: {}", e.getMessage());
                        return null;
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            };
            CustomPacketPayload.Type<T> type = (CustomPacketPayload.Type<T>) dummyPacket.type();
            PayloadTypeRegistry.playS2C().register(type, codec);
            ClientPlayNetworking.registerGlobalReceiver(type, (payload, context) -> {
                if (payload == null) {
                    return;
                }
                try {
                    if (ReplayInterface.INSTANCE.isRendering) {
                        if (Minecraft.getInstance().cameraEntity != null) {
                            VoicechatVoiceRenderer.onRecordingPacket((AbstractSoundPacket<?>) payload);
                        }
                        return;
                    }

                    if (ReplayInterface.INSTANCE.skipping) {
                        return;
                    }
                    context.client().execute(payload::onPacket);
                } catch (Exception e) {
                    ReplayVoicechat.LOGGER.error("Failed to process packet", e);
                }
            });
        } catch (Exception e) {
            ReplayVoicechat.LOGGER.error("Failed to register packet", e);
        }
    }

}
