package de.maxhenkel.replayvoicechat.rendering;

import com.replaymod.render.rendering.VideoRenderer;
import de.maxhenkel.replayvoicechat.ReplayVoicechat;
import de.maxhenkel.replayvoicechat.net.AbstractSoundPacket;
import de.maxhenkel.replayvoicechat.net.EntitySoundPacket;
import de.maxhenkel.replayvoicechat.net.LocationalSoundPacket;
import de.maxhenkel.replayvoicechat.net.StaticSoundPacket;
import de.maxhenkel.sonic.Sonic;
import de.maxhenkel.voicechat.api.Position;
import de.maxhenkel.voicechat.voice.client.*;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import xyz.breadloaf.replaymodinterface.ReplayInterface;

import javax.annotation.Nullable;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class VoicechatVoiceRenderer extends Thread {
    private static VoicechatVoiceRenderer INSTANCE;
    private static InitializationData data;
    private static final Minecraft MC = Minecraft.getInstance();

    public static void onStartRendering() {
        if (INSTANCE == null) {
            INSTANCE = new VoicechatVoiceRenderer();
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

    public static void onRecordingPacket(AbstractSoundPacket<?> packet) {
        if (INSTANCE != null && INSTANCE.running) {
            try {
                Vec3 cameraLocation = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
                float yrot = Minecraft.getInstance().gameRenderer.getMainCamera().getYRot();

                ClientVoicechat client = ClientManager.getClient();
                if (client == null) {
                    return;
                }
                client.getTalkCache().updateTalking(packet.getId(), packet instanceof EntitySoundPacket p && p.isWhispering());
                if (ReplayInterface.INSTANCE.isPlayerHidden(packet.getId())) {
                    return;
                }
                if (ReplayInterface.INSTANCE.videoRenderer != null) {
                    INSTANCE.packets.put(new PacketWrapper(packet, ReplayInterface.INSTANCE.videoRenderer.getVideoTime(), yrot, cameraLocation, ReplayInterface.getCurrentSpeed()));
                }
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
    private final HashMap<UUID, Sonic> sonicMap;

    private VoicechatVoiceRenderer() {
        packets = new LinkedBlockingQueue<>();
        sonicMap = new HashMap<>();
        setName("ReplayVoiceChatVoiceRenderThread");
    }

    private void startRecording() {
        String videoFileName = ReplayInterface.INSTANCE.videoRenderer.getRenderSettings().getOutputFile().getName();
        String filename = videoFileName.substring(0, videoFileName.lastIndexOf('.'));
        Path path = ReplayInterface.INSTANCE.videoRenderer.getRenderSettings().getOutputFile().toPath().getParent().resolve(filename + "_audio");
        recorder = new AudioRecorder(path, 0L);
        ClientVoicechat client = ClientManager.getClient();
        if (client != null) {
            client.getTalkCache().setTimestampSupplier(() -> {
                VideoRenderer videoRenderer = ReplayInterface.INSTANCE.videoRenderer;
                if (videoRenderer != null) {
                    return (long) videoRenderer.getVideoTime();
                }
                return System.currentTimeMillis();
            });
        }
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

            if (packetWrapper == null) {
                continue;
            }

            if (packetWrapper.packet instanceof LocationalSoundPacket packet) {
                onLocationalSoundPacket(packetWrapper, packet);
            } else if (packetWrapper.packet instanceof EntitySoundPacket packet) {
                onEntitySoundPacket(packetWrapper, packet);
            } else if (packetWrapper.packet instanceof StaticSoundPacket packet) {
                onStaticSoundPacket(packetWrapper, packet);
            }
        }
        onStop();
    }

    private void onLocationalSoundPacket(PacketWrapper packetWrapper, LocationalSoundPacket locationalSoundPacket) {
        try {
            Position location = locationalSoundPacket.getLocation();
            recorder.appendChunk(locationalSoundPacket.getId(), packetWrapper.timestamp,
                    PositionalAudioUtils.convertToStereoForRecording(
                            locationalSoundPacket.getDistance(),
                            packetWrapper.cameraPos,
                            packetWrapper.yrot,
                            new Vec3(location.getX(), location.getY(), location.getZ()),
                            setSpeed(locationalSoundPacket.getId(), locationalSoundPacket.getRawAudio(), packetWrapper.speed),
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
                            entitySoundPacket.getDistance(),
                            packetWrapper.cameraPos,
                            packetWrapper.yrot,
                            pos,
                            setSpeed(entitySoundPacket.getId(), entitySoundPacket.getRawAudio(), packetWrapper.speed),
                            multiplier
                    ));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void onStaticSoundPacket(PacketWrapper packetWrapper, StaticSoundPacket staticSoundPacket) {
        try {
            recorder.appendChunk(staticSoundPacket.getId(), packetWrapper.timestamp, PositionalAudioUtils.convertToStereo(setSpeed(staticSoundPacket.getId(), staticSoundPacket.getRawAudio(), packetWrapper.speed)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public short[] setSpeed(UUID channelId, short[] audio, double speed) {
        if (speed >= 0.99D && speed <= 1.01D) {
            return audio;
        }
        Sonic stream;
        if (!sonicMap.containsKey(channelId)) {
            stream = new Sonic(SoundManager.SAMPLE_RATE, 1);
            sonicMap.put(channelId, stream);
        } else {
            stream = sonicMap.get(channelId);
        }
        stream.setSpeed((float) speed);
        stream.setPitch((float) speed);
        stream.writeShortToStream(audio, audio.length);
        stream.flushStream();
        int numSamples = stream.samplesAvailable();
        short[] outSamples = new short[numSamples];
        stream.readShortFromStream(outSamples, numSamples);
        return outSamples;
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
        ReplayVoicechat.LOGGER.info("Voicechat data saved!");
        recorder.close();
    }

    public static class PacketWrapper {
        public AbstractSoundPacket<?> packet;
        public int timestamp;
        public float yrot;
        public Vec3 cameraPos;
        public double speed;

        public PacketWrapper(AbstractSoundPacket<?> packet, int timestamp, float yrot, Vec3 cameraPos, double speed) {
            this.packet = packet;
            this.timestamp = timestamp;
            this.cameraPos = cameraPos;
            this.yrot = yrot;
            this.speed = speed;
        }
    }
}
