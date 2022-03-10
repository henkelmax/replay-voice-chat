package de.maxhenkel.replayvoicechat;

import de.maxhenkel.replayvoicechat.recording.VoicechatRecorder;
import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.events.ClientReceiveSoundEvent;
import de.maxhenkel.voicechat.api.events.ClientSoundEvent;
import de.maxhenkel.voicechat.api.events.EventRegistration;

public class ReplayVoicechatPlugin implements VoicechatPlugin {

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
        registration.registerEvent(ClientSoundEvent.class, VoicechatRecorder::onSound);
    }

}
