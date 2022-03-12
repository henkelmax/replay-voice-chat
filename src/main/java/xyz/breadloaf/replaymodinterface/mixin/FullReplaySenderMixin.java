package xyz.breadloaf.replaymodinterface.mixin;

import com.replaymod.core.ReplayMod;
import com.replaymod.core.versions.MCVer;
import com.replaymod.replay.FullReplaySender;
import com.replaymod.replay.ReplayHandler;
import com.replaymod.replaystudio.io.ReplayInputStream;
import com.replaymod.replaystudio.replay.ReplayFile;
import io.netty.channel.ChannelHandlerContext;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import xyz.breadloaf.replaymodinterface.ReplayInterface;
import xyz.breadloaf.replaymodinterface.imsosorry.PacketData;
import xyz.breadloaf.replaymodinterface.mixin.accessor.ReplayHandlerAccessor;

import java.io.EOFException;
import java.io.IOException;
import java.util.Objects;

// other mod devs im so sorry we need access to the PacketData object in here
// but it is a inner class that is private so we cannot use local collection to access it
// i looked into other options but this is the only way

@Mixin(value = FullReplaySender.class, remap = false)
public abstract class FullReplaySenderMixin {

    @Shadow protected int lastTimeStamp;

    @Shadow protected ChannelHandlerContext ctx;

    @Shadow protected boolean terminate;

    @Shadow protected boolean hasWorldLoaded;

    @Shadow protected ReplayInputStream replayIn;

    @Shadow private boolean loginPhase;

    @Shadow protected boolean startFromBeginning;

    PacketData imSorry;

    @Shadow @Final private ReplayHandler replayHandler;

    @Shadow protected ReplayFile replayFile;

    @Shadow private long realTimeStart;

    @Shadow protected double replaySpeed;

    @Shadow public abstract void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception;

    //these are the injects we could use if the PacketData subclass was public
    /*@Inject(method = "doSendPacketsTill", at = @At(value = "HEAD"))
    private void doSendPacketsTillStart(int timestamp, CallbackInfo ci) {
        int delta = (timestamp - this.lastTimeStamp);
        if (delta > 50 || delta < 0) {
            ReplayInterface.logger.info("Skipping: " + delta + "ms");
            ReplayInterface.INSTANCE.skipping = true;
        }

    }

    @Inject(method = "doSendPacketsTill", at = @At(value = "RETURN"))
    private void doSendPacketsTillEnd(int timestamp, CallbackInfo ci) {
        ReplayInterface.INSTANCE.skipping = false;

    }*/

    @Overwrite
    private void doSendPacketsTill(int timestamp) {
        //this would be a inject at head
        int delta = (timestamp - this.lastTimeStamp);
        if (delta > 50 || delta < 0) {
            ReplayInterface.logger.info("Skipping: " + delta + "ms");
            ReplayInterface.INSTANCE.skipping = true;
        }
        while(true) {
            try {
                if (this.ctx == null && !this.terminate) {
                    Thread.sleep(10L);
                    continue;
                }

                synchronized(this) {
                    if (timestamp == this.lastTimeStamp) {
                        //this would be an inject at return
                        ReplayInterface.INSTANCE.skipping = false;
                        return;
                    }

                    if (timestamp < this.lastTimeStamp) {
                        this.hasWorldLoaded = false;
                        this.lastTimeStamp = 0;
                        if (this.replayIn != null) {
                            this.replayIn.close();
                            this.replayIn = null;
                        }

                        this.loginPhase = true;
                        this.startFromBeginning = false;
                        imSorry = null;
                        ReplayMod replayMod = ReplayMod.instance;
                        ReplayHandler replayHandler = this.replayHandler;
                        Objects.requireNonNull(replayHandler);
                        replayMod.runSync(((ReplayHandlerAccessor)replayHandler)::invokeRestartedReplay);
                    }

                    if (this.replayIn == null) {
                        this.replayIn = this.replayFile.getPacketData(MCVer.getPacketTypeRegistry(true));
                    }

                    while(true) {
                        try {
                            PacketData pd;
                            if (this.imSorry != null) {
                                pd = this.imSorry;
                                this.imSorry = null;
                            } else {
                                pd = new PacketData(this.replayIn, this.loginPhase);
                            }

                            int nextTimeStamp = pd.timestamp;
                            if (nextTimeStamp > timestamp) {
                                this.imSorry = pd;
                                break;
                            }

                            //this would be a inject/redirect of this call with local collection if the subclass was public
                            if (ReplayInterface.injectedPacketSendCheck(pd)) {
                                this.channelRead(this.ctx, pd.bytes);
                            }

                        } catch (EOFException e) {
                            this.replayIn = null;
                            break;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    this.realTimeStart = System.currentTimeMillis() - (long)((double)timestamp / this.replaySpeed);
                    this.lastTimeStamp = timestamp;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            //this would be an inject at return
            ReplayInterface.INSTANCE.skipping = false;
            return;
        }
    }


}
