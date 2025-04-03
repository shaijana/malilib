package fi.dy.masa.malilib.mixin.render;

import java.util.Map;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.platform.DestFactor;
import com.mojang.blaze3d.platform.PolygonMode;
import com.mojang.blaze3d.platform.SourceFactor;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gl.UniformType;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import fi.dy.masa.malilib.MaLiLibReference;
import fi.dy.masa.malilib.compat.iris.IrisCompat;
import fi.dy.masa.malilib.render.MaLiLibPipelines;

@Mixin(RenderPipelines.class)
public abstract class MixinRenderPipelines
{
    @Shadow
    @Final
    public static Map<Identifier, RenderPipeline> PIPELINES;
    @Shadow
    @Final
    public static RenderPipeline.Snippet MATRICES_SNIPPET;                       // MATRICES
    @Shadow
    @Final
    public static RenderPipeline.Snippet FOG_NO_COLOR_SNIPPET;                   // FOG_NO_COLOR
    @Shadow
    @Final
    public static RenderPipeline.Snippet FOG_SNIPPET;                            // FOG
    @Shadow
    @Final
    public static RenderPipeline.Snippet MATRICES_COLOR_SNIPPET;                 // MATRICES_COLOR
    @Shadow
    @Final
    public static RenderPipeline.Snippet MATRICES_COLOR_FOG_SNIPPET;             // MATRICES_COLOR_FOG
    @Shadow
    @Final
    public static RenderPipeline.Snippet MATRICES_COLOR_FOG_OFFSET_SNIPPET;      // MATRICES_COLOR_FOG_OFFSET
    @Shadow
    @Final
    public static RenderPipeline.Snippet MATRICES_COLOR_FOG_LIGHT_DIR_SNIPPET;   // MATRICES_COLOR_FOG_LIGHT_DIR
    @Shadow
    @Final
    public static RenderPipeline.Snippet TERRAIN_SNIPPET;                        // TERRAIN
    @Shadow
    @Final
    public static RenderPipeline.Snippet ENTITY_SNIPPET;                         // ENTITY
    @Shadow
    @Final
    public static RenderPipeline.Snippet RENDERTYPE_BEACON_BEAM_SNIPPET;         // RENDERTYPE_BEACON_BEAM
    @Shadow
    @Final
    public static RenderPipeline.Snippet TEXT_SNIPPET;                           // TEXT
    @Shadow
    @Final
    public static RenderPipeline.Snippet RENDERTYPE_END_PORTAL_SNIPPET;          // RENDERTYPE_END_PORTAL
    @Shadow
    @Final
    public static RenderPipeline.Snippet RENDERTYPE_CLOUDS_SNIPPET;              // RENDERTYPE_CLOUDS
    @Shadow
    @Final
    public static RenderPipeline.Snippet RENDERTYPE_LINES_SNIPPET;               // RENDERTYPE_LINES
    @Shadow
    @Final
    public static RenderPipeline.Snippet POSITION_COLOR_SNIPPET;                 // DEBUG_FILLED
    @Shadow
    @Final
    public static RenderPipeline.Snippet PARTICLE_SNIPPET;                       // PARTICLE_TEX
    @Shadow
    @Final
    public static RenderPipeline.Snippet WEATHER_SNIPPET;                        // WEATHER
    @Shadow
    @Final
    public static RenderPipeline.Snippet GUI_SNIPPET;                            // GUI
    @Shadow
    @Final
    public static RenderPipeline.Snippet POSITION_TEX_COLOR_SNIPPET;             // GUI_TEXTURED
    @Shadow
    @Final
    public static RenderPipeline.Snippet RENDERTYPE_OUTLINE_SNIPPET;             // RENDERTYPE_OUTLINE
    @Shadow
    @Final
    public static RenderPipeline.Snippet POST_EFFECT_PROCESSOR_SNIPPET;          // POST_PROCESSOR

    @Unique
    private static final BlendFunction MASA_BLEND = new BlendFunction(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
    // PANORAMA
    @Unique
    private static final BlendFunction MASA_BLEND_SIMPLE = new BlendFunction(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);

    @Shadow
    public static RenderPipeline register(RenderPipeline renderPipeline)
    {
        PIPELINES.put(renderPipeline.getLocation(), renderPipeline);
        return renderPipeline;
    }

// Common Uniforms
//      .withUniform("ModelViewMat", UniformType.MATRIX4X4)
//		.withUniform("ProjMat",      UniformType.MATRIX4X4)
//		.withUniform("FogStart",     UniformType.FLOAT)
//		.withUniform("FogEnd",       UniformType.FLOAT)
//		.withUniform("FogShape",     UniformType.INT)

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void malilib_onRegisterPipelines(CallbackInfo ci)
    {
        // STAGES
        MaLiLibPipelines.POSITION_TRANSLUCENT_STAGE =
                RenderPipeline.builder(MATRICES_COLOR_FOG_SNIPPET)
                              .withVertexShader("core/position")
                              .withFragmentShader("core/position")
                              .withBlend(BlendFunction.TRANSLUCENT)
                              .withVertexFormat(VertexFormats.POSITION, VertexFormat.DrawMode.QUADS)
                              .buildSnippet();

        MaLiLibPipelines.POSITION_MASA_STAGE =
                RenderPipeline.builder(MATRICES_COLOR_FOG_SNIPPET)
                              .withVertexShader("core/position")
                              .withFragmentShader("core/position")
                              .withBlend(MASA_BLEND)
                              .withVertexFormat(VertexFormats.POSITION, VertexFormat.DrawMode.QUADS)
                              .buildSnippet();

        MaLiLibPipelines.POSITION_COLOR_TRANSLUCENT_STAGE =
                RenderPipeline.builder(MATRICES_COLOR_SNIPPET)
                              .withVertexShader("core/position_color")
                              .withFragmentShader("core/position_color")
                              .withUniform("ModelOffset", UniformType.VEC3)
                              .withBlend(BlendFunction.TRANSLUCENT)
                              .withVertexFormat(VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.QUADS)
                              .buildSnippet();

        MaLiLibPipelines.POSITION_COLOR_MASA_STAGE =
                RenderPipeline.builder(MATRICES_COLOR_SNIPPET)
                              .withVertexShader("core/position_color")
                              .withFragmentShader("core/position_color")
                              .withUniform("ModelOffset", UniformType.VEC3)
                              .withBlend(MASA_BLEND)
                              .withVertexFormat(VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.QUADS)
                              .buildSnippet();

        MaLiLibPipelines.POSITION_TEX_TRANSLUCENT_STAGE =
                RenderPipeline.builder(MATRICES_COLOR_SNIPPET)
                              .withVertexShader("core/position_tex")
                              .withFragmentShader("core/position_tex")
                              .withSampler("Sampler0")
                              .withBlend(BlendFunction.TRANSLUCENT)
                              .withVertexFormat(VertexFormats.POSITION_TEXTURE, VertexFormat.DrawMode.QUADS)
                              .buildSnippet();

        MaLiLibPipelines.POSITION_TEX_OVERLAY_STAGE =
                RenderPipeline.builder(MATRICES_COLOR_SNIPPET)
                              .withVertexShader("core/position_tex")
                              .withFragmentShader("core/position_tex")
                              .withSampler("Sampler0")
                              .withBlend(BlendFunction.OVERLAY)
                              .withVertexFormat(VertexFormats.POSITION_TEXTURE, VertexFormat.DrawMode.QUADS)
                              .buildSnippet();

        MaLiLibPipelines.POSITION_TEX_MASA_STAGE =
                RenderPipeline.builder(MATRICES_COLOR_SNIPPET)
                              .withVertexShader("core/position_tex")
                              .withFragmentShader("core/position_tex")
                              .withSampler("Sampler0")
                              .withBlend(MASA_BLEND)
                              .withVertexFormat(VertexFormats.POSITION_TEXTURE, VertexFormat.DrawMode.QUADS)
                              .buildSnippet();

        MaLiLibPipelines.POSITION_TEX_COLOR_TRANSLUCENT_STAGE =
                RenderPipeline.builder(MATRICES_COLOR_SNIPPET)
                              .withVertexShader("core/position_tex_color")
                              .withFragmentShader("core/position_tex_color")
                              .withSampler("Sampler0")
                              .withBlend(BlendFunction.TRANSLUCENT)
                              .withVertexFormat(VertexFormats.POSITION_TEXTURE_COLOR, VertexFormat.DrawMode.QUADS)
                              .buildSnippet();

        MaLiLibPipelines.POSITION_TEX_COLOR_OVERLAY_STAGE =
                RenderPipeline.builder(MATRICES_COLOR_SNIPPET)
                              .withVertexShader("core/position_tex_color")
                              .withFragmentShader("core/position_tex_color")
                              .withSampler("Sampler0")
                              .withBlend(BlendFunction.OVERLAY)
                              .withVertexFormat(VertexFormats.POSITION_TEXTURE_COLOR, VertexFormat.DrawMode.QUADS)
                              .buildSnippet();

        MaLiLibPipelines.POSITION_TEX_COLOR_MASA_STAGE =
                RenderPipeline.builder(MATRICES_COLOR_SNIPPET)
                              .withVertexShader("core/position_tex_color")
                              .withFragmentShader("core/position_tex_color")
                              .withSampler("Sampler0")
                              .withBlend(MASA_BLEND)
                              .withVertexFormat(VertexFormats.POSITION_TEXTURE_COLOR, VertexFormat.DrawMode.QUADS)
                              .buildSnippet();

        MaLiLibPipelines.LINES_TRANSLUCENT_STAGE =
                RenderPipeline.builder(MATRICES_COLOR_FOG_SNIPPET)
                              .withVertexShader("core/rendertype_lines")
                              .withFragmentShader("core/rendertype_lines")
                              .withUniform("LineWidth", UniformType.FLOAT)
                              .withUniform("ScreenSize", UniformType.VEC2)
                              .withUniform("ModelOffset", UniformType.VEC3)
                              .withBlend(BlendFunction.TRANSLUCENT)
                              .withVertexFormat(VertexFormats.POSITION_COLOR_NORMAL, VertexFormat.DrawMode.LINES)
                              .buildSnippet();

        MaLiLibPipelines.LINES_MASA_SIMPLE_STAGE =
                RenderPipeline.builder(MATRICES_COLOR_FOG_SNIPPET)
                              .withVertexShader("core/rendertype_lines")
                              .withFragmentShader("core/rendertype_lines")
                              .withUniform("LineWidth", UniformType.FLOAT)
                              .withUniform("ScreenSize", UniformType.VEC2)
                              .withUniform("ModelOffset", UniformType.VEC3)
                              .withBlend(MASA_BLEND_SIMPLE)
                              .withVertexFormat(VertexFormats.POSITION_COLOR_NORMAL, VertexFormat.DrawMode.LINES)
                              .buildSnippet();

        MaLiLibPipelines.DEBUG_LINES_TRANSLUCENT_STAGE =
                RenderPipeline.builder(MATRICES_COLOR_SNIPPET)
                              .withVertexShader("core/position_color")
                              .withFragmentShader("core/position_color")
                              .withUniform("LineWidth", UniformType.FLOAT)
                              .withUniform("ScreenSize", UniformType.VEC2)
                              .withUniform("ModelOffset", UniformType.VEC3)
                              .withVertexFormat(VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.DEBUG_LINE_STRIP)
                              .withBlend(BlendFunction.TRANSLUCENT)
                              .buildSnippet();

        MaLiLibPipelines.DEBUG_LINES_MASA_SIMPLE_STAGE =
                RenderPipeline.builder(MATRICES_COLOR_SNIPPET)
                              .withVertexShader("core/position_color")
                              .withFragmentShader("core/position_color")
                              .withUniform("LineWidth", UniformType.FLOAT)
                              .withUniform("ScreenSize", UniformType.VEC2)
                              .withVertexFormat(VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.DEBUG_LINE_STRIP)
                              .withBlend(MASA_BLEND_SIMPLE)
                              .buildSnippet();

        MaLiLibPipelines.TERRAIN_TRANSLUCENT_STAGE =
                RenderPipeline.builder(MATRICES_COLOR_FOG_OFFSET_SNIPPET)
                              .withVertexShader("core/terrain")
                              .withFragmentShader("core/terrain")
                              .withSampler("Sampler0")
                              .withSampler("Sampler2")
                              .withVertexFormat(VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL, VertexFormat.DrawMode.QUADS)
                              .withBlend(BlendFunction.TRANSLUCENT)
                              .buildSnippet();

        MaLiLibPipelines.TERRAIN_MASA_STAGE =
                RenderPipeline.builder(MATRICES_COLOR_FOG_OFFSET_SNIPPET)
                              .withVertexShader("core/terrain")
                              .withFragmentShader("core/terrain")
                              .withSampler("Sampler0")
                              .withSampler("Sampler2")
                              .withVertexFormat(VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL, VertexFormat.DrawMode.QUADS)
                              .withBlend(MASA_BLEND)
                              .buildSnippet();

        // TODO later
//        MaLiLibPipelines.ENTITY_TRANSLUCENT_STAGE =
//                RenderPipeline.builder(MATRICES_COLOR_FOG_LIGHT_DIR_SNIPPET)
//                        .withVertexShader("core/entity")
//                        .withFragmentShader("core/entity")
//                        .withSampler("Sampler0")
//                        .withSampler("Sampler2")
//                        .withVertexFormat(VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, VertexFormat.DrawMode.QUADS)
//                        .withBlend(BlendFunction.TRANSLUCENT)
//                        .buildSnippet();
//
//        MaLiLibPipelines.ENTITY_MASA_STAGE =
//                RenderPipeline.builder(MATRICES_COLOR_FOG_LIGHT_DIR_SNIPPET)
//                        .withVertexShader("core/entity")
//                        .withFragmentShader("core/entity")
//                        .withSampler("Sampler0")
//                        .withSampler("Sampler2")
//                        .withVertexFormat(VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, VertexFormat.DrawMode.QUADS)
//                        .withBlend(MASA_BLEND)
//                        .buildSnippet();

        // POSITION_TRANSLUCENT
//        MaLiLibPipelines.POSITION_TRANSLUCENT_NO_DEPTH_NO_CULL =
//                RenderPipeline.builder(MaLiLibPipelines.POSITION_TRANSLUCENT_STAGE)
//                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/position/translucent/no_depth/no_cull"))
//                              .withCull(false)
//                              .withDepthWrite(false)
//                              .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
//                              .build();
//
//        MaLiLibPipelines.POSITION_TRANSLUCENT_NO_DEPTH =
//                RenderPipeline.builder(MaLiLibPipelines.POSITION_TRANSLUCENT_STAGE)
//                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/position/translucent/no_depth"))
//                              .withDepthWrite(false)
//                              .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
//                              .build();
//                // );
//
//        MaLiLibPipelines.POSITION_TRANSLUCENT_LESSER_DEPTH_OFFSET_1 =
//                RenderPipeline.builder(MaLiLibPipelines.POSITION_TRANSLUCENT_STAGE)
//                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/position/translucent/lesser_depth/offset_1"))
//                              .withCull(false)
//                              .withDepthWrite(false)
//                              .withDepthTestFunction(DepthTestFunction.LESS_DEPTH_TEST)
//                              .withDepthBias(-0.3f, -0.6f)
//                              .build();
//                // );
//
//        MaLiLibPipelines.POSITION_TRANSLUCENT_LESSER_DEPTH_OFFSET_2 =
//                RenderPipeline.builder(MaLiLibPipelines.POSITION_TRANSLUCENT_STAGE)
//                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/position/translucent/lesser_depth/offset_2"))
//                              .withCull(false)
//                              .withDepthWrite(false)
//                              .withDepthTestFunction(DepthTestFunction.LESS_DEPTH_TEST)
//                              .withDepthBias(-0.4f, -0.8f)
//                              .build();
//                // );
//
//        MaLiLibPipelines.POSITION_TRANSLUCENT_LESSER_DEPTH_OFFSET_3 =
//                RenderPipeline.builder(MaLiLibPipelines.POSITION_TRANSLUCENT_STAGE)
//                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/position/translucent/lesser_depth/offset_3"))
//                              .withCull(false)
//                              .withDepthWrite(false)
//                              .withDepthTestFunction(DepthTestFunction.LESS_DEPTH_TEST)
//                              .withDepthBias(-3f, -3f)
//                              .build();
//                // );
//
//        MaLiLibPipelines.POSITION_TRANSLUCENT_LESSER_DEPTH =
//                RenderPipeline.builder(MaLiLibPipelines.POSITION_TRANSLUCENT_STAGE)
//                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/position/translucent/lesser_depth"))
//                              .withCull(false)
//                              .withDepthWrite(false)
//                              .withDepthTestFunction(DepthTestFunction.LESS_DEPTH_TEST)
//                              .build();
//                // );
//
//        MaLiLibPipelines.POSITION_TRANSLUCENT_GREATER_DEPTH =
//                RenderPipeline.builder(MaLiLibPipelines.POSITION_TRANSLUCENT_STAGE)
//                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/position/translucent/greater_depth"))
//                              .withCull(false)
//                              .withDepthWrite(false)
//                              .withDepthTestFunction(DepthTestFunction.GREATER_DEPTH_TEST)
//                              .build();
//                // );
//
//        MaLiLibPipelines.POSITION_TRANSLUCENT_DEPTH_MASK =
//                RenderPipeline.builder(MaLiLibPipelines.POSITION_TRANSLUCENT_STAGE)
//                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/position/translucent/depth_mask"))
//                              .withDepthWrite(true)
//                              .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
//                              .build();
//                // );
//
//        MaLiLibPipelines.POSITION_TRANSLUCENT =
//                RenderPipeline.builder(MaLiLibPipelines.POSITION_TRANSLUCENT_STAGE)
//                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/position/translucent"))
//                              .build();
//                // );


        // POSITION_MASA
        MaLiLibPipelines.POSITION_MASA_NO_DEPTH_NO_CULL =
                RenderPipeline.builder(MaLiLibPipelines.POSITION_MASA_STAGE)
                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/position/masa/no_depth/no_cull"))
                              .withCull(false)
                              .withDepthWrite(false)
                              .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                              .build();

        MaLiLibPipelines.POSITION_MASA_NO_DEPTH =
                RenderPipeline.builder(MaLiLibPipelines.POSITION_MASA_STAGE)
                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/position/masa/no_depth"))
                              .withDepthWrite(false)
                              .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                              .build();

        MaLiLibPipelines.POSITION_MASA_LESSER_DEPTH_OFFSET_1 =
                RenderPipeline.builder(MaLiLibPipelines.POSITION_MASA_STAGE)
                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/position/masa/lesser_depth/offset_1"))
                              .withCull(false)
                              .withDepthWrite(false)
                              .withDepthTestFunction(DepthTestFunction.LESS_DEPTH_TEST)
                              .withDepthBias(-0.3f, -0.6f)
                              .build();

        MaLiLibPipelines.POSITION_MASA_LESSER_DEPTH_OFFSET_2 =
                RenderPipeline.builder(MaLiLibPipelines.POSITION_MASA_STAGE)
                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/position/masa/lesser_depth/offset_2"))
                              .withCull(false)
                              .withDepthWrite(false)
                              .withDepthTestFunction(DepthTestFunction.LESS_DEPTH_TEST)
                              .withDepthBias(-0.4f, -0.8f)
                              .build();

        MaLiLibPipelines.POSITION_MASA_LESSER_DEPTH_OFFSET_3 =
                RenderPipeline.builder(MaLiLibPipelines.POSITION_MASA_STAGE)
                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/position/masa/lesser_depth/offset_3"))
                              .withCull(false)
                              .withDepthWrite(false)
                              .withDepthTestFunction(DepthTestFunction.LESS_DEPTH_TEST)
                              .withDepthBias(-3f, -3f)
                              .build();

        MaLiLibPipelines.POSITION_MASA_LESSER_DEPTH =
                RenderPipeline.builder(MaLiLibPipelines.POSITION_MASA_STAGE)
                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/position/masa/lesser_depth"))
                              .withCull(false)
                              .withDepthWrite(false)
                              .withDepthTestFunction(DepthTestFunction.LESS_DEPTH_TEST)
                              .build();

        MaLiLibPipelines.POSITION_MASA_GREATER_DEPTH =
                RenderPipeline.builder(MaLiLibPipelines.POSITION_MASA_STAGE)
                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/position/masa/greater_depth"))
                              .withCull(false)
                              .withDepthWrite(false)
                              .withDepthTestFunction(DepthTestFunction.GREATER_DEPTH_TEST)
                              .build();

        MaLiLibPipelines.POSITION_MASA_DEPTH_MASK =
                RenderPipeline.builder(MaLiLibPipelines.POSITION_MASA_STAGE)
                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/position/masa/depth_mask"))
                              .withCull(false)
                              .withDepthWrite(true)
                              .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                              .build();

        MaLiLibPipelines.POSITION_MASA =
                RenderPipeline.builder(MaLiLibPipelines.POSITION_MASA_STAGE)
                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/position/masa"))
                              .withCull(false)
                              .withDepthWrite(false)
                              .build();

        // POSITION_COLOR_TRANSLUCENT
        MaLiLibPipelines.POSITION_COLOR_TRANSLUCENT_NO_DEPTH_NO_CULL =
                RenderPipeline.builder(MaLiLibPipelines.POSITION_COLOR_TRANSLUCENT_STAGE)
                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/position_color/translucent/no_depth/no_cull"))
                              .withCull(false)
                              .withDepthWrite(false)
                              .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                              .build();

        MaLiLibPipelines.POSITION_COLOR_TRANSLUCENT_NO_DEPTH =
                RenderPipeline.builder(MaLiLibPipelines.POSITION_COLOR_TRANSLUCENT_STAGE)
                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/position_color/translucent/no_depth"))
                              .withDepthWrite(false)
                              .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                              .build();

        MaLiLibPipelines.POSITION_COLOR_TRANSLUCENT_LESSER_DEPTH_OFFSET_1 =
                RenderPipeline.builder(MaLiLibPipelines.POSITION_COLOR_TRANSLUCENT_STAGE)
                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/position_color/translucent/lesser_depth/offset_1"))
                              .withCull(false)
                              .withDepthWrite(false)
                              .withDepthTestFunction(DepthTestFunction.LESS_DEPTH_TEST)
                              .withDepthBias(-0.3f, -0.6f)
                              .build();

        MaLiLibPipelines.POSITION_COLOR_TRANSLUCENT_LESSER_DEPTH_OFFSET_2 =
                RenderPipeline.builder(MaLiLibPipelines.POSITION_COLOR_TRANSLUCENT_STAGE)
                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/position_color/translucent/lesser_depth/offset_2"))
                              .withCull(false)
                              .withDepthWrite(false)
                              .withDepthTestFunction(DepthTestFunction.LESS_DEPTH_TEST)
                              .withDepthBias(-0.4f, -0.8f)
                              .build();

        MaLiLibPipelines.POSITION_COLOR_TRANSLUCENT_LESSER_DEPTH_OFFSET_3 =
                RenderPipeline.builder(MaLiLibPipelines.POSITION_COLOR_TRANSLUCENT_STAGE)
                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/position_color/translucent/lesser_depth/offset_3"))
                              .withCull(false)
                              .withDepthWrite(false)
                              .withDepthTestFunction(DepthTestFunction.LESS_DEPTH_TEST)
                              .withDepthBias(-3f, -3f)
                              .build();

        MaLiLibPipelines.POSITION_COLOR_TRANSLUCENT_LESSER_DEPTH_OFFSET_4 =
                RenderPipeline.builder(MaLiLibPipelines.POSITION_COLOR_TRANSLUCENT_STAGE)
                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/position_color/translucent/lesser_depth/offset_4"))
                              .withCull(false)
                              .withDepthWrite(false)
                              .withDepthTestFunction(DepthTestFunction.LESS_DEPTH_TEST)
                              .withDepthBias(-0.6f, -1.2f)
                              .build();

        MaLiLibPipelines.POSITION_COLOR_TRANSLUCENT_LESSER_DEPTH =
                RenderPipeline.builder(MaLiLibPipelines.POSITION_COLOR_TRANSLUCENT_STAGE)
                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/position_color/translucent/lesser_depth"))
                              .withCull(false)
                              .withDepthWrite(false)
                              .withDepthTestFunction(DepthTestFunction.LESS_DEPTH_TEST)
                              .build();

        MaLiLibPipelines.POSITION_COLOR_TRANSLUCENT_GREATER_DEPTH =
                RenderPipeline.builder(MaLiLibPipelines.POSITION_COLOR_TRANSLUCENT_STAGE)
                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/position_color/translucent/greater_depth"))
                              .withCull(false)
                              .withDepthWrite(false)
                              .withDepthTestFunction(DepthTestFunction.GREATER_DEPTH_TEST)
                              .build();

        MaLiLibPipelines.POSITION_COLOR_TRANSLUCENT_DEPTH_MASK =
                RenderPipeline.builder(MaLiLibPipelines.POSITION_COLOR_TRANSLUCENT_STAGE)
                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/position_color/translucent/depth_mask"))
                              .withDepthWrite(true)
                              .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                              .build();

        MaLiLibPipelines.POSITION_COLOR_TRANSLUCENT =
                RenderPipeline.builder(MaLiLibPipelines.POSITION_COLOR_TRANSLUCENT_STAGE)
                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/position_color/translucent"))
                              .build();

        // POSITION_COLOR_MASA
        MaLiLibPipelines.POSITION_COLOR_MASA_NO_DEPTH_NO_CULL =
                RenderPipeline.builder(MaLiLibPipelines.POSITION_COLOR_MASA_STAGE)
                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/position_color/masa/no_depth/no_cull"))
                              .withCull(false)
                              .withDepthWrite(false)
                              .withColorWrite(true)
                              .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                              .build();

        MaLiLibPipelines.POSITION_COLOR_MASA_NO_DEPTH =
                RenderPipeline.builder(MaLiLibPipelines.POSITION_COLOR_MASA_STAGE)
                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/position_color/masa/no_depth"))
                              .withDepthWrite(false)
                              .withColorWrite(true)
                              .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                              .build();

        MaLiLibPipelines.POSITION_COLOR_MASA_LESSER_DEPTH_OFFSET_1 =
                RenderPipeline.builder(MaLiLibPipelines.POSITION_COLOR_MASA_STAGE)
                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/position_color/masa/lesser_depth/offset_1"))
                              .withCull(false)
                              .withDepthWrite(false)
                              .withColorWrite(true)
                              .withDepthTestFunction(DepthTestFunction.LESS_DEPTH_TEST)
                              .withDepthBias(-0.3f, -0.6f)
                              .build();

        MaLiLibPipelines.POSITION_COLOR_MASA_LESSER_DEPTH_OFFSET_2 =
                RenderPipeline.builder(MaLiLibPipelines.POSITION_COLOR_MASA_STAGE)
                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/position_color/masa/lesser_depth/offset_2"))
                              .withCull(false)
                              .withDepthWrite(false)
                              .withColorWrite(true)
                              .withDepthTestFunction(DepthTestFunction.LESS_DEPTH_TEST)
                              .withDepthBias(-0.4f, -0.8f)
                              .build();

        MaLiLibPipelines.POSITION_COLOR_MASA_LESSER_DEPTH_OFFSET_3 =
                RenderPipeline.builder(MaLiLibPipelines.POSITION_COLOR_MASA_STAGE)
                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/position_color/masa/lesser_depth/offset_3"))
                              .withCull(false)
                              .withDepthWrite(false)
                              .withColorWrite(true)
                              .withDepthTestFunction(DepthTestFunction.LESS_DEPTH_TEST)
                              .withDepthBias(-3f, -3f)
                              .build();

        MaLiLibPipelines.POSITION_COLOR_MASA_LESSER_DEPTH_OFFSET_4 =
                RenderPipeline.builder(MaLiLibPipelines.POSITION_COLOR_MASA_STAGE)
                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/position_color/masa/lesser_depth/offset_4"))
                              .withCull(false)
                              .withDepthWrite(false)
                              .withColorWrite(true)
                              .withDepthTestFunction(DepthTestFunction.LESS_DEPTH_TEST)
                              .withDepthBias(-0.6f, -1.2f)
                              .build();

        MaLiLibPipelines.POSITION_COLOR_MASA_LESSER_DEPTH =
                RenderPipeline.builder(MaLiLibPipelines.POSITION_COLOR_MASA_STAGE)
                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/position_color/masa/lesser_depth"))
                              .withCull(false)
                              .withDepthWrite(false)
                              .withColorWrite(true)
                              .withDepthTestFunction(DepthTestFunction.LESS_DEPTH_TEST)
                              .build();

        MaLiLibPipelines.POSITION_COLOR_MASA_GREATER_DEPTH =
                RenderPipeline.builder(MaLiLibPipelines.POSITION_COLOR_MASA_STAGE)
                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/position_color/masa/greater_depth"))
                              .withCull(false)
                              .withDepthWrite(false)
                              .withColorWrite(true)
                              .withDepthTestFunction(DepthTestFunction.GREATER_DEPTH_TEST)
                              .build();

        MaLiLibPipelines.POSITION_COLOR_MASA_DEPTH_MASK =
                RenderPipeline.builder(MaLiLibPipelines.POSITION_COLOR_MASA_STAGE)
                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/position_color/masa/depth_mask"))
                              .withDepthWrite(true)
                              .withColorWrite(true)
                              .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                              .build();

        MaLiLibPipelines.POSITION_COLOR_MASA =
                RenderPipeline.builder(MaLiLibPipelines.POSITION_COLOR_MASA_STAGE)
                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/position_color/masa"))
                              .withDepthWrite(false)
                              .withColorWrite(true)
                              .build();

        // POSITION_TEX_TRANSLUCENT
//        MaLiLibPipelines.POSITION_TEX_TRANSLUCENT_NO_DEPTH_NO_CULL =
//                RenderPipeline.builder(MaLiLibPipelines.POSITION_TEX_TRANSLUCENT_STAGE)
//                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/position_tex/translucent/no_depth/no_cull"))
//                              .withCull(false)
//                              .withDepthWrite(false)
//                              .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
//                              .build();
//
//        MaLiLibPipelines.POSITION_TEX_TRANSLUCENT_NO_DEPTH =
//                RenderPipeline.builder(MaLiLibPipelines.POSITION_TEX_TRANSLUCENT_STAGE)
//                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/position_tex/translucent/no_depth"))
//                              .withDepthWrite(false)
//                              .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
//                              .build();
//
//        MaLiLibPipelines.POSITION_TEX_TRANSLUCENT_LESSER_DEPTH_OFFSET_1 =
//                RenderPipeline.builder(MaLiLibPipelines.POSITION_TEX_TRANSLUCENT_STAGE)
//                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/position_tex/translucent/lesser_depth/offset_1"))
//                              .withCull(false)
//                              .withDepthWrite(false)
//                              .withDepthTestFunction(DepthTestFunction.LESS_DEPTH_TEST)
//                              .withDepthBias(-0.3f, -0.6f)
//                              .build();
//
//        MaLiLibPipelines.POSITION_TEX_TRANSLUCENT_LESSER_DEPTH_OFFSET_2 =
//                RenderPipeline.builder(MaLiLibPipelines.POSITION_TEX_TRANSLUCENT_STAGE)
//                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/position_tex/translucent/lesser_depth/offset_2"))
//                              .withCull(false)
//                              .withDepthWrite(false)
//                              .withDepthTestFunction(DepthTestFunction.LESS_DEPTH_TEST)
//                              .withDepthBias(-0.4f, -0.8f)
//                              .build();
//
//        MaLiLibPipelines.POSITION_TEX_TRANSLUCENT_LESSER_DEPTH_OFFSET_3 =
//                RenderPipeline.builder(MaLiLibPipelines.POSITION_TEX_TRANSLUCENT_STAGE)
//                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/position_tex/translucent/lesser_depth/offset_3"))
//                              .withCull(false)
//                              .withDepthWrite(false)
//                              .withDepthTestFunction(DepthTestFunction.LESS_DEPTH_TEST)
//                              .withDepthBias(-3f, -3f)
//                              .build();
//
//        MaLiLibPipelines.POSITION_TEX_TRANSLUCENT_LESSER_DEPTH =
//                RenderPipeline.builder(MaLiLibPipelines.POSITION_TEX_TRANSLUCENT_STAGE)
//                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/position_tex/translucent/lesser_depth"))
//                              .withCull(false)
//                              .withDepthWrite(false)
//                              .withDepthTestFunction(DepthTestFunction.LESS_DEPTH_TEST)
//                              .build();
//
//        MaLiLibPipelines.POSITION_TEX_TRANSLUCENT_GREATER_DEPTH =
//                RenderPipeline.builder(MaLiLibPipelines.POSITION_TEX_TRANSLUCENT_STAGE)
//                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/position_tex/translucent/greater_depth"))
//                              .withCull(false)
//                              .withDepthWrite(false)
//                              .withDepthTestFunction(DepthTestFunction.GREATER_DEPTH_TEST)
//                              .build();
//
//        MaLiLibPipelines.POSITION_TEX_TRANSLUCENT_DEPTH_MASK =
//                RenderPipeline.builder(MaLiLibPipelines.POSITION_TEX_TRANSLUCENT_STAGE)
//                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/position_tex/translucent/depth_mask"))
//                              .withDepthWrite(true)
//                              .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
//                              .build();
//
//        MaLiLibPipelines.POSITION_TEX_TRANSLUCENT =
//                RenderPipeline.builder(MaLiLibPipelines.POSITION_TEX_TRANSLUCENT_STAGE)
//                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/position_tex/translucent"))
//                              .build();
//
//        // POSITION_TEX_OVERLAY
//        MaLiLibPipelines.POSITION_TEX_OVERLAY_NO_DEPTH_NO_CULL =
//                RenderPipeline.builder(MaLiLibPipelines.POSITION_TEX_OVERLAY_STAGE)
//                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/position_tex/overlay/no_depth/no_cull"))
//                              .withCull(false)
//                              .withDepthWrite(false)
//                              .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
//                              .build();
//
//        MaLiLibPipelines.POSITION_TEX_OVERLAY_NO_DEPTH =
//                RenderPipeline.builder(MaLiLibPipelines.POSITION_TEX_OVERLAY_STAGE)
//                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/position_tex/overlay/no_depth"))
//                              .withDepthWrite(false)
//                              .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
//                              .build();
//
//        MaLiLibPipelines.POSITION_TEX_OVERLAY_LESSER_DEPTH_OFFSET_1 =
//                RenderPipeline.builder(MaLiLibPipelines.POSITION_TEX_OVERLAY_STAGE)
//                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/position_tex/overlay/lesser_depth/offset_1"))
//                              .withCull(false)
//                              .withDepthWrite(false)
//                              .withDepthTestFunction(DepthTestFunction.LESS_DEPTH_TEST)
//                              .withDepthBias(-0.3f, -0.6f)
//                              .build();
//
//        MaLiLibPipelines.POSITION_TEX_OVERLAY_LESSER_DEPTH_OFFSET_2 =
//                RenderPipeline.builder(MaLiLibPipelines.POSITION_TEX_OVERLAY_STAGE)
//                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/position_tex/overlay/lesser_depth/offset_2"))
//                              .withCull(false)
//                              .withDepthWrite(false)
//                              .withDepthTestFunction(DepthTestFunction.LESS_DEPTH_TEST)
//                              .withDepthBias(-0.4f, -0.8f)
//                              .build();
//
//        MaLiLibPipelines.POSITION_TEX_OVERLAY_LESSER_DEPTH_OFFSET_3 =
//                RenderPipeline.builder(MaLiLibPipelines.POSITION_TEX_OVERLAY_STAGE)
//                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/position_tex/overlay/lesser_depth/offset_3"))
//                              .withCull(false)
//                              .withDepthWrite(false)
//                              .withDepthTestFunction(DepthTestFunction.LESS_DEPTH_TEST)
//                              .withDepthBias(-3f, -3f)
//                              .build();
//
//        MaLiLibPipelines.POSITION_TEX_OVERLAY_LESSER_DEPTH =
//                RenderPipeline.builder(MaLiLibPipelines.POSITION_TEX_OVERLAY_STAGE)
//                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/position_tex/overlay/lesser_depth"))
//                              .withCull(false)
//                              .withDepthWrite(false)
//                              .withDepthTestFunction(DepthTestFunction.LESS_DEPTH_TEST)
//                              .build();
//
//        MaLiLibPipelines.POSITION_TEX_OVERLAY_GREATER_DEPTH =
//                RenderPipeline.builder(MaLiLibPipelines.POSITION_TEX_OVERLAY_STAGE)
//                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/position_tex/overlay/greater_depth"))
//                              .withCull(false)
//                              .withDepthWrite(false)
//                              .withDepthTestFunction(DepthTestFunction.GREATER_DEPTH_TEST)
//                              .build();
//
//        MaLiLibPipelines.POSITION_TEX_OVERLAY_DEPTH_MASK =
//                RenderPipeline.builder(MaLiLibPipelines.POSITION_TEX_OVERLAY_STAGE)
//                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/position_tex/overlay/depth_mask"))
//                              .withDepthWrite(true)
//                              .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
//                              .build();
//
//        MaLiLibPipelines.POSITION_TEX_OVERLAY =
//                RenderPipeline.builder(MaLiLibPipelines.POSITION_TEX_OVERLAY_STAGE)
//                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/position_tex/overlay"))
//                              .build();

        // POSITION_TEX_MASA
        MaLiLibPipelines.POSITION_TEX_MASA_NO_DEPTH_NO_CULL =
                RenderPipeline.builder(MaLiLibPipelines.POSITION_TEX_MASA_STAGE)
                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/position_tex/masa/no_depth/no_cull"))
                              .withCull(false)
                              .withDepthWrite(false)
                              .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                              .build();

        MaLiLibPipelines.POSITION_TEX_MASA_NO_DEPTH =
                RenderPipeline.builder(MaLiLibPipelines.POSITION_TEX_MASA_STAGE)
                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/position_tex/masa/no_depth"))
                              .withDepthWrite(false)
                              .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                              .build();

        MaLiLibPipelines.POSITION_TEX_MASA_LESSER_DEPTH_OFFSET_1 =
                RenderPipeline.builder(MaLiLibPipelines.POSITION_TEX_MASA_STAGE)
                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/position_tex/masa/lesser_depth/offset_1"))
                              .withCull(false)
                              .withDepthWrite(false)
                              .withDepthTestFunction(DepthTestFunction.LESS_DEPTH_TEST)
                              .withDepthBias(-0.3f, -0.6f)
                              .build();

        MaLiLibPipelines.POSITION_TEX_MASA_LESSER_DEPTH_OFFSET_2 =
                RenderPipeline.builder(MaLiLibPipelines.POSITION_TEX_MASA_STAGE)
                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/position_tex/masa/lesser_depth/offset_2"))
                              .withCull(false)
                              .withDepthWrite(false)
                              .withDepthTestFunction(DepthTestFunction.LESS_DEPTH_TEST)
                              .withDepthBias(-0.4f, -0.8f)
                              .build();

        MaLiLibPipelines.POSITION_TEX_MASA_LESSER_DEPTH_OFFSET_3 =
                RenderPipeline.builder(MaLiLibPipelines.POSITION_TEX_MASA_STAGE)
                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/position_tex/masa/lesser_depth/offset_3"))
                              .withCull(false)
                              .withDepthWrite(false)
                              .withDepthTestFunction(DepthTestFunction.LESS_DEPTH_TEST)
                              .withDepthBias(-3f, -3f)
                              .build();

        MaLiLibPipelines.POSITION_TEX_MASA_LESSER_DEPTH =
                RenderPipeline.builder(MaLiLibPipelines.POSITION_TEX_MASA_STAGE)
                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/position_tex/masa/lesser_depth"))
                              .withCull(false)
                              .withDepthWrite(false)
                              .withDepthTestFunction(DepthTestFunction.LESS_DEPTH_TEST)
                              .build();

        MaLiLibPipelines.POSITION_TEX_MASA_GREATER_DEPTH =
                RenderPipeline.builder(MaLiLibPipelines.POSITION_TEX_MASA_STAGE)
                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/position_tex/masa/greater_depth"))
                              .withCull(false)
                              .withDepthWrite(false)
                              .withDepthTestFunction(DepthTestFunction.GREATER_DEPTH_TEST)
                              .build();

        MaLiLibPipelines.POSITION_TEX_MASA_DEPTH_MASK =
                RenderPipeline.builder(MaLiLibPipelines.POSITION_TEX_MASA_STAGE)
                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/position_tex/masa/depth_mask"))
                              .withDepthWrite(true)
                              .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                              .build();

        MaLiLibPipelines.POSITION_TEX_MASA =
                RenderPipeline.builder(MaLiLibPipelines.POSITION_TEX_MASA_STAGE)
                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/position_tex/masa"))
                              .build();

        // POSITION_TEX_COLOR_TRANSLUCENT
        MaLiLibPipelines.POSITION_TEX_COLOR_TRANSLUCENT_NO_DEPTH_NO_CULL =
                RenderPipeline.builder(MaLiLibPipelines.POSITION_TEX_COLOR_TRANSLUCENT_STAGE)
                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/position_tex_color/translucent/no_depth/no_cull"))
                              .withCull(false)
                              .withDepthWrite(false)
                              .withColorWrite(true)
                              .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                              .build();

        MaLiLibPipelines.POSITION_TEX_COLOR_TRANSLUCENT_NO_DEPTH =
                RenderPipeline.builder(MaLiLibPipelines.POSITION_TEX_COLOR_TRANSLUCENT_STAGE)
                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/position_tex_color/translucent/no_depth"))
                              .withDepthWrite(false)
                              .withColorWrite(true)
                              .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                              .build();

        MaLiLibPipelines.POSITION_TEX_COLOR_TRANSLUCENT_LESSER_DEPTH_OFFSET_1 =
                RenderPipeline.builder(MaLiLibPipelines.POSITION_TEX_COLOR_TRANSLUCENT_STAGE)
                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/position_tex_color/translucent/lesser_depth/offset_1"))
                              .withCull(false)
                              .withDepthWrite(false)
                              .withColorWrite(true)
                              .withDepthTestFunction(DepthTestFunction.LESS_DEPTH_TEST)
                              .withDepthBias(-0.3f, -0.6f)
                              .build();

        MaLiLibPipelines.POSITION_TEX_COLOR_TRANSLUCENT_LESSER_DEPTH_OFFSET_2 =
                RenderPipeline.builder(MaLiLibPipelines.POSITION_TEX_COLOR_TRANSLUCENT_STAGE)
                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/position_tex_color/translucent/lesser_depth/offset_2"))
                              .withCull(false)
                              .withDepthWrite(false)
                              .withColorWrite(true)
                              .withDepthTestFunction(DepthTestFunction.LESS_DEPTH_TEST)
                              .withDepthBias(-0.4f, -0.8f)
                              .build();

        MaLiLibPipelines.POSITION_TEX_COLOR_TRANSLUCENT_LESSER_DEPTH_OFFSET_3 =
                RenderPipeline.builder(MaLiLibPipelines.POSITION_TEX_COLOR_TRANSLUCENT_STAGE)
                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/position_tex_color/translucent/lesser_depth/offset_3"))
                              .withCull(false)
                              .withDepthWrite(false)
                              .withColorWrite(true)
                              .withDepthTestFunction(DepthTestFunction.LESS_DEPTH_TEST)
                              .withDepthBias(-3f, -3f)
                              .build();

        MaLiLibPipelines.POSITION_TEX_COLOR_TRANSLUCENT_LESSER_DEPTH =
                RenderPipeline.builder(MaLiLibPipelines.POSITION_TEX_COLOR_TRANSLUCENT_STAGE)
                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/position_tex_color/translucent/lesser_depth"))
                              .withCull(false)
                              .withDepthWrite(false)
                              .withColorWrite(true)
                              .withDepthTestFunction(DepthTestFunction.LESS_DEPTH_TEST)
                              .build();

        MaLiLibPipelines.POSITION_TEX_COLOR_TRANSLUCENT_GREATER_DEPTH =
                RenderPipeline.builder(MaLiLibPipelines.POSITION_TEX_COLOR_TRANSLUCENT_STAGE)
                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/position_tex_color/translucent/greater_depth"))
                              .withCull(false)
                              .withDepthWrite(false)
                              .withColorWrite(true)
                              .withDepthTestFunction(DepthTestFunction.GREATER_DEPTH_TEST)
                              .build();

        MaLiLibPipelines.POSITION_TEX_COLOR_TRANSLUCENT_DEPTH_MASK =
                RenderPipeline.builder(MaLiLibPipelines.POSITION_TEX_COLOR_TRANSLUCENT_STAGE)
                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/position_tex_color/translucent/depth_mask"))
                              .withDepthWrite(true)
                              .withColorWrite(true)
                              .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                              .build();

        MaLiLibPipelines.POSITION_TEX_COLOR_TRANSLUCENT =
                RenderPipeline.builder(MaLiLibPipelines.POSITION_TEX_COLOR_TRANSLUCENT_STAGE)
                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/position_tex_color/translucent"))
                              .withDepthWrite(false)
                              .withColorWrite(true)
                              .build();

//        // POSITION_TEX_COLOR_OVERLAY
//        MaLiLibPipelines.POSITION_TEX_COLOR_OVERLAY_NO_DEPTH_NO_CULL =
//                RenderPipeline.builder(MaLiLibPipelines.POSITION_TEX_COLOR_OVERLAY_STAGE)
//                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/position_tex_color/overlay/no_depth/no_cull"))
//                              .withCull(false)
//                              .withDepthWrite(false)
//                              .withColorWrite(true)
//                              .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
//                              .build();
//
//        MaLiLibPipelines.POSITION_TEX_COLOR_OVERLAY_NO_DEPTH =
//                RenderPipeline.builder(MaLiLibPipelines.POSITION_TEX_COLOR_OVERLAY_STAGE)
//                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/position_tex_color/overlay/no_depth"))
//                              .withDepthWrite(false)
//                              .withColorWrite(true)
//                              .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
//                              .build();
//
//        MaLiLibPipelines.POSITION_TEX_COLOR_OVERLAY_LESSER_DEPTH_OFFSET_1 =
//                RenderPipeline.builder(MaLiLibPipelines.POSITION_TEX_COLOR_OVERLAY_STAGE)
//                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/position_tex_color/overlay/lesser_depth/offset_1"))
//                              .withCull(false)
//                              .withDepthWrite(false)
//                              .withColorWrite(true)
//                              .withDepthTestFunction(DepthTestFunction.LESS_DEPTH_TEST)
//                              .withDepthBias(-0.3f, -0.6f)
//                              .build();
//
//        MaLiLibPipelines.POSITION_TEX_COLOR_OVERLAY_LESSER_DEPTH_OFFSET_2 =
//                RenderPipeline.builder(MaLiLibPipelines.POSITION_TEX_COLOR_OVERLAY_STAGE)
//                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/position_tex_color/overlay/lesser_depth/offset_2"))
//                              .withCull(false)
//                              .withDepthWrite(false)
//                              .withColorWrite(true)
//                              .withDepthTestFunction(DepthTestFunction.LESS_DEPTH_TEST)
//                              .withDepthBias(-0.4f, -0.8f)
//                              .build();
//
//        MaLiLibPipelines.POSITION_TEX_COLOR_OVERLAY_LESSER_DEPTH_OFFSET_3 =
//                RenderPipeline.builder(MaLiLibPipelines.POSITION_TEX_COLOR_OVERLAY_STAGE)
//                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/position_tex_color/overlay/lesser_depth/offset_3"))
//                              .withCull(false)
//                              .withDepthWrite(false)
//                              .withColorWrite(true)
//                              .withDepthTestFunction(DepthTestFunction.LESS_DEPTH_TEST)
//                              .withDepthBias(-3f, -3f)
//                              .build();
//
//        MaLiLibPipelines.POSITION_TEX_COLOR_OVERLAY_LESSER_DEPTH =
//                RenderPipeline.builder(MaLiLibPipelines.POSITION_TEX_COLOR_OVERLAY_STAGE)
//                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/position_tex_color/overlay/lesser_depth"))
//                              .withCull(false)
//                              .withDepthWrite(false)
//                              .withColorWrite(true)
//                              .withDepthTestFunction(DepthTestFunction.LESS_DEPTH_TEST)
//                              .build();
//
//        MaLiLibPipelines.POSITION_TEX_COLOR_OVERLAY_GREATER_DEPTH =
//                RenderPipeline.builder(MaLiLibPipelines.POSITION_TEX_COLOR_OVERLAY_STAGE)
//                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/position_tex_color/overlay/greater_depth"))
//                              .withCull(false)
//                              .withDepthWrite(false)
//                              .withColorWrite(true)
//                              .withDepthTestFunction(DepthTestFunction.GREATER_DEPTH_TEST)
//                              .build();
//
//        MaLiLibPipelines.POSITION_TEX_COLOR_OVERLAY_DEPTH_MASK =
//                RenderPipeline.builder(MaLiLibPipelines.POSITION_TEX_COLOR_OVERLAY_STAGE)
//                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/position_tex_color/overlay/depth_mask"))
//                              .withDepthWrite(true)
//                              .withColorWrite(true)
//                              .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
//                              .build();
//
//        MaLiLibPipelines.POSITION_TEX_COLOR_OVERLAY =
//                RenderPipeline.builder(MaLiLibPipelines.POSITION_TEX_COLOR_OVERLAY_STAGE)
//                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/position_tex_color/overlay"))
//                              .withDepthWrite(false)
//                              .withColorWrite(true)
//                              .build();

        // POSITION_TEX_COLOR_MASA
        MaLiLibPipelines.POSITION_TEX_COLOR_MASA_NO_DEPTH_NO_CULL =
                RenderPipeline.builder(MaLiLibPipelines.POSITION_TEX_COLOR_MASA_STAGE)
                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/position_tex_color/masa/no_depth/no_cull"))
                              .withCull(false)
                              .withDepthWrite(false)
                              .withColorWrite(true)
                              .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                              .build();

        MaLiLibPipelines.POSITION_TEX_COLOR_MASA_NO_DEPTH =
                RenderPipeline.builder(MaLiLibPipelines.POSITION_TEX_COLOR_MASA_STAGE)
                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/position_tex_color/masa/no_depth"))
                              .withDepthWrite(false)
                              .withColorWrite(true)
                              .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                              .build();

        MaLiLibPipelines.POSITION_TEX_COLOR_MASA_LESSER_DEPTH_OFFSET_1 =
                RenderPipeline.builder(MaLiLibPipelines.POSITION_TEX_COLOR_MASA_STAGE)
                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/position_tex_color/masa/lesser_depth/offset_1"))
                              .withCull(false)
                              .withDepthWrite(false)
                              .withColorWrite(true)
                              .withDepthTestFunction(DepthTestFunction.LESS_DEPTH_TEST)
                              .withDepthBias(-0.3f, -0.6f)
                              .build();

        MaLiLibPipelines.POSITION_TEX_COLOR_MASA_LESSER_DEPTH_OFFSET_2 =
                RenderPipeline.builder(MaLiLibPipelines.POSITION_TEX_COLOR_MASA_STAGE)
                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/position_tex_color/masa/lesser_depth/offset_2"))
                              .withCull(false)
                              .withDepthWrite(false)
                              .withColorWrite(true)
                              .withDepthTestFunction(DepthTestFunction.LESS_DEPTH_TEST)
                              .withDepthBias(-0.4f, -0.8f)
                              .build();

        MaLiLibPipelines.POSITION_TEX_COLOR_MASA_LESSER_DEPTH_OFFSET_3 =
                RenderPipeline.builder(MaLiLibPipelines.POSITION_TEX_COLOR_MASA_STAGE)
                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/position_tex_color/masa/lesser_depth/offset_3"))
                              .withCull(false)
                              .withDepthWrite(false)
                              .withColorWrite(true)
                              .withDepthTestFunction(DepthTestFunction.LESS_DEPTH_TEST)
                              .withDepthBias(-3f, -3f)
                              .build();

        MaLiLibPipelines.POSITION_TEX_COLOR_MASA_LESSER_DEPTH =
                RenderPipeline.builder(MaLiLibPipelines.POSITION_TEX_COLOR_MASA_STAGE)
                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/position_tex_color/masa/lesser_depth"))
                              .withCull(false)
                              .withDepthWrite(false)
                              .withColorWrite(true)
                              .withDepthTestFunction(DepthTestFunction.LESS_DEPTH_TEST)
                              .build();

        MaLiLibPipelines.POSITION_TEX_COLOR_MASA_GREATER_DEPTH =
                RenderPipeline.builder(MaLiLibPipelines.POSITION_TEX_COLOR_MASA_STAGE)
                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/position_tex_color/masa/greater_depth"))
                              .withCull(false)
                              .withDepthWrite(false)
                              .withColorWrite(true)
                              .withDepthTestFunction(DepthTestFunction.GREATER_DEPTH_TEST)
                              .build();

        MaLiLibPipelines.POSITION_TEX_COLOR_MASA_DEPTH_MASK =
                RenderPipeline.builder(MaLiLibPipelines.POSITION_TEX_COLOR_MASA_STAGE)
                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/position_tex_color/masa/depth_mask"))
                              .withDepthWrite(true)
                              .withColorWrite(true)
                              .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                              .build();

        MaLiLibPipelines.POSITION_TEX_COLOR_MASA =
                RenderPipeline.builder(MaLiLibPipelines.POSITION_TEX_COLOR_MASA_STAGE)
                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/position_tex_color/masa"))
                              .withDepthWrite(false)
                              .withColorWrite(true)
                              .build();

        // LINES_TRANSLUCENT
//        MaLiLibPipelines.LINES_TRANSLUCENT_NO_DEPTH_NO_CULL =
//                RenderPipeline.builder(MaLiLibPipelines.LINES_TRANSLUCENT_STAGE)
//                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/lines/translucent/no_depth/no_cull"))
//                              .withCull(false)
//                              .withDepthWrite(false)
//                              .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
//                              .build();
//
//        MaLiLibPipelines.LINES_TRANSLUCENT_NO_DEPTH =
//                RenderPipeline.builder(MaLiLibPipelines.LINES_TRANSLUCENT_STAGE)
//                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/lines/translucent/no_depth"))
//                              .withDepthWrite(false)
//                              .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
//                              .build();
//
//        MaLiLibPipelines.LINES_TRANSLUCENT_NO_CULL =
//                RenderPipeline.builder(MaLiLibPipelines.LINES_TRANSLUCENT_STAGE)
//                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/lines/translucent/no_cull"))
//                              .withCull(false)
//                              .build();
//
//        MaLiLibPipelines.LINES_TRANSLUCENT_OFFSET_1 =
//                RenderPipeline.builder(MaLiLibPipelines.LINES_TRANSLUCENT_STAGE)
//                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/lines/translucent/offset_1"))
//                              .withDepthBias(-0.8f, -1.8f)
//                              .build();
//
//        MaLiLibPipelines.LINES_TRANSLUCENT_OFFSET_2 =
//                RenderPipeline.builder(MaLiLibPipelines.LINES_TRANSLUCENT_STAGE)
//                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/lines/translucent/offset_2"))
//                              .withDepthBias(-1.2f, -0.2f)
//                              .build();
//
//        MaLiLibPipelines.LINES_TRANSLUCENT_OFFSET_3 =
//                RenderPipeline.builder(MaLiLibPipelines.LINES_TRANSLUCENT_STAGE)
//                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/lines/translucent/offset_3"))
//                              .withDepthBias(-3.0f, -3.0f)
//                              .build();
//
//        MaLiLibPipelines.LINES_TRANSLUCENT =
//                RenderPipeline.builder(MaLiLibPipelines.LINES_TRANSLUCENT_STAGE)
//                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/lines/translucent"))
//                              .build();
//                // );

        // LINES_MASA_SIMPLE
        MaLiLibPipelines.LINES_MASA_SIMPLE_NO_DEPTH_NO_CULL =
                RenderPipeline.builder(MaLiLibPipelines.LINES_MASA_SIMPLE_STAGE)
                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/lines/masa_simple/no_depth/no_cull"))
                              .withCull(false)
                              .withDepthWrite(false)
                              .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                              .build();

        MaLiLibPipelines.LINES_MASA_SIMPLE_NO_DEPTH =
                RenderPipeline.builder(MaLiLibPipelines.LINES_MASA_SIMPLE_STAGE)
                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/lines/masa_simple/no_depth"))
                              .withDepthWrite(false)
                              .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                              .build();

        MaLiLibPipelines.LINES_MASA_SIMPLE_NO_CULL =
                RenderPipeline.builder(MaLiLibPipelines.LINES_MASA_SIMPLE_STAGE)
                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/lines/masa_simple/no_cull"))
                              .withCull(false)
                              .withDepthWrite(false)
                              .build();

        MaLiLibPipelines.LINES_MASA_SIMPLE_OFFSET_1 =
                RenderPipeline.builder(MaLiLibPipelines.LINES_MASA_SIMPLE_STAGE)
                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/lines/masa_simple/offset_1"))
                              .withDepthBias(-0.8f, -1.8f)
                              .withDepthWrite(false)
                              .build();

        MaLiLibPipelines.LINES_MASA_SIMPLE_OFFSET_2 =
                RenderPipeline.builder(MaLiLibPipelines.LINES_MASA_SIMPLE_STAGE)
                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/lines/masa_simple/offset_2"))
                              .withDepthBias(-1.2f, -0.2f)
                              .withDepthWrite(false)
//                              .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
                              .build();

        MaLiLibPipelines.LINES_MASA_SIMPLE_OFFSET_3 =
                RenderPipeline.builder(MaLiLibPipelines.LINES_MASA_SIMPLE_STAGE)
                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/lines/masa_simple/offset_3"))
                              .withDepthBias(-3.0f, -3.0f)
                              .withDepthWrite(false)
                              .build();

        MaLiLibPipelines.LINES_MASA_SIMPLE =
                RenderPipeline.builder(MaLiLibPipelines.LINES_MASA_SIMPLE_STAGE)
                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/lines/masa_simple"))
                              .withDepthWrite(false)
                              .build();

        // DEBUG_LINES_TRANSLUCENT
//        MaLiLibPipelines.DEBUG_LINES_TRANSLUCENT_NO_DEPTH_NO_CULL =
//                RenderPipeline.builder(MaLiLibPipelines.DEBUG_LINES_TRANSLUCENT_STAGE)
//                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/debug_lines/translucent/no_depth/no_cull"))
//                              .withCull(false)
//                              .withDepthWrite(false)
//                              .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
//                              .build();
//
//        MaLiLibPipelines.DEBUG_LINES_TRANSLUCENT_NO_DEPTH =
//                RenderPipeline.builder(MaLiLibPipelines.DEBUG_LINES_TRANSLUCENT_STAGE)
//                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/debug_lines/translucent/no_depth"))
//                              .withDepthWrite(false)
//                              .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
//                              .build();
//
//        MaLiLibPipelines.DEBUG_LINES_TRANSLUCENT_NO_CULL =
//                RenderPipeline.builder(MaLiLibPipelines.DEBUG_LINES_TRANSLUCENT_STAGE)
//                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/debug_lines/translucent/no_cull"))
//                              .withCull(false)
//                              .build();
//
//        MaLiLibPipelines.DEBUG_LINES_TRANSLUCENT_OFFSET_1 =
//                RenderPipeline.builder(MaLiLibPipelines.DEBUG_LINES_TRANSLUCENT_STAGE)
//                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/debug_lines/translucent/offset_1"))
//                              .withDepthBias(-0.8f, -1.8f)
//                              .build();
//
//        MaLiLibPipelines.DEBUG_LINES_TRANSLUCENT_OFFSET_2 =
//                RenderPipeline.builder(MaLiLibPipelines.DEBUG_LINES_TRANSLUCENT_STAGE)
//                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/debug_lines/translucent/offset_2"))
//                              .withDepthBias(-1.2f, -0.2f)
//                              .build();
//
//        MaLiLibPipelines.DEBUG_LINES_TRANSLUCENT_OFFSET_3 =
//                RenderPipeline.builder(MaLiLibPipelines.DEBUG_LINES_TRANSLUCENT_STAGE)
//                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/debug_lines/translucent/offset_3"))
//                              .withDepthBias(-3.0f, -3.0f)
//                              .build();
//
//        MaLiLibPipelines.DEBUG_LINES_TRANSLUCENT =
//                RenderPipeline.builder(MaLiLibPipelines.DEBUG_LINES_TRANSLUCENT_STAGE)
//                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/debug_lines/translucent"))
//                              .build();

        // DEBUG_LINES_MASA_SIMPLE
        MaLiLibPipelines.DEBUG_LINES_MASA_SIMPLE_NO_DEPTH_NO_CULL =
                RenderPipeline.builder(MaLiLibPipelines.DEBUG_LINES_MASA_SIMPLE_STAGE)
                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/debug_lines/masa_simple/no_depth/no_cull"))
                              .withCull(false)
                              .withDepthWrite(false)
                              .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                              .build();

        MaLiLibPipelines.DEBUG_LINES_MASA_SIMPLE_NO_DEPTH =
                RenderPipeline.builder(MaLiLibPipelines.DEBUG_LINES_MASA_SIMPLE_STAGE)
                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/debug_lines/masa_simple/no_depth"))
                              .withDepthWrite(false)
                              .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                              .build();
                // );

        MaLiLibPipelines.DEBUG_LINES_MASA_SIMPLE_NO_CULL =
                RenderPipeline.builder(MaLiLibPipelines.DEBUG_LINES_MASA_SIMPLE_STAGE)
                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/debug_lines/masa_simple/no_cull"))
                              .withCull(false)
                              .withDepthWrite(false)
                              .build();

        MaLiLibPipelines.DEBUG_LINES_MASA_SIMPLE_OFFSET_1 =
                RenderPipeline.builder(MaLiLibPipelines.DEBUG_LINES_MASA_SIMPLE_STAGE)
                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/debug_lines/masa_simple/offset_1"))
                              .withDepthBias(-0.8f, -1.8f)
                              .withDepthWrite(false)
                              .build();

        MaLiLibPipelines.DEBUG_LINES_MASA_SIMPLE_OFFSET_2 =
                RenderPipeline.builder(MaLiLibPipelines.DEBUG_LINES_MASA_SIMPLE_STAGE)
                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/debug_lines/masa_simple/offset_2"))
                              .withDepthBias(-1.2f, -0.2f)
                              .withDepthWrite(false)
                              .build();

        MaLiLibPipelines.DEBUG_LINES_MASA_SIMPLE_OFFSET_3 =
                RenderPipeline.builder(MaLiLibPipelines.DEBUG_LINES_MASA_SIMPLE_STAGE)
                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/debug_lines/masa_simple/offset_3"))
                              .withDepthBias(-3.0f, -3.0f)
                              .withDepthWrite(false)
                              .build();

        MaLiLibPipelines.DEBUG_LINES_MASA_SIMPLE =
                RenderPipeline.builder(MaLiLibPipelines.DEBUG_LINES_MASA_SIMPLE_STAGE)
                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/debug_lines/masa_simple"))
                              .withDepthWrite(false)
                              .build();

        // TERRAIN_MASA_OFFSET
        MaLiLibPipelines.SOLID_MASA_OFFSET =
                RenderPipeline.builder(MaLiLibPipelines.TERRAIN_MASA_STAGE)
                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/solid/masa/offset"))
                              .withDepthBias(-0.3f, -0.6f)
                              .build();

        MaLiLibPipelines.WIREFRAME_MASA_OFFSET =
                RenderPipeline.builder(MaLiLibPipelines.TERRAIN_MASA_STAGE)
                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/wireframe/masa/offset"))
                              .withPolygonMode(PolygonMode.WIREFRAME)
                              .withDepthBias(-0.3f, -0.6f)
                              .build();

        MaLiLibPipelines.CUTOUT_MIPPED_MASA_OFFSET =
                RenderPipeline.builder(MaLiLibPipelines.TERRAIN_MASA_STAGE)
                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/cutout_mipped/masa/offset"))
                              .withShaderDefine("ALPHA_CUTOUT", 0.5F)
                              .withDepthBias(-0.3f, -0.6f)
                              .build();

        MaLiLibPipelines.CUTOUT_MASA_OFFSET =
                RenderPipeline.builder(MaLiLibPipelines.TERRAIN_MASA_STAGE)
                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/cutout/masa/offset"))
                              .withShaderDefine("ALPHA_CUTOUT", 0.1F)
                              .withDepthBias(-0.3f, -0.6f)
                              .build();

        MaLiLibPipelines.TRANSLUCENT_MASA_OFFSET =
                RenderPipeline.builder(MaLiLibPipelines.TERRAIN_MASA_STAGE)
                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/translucent/masa/offset"))
                              .withDepthBias(-0.3f, -0.6f)
                              .build();

        MaLiLibPipelines.TRIPWIRE_MASA_OFFSET =
                RenderPipeline.builder(MaLiLibPipelines.TERRAIN_MASA_STAGE)
                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/tripwire/masa/offset"))
                              .withShaderDefine("ALPHA_CUTOUT", 0.1F)
                              .withDepthBias(-0.3f, -0.6f)
                              .build();

        // TERRAIN_MASA
        MaLiLibPipelines.SOLID_MASA =
                RenderPipeline.builder(MaLiLibPipelines.TERRAIN_MASA_STAGE)
                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/solid/masa"))
                              .build();

        MaLiLibPipelines.WIREFRAME_MASA =
                RenderPipeline.builder(MaLiLibPipelines.TERRAIN_MASA_STAGE)
                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/wireframe/masa"))
                              .withPolygonMode(PolygonMode.WIREFRAME)
                              .build();

        MaLiLibPipelines.CUTOUT_MIPPED_MASA =
                RenderPipeline.builder(MaLiLibPipelines.TERRAIN_MASA_STAGE)
                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/cutout_mipped/masa"))
                              .withShaderDefine("ALPHA_CUTOUT", 0.5F)
                              .build();

        MaLiLibPipelines.CUTOUT_MASA =
                RenderPipeline.builder(MaLiLibPipelines.TERRAIN_MASA_STAGE)
                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/cutout/masa"))
                              .withShaderDefine("ALPHA_CUTOUT", 0.1F)
                              .build();

        MaLiLibPipelines.TRANSLUCENT_MASA =
                RenderPipeline.builder(MaLiLibPipelines.TERRAIN_MASA_STAGE)
                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/translucent/masa"))
                              .build();

        MaLiLibPipelines.TRIPWIRE_MASA =
                RenderPipeline.builder(MaLiLibPipelines.TERRAIN_MASA_STAGE)
                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/tripwire/masa"))
                              .withShaderDefine("ALPHA_CUTOUT", 0.1F)
                              .build();

        // MINIHUD_SHAPE
        MaLiLibPipelines.MINIHUD_SHAPE_NO_DEPTH_OFFSET =
                RenderPipeline.builder(MaLiLibPipelines.POSITION_COLOR_TRANSLUCENT_STAGE)
                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/minihud/shape/no_depth/offset"))
                              .withDepthBias(-3.0f, -3.0f)
                              .withCull(false)
                              .withDepthWrite(false)
                              .withColorWrite(true)
                              .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                              .build();

        MaLiLibPipelines.MINIHUD_SHAPE_NO_DEPTH =
                RenderPipeline.builder(MaLiLibPipelines.POSITION_COLOR_TRANSLUCENT_STAGE)
                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/minihud/shape/no_depth"))
                              .withCull(false)
                              .withDepthWrite(false)
                              .withColorWrite(true)
                              .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                              .build();

        MaLiLibPipelines.MINIHUD_SHAPE_OFFSET =
                RenderPipeline.builder(MaLiLibPipelines.POSITION_COLOR_TRANSLUCENT_STAGE)
                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/minihud/shape/offset"))
                              .withDepthBias(-3.0f, -3.0f)
                              .withCull(false)
                              .withDepthWrite(false)
                              .withColorWrite(true)
                              .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
                              .build();

        MaLiLibPipelines.MINIHUD_SHAPE_DEPTH_MASK =
                RenderPipeline.builder(MaLiLibPipelines.POSITION_COLOR_TRANSLUCENT_STAGE)
                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/minihud/shape/depth_mask"))
                              .withCull(false)
                              .withDepthWrite(true)
                              .withColorWrite(true)
                              .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                              .build();

        MaLiLibPipelines.MINIHUD_SHAPE =
                RenderPipeline.builder(MaLiLibPipelines.POSITION_COLOR_TRANSLUCENT_STAGE)
                              .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/minihud/shape"))
                              .withCull(false)
                              .withDepthWrite(false)
                              .withColorWrite(true)
                              .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
                              .build();

        // Try registering with Iris.
        IrisCompat.registerPipelines();
    }
}
