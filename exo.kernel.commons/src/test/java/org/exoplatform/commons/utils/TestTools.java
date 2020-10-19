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
package org.exoplatform.commons.utils;

import junit.framework.TestCase;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TestTools extends TestCase
{

   public void testEmtySet()
   {
      Set<String> strings = Tools.set();
      assertTrue(strings instanceof HashSet);
      assertTrue(strings.isEmpty());
   }

   public void testSingletonSet1()
   {
      Set<String> strings = Tools.set("a");
      assertTrue(strings instanceof HashSet);
      assertEquals(1, strings.size());
      assertTrue(strings.contains("a"));
   }

   public void testSingletonSet2()
   {
      Set<String> strings = Tools.set("a", "a");
      assertTrue(strings instanceof HashSet);
      assertEquals(1, strings.size());
      assertTrue(strings.contains("a"));
   }

   public void testTwoElementsInSet()
   {
      Set<String> strings = Tools.set("a", "b");
      assertTrue(strings instanceof HashSet);
      assertEquals(2, strings.size());
      assertTrue(strings.contains("a"));
      assertTrue(strings.contains("b"));
   }

   public void testSetThrowsIllegalArgumentException()
   {
      try
      {
         Tools.set((String[])null);
         fail();
      }
      catch (IllegalArgumentException expected)
      {
      }
   }

   public void testParseCommaList()
   {
      assertEquals(Tools.<String>set(),Tools.parseCommaList(""));
      assertEquals(Tools.<String>set(),Tools.parseCommaList(","));
      assertEquals(Tools.set("a"),Tools.parseCommaList("a,"));
      assertEquals(Tools.set("a"),Tools.parseCommaList(",a"));
      assertEquals(Tools.set("a", "b"),Tools.parseCommaList("a,,b"));
      assertEquals(Tools.set("a"),Tools.parseCommaList("a,a"));
      assertEquals(Tools.set("a", "b"),Tools.parseCommaList("a,b"));
   }

   public void testParseCommandListThrowsIllegalArgumentException()
   {
      try
      {
         Tools.parseCommaList(null);
         fail();
      }
      catch (IllegalArgumentException e)
      {
      }
   }

   public void testEndsWithIgnoreCase()
   {
      assertFalse(Tools.endsWithIgnoreCase("", "a"));
      assertFalse(Tools.endsWithIgnoreCase("b", "a"));
      assertTrue(Tools.endsWithIgnoreCase("a", "a"));
      assertTrue(Tools.endsWithIgnoreCase("a", "A"));
      assertTrue(Tools.endsWithIgnoreCase("A", "a"));
   }

   public void testEndsWithIgnoreCaseThrowsIllegalArgumentException()
   {
      try
      {
         Tools.endsWithIgnoreCase(null, "a");
         fail();
      }
      catch (IllegalArgumentException e)
      {
      }
      try
      {
         Tools.endsWithIgnoreCase("a", null);
         fail();
      }
      catch (IllegalArgumentException e)
      {
      }
      try
      {
         Tools.endsWithIgnoreCase(null, null);
         fail();
      }
      catch (IllegalArgumentException e)
      {
      }
   }

   public void testAsMap()
   {
      Properties props = new Properties();
      props.put(new Object(), new Object());
      props.put("a", "b");
      Map<String, String> map = Tools.asMap(props);
      assertEquals(Collections.singletonMap("a", "b"), map);
      map.put("a", "c");
      assertEquals(Collections.singletonMap("a", "c"), map);
      assertEquals(2, props.size());
      assertEquals("b", props.getProperty("a"));
   }

   public void testAsMapThrowsIllegalArgumentException()
   {
      try
      {
         Tools.asMap(null);
         fail();
      }
      catch (IllegalArgumentException e)
      {
      }
   }

   public void testAsProperties()
   {
      Map<String, String> map = new HashMap<String, String>();
      map.put("a", "b");
      Properties props = Tools.asProperties(map);
      assertEquals(1, props.size());
      assertEquals("b", props.getProperty("a"));
      props.put("a", "c");
      assertEquals(1, props.size());
      assertEquals("c", props.getProperty("a"));
      assertEquals(Collections.singletonMap("a", "b"), map);
   }

   public void testAsPropertiesThrowsIllegalArgumentException()
   {
      try
      {
         Tools.asProperties(null);
         fail();
      }
      catch (IllegalArgumentException e)
      {
      }
   }
}
