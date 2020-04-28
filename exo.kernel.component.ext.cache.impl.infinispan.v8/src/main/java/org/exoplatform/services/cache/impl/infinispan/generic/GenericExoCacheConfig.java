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
package org.exoplatform.services.cache.impl.infinispan.generic;

import org.exoplatform.services.cache.ExoCacheConfig;

/**
 * The {@link org.exoplatform.services.cache.ExoCacheConfig} for all the eviction algorithms 
 * available in infinispan
 * 
 * @author <a href="mailto:nicolas.filotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public class GenericExoCacheConfig extends ExoCacheConfig
{

   private String strategy;

   private long maxIdle;

   private long wakeUpInterval;
   
   /**
    * @return the strategy
    */
   public String getStrategy()
   {
      return strategy;
   }

   /**
    * @param strategy the strategy to set
    */
   public void setStrategy(String strategy)
   {
      this.strategy = strategy;
   }

   /**
    * @return the wakeUpInterval
    */
   public long getWakeUpInterval()
   {
      return wakeUpInterval;
   }

   /**
    * @param wakeUpInterval the wakeUpInterval to set
    */
   public void setWakeUpInterval(long wakeUpInterval)
   {
      this.wakeUpInterval = wakeUpInterval;
   }

   /**
    * @return the maxIdle
    */
   public long getMaxIdle()
   {
      return maxIdle;
   }

   /**
    * @param maxIdle the maxIdle to set
    */
   public void setMaxIdle(long maxIdle)
   {
      this.maxIdle = maxIdle;
   }
}