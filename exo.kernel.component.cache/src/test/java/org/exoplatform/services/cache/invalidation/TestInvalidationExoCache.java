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

import org.exoplatform.services.cache.CachedObjectSelector;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.cache.ObjectCacheInfo;

/**
 * The synchronous invalidation of a cache between two nodes, each with its own replicated cache: a hash reaches the
 * other node without its value, and a clear reaches no node, as with a replicated Infinispan cache.
 */
public class TestInvalidationExoCache extends TestCase
{
   private static final String KEY = "key";

   private InvalidationExoCache<String, String> writer;

   private InvalidationExoCache<String, String> reader;

   @Override
   @SuppressWarnings({"unchecked", "rawtypes"})
   protected void setUp() throws Exception
   {
      ReplicatedCache<String> writerNode = new ReplicatedCache<>();
      ReplicatedCache<String> readerNode = new ReplicatedCache<>();
      writerNode.other = readerNode;
      readerNode.other = writerNode;
      writer = new InvalidationExoCache<>((ExoCache)writerNode);
      reader = new InvalidationExoCache<>((ExoCache)readerNode);
   }

   public void testClearOnANodeEvictsTheEntriesOfTheOthers()
   {
      reader.put(KEY, "former");

      writer.clearCache();

      assertNull(reader.get(KEY));
   }

   public void testClearAgainEvictsTheEntriesLoadedSince()
   {
      writer.clearCache();
      reader.put(KEY, "former");

      writer.clearCache();

      assertNull(reader.get(KEY));
   }

   public void testSelectionOnANodeEvictsTheSelectedEntriesOfTheOthers() throws Exception
   {
      // A loader puts the entry in the local cache of the node that reads it only
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
}
