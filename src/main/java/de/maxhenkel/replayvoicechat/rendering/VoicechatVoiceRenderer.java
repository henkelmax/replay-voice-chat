package de.maxhenkel.replayvoicechat.rendering;

import de.maxhenkel.replayvoicechat.ReplayVoicechat;
import de.maxhenkel.replayvoicechat.net.AbstractSoundPacket;
import de.maxhenkel.replayvoicechat.net.EntitySoundPacket;
import de.maxhenkel.replayvoicechat.net.LocationalSoundPacket;
import de.maxhenkel.replayvoicechat.net.StaticSoundPacket;
import de.maxhenkel.voicechat.api.Position;
import de.maxhenkel.voicechat.voice.client.AudioRecorder;
import de.maxhenkel.voicechat.voice.client.InitializationData;
import de.maxhenkel.voicechat.voice.client.PositionalAudioUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.world.phys.Vec3;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class VoicechatVoiceRenderer extends Thread{
    private static VoicechatVoiceRenderer INSTANCE;
    private static InitializationData data;

    public static void onStartRendering() {
        if (INSTANCE == null) {
            INSTANCE = new VoicechatVoiceRenderer();
            INSTANCE.startRecording();
        } else {
            throw new IllegalStateException("Start called while started");
        }
    }

    public static void onStopRecording() {
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
        if (INSTANCE != null && INSTANCE.running) {
            try {
                INSTANCE.packets.put(new PacketWrapper(packet,timestamp,cameraYRot,cameraLocation));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            throw new IllegalStateException("packet recorded when recording stopped/not started");
        }
    }

    public static void onInitializationData(InitializationData data) {
        VoicechatVoiceRenderer.data = data;
    }

    private boolean running = true;
    private LinkedBlockingQueue<PacketWrapper> packets;
    private AudioRecorder recorder;
    private int initialTimestamp;

    private VoicechatVoiceRenderer(){
        packets = new LinkedBlockingQueue<>();
        setName("ReplayVoiceChatVoiceRenderThread");
    }

    private void startRecording() {
        recorder = new AudioRecorder(Minecraft.getInstance().gameDirectory.toPath().resolve("test_recording"),0L);
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
                AbstractSoundPacket soundPacket;
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
            }
        }
        onStop();
    }

    private void onLocationalSoundPacket(PacketWrapper packetWrapper, LocationalSoundPacket locationalSoundPacket) {
        Position location = locationalSoundPacket.getLocation();
        short[] rawaudio = locationalSoundPacket.getRawAudio();
        UUID uuid = locationalSoundPacket.getId();
        try {
            recorder.appendChunk(uuid, packetWrapper.timestamp, monoTo3DStereo(
                    rawaudio,
                    new Vec3(
                            location.getX(),
                            location.getY(),
                            location.getZ()
                    ),
                    packetWrapper.cameraPos,
                    packetWrapper.yrot));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void onEntitySoundPacket(PacketWrapper packetWrapper, EntitySoundPacket entitySoundPacket) {

    }
    private void onStaticSoundPacket(PacketWrapper packetWrapper, StaticSoundPacket staticSoundPacket) {

    }

    private short[] monoTo3DStereo(short[] rawaudio,Vec3 position, Vec3 cameraPos, float yRot) {
        //ReplayVoicechat.LOGGER.info(cameraPos.toString() + " :: "+ yRot);
        return PositionalAudioUtils.convertToStereoForRecording(data,cameraPos,yRot,position,rawaudio,1.0f);
    }

    private void stopAndWait() throws InterruptedException {
        ReplayVoicechat.LOGGER.info("Stopping");
        running = false;
        join();
    }

    private void onStop() {

        try {
            recorder.convert((progress) -> {
                ReplayVoicechat.LOGGER.info("Saving voicechat: "+String.valueOf(progress*100.0F)+"%");
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
