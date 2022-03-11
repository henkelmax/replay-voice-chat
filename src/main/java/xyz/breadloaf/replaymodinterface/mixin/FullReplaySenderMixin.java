package xyz.breadloaf.replaymodinterface.mixin;

import com.replaymod.replay.FullReplaySender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.breadloaf.replaymodinterface.ReplayInterface;

@Mixin(value = FullReplaySender.class, remap = false)
public class FullReplaySenderMixin {

    @Shadow protected int lastTimeStamp;

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
