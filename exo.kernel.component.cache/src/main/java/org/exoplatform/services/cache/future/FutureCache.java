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
package org.exoplatform.services.cache.future;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * A future cache that prevents the loading of the same resource twice. This should be used when the resource
 * to load is very expensive or cannot be concurrently retrieved (like a classloading). 
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 * @param <K> the key type parameter
 * @param <V> the value type parameter
 * @param <C> the context type parameter
 */
public abstract class FutureCache<K, V, C>
{

   /** . */
   private final Loader<K, V, C> loader;

   /** . */
   private final ConcurrentMap<K, FutureTask<V>> futureEntries;

   /** . */
   private static final Log LOG = ExoLogger.getLogger("exo.kernel.component.cache.FutureCache");


   public FutureCache(Loader<K, V, C> loader)
   {
      this.loader = loader;
      this.futureEntries = new ConcurrentHashMap<K, FutureTask<V>>();
   }

   protected abstract V get(K key);

   protected abstract void put(K key, V value);

   protected abstract void putOnly(K key, V value);

   /**
    * Perform a cache lookup for the specified key within the specified context.
    * When the value cannot be loaded (because it does not exist or it failed or anything else that
    * does not come to my mind), the value null is returned.
    *
    * @param context the context in which the resource is accessed
    * @param key the key identifying the resource
    * @return the value
    */
   public final V get(final C context, final K key)
   {
      // First we try a simple cache get
      V value = get(key);

      // If it does not succeed then we go through a process that will avoid to load
      // the same resource concurrently
      if (value == null)
      {
         // Create our future
         FutureTask<V> future = new FutureTask<V>(new Callable<V>()
         {
            public V call() throws Exception
            {
               // Retrieve the value from the loader
               V value = loader.retrieve(context, key);

               //
               if (value != null)
               {
                  // Cache it, it is made available to other threads (unless someone removes it)
                  putOnly(key, value);

                  // Return value
                  return value;
               }
               else
               {
                  return null;
               }
            }
         });

         // This boolean means we inserted in the local
         boolean inserted = true;

         //
         try
         {
            FutureTask<V> phantom = futureEntries.putIfAbsent(key, future);

            // Use the value that could have been inserted by another thread
            if (phantom != null)
            {
               future = phantom;
               inserted = false;
            }
            else
            {
               future.run();
            }

            // Returns the value
            value = future.get();
         }
         catch (ExecutionException e)
         {
            LOG.error("Computing of resource " + key + " threw an exception", e.getCause());
         }
         catch (Exception e)
         {
            LOG.error("Retrieval of resource " + key + " threw an exception", e);
         }
         finally
         {
            // Clean up the per key map but only if our insertion succeeded and with our future
            if (inserted)
            {
               futureEntries.remove(key, future);
            }
         }
      }

      //
      return value;
   }
}
