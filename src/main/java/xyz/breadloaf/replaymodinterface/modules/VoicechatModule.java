package xyz.breadloaf.replaymodinterface.modules;

import com.mojang.blaze3d.platform.InputConstants;
import com.replaymod.core.KeyBindingRegistry;
import com.replaymod.core.Module;
import com.replaymod.recording.ReplayModRecording;
import com.replaymod.recording.packet.PacketListener;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.keybinding.FabricKeyBinding;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;
import xyz.breadloaf.replaymodinterface.ReplayInterface;

public class VoicechatModule implements Module {
    @Override
    public void initCommon() {
        ReplayInterface.logger.info("ReplayModModule initCommon");
    }

    @Override
    public void initClient() {
        ReplayInterface.logger.info("ReplayModModule initClient");
    }

    @Override
    public void registerKeyBindings(KeyBindingRegistry registry) {
        ReplayInterface.logger.info("ReplayModModule registerKeybinds");
    }
}
