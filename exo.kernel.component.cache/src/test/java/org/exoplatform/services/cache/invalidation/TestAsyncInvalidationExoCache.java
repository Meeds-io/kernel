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

import junit.framework.TestCase;

import org.exoplatform.services.cache.concurrent.ConcurrentFIFOExoCache;

/**
 * The invalidation of an entry between two nodes, each with its own replicated cache: a hash reaches the other node
 * without its value, and a cache notifies the remove of a key it holds only, as a replicated Infinispan cache does.
 */
public class TestAsyncInvalidationExoCache extends TestCase
{
   private static final String KEY = "key";

   private AsyncInvalidationExoCache<String, String> writer;

   private AsyncInvalidationExoCache<String, String> reader;

   @Override
   protected void setUp() throws Exception
   {
      ReplicatedCache writerNode = new ReplicatedCache();
      ReplicatedCache readerNode = new ReplicatedCache();
      writerNode.other = readerNode;
      readerNode.other = writerNode;
      writer = new AsyncInvalidationExoCache<>(writerNode);
      reader = new AsyncInvalidationExoCache<>(readerNode);
   }

   public void testRemoveOnANodeThatDidNotLoadTheEntryEvictsItOnTheOthers()
   {
      // A loader puts the entry in the local cache of the node that reads it only
      reader.putLocal(KEY, "former");

      writer.remove(KEY);

      assertNull(reader.get(KEY));
   }

   public void testRemoveOnANodeThatLoadedTheEntryEvictsItOnTheOthers()
   {
      writer.putLocal(KEY, "former");
      reader.putLocal(KEY, "former");

      writer.remove(KEY);

      assertNull(reader.get(KEY));
   }

   public void testRemoveOfAPutEntryEvictsItOnTheOthers()
   {
      writer.put(KEY, "former");
      reader.putLocal(KEY, "former");

      writer.remove(KEY);

      assertNull(reader.get(KEY));
   }

   public void testRemoveAgainEvictsTheEntryLoadedSince()
   {
      writer.remove(KEY);
      reader.putLocal(KEY, "former");

      writer.remove(KEY);

      assertNull(reader.get(KEY));
   }

   public void testEntryOfTheOtherNodesIsKeptWithoutRemove()
   {
      reader.putLocal(KEY, "kept");

      writer.putLocal(KEY, "other");

      assertEquals("kept", reader.get(KEY));
   }

   /**
    * The replicated cache of a node, which sends its puts and removes to the other node.
    */
   private static class ReplicatedCache extends ConcurrentFIFOExoCache<String, HashCode<String>>
   {
      private ReplicatedCache other;

      private boolean received;

      private ReplicatedCache()
      {
         super("invalidated", 100);
      }

      @Override
      public void put(String key, HashCode<String> value)
      {
         super.put(key, value);
         if (!received)
         {
            other.receive(() -> other.put(key, overTheWire(value)));
         }
      }

      @Override
      public HashCode<String> remove(Serializable key)
      {
         HashCode<String> value = super.remove(key);
         if (!received)
         {
            other.receive(() -> other.remove(key));
         }
         return value;
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
      private static HashCode<String> overTheWire(HashCode<String> value)
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
               return (HashCode<String>)in.readObject();
            }
         }
         catch (IOException | ClassNotFoundException e)
         {
            throw new IllegalStateException(e);
         }
      }
   }
}
