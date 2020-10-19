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
package org.exoplatform.container.util;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.io.IOException;
import java.io.InputStream;

//import net.sourceforge.wurfl.wurflapi.WurflSource;

public class ExoWurflSource /* implements WurflSource */
{
   
   /**
    * The logger
    */
   private static final Log LOG = ExoLogger.getLogger("exo.kernel.container.util.ExoWurflSource");

   public InputStream getWurflInputStream()
   {
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      java.net.URL wurflUrl = cl.getResource("conf/wurfl.xml");
      try
      {
         return wurflUrl.openStream();
      }
      catch (IOException e)
      {
         LOG.error(e.getLocalizedMessage(), e);
         return null;
      }
   }

   public InputStream getWurflPatchInputStream()
   {
      return null;
   }
}
