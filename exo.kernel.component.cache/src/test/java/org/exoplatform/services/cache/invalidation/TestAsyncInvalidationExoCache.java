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

import junit.framework.TestCase;

import java.util.concurrent.atomic.AtomicInteger;

import org.exoplatform.services.cache.CacheListener;
import org.exoplatform.services.cache.CacheListenerContext;
import org.exoplatform.services.cache.CachedObjectSelector;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.cache.ObjectCacheInfo;

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
      ReplicatedCache<String> writerNode = new ReplicatedCache<>();
      ReplicatedCache<String> readerNode = new ReplicatedCache<>();
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

   public void testClearOnANodeEvictsTheEntriesOfTheOthers()
   {
      reader.putLocal(KEY, "former");
      reader.putLocal("other", "former");

      writer.clearCache();

      assertNull(reader.get(KEY));
      assertNull(reader.get("other"));
   }

   public void testClearAgainEvictsTheEntriesLoadedSince()
   {
      writer.clearCache();
      reader.putLocal(KEY, "former");

      writer.clearCache();

      assertNull(reader.get(KEY));
   }

   public void testSelectionOnANodeEvictsTheSelectedEntriesOfTheOthers() throws Exception
   {
      reader.putLocal(KEY, "former");
      reader.putLocal("other", "kept");

      writer.select(new KeySelector(KEY));

      assertNull(reader.get(KEY));
      assertEquals("kept", reader.get("other"));
   }

   public void testSelectionKeepsTheEntriesOfItsNodeItDoesNotSelect() throws Exception
   {
      writer.putLocal(KEY, "former");
      writer.putLocal("other", "kept");

      writer.select(new KeySelector(KEY));

      assertNull(writer.get(KEY));
      assertEquals("kept", writer.get("other"));
   }

   public void testSelectionAgainEvictsTheEntriesLoadedSince() throws Exception
   {
      writer.select(new KeySelector(KEY));
      reader.putLocal(KEY, "former");

      writer.select(new KeySelector(KEY));

      assertNull(reader.get(KEY));
   }

   public void testSelectionOfAnotherKindClearsTheOtherNodes() throws Exception
   {
      reader.putLocal(KEY, "former");
      reader.putLocal("other", "former");
      writer.putLocal("other", "kept");

      writer.select(new CachedObjectSelector<String, String>()
      {
         @Override
         public boolean select(String key, ObjectCacheInfo<? extends String> ocinfo)
         {
            return KEY.equals(key);
         }

         @Override
         public void onSelect(ExoCache<? extends String, ? extends String> cache, String key,
            ObjectCacheInfo<? extends String> ocinfo)
         {
            cache.remove(key);
         }
      });

      assertNull(reader.get(KEY));
      assertNull(reader.get("other"));
      assertEquals("kept", writer.get("other"));
   }

   public void testLoadNotifiesTheListenersOfALocalPut()
   {
      AtomicInteger puts = new AtomicInteger();
      AtomicInteger localPuts = new AtomicInteger();
      writer.addCacheListener(new Listener()
      {
         @Override
         public void onPut(CacheListenerContext context, String key, String obj)
         {
            puts.incrementAndGet();
         }

         @Override
         public void onPutLocal(CacheListenerContext context, String key, String obj)
         {
            localPuts.incrementAndGet();
         }
      });

      writer.putLocal(KEY, "loaded");

      assertEquals(0, puts.get());
      assertEquals(1, localPuts.get());
   }

   public void testPutNotifiesTheListenersOfItsNodeOnce()
   {
      AtomicInteger puts = new AtomicInteger();
      writer.addCacheListener(new Listener()
      {
         @Override
         public void onPut(CacheListenerContext context, String key, String obj)
         {
            puts.incrementAndGet();
         }
      });

      writer.put(KEY, "written");

      // The hash of the put reaches the replicated cache of its node too, with its value
      assertEquals(1, puts.get());
      assertEquals("written", writer.get(KEY));
   }

   public void testPutOfTheValueTheOtherNodeHoldsNotifiesItsListenersOfNothing()
   {
      reader.putLocal(KEY, "written");
      AtomicInteger puts = new AtomicInteger();
      AtomicInteger removes = new AtomicInteger();
      reader.addCacheListener(new Listener()
      {
         @Override
         public void onPut(CacheListenerContext context, String key, String obj)
         {
            puts.incrementAndGet();
         }

         @Override
         public void onRemove(CacheListenerContext context, String key, String obj)
         {
            removes.incrementAndGet();
         }
      });

      // A put of this node comes back so too when the other node owns the segment of its key
      writer.put(KEY, "written");

      assertEquals("written", reader.get(KEY));
      assertEquals(0, puts.get());
      assertEquals(0, removes.get());
   }

   public void testRemoveNotifiesTheListenersOfARemoveOnly()
   {
      writer.putLocal(KEY, "loaded");
      AtomicInteger puts = new AtomicInteger();
      AtomicInteger removes = new AtomicInteger();
      writer.addCacheListener(new Listener()
      {
         @Override
         public void onPut(CacheListenerContext context, String key, String obj)
         {
            puts.incrementAndGet();
         }

         @Override
         public void onRemove(CacheListenerContext context, String key, String obj)
         {
            removes.incrementAndGet();
         }
      });

      writer.remove(KEY);

      assertEquals(0, puts.get());
      assertEquals(1, removes.get());
   }

   public void testLoadOnANodeWhoseListenerClearsOnAPutKeepsTheEntriesOfTheOthers()
   {
      // A listener that clears its cache on every write, as a cache of lists of entries does
      writer.addCacheListener(new Listener()
      {
         @Override
         public void onPut(CacheListenerContext context, String key, String obj)
         {
            writer.clearCache();
         }

         @Override
         public void onPutLocal(CacheListenerContext context, String key, String obj)
         {
            // A load changes nothing to clear
         }
      });
      reader.putLocal(KEY, "kept");

      writer.putLocal("other", "loaded");

      assertEquals("kept", reader.get(KEY));
      assertEquals("loaded", writer.get("other"));
   }

   public void testEntryOfTheOtherNodesIsKeptWithoutRemove()
   {
      reader.putLocal(KEY, "kept");

      writer.putLocal(KEY, "other");

      assertEquals("kept", reader.get(KEY));
   }

   private abstract static class Listener implements CacheListener<String, String>
   {
      @Override
      public void onExpire(CacheListenerContext context, String key, String obj)
      {
         // Not observed
      }

      @Override
      public void onRemove(CacheListenerContext context, String key, String obj)
      {
         // Not observed
      }

      @Override
      public void onGet(CacheListenerContext context, String key, String obj)
      {
         // Not observed
      }

      @Override
      public void onClearCache(CacheListenerContext context)
      {
         // Not observed
      }
   }
}
