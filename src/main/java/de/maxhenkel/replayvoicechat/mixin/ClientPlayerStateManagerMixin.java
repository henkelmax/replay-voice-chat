package de.maxhenkel.replayvoicechat.mixin;

import de.maxhenkel.voicechat.voice.client.ClientPlayerStateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.breadloaf.replaymodinterface.ReplayInterface;

@Mixin(value = ClientPlayerStateManager.class, remap = false)
public class ClientPlayerStateManagerMixin {

    @Inject(method = "isDisabled", at = @At(value = "HEAD"), cancellable = true)
    private void isDisabled(CallbackInfoReturnable<Boolean> ci) {
        if (ReplayInterface.INSTANCE.isInReplayEditor) {
            ci.setReturnValue(false);
        }
    }

}
