package de.maxhenkel.replayvoicechat.net;

import de.maxhenkel.replayvoicechat.ReplayVoicechat;
import de.maxhenkel.replayvoicechat.playback.AudioPlaybackManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public class EntitySoundPacket extends AbstractSoundPacket<EntitySoundPacket> {

    public static ResourceLocation ID = new ResourceLocation(ReplayVoicechat.MOD_ID, "entity_sound");

    private boolean whispering;

    public EntitySoundPacket(UUID id, short[] rawAudio, boolean whispering) {
        super(id, rawAudio);
        this.whispering = whispering;
    }

    public EntitySoundPacket() {

    }

    @Override
    public ResourceLocation getIdentifier() {
        return ID;
    }

    public boolean isWhispering() {
        return whispering;
    }

    @Override
    public EntitySoundPacket fromBytes(FriendlyByteBuf buf) {
        super.fromBytes(buf);
        whispering = buf.readBoolean();
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        super.toBytes(buf);
        buf.writeBoolean(whispering);
    }

    @Override
    public void onPacket() {
        AudioPlaybackManager.INSTANCE.onEntitySound(this);
    }

}
