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
package org.exoplatform.management.spi;

/**
 * Meta data that describes the parameter of a managed method.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ManagedMethodParameterMetaData extends ManagedParameterMetaData
{

   /** . */
   private final int index;

   /**
    * Build a managed method parameter meta data.
    *
    * @param index the parameter index
    * @throws IllegalArgumentException if the index is negative
    */
   public ManagedMethodParameterMetaData(int index) throws IllegalArgumentException
   {
      if (index < 0)
      {
         throw new IllegalArgumentException("Non negative index value accepted " + index);
      }
      this.index = index;
   }

   public int getIndex()
   {
      return index;
   }
}
