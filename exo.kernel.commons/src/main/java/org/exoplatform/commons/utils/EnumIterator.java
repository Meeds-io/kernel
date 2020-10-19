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

import java.util.Enumeration;
import java.util.Iterator;

/**
 * @author Ove Ranheim (oranheim@users.sourceforge.net)
 * @since Nov 9, 2003 4:01:29 PM
 */
public class EnumIterator implements Iterator
{
   private Enumeration p_enum;

   public EnumIterator(Enumeration e)
   {
      p_enum = e;
   }

   public boolean hasNext()
   {
      return p_enum.hasMoreElements();
   }

   public Object next()
   {
      return p_enum.nextElement();
   }

   public void remove()
   {
      throw new UnsupportedOperationException();
   }
}
