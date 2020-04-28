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
package org.exoplatform.services.cache.concurrent;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * This is unit test for the class (@link org.exoplatform.services.cache.concurrent.ConcurrentFIFOExoCache)
 */
public class TestConcurrentFIFOExoCache {

  private int LIMIT = 1000;

  /**
   * This test if ConcurrentFIFOExoCache#put threadsafe
   */
  @Test
  public void testPut() throws Exception {
    ConcurrentFIFOExoCache<String, String> cache = new ConcurrentFIFOExoCache<>(LIMIT);

    //
    CountDownLatch start = new CountDownLatch(1);
    CountDownLatch finish = new CountDownLatch(1000);
    //
    for (int i = 0; i < 1000; i++) {
      Thread t = new Thread(new Runnable() {
        @Override
        public void run() {
          try {
            start.await(10, TimeUnit.SECONDS);
          } catch (InterruptedException e) {
            Assert.fail("CountDownLatch fail to wait");
          }
          
          for (int j = 0; j < 10000; j++) {
            if (cache.getCacheSize() <= LIMIT) {
              cache.put("", "");
            }
          }
          finish.countDown();
        }
      });
      t.start();
    }
    //
    start.countDown();
    finish.await(1, TimeUnit.MINUTES);

    Assert.assertEquals(LIMIT, cache.getCacheSize());
  }
}
