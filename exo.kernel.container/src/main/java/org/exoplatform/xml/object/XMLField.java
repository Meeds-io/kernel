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
package org.exoplatform.xml.object;

/**
 * @author Tuan Nguyen (tuan08@users.sourceforge.net)
 * @since Apr 10, 2005
 * @version $Id: XMLField.java 5799 2006-05-28 17:55:42Z geaz $
 */
public class XMLField extends XMLBaseObject
{

   private String name;

   public XMLField()
   {
   }

   public XMLField(String fieldName, Class<?> type, Object val) throws Exception
   {
      super(type, val);
      this.name = fieldName;
   }

   public String getName()
   {
      return name;
   }

   public void setName(String s)
   {
      name = s;
   }

   public String toString()
   {
      return "{" + name + ", " + type + ", " + value + "}";
   }
}
