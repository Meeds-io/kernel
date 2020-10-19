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
package org.exoplatform.services.cache.impl;

import org.exoplatform.services.cache.CacheListener;
import org.exoplatform.services.cache.CacheListenerContext;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.io.Serializable;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Oct 4, 2008  
 */
public class LoggingCacheListener implements CacheListener
{

   private static final Log LOG = ExoLogger.getLogger("exo.kernel.component.cache.LoggingCacheListener");

   public void onClearCache(CacheListenerContext context) throws Exception
   {
      if (LOG.isDebugEnabled())
      {
         LOG.debug("Cleared region " + context.getCacheInfo().getName());
      }
   }

   public void onExpire(CacheListenerContext context, Serializable key, Object obj) throws Exception
   {
      if (LOG.isTraceEnabled())
      {
         LOG.trace("Expired entry " + key + " on region " + context.getCacheInfo().getName());
      }
   }

   public void onGet(CacheListenerContext context, Serializable key, Object obj) throws Exception
   {
      if (LOG.isTraceEnabled())
      {
         LOG.trace("Get entry " + key + " on region " + context.getCacheInfo().getName());
      }
   }

   public void onPut(CacheListenerContext context, Serializable key, Object obj) throws Exception
   {
      if (LOG.isTraceEnabled())
      {
         LOG.trace("Put entry " + key + " region " + context.getCacheInfo().getName());
      }
      if (LOG.isWarnEnabled())
      {
         int maxSize = context.getCacheInfo().getMaxSize();
         int size = context.getCacheInfo().getSize();
         double treshold = maxSize * 0.95;
         if (size >= treshold)
         {
            LOG.warn("region " + context.getCacheInfo().getName() + " is 95% full, consider extending maxSize");
         }
      }

   }

   public void onRemove(CacheListenerContext context, Serializable key, Object obj) throws Exception
   {
      if (LOG.isTraceEnabled())
      {
         LOG.trace("Removed entry " + key + " region " + context.getCacheInfo().getName());
      }
   }
}
