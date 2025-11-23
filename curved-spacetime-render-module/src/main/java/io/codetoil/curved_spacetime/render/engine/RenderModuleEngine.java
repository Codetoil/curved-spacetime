package io.codetoil.curved_spacetime.render.engine;

import com.google.common.collect.Sets;
import io.codetoil.curved_spacetime.MainCallback;
import io.codetoil.curved_spacetime.engine.Engine;
import io.codetoil.curved_spacetime.render.scene_renderer.RenderModuleSceneRendererCallback;
import io.codetoil.curved_spacetime.render.scene_renderer.RenderModuleSceneRenderer;
import io.codetoil.curved_spacetime.render.scene_renderer.RenderModuleSceneRendererCallbackSupplier;
import org.tinylog.Logger;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

public class RenderModuleEngine implements MainCallback
{
	protected final Set<RenderModuleSceneRenderer> sceneRenderers = new HashSet<>();
	private final Set<RenderModuleSceneRendererCallback> sceneRendererCallbacks = Sets.newConcurrentHashSet();

	@Override
	public void init()
	{
		Logger.info("Accumulating Scene Renderer Callbacks");
		Engine.getInstance().accumulateCallbacks(RenderModuleSceneRendererCallbackSupplier.class,
				this.sceneRenderers, this.sceneRendererCallbacks);
		Logger.info("Initializing Scene Renderer Callbacks");
		this.sceneRendererCallbacks.forEach(RenderModuleSceneRendererCallback::init);
	}

	@Override
	public void loop()
	{
		this.sceneRendererCallbacks.forEach(RenderModuleSceneRendererCallback::loop);
	}

	@Override
	public void clean()
	{
		this.sceneRenderers.forEach(this::deregisterRenderEnvironment);
	}

	public void deregisterRenderEnvironment(RenderModuleSceneRenderer renderModuleSceneRenderer)
	{
		this.sceneRendererCallbacks.stream()
				.filter(callback ->
						Objects.equals(callback.renderEnvironment(), renderModuleSceneRenderer))
				.toList().forEach(callback -> {
					callback.clean();
					this.sceneRendererCallbacks.remove(callback);
				});
		this.sceneRenderers.remove(renderModuleSceneRenderer);
	}

	public Set<RenderModuleSceneRendererCallback> registerSceneRenderCallbackAndInit
			(Function<RenderModuleSceneRenderer, RenderModuleSceneRendererCallback> callbackSupplier)
	{
		return Engine.getInstance().registerCallbackAndInit(callbackSupplier, this.sceneRenderers,
				this.sceneRendererCallbacks, RenderModuleSceneRendererCallback::init);
	}

	public Set<RenderModuleSceneRenderer> getSceneRenderers()
	{
		return sceneRenderers;
	}

	public Set<RenderModuleSceneRendererCallback> getSceneRendererCallbacks()
	{
		return sceneRendererCallbacks;
	}
}
