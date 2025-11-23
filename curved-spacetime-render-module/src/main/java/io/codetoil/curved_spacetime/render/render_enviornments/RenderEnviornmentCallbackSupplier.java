package io.codetoil.curved_spacetime.render.render_enviornments;

import java.util.function.Function;

public record RenderEnviornmentCallbackSupplier(Function<RenderEnvironment, RenderEnviornmentCallback> supplier)
		implements Function<RenderEnvironment, RenderEnviornmentCallback>
{
	public RenderEnviornmentCallback apply(RenderEnvironment args)
	{
		return supplier.apply(args);
	}
}
