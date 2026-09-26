package dev.ryanhcode.sable.sublevel.render;

import com.mojang.blaze3d.textures.GpuSampler;
import org.joml.Matrix4fc;

/**
 * Describes where and how sub-levels are drawn.
 *
 * @param frustumMatrix  The view rotation matrix (vanilla "frustum matrix"), without any translation
 * @param cameraX        The x position of the camera in world space
 * @param cameraY        The y position of the camera in world space
 * @param cameraZ        The z position of the camera in world space
 * @param partialTicks   The partial tick used to interpolate sub-level poses
 * @param sampler        The sampler to sample the block atlas with
 * @param normalLighting Whether sub-levels should use dynamic directional shading
 * @param mainLevel      Whether this is the main level render, which updates sorting, culling and occlusion effects
 */
public record SubLevelRenderContext(Matrix4fc frustumMatrix,
                                    double cameraX,
                                    double cameraY,
                                    double cameraZ,
                                    float partialTicks,
                                    GpuSampler sampler,
                                    boolean normalLighting,
                                    boolean mainLevel) {
}
