package de.maxhenkel.replayvoicechat.net;

import de.maxhenkel.replayvoicechat.ReplayVoicechat;
import de.maxhenkel.replayvoicechat.ReplayVoicechatPlugin;
import de.maxhenkel.replayvoicechat.playback.AudioPlaybackManager;
import de.maxhenkel.voicechat.api.Position;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public class LocationalSoundPacket extends AbstractSoundPacket<LocationalSoundPacket> {

    public static ResourceLocation ID = new ResourceLocation(ReplayVoicechat.MOD_ID, "locational_sound");

    private Position location;

    public LocationalSoundPacket(UUID id, short[] rawAudio, Position location) {
        super(id, rawAudio);
        this.location = location;
    }

    public LocationalSoundPacket() {

    }

    public Position getLocation() {
        return location;
    }

    @Override
    public ResourceLocation getIdentifier() {
        return ID;
    }

    @Override
    public LocationalSoundPacket fromBytes(FriendlyByteBuf buf) throws VersionCompatibilityException {
        super.fromBytes(buf);
        location = ReplayVoicechatPlugin.CLIENT_API.createPosition(buf.readDouble(), buf.readDouble(), buf.readDouble());
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        super.toBytes(buf);
        buf.writeDouble(location.getX());
        buf.writeDouble(location.getY());
        buf.writeDouble(location.getZ());
    }

    @Override
    public void onPacket() {
        AudioPlaybackManager.INSTANCE.onLocationalSound(this);
    }

}
