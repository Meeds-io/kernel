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

import java.io.Serializable;
import java.util.Collection;

/**
 * Created by The eXo Platform SAS<br>
 * The cache service.
 *
 * @author : <a href="tuan08@users.sourceforge.net">Tuan Nguyen</a>.
 *
 * @LevelAPI Platform
 */
public interface CacheService
{

   /**
    * Adds a cache configuration plugin.
    *
    * @param plugin the plugin
    */
   public void addExoCacheConfig(ExoCacheConfigPlugin plugin);

   /**
    * Returns a specific cache instance.
    *
    * @param region the cache region
    * @param <K> the key type
    * @param <V> the value type
    * @return the cache
    * @throws NullPointerException if the region argument is null
    * @throws IllegalArgumentException if the region argument length is zero
    */
   public <K extends Serializable, V> ExoCache<K, V> getCacheInstance(String region) throws NullPointerException,
      IllegalArgumentException;

   /**
    * Returns a collection of all the cache instances.
    *
    * @return all the caches
    */
   public Collection<ExoCache<? extends Serializable, ?>> getAllCacheInstances();
}
