package de.maxhenkel.replayvoicechat;

import de.maxhenkel.replayvoicechat.playback.AudioPlaybackManager;
import de.maxhenkel.replayvoicechat.recording.VoicechatRecorder;
import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatClientApi;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.events.*;
import de.maxhenkel.voicechat.plugins.impl.VoicechatClientApiImpl;

public class ReplayVoicechatPlugin implements VoicechatPlugin {

    public static VoicechatClientApi CLIENT_API = VoicechatClientApiImpl.instance();

    @Override
    public String getPluginId() {
        return ReplayVoicechat.MOD_ID;
    }

    @Override
    public void initialize(VoicechatApi api) {

    }

    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(ClientReceiveSoundEvent.EntitySound.class, VoicechatRecorder::onEntitySound);
        registration.registerEvent(ClientReceiveSoundEvent.LocationalSound.class, VoicechatRecorder::onLocationalSound);
        registration.registerEvent(ClientReceiveSoundEvent.StaticSound.class, VoicechatRecorder::onStaticSound);
        registration.registerEvent(ClientSoundEvent.class, VoicechatRecorder::onSound);
        registration.registerEvent(OpenALSoundEvent.class, AudioPlaybackManager::setPlaybackRate);
    }

}
