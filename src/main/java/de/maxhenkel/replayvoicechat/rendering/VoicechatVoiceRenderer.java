package de.maxhenkel.replayvoicechat.rendering;

import de.maxhenkel.replayvoicechat.ReplayVoicechat;
import de.maxhenkel.replayvoicechat.net.*;
import de.maxhenkel.voicechat.api.Position;
import de.maxhenkel.voicechat.voice.client.AudioRecorder;
import de.maxhenkel.voicechat.voice.client.InitializationData;
import de.maxhenkel.voicechat.voice.client.PositionalAudioUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import xyz.breadloaf.replaymodinterface.ReplayInterface;

import javax.annotation.Nullable;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class VoicechatVoiceRenderer extends Thread {
    private static VoicechatVoiceRenderer INSTANCE;
    private static InitializationData data;
    private static final Minecraft MC = Minecraft.getInstance();

    public static void onStartRendering(int initialTimestamp) {
        if (INSTANCE == null) {
            INSTANCE = new VoicechatVoiceRenderer(initialTimestamp);
            INSTANCE.startRecording();
        } else {
            ReplayVoicechat.LOGGER.warn("Started rendering when already rendering");
        }
    }

    public static void onStopRendering() {
        if (INSTANCE != null) {
            try {
                INSTANCE.stopAndWait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            INSTANCE = null;
        } else {
            throw new IllegalStateException("Stop called while stopped");
        }

    }

    public static void onRecordingPacket(ClientboundCustomPayloadPacket packet, int timestamp, Vec3 cameraLocation, float cameraYRot) {
        if (INSTANCE != null && INSTANCE.running && INSTANCE.initialTimestamp <= timestamp) {
            try {
                INSTANCE.packets.put(new PacketWrapper(packet, timestamp, cameraYRot, cameraLocation));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void onInitializationData(InitializationData data) {
        VoicechatVoiceRenderer.data = data;
    }

    private boolean running = true;
    private final LinkedBlockingQueue<PacketWrapper> packets;
    private AudioRecorder recorder;
    private final int initialTimestamp;

    private VoicechatVoiceRenderer(int initialTimestamp) {
        packets = new LinkedBlockingQueue<>();
        this.initialTimestamp = initialTimestamp;
        setName("ReplayVoiceChatVoiceRenderThread");
    }

    private void startRecording() {
        String filename = ReplayInterface.INSTANCE.videoRenderer.getRenderSettings().getOutputFile().getName().split("\\.")[0];
        Path path = ReplayInterface.INSTANCE.videoRenderer.getRenderSettings().getOutputFile().toPath().getParent().resolve(filename + "_audio");
        recorder = new AudioRecorder(path, initialTimestamp);
        start();
    }

    @Override
    public void run() {
        while (this.running || packets.size() > 0) {
            PacketWrapper packetWrapper = null;
            try {
                packetWrapper = packets.poll(250, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (packetWrapper != null) {
                AbstractSoundPacket<?> soundPacket;
                try {
                    if (packetWrapper.packet.getIdentifier().equals(LocationalSoundPacket.ID)) {
                        soundPacket = new LocationalSoundPacket();
                        soundPacket.fromBytes(packetWrapper.packet.getData());
                        onLocationalSoundPacket(packetWrapper, (LocationalSoundPacket) soundPacket);
                    } else if (packetWrapper.packet.getIdentifier().equals(EntitySoundPacket.ID)) {
                        soundPacket = new EntitySoundPacket();
                        soundPacket.fromBytes(packetWrapper.packet.getData());
                        onEntitySoundPacket(packetWrapper, (EntitySoundPacket) soundPacket);
                    } else if (packetWrapper.packet.getIdentifier().equals(StaticSoundPacket.ID)) {
                        soundPacket = new StaticSoundPacket();
                        soundPacket.fromBytes(packetWrapper.packet.getData());
                        onStaticSoundPacket(packetWrapper, (StaticSoundPacket) soundPacket);
                    }
                } catch (VersionCompatibilityException e) {
                    ReplayVoicechat.LOGGER.warn("Failed to read packet: {}", e.getMessage());
                }
            }
        }
        onStop();
    }

    private void onLocationalSoundPacket(PacketWrapper packetWrapper, LocationalSoundPacket locationalSoundPacket) {
        try {
            Position location = locationalSoundPacket.getLocation();
            recorder.appendChunk(locationalSoundPacket.getId(), packetWrapper.timestamp,
                    PositionalAudioUtils.convertToStereoForRecording(
                            data,
                            packetWrapper.cameraPos,
                            packetWrapper.yrot,
                            new Vec3(location.getX(), location.getY(), location.getZ()),
                            locationalSoundPacket.getRawAudio(),
                            1F
                    ));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void onEntitySoundPacket(PacketWrapper packetWrapper, EntitySoundPacket entitySoundPacket) {
        try {
            @Nullable Player player = MC.level.getPlayerByUUID(entitySoundPacket.getId());
            Vec3 pos = player.getEyePosition();

            float crouchMultiplayer = player.isCrouching() ? (float) data.getCrouchDistanceMultiplier() : 1F;
            float whisperMultiplayer = entitySoundPacket.isWhispering() ? (float) data.getWhisperDistanceMultiplier() : 1F;
            float multiplier = crouchMultiplayer * whisperMultiplayer;

            recorder.appendChunk(entitySoundPacket.getId(), packetWrapper.timestamp,
                    PositionalAudioUtils.convertToStereoForRecording(
                            data,
                            packetWrapper.cameraPos,
                            packetWrapper.yrot,
                            pos,
                            entitySoundPacket.getRawAudio(),
                            multiplier
                    ));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void onStaticSoundPacket(PacketWrapper packetWrapper, StaticSoundPacket staticSoundPacket) {
        try {
            recorder.appendChunk(staticSoundPacket.getId(), packetWrapper.timestamp, PositionalAudioUtils.convertToStereo(staticSoundPacket.getRawAudio()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopAndWait() throws InterruptedException {
        ReplayVoicechat.LOGGER.info("Stopping");
        running = false;
        join();
    }

    private void onStop() {
        try {
            recorder.convert((progress) -> {
                ReplayVoicechat.LOGGER.info("Saving voicechat: " + progress * 100F + "%");
            });
        } catch (UnsupportedAudioFileException | IOException e) {
            e.printStackTrace();
        }
        recorder.close();
    }

    public static class PacketWrapper {
        public ClientboundCustomPayloadPacket packet;
        public int timestamp;
        public float yrot;
        public Vec3 cameraPos;

        public PacketWrapper(ClientboundCustomPayloadPacket packet, int timestamp, float yrot, Vec3 cameraPos) {
            this.packet = packet;
            this.timestamp = timestamp;
            this.cameraPos = cameraPos;
            this.yrot = yrot;
        }
    }
}
