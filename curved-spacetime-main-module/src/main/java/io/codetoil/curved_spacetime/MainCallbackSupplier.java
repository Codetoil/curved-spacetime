package io.codetoil.curved_spacetime;

import java.util.function.Function;
import java.util.function.Supplier;

public record MainCallbackSupplier(Supplier<MainCallback> supplier)
		implements Function<Void, MainCallback>
{
	public MainCallback apply(Void args) {
		return supplier.get();
	}
}