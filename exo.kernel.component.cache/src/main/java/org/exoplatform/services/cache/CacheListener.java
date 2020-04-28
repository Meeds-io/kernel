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

/**
 * Created by The eXo Platform SAS<br>
 * The cache listener allows to be aware of various events that occurs for a cache. For performance reason
 * a listener must perform short time lived operations or should consider to perform jobs asynchronously.
 *
 * @author tuan08@users.sourceforge.net Tuan Nguyen
 * @LevelAPI Platform
 */
public interface CacheListener<K extends Serializable, V>
{

   /**
    * An entry is expired from the cache.
    *
    * @param context the listener context
    * @param key the entry key
    * @param obj the entry value
    * @throws Exception any exception
    */
   public void onExpire(CacheListenerContext context, K key, V obj) throws Exception;

   /**
    * An entry is removed from the cache.
    *
    * @param context the listener context
    * @param key the entry key
    * @param obj the entry value
    * @throws Exception any exception
    */
   public void onRemove(CacheListenerContext context, K key, V obj) throws Exception;

   /**
    * An entry is inserted in the cache.
    *
    * @param context the listener context
    * @param key the entry key
    * @param obj the entry value
    * @throws Exception any exception
    */
   public void onPut(CacheListenerContext context, K key, V obj) throws Exception;

   /**
    * An entry is inserted in the cache (local mode without replication).
    *
    * @param context the listener context
    * @param key     the entry key
    * @param obj     the entry value
    * @throws Exception any exception
    */
   public default void onPutLocal(CacheListenerContext context, K key, V obj) throws Exception {
      onPut(context, key, obj);
   }

   /**
    * An entry is retrieved from the cache.
    *
    * @param context the listener context
    * @param key the entry key
    * @param obj the entry value
    * @throws Exception any exception
    */
   public void onGet(CacheListenerContext context, K key, V obj) throws Exception;

   /**
    * The cache is globally cleared.
    *
    * @param context the listener context
    * @throws Exception any exception
    */
   public void onClearCache(CacheListenerContext context) throws Exception;
}
