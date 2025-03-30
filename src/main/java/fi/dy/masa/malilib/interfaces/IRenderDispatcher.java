package fi.dy.masa.malilib.interfaces;

public interface IRenderDispatcher
{
    /**
     * Registers a renderer which will have its {@link IRenderer.onRenderGameOverlayPost}
     * method called after the vanilla rendering is done
     * @param renderer
     */
    void registerGameOverlayRenderer(IRenderer renderer);

    /**
     * Registers a renderer which will have its {@link IRenderer.onRenderTooltipLast}
     * method called after the vanilla tooltip text has been rendered.
     * @param renderer
     */
    void registerTooltipLastRenderer(IRenderer renderer);

    /**
     * Registers a renderer which will have its {@link IRenderer.onRenderWorldPreMain}
     * method called before the vanilla Weather rendering is done
     * @param renderer
     */
//    void registerWorldPreMainRenderer(IRenderer renderer);

    /**
     * Registers a renderer which will have its {@link IRenderer.onRenderWorldLayerPass}
     * method called after the vanilla Debug rendering is done
     * @param renderer
     */
    void registerWorldLayerPassRenderer(IRenderer renderer);

    /**
     * Registers a renderer which will have its {@link IRenderer.onRenderWorldPostDebugRender}
     * method called after the vanilla Debug rendering is done
     * @param renderer
     */
    void registerWorldPostDebugRenderer(IRenderer renderer);

    /**
     * Registers a renderer which will have its {@link IRenderer.onRenderWorldPreParticle}
     * method called before the vanilla Weather rendering is done
     * @param renderer
     */
//    void registerWorldPreParticleRenderer(IRenderer renderer);

    /**
     * Registers a renderer which will have its {@link IRenderer.onRenderWorldPreWeather}
     * method called before the vanilla Weather rendering is done
     * @param renderer
     */
    void registerWorldPreWeatherRenderer(IRenderer renderer);

    /**
     * Registers a renderer which will have its {@link IRenderer.onRenderWorldPost}
     * method called after the vanilla rendering is done
     * @param renderer
     */
    void registerWorldLastRenderer(IRenderer renderer);
}
