package de.maxhenkel.replayvoicechat.net;

import de.maxhenkel.replayvoicechat.ReplayVoicechat;
import de.maxhenkel.replayvoicechat.ReplayVoicechatPlugin;
import de.maxhenkel.replayvoicechat.playback.AudioPlaybackManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public class EntitySoundPacket extends AbstractSoundPacket<EntitySoundPacket> {

    public static final CustomPacketPayload.Type<EntitySoundPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(ReplayVoicechat.MOD_ID, "entity_sound"));

    private boolean whispering;
    private float distance;

    public EntitySoundPacket(UUID id, short[] rawAudio, boolean whispering, float distance) {
        super(id, rawAudio);
        this.whispering = whispering;
        this.distance = distance;
    }

    public EntitySoundPacket() {

    }

    public boolean isWhispering() {
        return whispering;
    }

    public float getDistance() {
        return distance;
    }

    @Override
    public EntitySoundPacket fromBytes(FriendlyByteBuf buf) throws VersionCompatibilityException {
        super.fromBytes(buf);
        whispering = buf.readBoolean();
        if (version >= 1) {
            distance = buf.readFloat();
        } else {
            distance = (float) ReplayVoicechatPlugin.CLIENT_API.getVoiceChatDistance();
        }
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        super.toBytes(buf);
        buf.writeBoolean(whispering);
        buf.writeFloat(distance);
    }

    @Override
    public void onPacket() {
        AudioPlaybackManager.INSTANCE.onEntitySound(this);
    }

    @Override
    public Type<EntitySoundPacket> type() {
        return TYPE;
    }
}
