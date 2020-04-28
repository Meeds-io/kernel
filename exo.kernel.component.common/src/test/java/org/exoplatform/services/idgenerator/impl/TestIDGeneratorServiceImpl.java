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
package org.exoplatform.services.idgenerator.impl;

import org.databene.contiperf.PerfTest;
import org.databene.contiperf.junit.ContiPerfRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public class TestIDGeneratorServiceImpl
{
   @Rule
   public ContiPerfRule rule = new ContiPerfRule();

   private IDGeneratorServiceImpl generator;
   private AtomicInteger count;
   private ConcurrentMap<String, String> ids;

   @Before
   public void setUp()
   {
      generator = new IDGeneratorServiceImpl();
      count = new AtomicInteger();
      ids = new ConcurrentHashMap<String, String>();
   }

   @Test
   @PerfTest(invocations = 750000, threads = 50)
   public void testConcurrentCreation() throws Exception
   {
      String id = generator.generateStringID(Long.toString(System.currentTimeMillis()));
      if (ids.putIfAbsent(id, id) == null)
      {
         count.incrementAndGet();
      }
      else
      {
         throw new IllegalStateException("The id '" + id + "' already exists");
      }
   }
}
