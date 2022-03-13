package xyz.breadloaf.replaymodinterface.mixin;

import com.replaymod.replay.FullReplaySender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.breadloaf.replaymodinterface.ReplayInterface;

// other mod devs im so sorry we need access to the PacketData object in here
// but it is a inner class that is private so we cannot use local collection to access it
// i looked into other options but this is the only way

@Mixin(value = FullReplaySender.class, remap = false)
public abstract class FullReplaySenderMixin {
    @Shadow protected int lastTimeStamp;

    //these are the injects we could use if the PacketData subclass was public
    @Inject(method = "doSendPacketsTill", at = @At(value = "HEAD"))
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

    }
}
