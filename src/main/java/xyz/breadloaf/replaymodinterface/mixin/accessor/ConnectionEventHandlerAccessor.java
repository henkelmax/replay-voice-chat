package xyz.breadloaf.replaymodinterface.mixin.accessor;

import com.replaymod.recording.handler.ConnectionEventHandler;
import com.replaymod.recording.handler.RecordingEventHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = ConnectionEventHandler.class, remap = false)
public interface ConnectionEventHandlerAccessor {
    @Accessor("recordingEventHandler")
    RecordingEventHandler getRecordingEventHandler();
}
