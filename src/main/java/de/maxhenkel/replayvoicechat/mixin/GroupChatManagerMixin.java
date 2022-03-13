package de.maxhenkel.replayvoicechat.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import de.maxhenkel.voicechat.voice.client.GroupChatManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.breadloaf.replaymodinterface.ReplayInterface;

@Mixin(value = GroupChatManager.class, remap = false)
public class GroupChatManagerMixin {

    @Inject(method = "renderIcons", at = @At(value = "HEAD"), cancellable = true)
    private static void renderIcons(PoseStack matrixStack, CallbackInfo ci) {
        if (ReplayInterface.INSTANCE.isInReplayEditor) {
            ci.cancel();
        }
    }

}
