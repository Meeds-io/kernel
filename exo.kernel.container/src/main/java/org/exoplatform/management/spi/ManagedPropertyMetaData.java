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

import java.lang.reflect.Method;

/**
 * Meta data that describes a managed property.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ManagedPropertyMetaData extends ManagedMetaData
{

   /** . */
   private final String name;

   /** . */
   private final Method getter;

   /** . */
   private final String getterDescription;

   /** . */
   private final Method setter;

   /** . */
   private final String setterDescription;

   /** . */
   private final ManagedParameterMetaData setterParameter;

   public ManagedPropertyMetaData(String name, Method getter, String getterDescription, Method setter,
      String setterDescription, ManagedParameterMetaData setterParameter) throws IllegalArgumentException
   {
      if (name == null)
      {
         throw new IllegalArgumentException("No null name accepted");
      }
      if (setter != null)
      {
         if (setterParameter == null)
         {
            throw new IllegalArgumentException("No setter parameter provided");
         }
      }
      else
      {
         if (setterParameter != null)
         {
            throw new IllegalArgumentException("No setter provided but a setter parameter was provided");
         }
      }

      //
      this.name = name;
      this.getter = getter;
      this.getterDescription = getterDescription;
      this.setter = setter;
      this.setterDescription = setterDescription;
      this.setterParameter = setterParameter;
   }

   public String getName()
   {
      return name;
   }

   public Method getGetter()
   {
      return getter;
   }

   public Method getSetter()
   {
      return setter;
   }

   public String getGetterDescription()
   {
      return getterDescription;
   }

   public String getSetterDescription()
   {
      return setterDescription;
   }

   public ManagedParameterMetaData getSetterParameter()
   {
      return setterParameter;
   }

   @Override
   public String toString()
   {
      return "ManagedPropertyMetaData[" + "name=" + name + "getter=" + getter.getName() + "setter=" + setter.getName()
         + "]";
   }
}
