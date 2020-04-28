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
package org.exoplatform.services.cache;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by The eXo Platform SAS<br>
 *
 * ExoCacheConfigPlugin is a component-plugin that allows to register a set of ExoCacheConfig to the CacheService
 *
 * @author : <a href="tuan08@users.sourceforge.net">Tuan Nguyen</a>.
 * @LevelAPI Platform
 */
public class ExoCacheConfigPlugin extends BaseComponentPlugin
{
   private List<ExoCacheConfig> configs_;

   public ExoCacheConfigPlugin(InitParams params)
   {
      configs_ = new ArrayList<ExoCacheConfig>();
      List configs = params.getObjectParamValues(ExoCacheConfig.class);
      for (int i = 0; i < configs.size(); i++)
      {
         ExoCacheConfig config = (ExoCacheConfig)configs.get(i);
         configs_.add(config);
      }
   }
   /**
    * @return the list of ExoCacheConfig
    */
   public List<ExoCacheConfig> getConfigs()
   {
      return configs_;
   }
}
