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
package org.exoplatform.container.log;

import junit.framework.TestCase;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.log.LogConfigurationInitializer;

import java.util.Properties;

/**
 * Created by The eXo Platform SAS
 * 
 */
public class TestLoggers extends TestCase
{


   public void testLog4jContainer() throws Exception
   {
      Log log = ExoLogger.getLogger(TestLoggers.class);
      log.info("Log4j Container Tests");
      log.info("Log4j Container {}", "Tests");
      log.info("Log4j Conta{} Te{}", "iner", "sts");
      log.info("Log4j Container Tests", 1, 2, 3);
      logOut(log);
   }


   private void logOut(Log log)
   {
      log.debug(log.getClass().getName() + ": \tDEBUG");
      log.error(log.getClass().getName() + ": \tERROR");
      log.info(log.getClass().getName() + ": \tINFO");
      log.trace(log.getClass().getName() + ": \tTRACE");
      log.warn(log.getClass().getName() + ": \tWARNING\n");
   }

}
