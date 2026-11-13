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

import junit.framework.TestCase;

/**
 * The mode of a cache configuration built in code rather than read from a configuration file.
 */
public class TestExoCacheConfig extends TestCase
{
   public void testModeSetInCodeIsKept() throws Exception
   {
      ExoCacheConfig config = new ExoCacheConfig();

      config.setCacheMode(CacheMode.ASYNCINVALIDATION);

      // The cache service creates the cache from a copy
      ExoCacheConfig copy = config.clone();
      assertEquals(CacheMode.ASYNCINVALIDATION, copy.getCacheMode());
      assertTrue(copy.isInvalidated());
      assertTrue(copy.isRepicated());
      assertTrue(copy.isAsync());
   }

   public void testConfigWithoutModeIsLocal()
   {
      assertEquals(CacheMode.LOCAL, new ExoCacheConfig().getCacheMode());
   }
}
