package xyz.breadloaf.replaymodinterface.mixin.accessor;

import com.replaymod.replay.ReplayHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = ReplayHandler.class, remap = false)
public interface ReplayHandlerAccessor {
    @Invoker("restartedReplay")
    void invokeRestartedReplay();
}
