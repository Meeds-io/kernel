/*
 * Copyright (C) 2009 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
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
 * @author <a href="work.visor.ck@gmail.com">Dmytro Katayev</a> Jun 24, 2009
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
