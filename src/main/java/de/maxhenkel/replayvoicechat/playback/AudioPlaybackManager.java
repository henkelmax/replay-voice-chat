package de.maxhenkel.replayvoicechat.playback;

import de.maxhenkel.replayvoicechat.ReplayVoicechatPlugin;
import de.maxhenkel.replayvoicechat.net.EntitySoundPacket;
import de.maxhenkel.replayvoicechat.net.LocationalSoundPacket;
import de.maxhenkel.replayvoicechat.net.StaticSoundPacket;
import de.maxhenkel.voicechat.api.audiochannel.ClientAudioChannel;
import de.maxhenkel.voicechat.api.audiochannel.ClientEntityAudioChannel;
import de.maxhenkel.voicechat.api.audiochannel.ClientLocationalAudioChannel;
import de.maxhenkel.voicechat.api.audiochannel.ClientStaticAudioChannel;
import de.maxhenkel.voicechat.api.events.OpenALSoundEvent;
import org.lwjgl.openal.AL11;
import xyz.breadloaf.replaymodinterface.ReplayInterface;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AudioPlaybackManager {

    public static final AudioPlaybackManager INSTANCE = new AudioPlaybackManager();

    private final Map<UUID, ClientAudioChannel> audioChannels;

    public AudioPlaybackManager() {
        audioChannels = new HashMap<>();
    }

    @Nullable
    private <T extends ClientAudioChannel> T getAudioChannel(Class<T> channelClass, UUID uuid) {
        ClientAudioChannel clientAudioChannel = audioChannels.get(uuid);

        if (clientAudioChannel == null) {
            return null;
        }

        if (clientAudioChannel.getClass().isAssignableFrom(channelClass)) {
            return (T) clientAudioChannel;
        }
        return null;
    }

    public void onEntitySound(EntitySoundPacket packet) {
        ClientEntityAudioChannel channel = getAudioChannel(ClientEntityAudioChannel.class, packet.getId());
        if (channel == null) {
            channel = ReplayVoicechatPlugin.CLIENT_API.createEntityAudioChannel(packet.getId());
            audioChannels.put(packet.getId(), channel);
        }
        channel.play(packet.getRawAudio());
    }

    public void onLocationalSound(LocationalSoundPacket packet) {
        ClientLocationalAudioChannel channel = getAudioChannel(ClientLocationalAudioChannel.class, packet.getId());
        if (channel == null) {
            channel = ReplayVoicechatPlugin.CLIENT_API.createLocationalAudioChannel(packet.getId(), packet.getLocation());
            audioChannels.put(packet.getId(), channel);
        } else {
            channel.setLocation(packet.getLocation());
        }
        channel.play(packet.getRawAudio());
    }

    public void onStaticSound(StaticSoundPacket packet) {
        ClientStaticAudioChannel channel = getAudioChannel(ClientStaticAudioChannel.class, packet.getId());
        if (channel == null) {
            channel = ReplayVoicechatPlugin.CLIENT_API.createStaticAudioChannel(packet.getId());
            audioChannels.put(packet.getId(), channel);
        }
        channel.play(packet.getRawAudio());
    }

    public static void setPlaybackRate(OpenALSoundEvent event) {
        //TODO: need a better way of setting speed this does not handle very slow or very fast well at all
        // also maybe we can do this without pitch shifting audio? (have to write resample function?)
        if (ReplayInterface.INSTANCE.isInReplayEditor) {
            AL11.alSourcef(event.getSource(), AL11.AL_PITCH, (float) ReplayInterface.getCurrentSpeed());
        }
    }
}
