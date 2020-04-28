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

import java.util.ArrayList;

/**
 * @author Tuan Nguyen (tuan08@users.sourceforge.net)
 * @since Nov 10, 2004
 * @version $Id$
 */
public class ListenerStack extends ArrayList
{
   public ListenerStack()
   {
      super();
   }

   public ListenerStack(int size)
   {
      super(size);
   }

   public void add(int index, Object element)
   {
      throw new UnsupportedOperationException("use add(java.lang.Object)");
   }

   public boolean add(java.lang.Object obj)
   {
      String name = obj.getClass().getName();
      for (int i = 0; i < size(); i++)
      {
         Object found = get(i);
         if (name.equals(found.getClass().getName()))
         {
            remove(i);
            break;
         }
      }
      return super.add(obj);
   }
}
