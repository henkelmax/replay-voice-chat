package de.maxhenkel.replayvoicechat.playback;

import de.maxhenkel.replayvoicechat.ReplayVoicechatPlugin;
import de.maxhenkel.replayvoicechat.net.EntitySoundPacket;
import de.maxhenkel.replayvoicechat.net.LocationalSoundPacket;
import de.maxhenkel.replayvoicechat.net.StaticSoundPacket;
import de.maxhenkel.voicechat.api.audiochannel.ClientEntityAudioChannel;
import de.maxhenkel.voicechat.api.audiochannel.ClientLocationalAudioChannel;
import de.maxhenkel.voicechat.api.audiochannel.ClientStaticAudioChannel;
import de.maxhenkel.voicechat.api.events.OpenALSoundEvent;
import org.lwjgl.openal.AL11;
import xyz.breadloaf.replaymodinterface.ReplayInterface;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AudioPlaybackManager {

    public static final AudioPlaybackManager INSTANCE = new AudioPlaybackManager();

    private final Map<UUID, ClientLocationalAudioChannel> locationalAudioChannels;
    private final Map<UUID, ClientEntityAudioChannel> entityAudioChannels;
    private final Map<UUID, ClientStaticAudioChannel> staticAudioChannels;

    public AudioPlaybackManager() {
        locationalAudioChannels = new HashMap<>();
        entityAudioChannels = new HashMap<>();
        staticAudioChannels = new HashMap<>();
    }

    //TODO close audio channel from other list if uuid switches to other type
    public void onEntitySound(EntitySoundPacket packet) {

        ClientEntityAudioChannel channel = entityAudioChannels.get(packet.getId());
        if (channel == null) {
            channel = ReplayVoicechatPlugin.CLIENT_API.createEntityAudioChannel(packet.getId());
            entityAudioChannels.put(packet.getId(), channel);
        }
        channel.play(packet.getRawAudio());
    }

    public void onLocationalSound(LocationalSoundPacket packet) {

        ClientLocationalAudioChannel channel = locationalAudioChannels.get(packet.getId());
        if (channel == null) {
            channel = ReplayVoicechatPlugin.CLIENT_API.createLocationalAudioChannel(packet.getId(), packet.getLocation());
            locationalAudioChannels.put(packet.getId(), channel);
        } else {
            channel.setLocation(packet.getLocation());
        }
        channel.play(packet.getRawAudio());
    }

    public void onStaticSound(StaticSoundPacket packet) {

        ClientStaticAudioChannel channel = staticAudioChannels.get(packet.getId());
        if (channel == null) {
            channel = ReplayVoicechatPlugin.CLIENT_API.createStaticAudioChannel(packet.getId());
        }
        channel.play(packet.getRawAudio());
    }

    public static void setPlaybackRate(OpenALSoundEvent event) {
        //TODO: need a better way of setting speed this does not handle very slow or very fast well at all
        // also maybe we can do this without pitch shifting audio? (have to write resample function?)
        if (ReplayInterface.INSTANCE.isInReplayEditor) {
            AL11.alSourcef(event.getSource(), AL11.AL_PITCH, (float) ReplayInterface.INSTANCE.replayHandler.getReplaySender().getReplaySpeed());
        }
    }
}
