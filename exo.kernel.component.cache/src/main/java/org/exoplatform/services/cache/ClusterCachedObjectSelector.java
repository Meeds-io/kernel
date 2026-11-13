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
package org.exoplatform.services.cache;

import java.io.Serializable;
import java.util.Arrays;

/**
 * A selection of the entries to remove from a cache, which reaches every node of a cluster. Each node holds entries
 * the others do not hold, under keys the others never loaded: an invalidated cache sends the selector itself to the
 * other nodes, and each of them removes the entries it selects among its own. A selector of another kind selects
 * among the entries of one node only, and an invalidated cache clears the others instead.
 * <p>
 * The selector travels: its fields are serializable values of a type the cluster accepts, and a selector is never an
 * inner class, which would carry the instance that encloses it. Two selectors of the same class and the same criteria
 * are the same selection, which the cluster keeps once however often it is made.
 */
public abstract class ClusterCachedObjectSelector<K extends Serializable, V> implements CachedObjectSelector<K, V>,
   Serializable
{
   private static final long serialVersionUID = 1L;

   /**
    * @return the values the selection depends on, which make two selectors of the same class the same selection
    */
   protected abstract Object[] criteria();

   /**
    * Removes the selected entry: a node that receives the selection removes the entries it selects from itself only.
    */
   @Override
   public final void onSelect(ExoCache<? extends K, ? extends V> cache, K key, ObjectCacheInfo<? extends V> ocinfo)
   {
      cache.remove(key);
   }

   @Override
   public final boolean equals(Object o)
   {
      return o == this || o != null && o.getClass() == getClass()
         && Arrays.deepEquals(criteria(), ((ClusterCachedObjectSelector<?, ?>)o).criteria());
   }

   @Override
   public final int hashCode()
   {
      return 31 * getClass().getName().hashCode() + Arrays.deepHashCode(criteria());
   }

   @Override
   public String toString()
   {
      return getClass().getSimpleName() + Arrays.deepToString(criteria());
   }
}
