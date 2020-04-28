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
package org.exoplatform.container.definition;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.Deserializer;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValuesParam;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class allows you to dynamically disable one or several portal containers.
 * 
 * Created by The eXo Platform SAS
 * Author : Nicolas Filotto 
 *          nicolas.filotto@exoplatform.com
 * 9 juil. 2010  
 */
public class PortalContainerDefinitionDisablePlugin extends BaseComponentPlugin
{
   
   /**
    * A set of specific portal container names that we want to disable.
    */
   private Set<String> names;
   
   @SuppressWarnings("unchecked")
   public PortalContainerDefinitionDisablePlugin(InitParams params)
   {
      ValuesParam vsp = params.getValuesParam("names");
      if (vsp != null && !vsp.getValues().isEmpty())
      {
         this.names = new HashSet<String>(vsp.getValues().size());
         List<String> lnames = vsp.getValues();
         for (String name : lnames)
         {
            names.add(Deserializer.resolveVariables(name));
         }         
      }
   }

   public Set<String> getNames()
   {
      return names;
   }
}
