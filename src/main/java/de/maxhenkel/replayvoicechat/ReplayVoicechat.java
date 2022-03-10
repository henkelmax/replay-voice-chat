package de.maxhenkel.replayvoicechat;

import de.maxhenkel.replayvoicechat.playback.NetManager;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReplayVoicechat implements ClientModInitializer {

    public static final String MOD_ID = "replayvoicechat";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        NetManager.init();
    }
}
