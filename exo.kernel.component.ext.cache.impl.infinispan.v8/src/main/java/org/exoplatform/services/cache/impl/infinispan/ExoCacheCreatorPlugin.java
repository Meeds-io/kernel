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
package org.exoplatform.services.cache.impl.infinispan;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;

import java.util.ArrayList;
import java.util.List;

/**
 * This class allows us to define new creators
 * @author <a href="mailto:nicolas.filotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 */
public class ExoCacheCreatorPlugin extends BaseComponentPlugin
{

   /**
    * The list of all the creators defined for this ComponentPlugin
    */
   private final List<ExoCacheCreator> creators;

   public ExoCacheCreatorPlugin(InitParams params)
   {
      creators = new ArrayList<ExoCacheCreator>();
      List<?> configs = params.getObjectParamValues(ExoCacheCreator.class);
      for (int i = 0; i < configs.size(); i++)
      {
         ExoCacheCreator config = (ExoCacheCreator)configs.get(i);
         creators.add(config);
      }
   }

   /**
    * Returns all the creators defined for this ComponentPlugin
    */
   public List<ExoCacheCreator> getCreators()
   {
      return creators;
   }
}
