package de.maxhenkel.replayvoicechat.mixin;

import de.maxhenkel.replayvoicechat.ReplayVoicechat;
import de.maxhenkel.replayvoicechat.net.FakeVoicechatConnection;
import de.maxhenkel.replayvoicechat.rendering.VoicechatVoiceRenderer;
import de.maxhenkel.voicechat.net.SecretPacket;
import de.maxhenkel.voicechat.voice.client.ClientManager;
import de.maxhenkel.voicechat.voice.client.ClientVoicechat;
import de.maxhenkel.voicechat.voice.client.InitializationData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.breadloaf.replaymodinterface.ReplayInterface;

import javax.annotation.Nullable;
import java.io.IOException;

@Mixin(value = ClientManager.class, remap = false)
public class ClientManagerMixin {

    @Shadow
    @Nullable
    private ClientVoicechat client;

    @Inject(method = "authenticate", at = @At(value = "HEAD"), cancellable = true)
    private void connect(SecretPacket secretPacket, CallbackInfo ci) {
        if (!ReplayInterface.INSTANCE.isInReplayEditor) {
            return;
        }
        ReplayVoicechat.LOGGER.info("Fake authentication");
        if (this.client != null) {
            try {
                InitializationData initializationData = new InitializationData("127.0.0.1", secretPacket);
                VoicechatVoiceRenderer.onInitializationData(initializationData);
                ((ClientVoicechatAccessor) this.client).setConnection(new FakeVoicechatConnection(client, initializationData));
                ((ClientVoicechatAccessor) this.client).getConnection().start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        ci.cancel();
    }
}
