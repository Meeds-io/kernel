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
/**
 * Created by The eXo Platform SAS
 * Author : Mestrallet Benjamin
 *          benjmestrallet@users.sourceforge.net
 * Date: Aug 7, 2003
 * Time: 11:39:25 PM
 */
package org.exoplatform.mocks;

import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.container.xml.InitParams;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MockService
{
   Map plugins_ = new HashMap();

   Map listeners_ = new HashMap();

   public MockService(InitParams params)
   {
   }

   public String hello()
   {
      return "HELLO WORLD SERVICE";
   }

   public String getTest()
   {
      return "heh";
   }

   public void addPlugin(ComponentPlugin plugin)
   {
      plugins_.put(plugin.getName(), plugin);
   }

   public ComponentPlugin removePlugin(String name)
   {
      return (ComponentPlugin)plugins_.remove(name);
   }

   public Collection getPlugins()
   {
      return plugins_.values();
   }

}
