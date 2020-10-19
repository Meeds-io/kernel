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
package org.exoplatform.services.net.test;

import junit.framework.TestCase;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.net.NetService;

/**
 * Created by The eXo Platform SAS Author : HoaPham phamvuxuanhoa@yahoo.com Jan
 * 10, 2006
 */
public class TestNetService extends TestCase
{
   private NetService service_;

   public TestNetService(String name)
   {
      super(name);
   }

   public void setUp() throws Exception
   {
      if (service_ == null)
      {
         PortalContainer manager = PortalContainer.getInstance();
         service_ = (NetService)manager.getComponentInstanceOfType(NetService.class);
      }
   }

   public void tearDown() throws Exception
   {

   }

   public void testNetService() throws Exception
   {
      ping(null, 0);
      ping("www.google.com", 80);
      ping("www.vnexpress.net", 80);
      ping("www.exoplatform.org", 80);
      // ----ping a host on LAN
      ping("localhost", 25);
   }

   private void ping(String host, int port) throws Exception
   {
      service_.ping(host, port);
   }
}
