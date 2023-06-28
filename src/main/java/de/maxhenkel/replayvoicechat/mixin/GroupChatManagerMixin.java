package de.maxhenkel.replayvoicechat.mixin;

import de.maxhenkel.voicechat.voice.client.GroupChatManager;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.breadloaf.replaymodinterface.ReplayInterface;

@Mixin(value = GroupChatManager.class, remap = false)
public class GroupChatManagerMixin {

    @Inject(method = "renderIcons", at = @At(value = "HEAD"), cancellable = true)
    private static void renderIcons(GuiGraphics guiGraphics, CallbackInfo ci) {
        if (ReplayInterface.INSTANCE.isInReplayEditor) {
            ci.cancel();
        }
    }

}
