package de.maxhenkel.replayvoicechat.net;

import de.maxhenkel.replayvoicechat.ReplayVoicechat;
import de.maxhenkel.replayvoicechat.playback.AudioPlaybackManager;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public class StaticSoundPacket extends AbstractSoundPacket<StaticSoundPacket> {

    public static final CustomPacketPayload.Type<StaticSoundPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(ReplayVoicechat.MOD_ID, "static_sound"));


    public StaticSoundPacket(UUID id, short[] rawAudio) {
        super(id, rawAudio);
    }

    public StaticSoundPacket() {

    }

    @Override
    public void onPacket() {
        AudioPlaybackManager.INSTANCE.onStaticSound(this);
    }

    @Override
    public Type<StaticSoundPacket> type() {
        return TYPE;
    }
}
