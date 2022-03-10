package de.maxhenkel.replayvoicechat.mixin;

import de.maxhenkel.replayvoicechat.ReplayVoicechat;
import de.maxhenkel.voicechat.voice.client.ClientManager;
import de.maxhenkel.voicechat.voice.client.ClientVoicechat;
import de.maxhenkel.voicechat.voice.client.ClientVoicechatConnection;
import de.maxhenkel.voicechat.voice.client.InitializationData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.IOException;

@Mixin(value = ClientManager.class, remap = false)
public class ClientManagerMixin {

    //TODO
    /*@Redirect(method = "authenticate", at = @At(value = "INVOKE", target = "Lde/maxhenkel/voicechat/voice/client/ClientVoicechat;connect(Lde/maxhenkel/voicechat/voice/client/InitializationData;)V"))
    private void connect(ClientVoicechat client, InitializationData initializationData) {
        ReplayVoicechat.LOGGER.info("Fake auth");
        //TODO only when in replay!!!
        try {
            ((ClientVoicechatAccessor) client).setConnection(new ClientVoicechatConnection(client, initializationData));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/

}
