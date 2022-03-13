package xyz.breadloaf.replaymodinterface;

import com.replaymod.pathing.properties.TimestampProperty;
import com.replaymod.recording.ReplayModRecording;
import com.replaymod.render.rendering.VideoRenderer;
import com.replaymod.replay.ReplayHandler;
import com.replaymod.replaystudio.pathing.impl.TimelineImpl;
import com.replaymod.replaystudio.pathing.path.Keyframe;
import com.replaymod.replaystudio.pathing.path.Path;
import com.replaymod.replaystudio.pathing.path.Timeline;
import com.replaymod.replaystudio.protocol.PacketType;
import com.replaymod.simplepathing.ReplayModSimplePathing;
import de.maxhenkel.replayvoicechat.ReplayVoicechat;
import de.maxhenkel.replayvoicechat.rendering.VoicechatVoiceRenderer;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.breadloaf.replaymodinterface.imsosorry.PacketData;
import xyz.breadloaf.replaymodinterface.mixin.accessor.ConnectionEventHandlerAccessor;
import xyz.breadloaf.replaymodinterface.mixin.accessor.GuiPathingAccessor;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class ReplayInterface implements ClientModInitializer {
    public static Logger logger = LogManager.getLogger("ReplayInterface");
    public static ReplayInterface INSTANCE;
    public boolean isInReplayEditor;
    @Nullable
    public ReplayHandler replayHandler;
    public boolean skipping;
    public boolean isRendering;
    @Nullable
    public VideoRenderer videoRenderer;

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

    public static boolean injectedPacketSendCheck(PacketData pd) {
        //we use fake plugin message packets to pack voicechat data
        if (!INSTANCE.isRendering) {
            return true;
        }
        if (pd.packet.getType() == PacketType.PluginMessage) {
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.wrappedBuffer(pd.bytes));
            buf.readVarInt(); //we just need to advance this so we dont try and parse the id as a resourcelocation
            ClientboundCustomPayloadPacket customPayloadPacket = new ClientboundCustomPayloadPacket(buf);
            if (customPayloadPacket.getIdentifier().getNamespace().equals(ReplayVoicechat.MOD_ID)) {
                if (Minecraft.getInstance().cameraEntity != null) {
                    Vec3 cameraLocation = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
                    float yrot = Minecraft.getInstance().gameRenderer.getMainCamera().getYRot();
                    VoicechatVoiceRenderer.onRecordingPacket(customPayloadPacket, pd.timestamp, cameraLocation, yrot);
                }
                return false; //stop this packet being sent
            }
        }
        return true;
    }

    public static double getCurrentSpeed() {
        ReplayHandler replayHandler = INSTANCE.replayHandler;
        if (replayHandler == null) {
            return 1D;
        }

        if (replayHandler.getReplaySender().isAsyncMode()) {
            return replayHandler.getReplaySender().getReplaySpeed();
        }

        Timeline tl = ReplayModSimplePathing.instance.getCurrentTimeline().getTimeline();

        if (!(tl instanceof TimelineImpl timeline)) {
            return 1D;
        }

        if (!(ReplayModSimplePathing.instance.getGuiPathing() instanceof GuiPathingAccessor guiPathingAccessor)) {
            return 1D;
        }

        long currentRealtime = guiPathingAccessor.getPlayer().getTimePassed();

        List<Path> paths = timeline.getPaths();

        for (Path path : paths) {
            Collection<Keyframe> keyframes = path.getKeyframes();

            TimeKeyframe last = null;

            for (Keyframe keyframe : keyframes) {
                Optional<Integer> value = keyframe.getValue(TimestampProperty.PROPERTY);
                if (value.isEmpty()) {
                    continue;
                }

                long realtimeKeyframe = keyframe.getTime();
                long mcKeyframe = value.get();

                TimeKeyframe current = new TimeKeyframe(realtimeKeyframe, mcKeyframe);

                if (realtimeKeyframe >= currentRealtime) {
                    if (last == null) {
                        //Time will stand still because we aren't past our first time keyframe
                        return 0D;
                    } else {
                        double mcDuration = mcKeyframe - last.mcKeyframe;
                        double realtimeDuration = realtimeKeyframe - last.realtimeKeyframe;
                        return mcDuration / realtimeDuration;
                    }
                }
                last = current;
            }
        }

        return 1D;
    }

    public static class TimeKeyframe {
        long realtimeKeyframe;
        long mcKeyframe;

        public TimeKeyframe(long realtimeKeyframe, long mcKeyframe) {
            this.realtimeKeyframe = realtimeKeyframe;
            this.mcKeyframe = mcKeyframe;
        }
    }

}
