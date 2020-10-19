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
package org.exoplatform.container.context;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;

import java.util.List;

/**
 * This class allows you to dynamically register new context definitions
 * 
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public class ContextPlugin extends BaseComponentPlugin
{

   /**
    * The initial parameter of this plugin
    */
   private final InitParams params;

   public ContextPlugin(InitParams params)
   {
      this.params = params;
   }

   /**
    * @return all the {@link AdvancedContext} related to this plugin
    */
   @SuppressWarnings("rawtypes")
   public List<AdvancedContext> getContexts()
   {
      return params.getObjectParamValues(AdvancedContext.class);
   }
}
