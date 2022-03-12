package de.maxhenkel.replayvoicechat.recording;

import de.maxhenkel.replayvoicechat.net.EntitySoundPacket;
import de.maxhenkel.replayvoicechat.net.LocationalSoundPacket;
import de.maxhenkel.replayvoicechat.net.Packet;
import de.maxhenkel.replayvoicechat.net.StaticSoundPacket;
import de.maxhenkel.voicechat.api.events.ClientReceiveSoundEvent;
import de.maxhenkel.voicechat.api.events.ClientSoundEvent;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import xyz.breadloaf.replaymodinterface.ReplayInterface;

import java.util.UUID;

public class VoicechatRecorder {

    private static final Minecraft MC = Minecraft.getInstance();

    public static void onEntitySound(ClientReceiveSoundEvent.EntitySound event) {
        send(new EntitySoundPacket(event.getId(), event.getRawAudio(), event.isWhispering()));
    }

    public static void onLocationalSound(ClientReceiveSoundEvent.LocationalSound event) {
        send(new LocationalSoundPacket(event.getId(), event.getRawAudio(), event.getPosition()));
    }

    public static void onStaticSound(ClientReceiveSoundEvent.StaticSound event) {
        send(new StaticSoundPacket(event.getId(), event.getRawAudio()));
    }

    // TODO check if player is in group
    public static void onSound(ClientSoundEvent event) {
        if (MC.player == null) {
            return;
        }
        UUID id = MC.getUser().getGameProfile().getId();
        short[] rawAudio = event.getRawAudio();
        if (rawAudio.length <= 0) {
            send(new EntitySoundPacket(id, null, event.isWhispering()));
        }
        send(new EntitySoundPacket(id, rawAudio, event.isWhispering()));
    }

    public static void send(Packet<?> packet) {
        if (!ReplayInterface.INSTANCE.isReplayModActive()) {
            return;
        }
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        packet.toBytes(buf);
        ReplayInterface.INSTANCE.sendFakePacket(packet.getIdentifier(), buf);
    }

}
