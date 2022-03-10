package de.maxhenkel.replayvoicechat.mixin;

import com.replaymod.recording.handler.ConnectionEventHandler;
import com.replaymod.recording.packet.PacketListener;
import com.replaymod.replaystudio.replay.ReplayFile;
import com.replaymod.replaystudio.replay.ReplayMetaData;
import de.maxhenkel.replayvoicechat.ReplayVoicechat;
import net.minecraft.network.Connection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.nio.file.Path;

@Mixin(value = ConnectionEventHandler.class, remap = false)
public class ReplayModConnectionEventHandlerMixin {

    @Shadow
    private PacketListener packetListener;

    @Inject(method = "onConnectedToServerEvent", at = @At(value = "INVOKE", target = "Lcom/replaymod/recording/gui/GuiRecordingOverlay;register()V", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
    public void onConnectedToServerEvent(Connection connection, CallbackInfo info, boolean local, String serverName, String worldName, boolean autoStart, String name, Path outputPath, ReplayFile replayFile, ReplayMetaData metaData) {
        ReplayVoicechat.LOGGER.info("RECORDING {} | {} | {} | {}", metaData.getFileFormat(), metaData.getDate(), metaData.getServerName(), packetListener.getCurrentDuration());
    }

    @Inject(method = "reset", at = @At(value = "HEAD"))
    public void reset(CallbackInfo info) {
        ReplayVoicechat.LOGGER.info("STOP RECORDING?");
    }

}
