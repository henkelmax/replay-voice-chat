package xyz.breadloaf.replaymodinterface.mixin.accessor;

import com.replaymod.pathing.player.RealtimeTimelinePlayer;
import com.replaymod.simplepathing.gui.GuiPathing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = GuiPathing.class, remap = false)
public interface GuiPathingAccessor {

    @Accessor("player")
    RealtimeTimelinePlayer getPlayer();

}
