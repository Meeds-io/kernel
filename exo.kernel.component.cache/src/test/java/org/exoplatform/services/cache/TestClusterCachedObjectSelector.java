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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import junit.framework.TestCase;

import org.exoplatform.services.cache.concurrent.ConcurrentFIFOExoCache;

/**
 * A selection is defined by its class and its criteria, which the nodes of a cluster receive.
 */
public class TestClusterCachedObjectSelector extends TestCase
{
   public void testSameCriteriaOfTheSameClassAreTheSameSelection()
   {
      assertEquals(new PrefixSelector("a", 1L), new PrefixSelector("a", 1L));
      assertEquals(new PrefixSelector("a", 1L).hashCode(), new PrefixSelector("a", 1L).hashCode());
      assertEquals(new PrefixSelector("a", null), new PrefixSelector("a", null));
   }

   public void testOtherCriteriaAreAnotherSelection()
   {
      assertFalse(new PrefixSelector("a", 1L).equals(new PrefixSelector("a", 2L)));
      assertFalse(new PrefixSelector("a", 1L).equals(new PrefixSelector("b", 1L)));
   }

   public void testSameCriteriaOfAnotherClassAreAnotherSelection()
   {
      assertFalse(new PrefixSelector("a", 1L).equals(new OtherPrefixSelector("a", 1L)));
      assertFalse(new OtherPrefixSelector("a", 1L).equals(new PrefixSelector("a", 1L)));
   }

   public void testReceivedSelectionIsTheSameSelection() throws Exception
   {
      PrefixSelector selector = new PrefixSelector("a", 1L);

      ByteArrayOutputStream bytes = new ByteArrayOutputStream();
      try (ObjectOutputStream out = new ObjectOutputStream(bytes))
      {
         out.writeObject(selector);
      }
      Object received;
      try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray())))
      {
         received = in.readObject();
      }

      assertEquals(selector, received);
      assertEquals(selector.hashCode(), received.hashCode());
   }

   public void testSelectedEntriesAreRemoved() throws Exception
   {
      ConcurrentFIFOExoCache<String, String> cache = new ConcurrentFIFOExoCache<>("selected", 10);
      cache.put("a1", "value");
      cache.put("b1", "value");

      cache.select(new PrefixSelector("a", null));

      assertNull(cache.get("a1"));
      assertEquals("value", cache.get("b1"));
   }

   private static class PrefixSelector extends ClusterCachedObjectSelector<String, String>
   {
      private static final long serialVersionUID = 1L;

      private final String prefix;

      private final Long   version;

      private PrefixSelector(String prefix, Long version)
      {
         this.prefix = prefix;
         this.version = version;
      }

      @Override
      protected Object[] criteria()
      {
         return new Object[] {prefix, version};
      }

      @Override
      public boolean select(String key, ObjectCacheInfo<? extends String> ocinfo)
      {
         return key.startsWith(prefix);
      }
   }

   private static class OtherPrefixSelector extends PrefixSelector
   {
      private static final long serialVersionUID = 1L;

      private OtherPrefixSelector(String prefix, Long version)
      {
         super(prefix, version);
      }
   }
}
