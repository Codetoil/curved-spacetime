package io.codetoil.curved_spacetime.loader.closed_world;

import io.codetoil.curved_spacetime.engine.Engine;
import org.tinylog.Logger;

public class Main
{
	static void main(String[] args)
	{
		Logger.info("Starting closed-world version of Engine!");
		Engine.main(args, new CurvedSpacetimeLoaderClosedLoader());
	}
}
