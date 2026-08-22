/**
 * Curved Spacetime is a work-in-progress easy-to-use modular simulator for General Relativity.<br> Copyright (C) 2025
 * Anthony Michalek (Codetoil)<br> Copyright (c) 2025 Antonio Hernández Bejarano<br>
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

package io.codetoil.curved_spacetime.loader.entrypoint;

import java.io.IOException;

/**
 * A module's persisted settings, backed by a {@code java.util.Properties} file under
 * {@code config/}.
 * <p>
 * A configuration is never allowed to fail because its file is absent or incomplete. Loading
 * substitutes a documented default for every key it cannot read, records that it did so, and the
 * owning module then writes the completed file back:
 * <pre>{@code
 * this.config = new XModuleConfig(this.logger).load();
 * if (this.config.isDirty()) this.config.save();
 * }</pre>
 * <p>
 * Implementations must satisfy requirements R31 through R37 of the Module System Specification.
 */
public interface ModuleConfig
{
	/**
	 * Reads this configuration from disk, substituting defaults for anything missing or invalid.
	 * <p>
	 * An absent file is not an error. For each key that is missing or fails to parse, an
	 * implementation logs a warning identifying the key and the accepted range, substitutes the
	 * documented default, and sets the dirty flag.
	 *
	 * @return this configuration, so that construction and loading compose
	 * @throws IOException if the file exists but cannot be read
	 */
	ModuleConfig load() throws IOException;

	/**
	 * Writes this configuration to disk, creating {@code config/} if it does not exist.
	 * <p>
	 * Every known key is written, not only those that changed, so that a user who deletes the
	 * file gets a complete one back on the next run. The dirty flag is cleared on success.
	 *
	 * @throws IOException if the directory cannot be created or the file cannot be written
	 */
	void save() throws IOException;

	/**
	 * Returns whether this configuration differs from what is on disk.
	 * <p>
	 * Set whenever the in-memory state diverges from the file. {@link #load()} substituting a
	 * default for a missing or unparseable key is the usual cause, but any later change to a value
	 * counts equally. {@link #save()} clears it, since writing the file makes the two agree again.
	 *
	 * @return {@code true} if the file does not match this configuration and should be rewritten
	 */
	boolean isDirty();
}
