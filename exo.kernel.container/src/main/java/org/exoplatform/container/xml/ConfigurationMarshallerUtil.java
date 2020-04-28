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
package org.exoplatform.container.xml;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.jibx.runtime.IMarshallingContext;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


/**
 * This class is an utility class used to marshall the top level objects that compose the configuration
 * 
 * Created by The eXo Platform SAS
 * Author : Nicolas Filotto 
 *          nicolas.filotto@exoplatform.com
 * 12 mai 2010  
 */
public class ConfigurationMarshallerUtil
{

   /**
    * The logger
    */
   private static final Log LOG = ExoLogger.getLogger("exo.kernel.container.ConfigurationMarshallerUtil");
   
   /**
    * This method adds the given {@link URL} as comment to XML content. 
    */
   static void addURLToContent(URL source, IMarshallingContext ictx)
   {
      try
      {
         ictx.getXmlWriter().writeComment(" Loaded from '" + source + "' ");
      }
      catch (IOException e)
      {
         LOG.warn("Could not add the source into the XML document", e);
      }
   }

   public static List<?> createList()
   {
      return new ArrayList<Object>();
   }
}
