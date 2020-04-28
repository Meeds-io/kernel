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
package org.exoplatform.container.component;

/**
 * @author Tuan Nguyen (tuan08@users.sourceforge.net)
 * @since Apr 18, 2005
 * @version $Id: BaseComponentPlugin.java 5799 2006-05-28 17:55:42Z geaz $
 */
public class BaseComponentPlugin implements ComponentPlugin
{

   protected String name;

   protected String desc;

   public String getName()
   {
      return name;
   }

   public void setName(String s)
   {
      name = s;
   }

   public String getDescription()
   {
      return desc;
   }

   public void setDescription(String s)
   {
      desc = s;
   }
}
