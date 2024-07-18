package de.maxhenkel.replayvoicechat.recording;

import de.maxhenkel.replayvoicechat.ReplayVoicechatPlugin;
import de.maxhenkel.replayvoicechat.net.EntitySoundPacket;
import de.maxhenkel.replayvoicechat.net.LocationalSoundPacket;
import de.maxhenkel.replayvoicechat.net.Packet;
import de.maxhenkel.replayvoicechat.net.StaticSoundPacket;
import de.maxhenkel.voicechat.api.events.ClientReceiveSoundEvent;
import de.maxhenkel.voicechat.api.events.ClientSoundEvent;
import de.maxhenkel.voicechat.voice.client.ClientManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import xyz.breadloaf.replaymodinterface.ReplayInterface;

import java.util.UUID;

public class VoicechatRecorder {

    private static final Minecraft MC = Minecraft.getInstance();

    public static void onEntitySound(ClientReceiveSoundEvent.EntitySound event) {
        send(new EntitySoundPacket(event.getId(), event.getRawAudio(), event.isWhispering(), event.getDistance()));
    }

    public static void onLocationalSound(ClientReceiveSoundEvent.LocationalSound event) {
        send(new LocationalSoundPacket(event.getId(), event.getRawAudio(), event.getPosition(), event.getDistance()));
    }

    public static void onStaticSound(ClientReceiveSoundEvent.StaticSound event) {
        send(new StaticSoundPacket(event.getId(), event.getRawAudio()));
    }

    public static void onSound(ClientSoundEvent event) {
        if (MC.player == null) {
            return;
        }
        UUID id = ClientManager.getPlayerStateManager().getOwnID();
        short[] rawAudio = event.getRawAudio();

        if (ReplayVoicechatPlugin.CLIENT_API.getGroup() != null) {
            send(new StaticSoundPacket(id, rawAudio));
        } else {
            send(new EntitySoundPacket(id, rawAudio, event.isWhispering(), (float) ReplayVoicechatPlugin.CLIENT_API.getVoiceChatDistance()));
        }
    }

    public static void send(Packet<?> packet) {
        if (!ReplayInterface.INSTANCE.isReplayModActive()) {
            return;
        }
        ReplayInterface.INSTANCE.sendFakePacket(new ClientboundCustomPayloadPacket(packet));
    }

}
