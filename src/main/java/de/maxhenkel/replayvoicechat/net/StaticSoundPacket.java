package de.maxhenkel.replayvoicechat.net;

import de.maxhenkel.replayvoicechat.ReplayVoicechat;
import de.maxhenkel.replayvoicechat.playback.AudioPlaybackManager;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public class StaticSoundPacket extends AbstractSoundPacket<StaticSoundPacket> {

    public static ResourceLocation ID = new ResourceLocation(ReplayVoicechat.MOD_ID, "static_sound");

    public StaticSoundPacket(UUID id, short[] rawAudio) {
        super(id, rawAudio);
    }

    public StaticSoundPacket() {

    }

    @Override
    public ResourceLocation getIdentifier() {
        return ID;
    }

    @Override
    public void onPacket() {
        AudioPlaybackManager.INSTANCE.onStaticSound(this);
    }

}
