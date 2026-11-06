/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2026 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.exoplatform.services.cache.invalidation;

import org.exoplatform.services.cache.ClusterCachedObjectSelector;
import org.exoplatform.services.cache.ObjectCacheInfo;

/**
 * Selects the entry of one key, on every node.
 */
class KeySelector extends ClusterCachedObjectSelector<String, String>
{
   private static final long serialVersionUID = 1L;

   private final String key;

   KeySelector(String key)
   {
      this.key = key;
   }

   @Override
   protected Object[] criteria()
   {
      return new Object[] {key};
   }

   @Override
   public boolean select(String cachedKey, ObjectCacheInfo<? extends String> ocinfo)
   {
      return key.equals(cachedKey);
   }
}
