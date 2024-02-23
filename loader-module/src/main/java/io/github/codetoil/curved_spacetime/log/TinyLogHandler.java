/**
 * Curved Spacetime is an easy-to-use modular simulator for General Relativity.<br>
 * Copyright (C) 2023-2024 Anthony Michalek (Codetoil)<br>
 * <br>
 * This file is part of Curved Spacetime<br>
 * <br>
 * This program is free software: you can redistribute it and/or modify <br>
 * it under the terms of the GNU General Public License as published by <br>
 * the Free Software Foundation, either version 3 of the License, or <br>
 * (at your option) any later version.<br>
 * <br>
 * This program is distributed in the hope that it will be useful,<br>
 * but WITHOUT ANY WARRANTY; without even the implied warranty of<br>
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the<br>
 * GNU General Public License for more details.<br>
 * <br>
 * You should have received a copy of the GNU General Public License<br>
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.<br>
 */

package io.github.codetoil.curved_spacetime.log;

import org.quiltmc.loader.impl.util.log.LogCategory;
import org.quiltmc.loader.impl.util.log.LogHandler;
import org.quiltmc.loader.impl.util.log.LogLevel;
import org.tinylog.Logger;

public class TinyLogHandler implements LogHandler {
    @Override
    public void log(long time, LogLevel level, LogCategory category, String msg, Throwable exc, boolean fromReplay,
                    boolean wasSuppressed) {
        switch (level)
        {
            case LogLevel.TRACE -> {
                Logger.trace(exc, "[{}] {}", category.toString(), msg);
            }
            case LogLevel.DEBUG -> {
                Logger.debug(exc, "[{}] {}", category.toString(), msg);
            }
            case LogLevel.INFO -> {
                Logger.info(exc, "[{}] {}", category.toString(), msg);
            }
            case LogLevel.WARN -> {
                Logger.warn(exc, "[{}] {}", category.toString(), msg);
            }
            case LogLevel.ERROR -> {
                Logger.error(exc, "[{}] {}", category.toString(), msg);
            }
        }
    }

    @Override
    public boolean shouldLog(LogLevel level, LogCategory category) {
        switch (level)
        {
            case LogLevel.INFO -> {
                return Logger.isInfoEnabled();
            }
            case LogLevel.WARN -> {
                return Logger.isWarnEnabled();
            }
            case LogLevel.DEBUG -> {
                return Logger.isDebugEnabled();
            }
            case LogLevel.ERROR -> {
                return Logger.isErrorEnabled();
            }
            case LogLevel.TRACE -> {
                return Logger.isTraceEnabled();
            }
            case null, default -> {
                return false;
            }
        }
    }

    @Override
    public void close() { }
}
