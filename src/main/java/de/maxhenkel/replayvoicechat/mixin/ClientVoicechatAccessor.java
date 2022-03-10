package de.maxhenkel.replayvoicechat.mixin;

import de.maxhenkel.voicechat.voice.client.ClientVoicechat;
import de.maxhenkel.voicechat.voice.client.ClientVoicechatConnection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = ClientVoicechat.class, remap = false)
public interface ClientVoicechatAccessor {

    @Accessor("connection")
    ClientVoicechatConnection getConnection();

    @Accessor("connection")
    void setConnection(ClientVoicechatConnection connection);

}
