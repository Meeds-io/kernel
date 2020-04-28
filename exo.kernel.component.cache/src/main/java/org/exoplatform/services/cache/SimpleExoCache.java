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

import org.exoplatform.services.cache.concurrent.ConcurrentFIFOExoCache;

import java.io.Serializable;

/**
 * Created by The eXo Platform SAS Author : Tuan Nguyen
 * tuan08@users.sourceforge.net Sat, Sep 13, 2003 @ Time: 1:12:22 PM
 *
 * @deprecated use {@link ConcurrentFIFOExoCache} instead
 */
@Deprecated
public class SimpleExoCache<K extends Serializable, V> extends ConcurrentFIFOExoCache<K, V>
{
   public SimpleExoCache(int maxSize)
   {
      super(maxSize);
   }

   public SimpleExoCache()
   {
      super();
   }

   public SimpleExoCache(String name, int maxSize)
   {
      super(name, maxSize);
   }
}
