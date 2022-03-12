package de.maxhenkel.replayvoicechat.net;

import de.maxhenkel.replayvoicechat.Utils;
import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;

public abstract class AbstractSoundPacket<T extends Packet<T>> implements Packet<T> {

    //TODO versioning
    private UUID id;
    private short[] rawAudio;

    public AbstractSoundPacket(UUID id, short[] rawAudio) {
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
    public T fromBytes(FriendlyByteBuf buf) {
        id = buf.readUUID();
        rawAudio = Utils.bytesToShorts(buf.readByteArray());
        return (T) this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(id);
        buf.writeByteArray(Utils.shortsToBytes(rawAudio));
    }
}
