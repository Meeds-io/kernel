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

import junit.framework.Assert;
import junit.framework.TestCase;

import java.util.concurrent.Callable;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class GetTestCase extends TestCase
{
   public void testGet()
   {
      FutureMap<Callable<String>> futureCache = new FutureMap<Callable<String>>(new StringLoader());
      Assert.assertEquals("foo_value", futureCache.get(new Callable<String>()
      {
         public String call() throws Exception
         {
            return "foo_value";
         }
      }, "foo"));
      Assert.assertEquals("foo_value", futureCache.data.get("foo"));
   }

   public void testNullValue()
   {
      FutureMap<Callable<String>> futureCache = new FutureMap<Callable<String>>(new StringLoader());
      Assert.assertEquals(null, futureCache.get(new Callable<String>()
      {
         public String call() throws Exception
         {
            return null;
         }
      }, "foo"));
      Assert.assertFalse(futureCache.data.containsKey("foo"));
   }

   public void testThrowException()
   {
      FutureMap<Callable<String>> futureCache = new FutureMap<Callable<String>>(new StringLoader());
      Assert.assertEquals(null, futureCache.get(new Callable<String>()
      {
         public String call() throws Exception
         {
            throw new Exception("DON'T FREAK OUT");
         }
      }, "foo"));
      Assert.assertFalse(futureCache.data.containsKey("foo"));
   }
}
