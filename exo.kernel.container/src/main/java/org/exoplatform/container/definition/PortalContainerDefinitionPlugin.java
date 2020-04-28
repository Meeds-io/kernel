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
import org.exoplatform.container.xml.InitParams;

import java.util.List;

/**
 * This class allows you to dynamically define a new portal container with all its dependencies
 *  
 * Created by The eXo Platform SAS
 * Author : Nicolas Filotto
 *          nicolas.filotto@exoplatform.com
 * 8 sept. 2009  
 */
public class PortalContainerDefinitionPlugin extends BaseComponentPlugin
{

   /**
    * The initial parameter of this plugin
    */
   private final InitParams params;

   public PortalContainerDefinitionPlugin(InitParams params)
   {
      this.params = params;
   }

   /**
    * @return all the {@link PortalContainerDefinition} related to this plugin
    */
   public List<PortalContainerDefinition> getPortalContainerDefinitions()
   {
      return params.getObjectParamValues(PortalContainerDefinition.class);
   }
}
