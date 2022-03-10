package de.maxhenkel.replayvoicechat.recording;

import de.maxhenkel.replayvoicechat.ReplayVoicechat;
import de.maxhenkel.voicechat.api.events.ClientReceiveSoundEvent;
import de.maxhenkel.voicechat.api.events.ClientSoundEvent;

public class VoicechatRecorder {

    public VoicechatRecorder() {

    }

    public static void onEntitySound(ClientReceiveSoundEvent.EntitySound event) {
        ReplayVoicechat.LOGGER.info("Test: {}", (event.getRawAudio() != null ? event.getRawAudio().length : 0));
    }

    public static void onSound(ClientSoundEvent event) {

    }

}
