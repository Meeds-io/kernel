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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.exoplatform.services.cache.concurrent.ConcurrentFIFOExoCache;

/**
 * The replicated cache of a node, which sends its puts and removes to the other node as a replicated Infinispan cache
 * does: serialized, the hash of a value without its value, a clear and the local operations to no node. Generic in its
 * key, as the Infinispan cache is: it takes the keys the invalidation itself puts, whatever the type of the cached keys.
 */
class ReplicatedCache<K extends Serializable> extends ConcurrentFIFOExoCache<K, HashCode<String>>
{
   ReplicatedCache<K> other;

   private boolean received;

   ReplicatedCache()
   {
      super("invalidated", 100);
   }

   @Override
   public void put(K key, HashCode<String> value)
   {
      super.put(key, value);
      if (!received)
      {
         other.receive(() -> other.put(overTheWire(key), overTheWire(value)));
      }
   }

   @Override
   public void putLocal(K key, HashCode<String> value)
   {
      super.put(key, value);
   }

   @Override
   public HashCode<String> remove(Serializable key)
   {
      HashCode<String> value = super.remove(key);
      if (!received)
      {
         other.receive(() -> other.remove(overTheWire(key)));
      }
      return value;
   }

   @Override
   public void removeLocal(Serializable key)
   {
      super.remove(key);
   }

   private void receive(Runnable command)
   {
      received = true;
      try
      {
         command.run();
      }
      finally
      {
         received = false;
      }
   }

   @SuppressWarnings("unchecked")
   private static <T> T overTheWire(T value)
   {
      try
      {
         ByteArrayOutputStream bytes = new ByteArrayOutputStream();
         try (ObjectOutputStream out = new ObjectOutputStream(bytes))
         {
            out.writeObject(value);
         }
         try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray())))
         {
            return (T)in.readObject();
         }
      }
      catch (IOException | ClassNotFoundException e)
      {
         throw new IllegalStateException(e);
      }
   }
}
