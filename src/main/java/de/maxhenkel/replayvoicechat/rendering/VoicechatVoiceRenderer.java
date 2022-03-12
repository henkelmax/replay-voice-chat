package de.maxhenkel.replayvoicechat.rendering;

import com.replaymod.lib.org.blender.dna.Link;
import de.maxhenkel.replayvoicechat.ReplayVoicechat;
import de.maxhenkel.replayvoicechat.net.EntitySoundPacket;
import de.maxhenkel.replayvoicechat.net.LocationalSoundPacket;
import de.maxhenkel.replayvoicechat.net.StaticSoundPacket;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class VoicechatVoiceRenderer extends Thread{
    private static VoicechatVoiceRenderer INSTANCE;

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

    public static void onRecordingPacket(ClientboundCustomPayloadPacket packet, int timestamp) {
        if (INSTANCE != null && INSTANCE.running) {
            try {
                INSTANCE.packets.put(new PacketWrapper(packet,timestamp));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            throw new IllegalStateException("packet recorded when recording stopped/not started");
        }
    }

    private boolean running = true;
    private LinkedBlockingQueue<PacketWrapper> packets;

    private VoicechatVoiceRenderer(){
        packets = new LinkedBlockingQueue<>();
        setName("ReplayVoiceChatVoiceRenderThread");
    }

    private void startRecording() {
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
                if (packetWrapper.packet.getIdentifier().equals(LocationalSoundPacket.ID)) {
                    onLocationalSoundPacket(packetWrapper);
                } else if (packetWrapper.packet.getIdentifier().equals(EntitySoundPacket.ID)) {
                    onEntitySoundPacket(packetWrapper);
                } else if (packetWrapper.packet.getIdentifier().equals(StaticSoundPacket.ID)) {
                    onStaticSoundPacket(packetWrapper);
                }
            }
        }
    }

    private void onLocationalSoundPacket(PacketWrapper packetWrapper) {
        ReplayVoicechat.LOGGER.info("recording locational sound packet");
    }
    private void onEntitySoundPacket(PacketWrapper packetWrapper) {

    }
    private void onStaticSoundPacket(PacketWrapper packetWrapper) {

    }

    private void stopAndWait() throws InterruptedException {
        running = false;
        join();
    }


    public static class PacketWrapper {
        public ClientboundCustomPayloadPacket packet;
        public int timestamp;
        public PacketWrapper(ClientboundCustomPayloadPacket packet, int timestamp) {
            this.packet = packet;
            this.timestamp = timestamp;
        }
    }
}
