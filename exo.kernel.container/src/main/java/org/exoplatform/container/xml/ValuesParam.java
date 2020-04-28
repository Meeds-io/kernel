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
package org.exoplatform.container.xml;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Jul 19, 2004
 * 
 * @author: Tuan Nguyen
 * @version: $Id: ValuesParam.java 5799 2006-05-28 17:55:42Z geaz $
 */
public class ValuesParam extends Parameter
{

   private List<String> values = new ArrayList<String>();

   public List<String> getValues()
   {
      return values;
   }

   public void setValues(List<String> values)
   {
      this.values = values;
   }

   public String getValue()
   {
      if (values.size() == 0)
         return null;
      return values.get(0);
   }

   public String toString()
   {
      Iterator<String> it = values.iterator();
      StringBuilder builder = new StringBuilder();
      while (it.hasNext())
      {
         String value = it.next();
         builder.append(value);
         if (it.hasNext())
         {
            builder.append(",");
         }
      }
      return builder.toString();
   }
}
