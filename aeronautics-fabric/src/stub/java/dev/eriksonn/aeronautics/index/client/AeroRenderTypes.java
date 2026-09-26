package dev.eriksonn.aeronautics.index.client;

import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;

/** Stub — levitite RenderType pipeline deferred. */
public final class AeroRenderTypes {
    public static final Identifier LEVITITE_SHADER = Identifier.fromNamespaceAndPath("aeronautics", "levitite");
    private AeroRenderTypes() {}
    public static RenderType levitite() { return null; }
    public static RenderType levititeGhosts() { return null; }
}
