package de.maxhenkel.replayvoicechat.mixin;

import de.maxhenkel.voicechat.voice.client.RenderEvents;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.breadloaf.replaymodinterface.ReplayInterface;

@Mixin(value = RenderEvents.class, remap = false)
public class RenderEventsMixin {

    @Inject(method = "shouldShowIcons", at = @At(value = "HEAD"), cancellable = true)
    private void shouldShowIcons(CallbackInfoReturnable<Boolean> cir) {
        if (ReplayInterface.INSTANCE.isInReplayEditor) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "renderIcon", at = @At(value = "HEAD"), cancellable = true)
    private void renderIcon(GuiGraphics guiGraphics, ResourceLocation texture, CallbackInfo ci) {
        if (ReplayInterface.INSTANCE.isInReplayEditor) {
            ci.cancel();
        }
    }

}
