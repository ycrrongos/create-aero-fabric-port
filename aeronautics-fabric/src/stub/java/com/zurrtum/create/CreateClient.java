package com.zurrtum.create;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;
import java.util.List;

/** Fabric spike stub for Create Fly client hooks used by Aeronautics. */
public final class CreateClient {
    private CreateClient() {}
    public static final ValueSettingsHandler VALUE_SETTINGS_HANDLER = new ValueSettingsHandler();
    public static final ZapperRenderHandler ZAPPER_RENDER_HANDLER = new ZapperRenderHandler();
    public static final class ValueSettingsHandler {
        public void showHoverTip(List<Component> tip) {}
    }
    public static final class ZapperRenderHandler {
        public void shoot(InteractionHand hand, Vec3 location) {}
    }
}
