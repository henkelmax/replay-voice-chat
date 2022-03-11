package xyz.breadloaf.replaymodinterface;

import com.replaymod.recording.ReplayModRecording;
import com.replaymod.replay.ReplayHandler;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.breadloaf.replaymodinterface.mixin.accessor.ConnectionEventHandlerAccessor;

import javax.annotation.Nullable;

public class ReplayInterface implements ClientModInitializer {
    public static Logger logger = LogManager.getLogger("ReplayInterface");
    public static ReplayInterface INSTANCE;
    public boolean isInReplayEditor;
    @Nullable
    public ReplayHandler replayHandler;
    public boolean skipping;

    public ReplayInterface() {
        if (INSTANCE == null) {
            INSTANCE = this;
        }
    }

    @Override
    public void onInitializeClient() {
        /*KeyMapping keyBinding = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.examplemod.spook", // The translation key of the keybinding's name
                InputConstants.Type.KEYSYM, // The type of the keybinding, KEYSYM for keyboard, MOUSE for mouse.
                GLFW.GLFW_KEY_R, // The keycode of the key
                "category.examplemod.test" // The translation key of the keybinding's category.
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (keyBinding.isDown() && System.currentTimeMillis() - lastKeyPress  >= 20) {
                client.player.sendMessage(new TextComponent("Key 1 was pressed!"), null);
                for (int i = 0; i<= 200; i++) {
                    ReplayInterface.INSTANCE.sendFakePacket(new ResourceLocation("svcreplaymod","audiochannel"),new FriendlyByteBuf(Unpooled.buffer()));
                }
                lastKeyPress = System.currentTimeMillis();
            }
        });*/
    }

    //Check if replay mod is active (IE loaded at world load)
    public boolean isReplayModActive() {
        return ((ConnectionEventHandlerAccessor) ReplayModRecording.instance.getConnectionEventHandler()).getRecordingEventHandler() != null;
    }

    //Adds a fake packet into recording data
    public void sendFakePacket(ResourceLocation resourceLocation, FriendlyByteBuf packetData) {
        ClientboundCustomPayloadPacket packet = new ClientboundCustomPayloadPacket(resourceLocation, packetData);
        sendFakePacket(packet);
    }

    public void sendFakePacket(Packet<?> packet) {
        if (ReplayModRecording.instance.getConnectionEventHandler() != null) {
            ReplayModRecording.instance.getConnectionEventHandler().getPacketListener().save(packet);
        }
    }


}