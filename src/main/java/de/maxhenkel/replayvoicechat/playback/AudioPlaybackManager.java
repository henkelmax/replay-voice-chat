package de.maxhenkel.replayvoicechat.playback;

import de.maxhenkel.replayvoicechat.ReplayVoicechatPlugin;
import de.maxhenkel.replayvoicechat.mixin.ClientVoicechatAccessor;
import de.maxhenkel.replayvoicechat.net.EntitySoundPacket;
import de.maxhenkel.replayvoicechat.net.LocationalSoundPacket;
import de.maxhenkel.replayvoicechat.net.StaticSoundPacket;
import de.maxhenkel.voicechat.api.audiochannel.ClientLocationalAudioChannel;
import de.maxhenkel.voicechat.voice.client.ClientManager;
import de.maxhenkel.voicechat.voice.client.ClientVoicechatConnection;
import de.maxhenkel.voicechat.voice.client.InitializationData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AudioPlaybackManager {

    public static final AudioPlaybackManager INSTANCE = new AudioPlaybackManager();

    private Map<UUID, ClientLocationalAudioChannel> locationalAudioChannels;

    public AudioPlaybackManager() {
        locationalAudioChannels = new HashMap<>();
    }

    public void onEntitySound(EntitySoundPacket packet) {
        //TODO
    }

    public void onLocationalSound(LocationalSoundPacket packet) {
        System.out.println(packet.getRawAudio().length);
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
        //TODO
    }

}
