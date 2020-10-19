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
package org.exoplatform.container.configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tuan Nguyen (tuan08@users.sourceforge.net)
 * @since Oct 27, 2004
 * @version $Id: ConfigParam.java 5799 2006-05-28 17:55:42Z geaz $
 */
public class ConfigParam
{
   private List role;

   private List group;

   private List ignoredUser;

   public ConfigParam()
   {
      role = new ArrayList(3);
      group = new ArrayList(3);
      ignoredUser = new ArrayList(5);
   }

   public List getRole()
   {
      return role;
   }

   public List getGroup()
   {
      return group;
   }

   public List getIgnoredUser()
   {
      return ignoredUser;
   }

   static public class Group
   {
      public String name;

      public String membership;

      public Group()
      {
      }

      public String getName()
      {
         return name;
      }

      public void setName(String s)
      {
         name = s;
      }

      public String getMembership()
      {
         return membership;
      }

      public void setMembership(String s)
      {
         membership = s;
      }
   }
}
