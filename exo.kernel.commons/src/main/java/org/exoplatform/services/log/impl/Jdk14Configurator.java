/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 Meeds Association
 * contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.exoplatform.services.log.impl;

import org.exoplatform.services.log.AbstractLogConfigurator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.LogManager;

/**
 * Created by The eXo Platform SAS.
 * 
 *<br> JDK's log system configurator. See $JAVA_HOME/jre/lib/logging.properties for details.
 * 
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: Jdk14Configurator.java 34394 2009-07-23 09:23:31Z dkatayev $
 */

public class Jdk14Configurator extends AbstractLogConfigurator
{

   public void configure(Properties properties)
   {
      try
      {
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         properties.store(baos, null);
         LogManager.getLogManager().readConfiguration(new ByteArrayInputStream(baos.toByteArray()));
      }
      catch (IOException e)
      {
         // We need to use the standard out print since we are actually 
         // configuring the logger
         e.printStackTrace(); //NOSONAR
      }
      this.properties = properties;
   }

}
