package xyz.breadloaf.replaymodinterface.modules;

import com.replaymod.core.KeyBindingRegistry;
import com.replaymod.core.Module;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.EventRegistrations;
import com.replaymod.replay.events.ReplayClosingCallback;
import com.replaymod.replay.events.ReplayOpenedCallback;
import xyz.breadloaf.replaymodinterface.ReplayInterface;

public class VoicechatModule extends EventRegistrations implements Module {
    @Override
    public void initCommon() {
        ReplayInterface.logger.info("ReplayModModule initCommon");
    }

    @Override
    public void initClient() {
        ReplayInterface.logger.info("ReplayModModule initClient");
        on(ReplayOpenedCallback.EVENT,replayHandler -> {
            ReplayInterface.logger.info("ReplayOpenedCallback");
            ReplayInterface.INSTANCE.isInReplayEditor = true;
        });
        on(ReplayClosingCallback.EVENT, replayHandler -> {
            ReplayInterface.logger.info("ReplayClosingCallback");
            ReplayInterface.INSTANCE.isInReplayEditor = false;
        });
        register();
    }

    @Override
    public void registerKeyBindings(KeyBindingRegistry registry) {
        ReplayInterface.logger.info("ReplayModModule registerKeybinds");
    }
}
