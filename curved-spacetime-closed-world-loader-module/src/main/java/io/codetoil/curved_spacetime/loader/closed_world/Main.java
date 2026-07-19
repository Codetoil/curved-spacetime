package io.codetoil.curved_spacetime.loader.closed_world;

import io.codetoil.curved_spacetime.Start;

import java.util.logging.Logger;

public class Main
{
	static void main(String[] args)
	{
		Logger.getGlobal().info("Starting closed-world version of Engine!");
		Start.start(args, new CurvedSpacetimeLoaderClosedLoader());
	}
}
