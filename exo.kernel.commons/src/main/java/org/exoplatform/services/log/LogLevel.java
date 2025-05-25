/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.exoplatform.services.log;

import org.slf4j.spi.LocationAwareLogger;

/**
 * The level of a log message.
 *
 */
public enum LogLevel
{
  /**
   * Trace log level.
   */
  TRACE(LocationAwareLogger.TRACE_INT)
      {
        @Override
        public void log(Log delegate, Object msg, Throwable throwable)
        {
          delegate.trace(String.valueOf(msg), throwable);
        }

        @Override
        public boolean isEnabled(Log delegate)
        {
          return delegate.isTraceEnabled();
        }
      },

  /**
   * Debug log level.
   */
  DEBUG(LocationAwareLogger.DEBUG_INT)
      {
        @Override
        public void log(Log delegate, Object msg, Throwable throwable)
        {
          delegate.debug(String.valueOf(msg), throwable);
        }

        @Override
        public boolean isEnabled(Log delegate)
        {
          return delegate.isDebugEnabled();
        }
      },

  /**
   * Info log level.
   */
  INFO(LocationAwareLogger.INFO_INT)
      {
        @Override
        public void log(Log delegate, Object msg, Throwable throwable)
        {
          delegate.info(String.valueOf(msg), throwable);
        }

        @Override
        public boolean isEnabled(Log delegate)
        {
          return delegate.isInfoEnabled();
        }
      },

  /**
   * Warn log level.
   */
  WARN(LocationAwareLogger.WARN_INT)
      {
        @Override
        public void log(Log delegate, Object msg, Throwable throwable)
        {
          delegate.warn(String.valueOf(msg), throwable);
        }

        @Override
        public boolean isEnabled(Log delegate)
        {
          return delegate.isWarnEnabled();
        }
      },

  /**
   * Warn log level.
   */
  ERROR(LocationAwareLogger.ERROR_INT)
      {
        @Override
        public void log(Log delegate, Object msg, Throwable throwable)
        {
          delegate.error(String.valueOf(msg), throwable);
        }

        @Override
        public boolean isEnabled(Log delegate)
        {
          return delegate.isErrorEnabled();
        }
      }

  ;

  private final int slf4Jlevel;

  LogLevel(int slf4Jlevel)
  {
    this.slf4Jlevel = slf4Jlevel;
  }

  int getSLF4Jlevel()
  {
    return slf4Jlevel;
  }

  public abstract void log(Log delegate, Object msg, Throwable throwable);

  public void log(Log delegate, Object msg) {
    log(delegate,msg,null);
  }

  public abstract boolean isEnabled(Log delegate);

}
