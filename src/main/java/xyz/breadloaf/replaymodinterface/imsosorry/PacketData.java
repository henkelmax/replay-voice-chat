package xyz.breadloaf.replaymodinterface.imsosorry;

import com.replaymod.core.ReplayMod;
import com.replaymod.lib.com.github.steveice10.packetlib.io.NetOutput;
import com.replaymod.lib.com.github.steveice10.packetlib.tcp.io.ByteBufNetOutput;
import com.replaymod.replaystudio.io.ReplayInputStream;
import com.replaymod.replaystudio.util.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.commons.io.IOUtils;

import java.io.EOFException;
import java.io.IOException;

//future mod devs im sorry there was no other way
public class PacketData {
    private static final ByteBuf byteBuf = Unpooled.buffer();
    private static final NetOutput netOutput;
    public final int timestamp;
    public final byte[] bytes;
    //this would be done by a mixin inject to grab it but as we already made our own ill just make it a public member
    public com.replaymod.replaystudio.protocol.Packet packet;

    public PacketData(ReplayInputStream in, boolean loginPhase) throws IOException {
        if (ReplayMod.isMinimalMode()) {
            this.timestamp = Utils.readInt(in);
            int length = Utils.readInt(in);
            if (this.timestamp == -1 || length == -1) {
                throw new EOFException();
            }

            this.bytes = new byte[length];
            IOUtils.readFully(in, this.bytes);
        } else {
            com.replaymod.replaystudio.PacketData data = in.readPacket();
            if (data == null) {
                throw new EOFException();
            }

            this.timestamp = (int)data.getTime();
            packet = data.getPacket();
            synchronized(byteBuf) {
                byteBuf.markReaderIndex();
                byteBuf.markWriterIndex();
                netOutput.writeVarInt(packet.getId());
                int idSize = byteBuf.readableBytes();
                int contentSize = packet.getBuf().readableBytes();
                this.bytes = new byte[idSize + contentSize];
                byteBuf.readBytes(this.bytes, 0, idSize);
                packet.getBuf().readBytes(this.bytes, idSize, contentSize);
                byteBuf.resetReaderIndex();
                byteBuf.resetWriterIndex();
            }

            packet.getBuf().release();
        }

    }

    static {
        netOutput = new ByteBufNetOutput(byteBuf);
    }
}