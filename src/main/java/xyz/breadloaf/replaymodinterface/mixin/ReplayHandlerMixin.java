package xyz.breadloaf.replaymodinterface.mixin;

import com.replaymod.replay.ReplayHandler;
import com.replaymod.replaystudio.replay.ReplayFile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.breadloaf.replaymodinterface.ReplayInterface;

@Mixin(value = ReplayHandler.class, remap = false)
public class ReplayHandlerMixin {

    @Inject(method = "<init>", at=@At("RETURN"))
    private void constructionHook(ReplayFile replayFile, boolean asyncMode, CallbackInfo ci) {
        ReplayInterface.INSTANCE.replayHandler = (ReplayHandler) (Object) this;
    }
}
