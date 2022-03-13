package xyz.breadloaf.replaymodinterface.modules;

import com.replaymod.core.KeyBindingRegistry;
import com.replaymod.core.Module;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.EventRegistrations;
import com.replaymod.render.events.ReplayRenderCallback;
import com.replaymod.replay.events.ReplayClosingCallback;
import com.replaymod.replay.events.ReplayOpenedCallback;
import de.maxhenkel.replayvoicechat.rendering.VoicechatVoiceRenderer;
import xyz.breadloaf.replaymodinterface.ReplayInterface;

public class VoicechatModule extends EventRegistrations implements Module {
    @Override
    public void initCommon() {
    }

    @Override
    public void initClient() {
        on(ReplayOpenedCallback.EVENT,replayHandler -> {
            ReplayInterface.INSTANCE.isInReplayEditor = true;
        });
        on(ReplayClosingCallback.EVENT, replayHandler -> {
            ReplayInterface.INSTANCE.isInReplayEditor = false;
        });
        on(ReplayRenderCallback.Pre.EVENT, videoRenderer -> {
            ReplayInterface.INSTANCE.isRendering = true;
            ReplayInterface.INSTANCE.videoRenderer = videoRenderer;
        });
        on(ReplayRenderCallback.Post.EVENT, videoRenderer -> {
            ReplayInterface.INSTANCE.isRendering = false;
            VoicechatVoiceRenderer.onStopRendering();
            ReplayInterface.INSTANCE.videoRenderer = null;
        });
        register();
    }

    @Override
    public void registerKeyBindings(KeyBindingRegistry registry) {
    }
}

