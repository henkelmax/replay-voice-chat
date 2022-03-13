package de.maxhenkel.replayvoicechat.net;

import de.maxhenkel.replayvoicechat.Utils;
import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;

public abstract class AbstractSoundPacket<T extends Packet<T>> implements Packet<T> {

    public static final short CURRENT_VERSION = 0;

    private short version;
    private UUID id;
    private short[] rawAudio;

    public AbstractSoundPacket(UUID id, short[] rawAudio) {
        version = CURRENT_VERSION;
        this.id = id;
        this.rawAudio = rawAudio;
    }

    public AbstractSoundPacket() {

    }

    public UUID getId() {
        return id;
    }

    public short[] getRawAudio() {
        return rawAudio;
    }

    @Override
    public T fromBytes(FriendlyByteBuf buf) throws VersionCompatibilityException {
        version = buf.readShort();
        if (version != CURRENT_VERSION) {
            throw new VersionCompatibilityException("Incompatible version");
        }
        id = buf.readUUID();
        rawAudio = Utils.bytesToShorts(buf.readByteArray());
        return (T) this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeShort(version);
        buf.writeUUID(id);
        buf.writeByteArray(Utils.shortsToBytes(rawAudio));
    }
}
