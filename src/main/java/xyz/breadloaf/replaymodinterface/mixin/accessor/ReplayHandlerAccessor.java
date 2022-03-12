package xyz.breadloaf.replaymodinterface.mixin.accessor;

import com.replaymod.replay.ReplayHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ReplayHandler.class)
public interface ReplayHandlerAccessor {
    @Invoker("restartedReplay")
    void invokeRestartedReplay();
}
