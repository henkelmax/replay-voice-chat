package xyz.breadloaf.replaymodinterface;

import com.replaymod.extras.ReplayModExtras;
import com.replaymod.extras.playeroverview.PlayerOverview;
import com.replaymod.pathing.properties.TimestampProperty;
import com.replaymod.recording.ReplayModRecording;
import com.replaymod.render.rendering.VideoRenderer;
import com.replaymod.replay.ReplayHandler;
import com.replaymod.replaystudio.pathing.impl.TimelineImpl;
import com.replaymod.replaystudio.pathing.path.Keyframe;
import com.replaymod.replaystudio.pathing.path.Path;
import com.replaymod.replaystudio.pathing.path.Timeline;
import com.replaymod.simplepathing.ReplayModSimplePathing;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.breadloaf.replaymodinterface.mixin.accessor.ConnectionEventHandlerAccessor;
import xyz.breadloaf.replaymodinterface.mixin.accessor.GuiPathingAccessor;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

    }

    //Check if replay mod is active (IE loaded at world load)
    public boolean isReplayModActive() {
        return ((ConnectionEventHandlerAccessor) ReplayModRecording.instance.getConnectionEventHandler()).getRecordingEventHandler() != null;
    }

    //Adds a fake packet into recording data
    public void sendFakePacket(ResourceLocation resourceLocation, FriendlyByteBuf packetData) {
        sendFakePacket(ServerPlayNetworking.createS2CPacket(resourceLocation, packetData));
    }

    public void sendFakePacket(Packet<?> packet) {
        if (ReplayModRecording.instance.getConnectionEventHandler() != null) {
            ReplayModRecording.instance.getConnectionEventHandler().getPacketListener().save(packet);
        }
    }

    public boolean isPlayerHidden(UUID uuid) {
        Optional<PlayerOverview> playerOverview = ReplayModExtras.instance.get(PlayerOverview.class);
        return playerOverview.map(overview -> overview.isHidden(uuid)).orElse(false);
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

        long currentRealtime;
        if (INSTANCE.isRendering) {
            currentRealtime = ReplayInterface.INSTANCE.videoRenderer.getVideoTime();
        } else {
            currentRealtime = guiPathingAccessor.getPlayer().getTimePassed();
        }

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
