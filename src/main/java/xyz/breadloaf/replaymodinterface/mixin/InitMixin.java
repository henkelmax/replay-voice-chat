package xyz.breadloaf.replaymodinterface.mixin;

import com.replaymod.core.Module;
import com.replaymod.core.ReplayMod;
import com.replaymod.core.ReplayModBackend;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.breadloaf.replaymodinterface.modules.VoicechatModule;

import java.util.List;

@Mixin(value = ReplayMod.class ,remap = false)
public class InitMixin {

    @Shadow @Final private List<Module> modules;

    @Inject(method="<init>", at=@At("RETURN"))
    private void constructorInject(ReplayModBackend backend, CallbackInfo ci)
    {
        this.modules.add(new VoicechatModule());
    }
}
