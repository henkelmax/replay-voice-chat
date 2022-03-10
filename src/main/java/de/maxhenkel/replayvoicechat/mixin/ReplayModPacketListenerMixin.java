package de.maxhenkel.replayvoicechat.mixin;

import com.replaymod.recording.packet.PacketListener;
import de.maxhenkel.replayvoicechat.ReplayVoicechat;
import net.minecraft.network.protocol.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = PacketListener.class, remap = false)
public class ReplayModPacketListenerMixin {

    @Inject(method = "saveMetaData", at = @At(value = "HEAD"))
    public void saveMetaData(CallbackInfo info) {
        ReplayVoicechat.LOGGER.info("saveMetaData");
    }

    @Inject(method = "save", at = @At(value = "HEAD"))
    public void save(Packet packet, CallbackInfo info) {

    }

    @Inject(method = "channelInactive", at = @At(value = "HEAD"))
    public void channelInactive(CallbackInfo info) {

    }

    @Inject(method = "channelRead", at = @At(value = "HEAD"))
    public void channelRead(CallbackInfo info) {

    }

    @Inject(method = "addMarker(Ljava/lang/String;I)V", at = @At(value = "HEAD"))
    public void addMarker(String name, int timestamp, CallbackInfo info) {
        ReplayVoicechat.LOGGER.info("addMarker {} {}", name, timestamp);
    }

}
