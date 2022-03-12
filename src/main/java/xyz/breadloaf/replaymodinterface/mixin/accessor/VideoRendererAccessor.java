package xyz.breadloaf.replaymodinterface.mixin.accessor;

import com.replaymod.render.rendering.VideoRenderer;
import com.replaymod.replaystudio.pathing.path.Timeline;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = VideoRenderer.class,remap = false)
public interface VideoRendererAccessor {
    @Accessor("timeline")
    Timeline getTimeline();
}
