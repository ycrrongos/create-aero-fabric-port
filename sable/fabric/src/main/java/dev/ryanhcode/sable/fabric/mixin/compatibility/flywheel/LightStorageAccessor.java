package dev.ryanhcode.sable.fabric.mixin.compatibility.flywheel;

import com.zurrtum.create.client.flywheel.backend.engine.LightDataCollector;
import com.zurrtum.create.client.flywheel.backend.engine.LightStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LightStorage.class)
public interface LightStorageAccessor {

    @Accessor
    LightDataCollector getCollector();

    @Accessor
    void setNeedsLutRebuild(boolean needsLutRebuild);

}
