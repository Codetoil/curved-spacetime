/**
 * Curved Spacetime is an easy-to-use modular simulator for General Relativity.<br> Copyright (C) 2025 Anthony Michalek
 * (Codetoil)<br>
 * <br>
 * This file is part of Curved Spacetime<br>
 * <br>
 * This program is free software: you can redistribute it and/or modify <br> it under the terms of the GNU General
 * Public License as published by <br> the Free Software Foundation, either version 3 of the License, or <br> (at your
 * option) any later version.<br>
 * <br>
 * This program is distributed in the hope that it will be useful,<br> but WITHOUT ANY WARRANTY; without even the
 * implied warranty of<br> MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the<br> GNU General Public License
 * for more details.<br>
 * <br>
 * You should have received a copy of the GNU General Public License<br> along with this program.  If not, see <a
 * href="https://www.gnu.org/licenses/">https://www.gnu.org/licenses/</a>.<br>
 */

package io.codetoil.curved_spacetime.api;

import io.codetoil.curved_spacetime.api.entrypoint.ModuleInitializer;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Flow;
import java.util.concurrent.Flow.Subscription;
import java.util.function.Consumer;

public class ModuleDependentFlowSubscriber
		implements Flow.Subscriber<ModuleInitializer>
{
	private final List<ModuleInitializer> moduleInitializers = new ArrayList<>();
	private final Consumer<Collection<ModuleInitializer>> onFinish;

	public ModuleDependentFlowSubscriber(Consumer<Collection<ModuleInitializer>> onFinish)
	{
		this.onFinish = onFinish;
	}

	@Override
	public void onSubscribe(Subscription subscription)
	{
		subscription.request(1);
	}

	@Override
	public void onNext(ModuleInitializer item)
	{
		moduleInitializers.add(item);
	}

	@Override
	public void onError(Throwable throwable)
	{
		Logger.error(throwable);
	}

	@Override
	public void onComplete()
	{
		onFinish.accept(moduleInitializers);
	}
}
