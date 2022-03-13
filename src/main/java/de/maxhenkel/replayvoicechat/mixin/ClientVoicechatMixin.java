package de.maxhenkel.replayvoicechat.mixin;

import de.maxhenkel.voicechat.voice.client.ClientVoicechat;
import de.maxhenkel.voicechat.voice.client.ClientVoicechatConnection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.breadloaf.replaymodinterface.ReplayInterface;

@Mixin(value = ClientVoicechat.class, remap = false)
public class ClientVoicechatMixin {

    @Inject(method = "startMicThread", at = @At(value = "HEAD"), cancellable = true)
    private void startMicThread(ClientVoicechatConnection connection, CallbackInfo ci) {
        if (ReplayInterface.INSTANCE.isInReplayEditor) {
            ci.cancel();
        }
    }

}
