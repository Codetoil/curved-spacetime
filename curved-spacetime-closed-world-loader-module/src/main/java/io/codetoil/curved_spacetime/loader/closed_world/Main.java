package io.codetoil.curved_spacetime.loader.closed_world;

import io.codetoil.curved_spacetime.MainModuleEngine;

import java.util.logging.Logger;

public class Main
{
	static void main(String[] args)
	{
		Logger.getGlobal().info("Starting closed-world version of Engine!");
		MainModuleEngine.start(args, new CurvedSpacetimeLoaderClosedLoader());
	}
}
