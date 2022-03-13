package de.maxhenkel.replayvoicechat.mixin;

import de.maxhenkel.voicechat.voice.client.InitializationData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.breadloaf.replaymodinterface.ReplayInterface;

@Mixin(value = InitializationData.class, remap = false)
public class InitializationDataMixin {

    @Inject(method = "groupsEnabled", at = @At(value = "HEAD"), cancellable = true)
    private void groupsEnabled(CallbackInfoReturnable<Boolean> ci) {
        if (ReplayInterface.INSTANCE.isInReplayEditor) {
            ci.setReturnValue(false);
        }
    }

    @Inject(method = "allowRecording", at = @At(value = "HEAD"), cancellable = true)
    private void allowRecording(CallbackInfoReturnable<Boolean> ci) {
        if (ReplayInterface.INSTANCE.isInReplayEditor) {
            ci.setReturnValue(false);
        }
    }

}
