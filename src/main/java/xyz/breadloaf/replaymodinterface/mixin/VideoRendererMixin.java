package xyz.breadloaf.replaymodinterface.mixin;

import com.replaymod.render.rendering.Pipeline;
import de.maxhenkel.replayvoicechat.rendering.VoicechatVoiceRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Pipeline.class, remap = false)
public class VideoRendererMixin {

    private boolean ranBefore;

    @Inject(method = "run", at = @At("HEAD"))
    public void run(CallbackInfo ci) {
        this.ranBefore = false;
    }

    @Inject(method = "run", at = @At(value = "INVOKE", target = "Ljava/util/concurrent/ExecutorService;submit(Ljava/lang/Runnable;)Ljava/util/concurrent/Future;"))
    public void submit(CallbackInfo ci) {
        if (!this.ranBefore) {
            VoicechatVoiceRenderer.onStartRendering();
            this.ranBefore = true;
        }
    }

}
