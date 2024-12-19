package fi.dy.masa.malilib.event;

import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Matrix4f;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.util.Handle;
import net.minecraft.item.ItemStack;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.profiler.Profilers;

import fi.dy.masa.malilib.MaLiLibReference;
import fi.dy.masa.malilib.interfaces.IRenderDispatcher;
import fi.dy.masa.malilib.interfaces.IRenderer;
import fi.dy.masa.malilib.util.InfoUtils;

public class RenderEventHandler implements IRenderDispatcher
{
    private static final RenderEventHandler INSTANCE = new RenderEventHandler();

    private final List<IRenderer> overlayRenderers = new ArrayList<>();
    private final List<IRenderer> tooltipLastRenderers = new ArrayList<>();
    private final List<IRenderer> worldPreWeatherRenderers = new ArrayList<>();
    private final List<IRenderer> worldLastRenderers = new ArrayList<>();

    public static IRenderDispatcher getInstance()
    {
        return INSTANCE;
    }

    @Override
    public void registerGameOverlayRenderer(IRenderer renderer)
    {
        if (this.overlayRenderers.contains(renderer) == false)
        {
            this.overlayRenderers.add(renderer);
        }
    }

    @Override
    public void registerTooltipLastRenderer(IRenderer renderer)
    {
        if (this.tooltipLastRenderers.contains(renderer) == false)
        {
            this.tooltipLastRenderers.add(renderer);
        }
    }

    @Override
    public void registerWorldPreWeatherRenderer(IRenderer renderer)
    {
        if (this.worldPreWeatherRenderers.contains(renderer) == false)
        {
            this.worldPreWeatherRenderers.add(renderer);
        }
    }

    @Override
    public void registerWorldLastRenderer(IRenderer renderer)
    {
        if (this.worldLastRenderers.contains(renderer) == false)
        {
            this.worldLastRenderers.add(renderer);
        }
    }

    @ApiStatus.Internal
    public void onRenderGameOverlayLastDrawer(DrawContext drawContext, MinecraftClient mc, float partialTicks)
    {
        Profiler profiler = Profilers.get();

        profiler.push(MaLiLibReference.MOD_ID+"_overlay_last_drawer");

        if (this.overlayRenderers.isEmpty() == false)
        {
            for (IRenderer renderer : this.overlayRenderers)
            {
                profiler.push(renderer.getProfilerSectionSupplier());
                renderer.onRenderGameOverlayLastDrawer(drawContext, partialTicks, profiler, mc);
                profiler.pop();
            }
        }

        profiler.pop();
    }

    @ApiStatus.Internal
    public void onRenderGameOverlayPost(DrawContext drawContext, MinecraftClient mc, float partialTicks)
    {
        Profiler profiler = Profilers.get();

        profiler.push(MaLiLibReference.MOD_ID+"_game_overlay");

        if (this.overlayRenderers.isEmpty() == false)
        {
            for (IRenderer renderer : this.overlayRenderers)
            {
                profiler.push(renderer.getProfilerSectionSupplier());
                renderer.onRenderGameOverlayPostAdvanced(drawContext, partialTicks, profiler, mc);
                renderer.onRenderGameOverlayPost(drawContext);
                profiler.pop();
            }
        }

        profiler.swap(MaLiLibReference.MOD_ID+"_game_messages");
        InfoUtils.renderInGameMessages(drawContext);
        profiler.pop();
    }

    @ApiStatus.Internal
    public void onRenderTooltipLast(DrawContext drawContext, ItemStack stack, int x, int y)
    {
        Profiler profiler = Profilers.get();

        profiler.push(MaLiLibReference.MOD_ID+"_tooltip");

        if (this.tooltipLastRenderers.isEmpty() == false)
        {
            for (IRenderer renderer : this.tooltipLastRenderers)
            {
                profiler.swap(renderer.getProfilerSectionSupplier());
                renderer.onRenderTooltipLast(drawContext ,stack, x, y);
            }
        }

        profiler.pop();
    }

    @ApiStatus.Internal
    public void runRenderWorldPreWeather(Matrix4f posMatrix, Matrix4f projMatrix, MinecraftClient mc,
                                         FrameGraphBuilder frameGraphBuilder, DefaultFramebufferSet fbSet,
                                         Frustum frustum, Camera camera, Profiler profiler)
    {
        profiler.push(MaLiLibReference.MOD_ID+"_pre_weather");

        if (this.worldPreWeatherRenderers.isEmpty() == false)
        {
            Handle<Framebuffer> handleMain;
            //Handle<Framebuffer> handleTranslucent;
            RenderPass renderPass = frameGraphBuilder.createPass(MaLiLibReference.MOD_ID+"_pre_weather");

            /*
            if (fbSet.translucentFramebuffer != null)
            {
                fbSet.translucentFramebuffer = renderPass.transfer(fbSet.translucentFramebuffer);
                handleTranslucent = fbSet.translucentFramebuffer;
            }
            else
            {
                handleTranslucent = null;
            }
             */

            fbSet.mainFramebuffer = renderPass.transfer(fbSet.mainFramebuffer);
            handleMain = fbSet.mainFramebuffer;

            renderPass.setRenderer(() ->
            {
                Fog fog = RenderSystem.getShaderFog();
                ShaderProgram shaders = RenderSystem.getShader();

                /*
                if (handleTranslucent != null)
                {
                    handleTranslucent.get().setClearColor(0.0f, 0.0f, 0.0f, 0.0f);
                    handleTranslucent.get().clear();
                    handleTranslucent.get().copyDepthFrom(handleMain.get());
                }
                 */

                if (shaders != null)
                {
                    shaders.initializeUniforms(VertexFormat.DrawMode.QUADS, posMatrix, projMatrix, mc.getWindow());
                    shaders.bind();
                }

                /*
                if (handleTranslucent != null)
                {
                    handleTranslucent.get().beginWrite(false);
                }
                 */
                handleMain.get().beginWrite(false);
                for (IRenderer renderer : this.worldPreWeatherRenderers)
                {
                    profiler.push(renderer.getProfilerSectionSupplier());
                    renderer.onRenderWorldPreWeather(posMatrix, projMatrix, frustum, camera, fog, profiler);
                    profiler.pop();
                }

                if (shaders != null)
                {
                    shaders.unbind();
                }
            });
        }

        profiler.pop();
    }

    @ApiStatus.Internal
    public void runRenderWorldLast(Matrix4f posMatrix, Matrix4f projMatrix, MinecraftClient mc,
                                   FrameGraphBuilder frameGraphBuilder, DefaultFramebufferSet fbSet,
                                   Frustum frustum, Camera camera, Profiler profiler)
    {
        profiler.push(MaLiLibReference.MOD_ID+"_world_last");

        if (this.worldLastRenderers.isEmpty() == false)
        {
            Handle<Framebuffer> handleMain;
            //Handle<Framebuffer> handleTranslucent;
            RenderPass renderPass = frameGraphBuilder.createPass(MaLiLibReference.MOD_ID+"_world_last");

            // Don't write to translucent Frame Buffer, bad things will happen,
            //  at Best, the Player will be able to see through objects ...
            //  or just through the world ...
            /*
            if (fbSet.translucentFramebuffer != null)
            {
                fbSet.translucentFramebuffer = renderPass.transfer(fbSet.translucentFramebuffer);
                handleTranslucent = fbSet.translucentFramebuffer;
            }
            else
            {
                handleTranslucent = null;
            }
             */

            fbSet.mainFramebuffer = renderPass.transfer(fbSet.mainFramebuffer);
            handleMain = fbSet.mainFramebuffer;

            renderPass.setRenderer(() ->
            {
                Fog fog = RenderSystem.getShaderFog();
                ShaderProgram shaders = RenderSystem.getShader();

                //RenderSystem.setShaderFog(Fog.DUMMY);

                /*
                if (handleTranslucent != null)
                {
                    handleTranslucent.get().setClearColor(0.0f, 0.0f, 0.0f, 0.0f);
                    handleTranslucent.get().clear();
                    handleTranslucent.get().copyDepthFrom(handleMain.get());
                }
                 */

                if (shaders != null)
                {
                    shaders.initializeUniforms(VertexFormat.DrawMode.QUADS, posMatrix, projMatrix, mc.getWindow());
                    shaders.bind();
                }

                /*
                Framebuffer fb = null;
                if (fbSet.translucentFramebuffer != null && mc.worldRenderer != null)
                {
                    try
                    {
                        fb = MinecraftClient.isFabulousGraphicsOrBetter() ? fbSet.translucentFramebuffer.get() : null;
                    }
                    catch (Exception e)
                    {
                        MaLiLib.logger.warn("onRenderWorldPost: getTranslucentFramebuffer() throw: [{}]", e.getMessage());
                    }
                }

                if (fb != null)
                {
                    fb.beginWrite(false);
                }
                 */
                // Trusty and reliable Main Framebuffer write.
                handleMain.get().beginWrite(false);

                for (IRenderer renderer : this.worldLastRenderers)
                {
                    profiler.push(renderer.getProfilerSectionSupplier());
                    // This really should be used either or, and never both in the same mod.
                    renderer.onRenderWorldLastAdvanced(posMatrix, projMatrix, frustum, camera, fog, profiler);
                    renderer.onRenderWorldLast(posMatrix, projMatrix);
                    profiler.pop();
                }

                /*
                if (fb != null)
                {
                    mc.getFramebuffer().beginWrite(false);
                }
                 */

                if (shaders != null)
                {
                    shaders.unbind();
                }

                //RenderSystem.setShaderFog(fog);
            });
        }

        profiler.pop();
    }
}
