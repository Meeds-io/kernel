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
package org.exoplatform.management.jmx.impl;

import org.exoplatform.management.jmx.annotations.Property;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import javax.management.ObjectName;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class PropertyInfo
{

   /** . */
   private final String key;

   /** . */
   private final Value value;

   public PropertyInfo(Class clazz, Property def)
   {
      String tmp = def.value();
      Value value;
      int length = tmp.length();
      if (length > 2 && tmp.charAt(0) == '{' && tmp.charAt(length - 1) == '}')
      {
         String s = tmp.substring(1, length - 1);
         String getterName = "get" + s;
         Method getter;
         try
         {
            getter = clazz.getMethod(getterName);
         }
         catch (NoSuchMethodException e)
         {
            throw new IllegalArgumentException("Getter parameter for property " + s + " on class " + clazz.getName()
               + " does not exist", e);
         }

         //
         if (getter.getReturnType() == void.class)
         {
            throw new IllegalArgumentException("Getter return type for property " + s + " on class " + clazz.getName()
               + " cannot be void");
         }
         if (getter.getParameterTypes().length > 0)
         {
            throw new IllegalArgumentException("Getter parameter type for property " + s + " on class "
               + clazz.getName() + " is not empty");
         }
         if (Modifier.isStatic(getter.getModifiers()))
         {
            throw new IllegalArgumentException("Getter for property " + s + " on class " + clazz.getName()
               + " is static");
         }

         //
         value = new DynamicValue(getter);
      }
      else
      {
         value = new LitteralValue(tmp);
      }

      //
      this.key = def.key();
      this.value = value;
   }

   public String resolveValue(Object instance)
   {
      return value.resolve(instance);
   }

   public String getKey()
   {
      return key;
   }

   private abstract static class Value
   {
      abstract String resolve(Object instance);
   }

   private class DynamicValue extends Value
   {

      /** . */
      private final Method getter;

      private DynamicValue(Method getter)
      {
         this.getter = getter;
      }

      boolean hasSpecialChars(String valueString)
      {
         for (int i = 0, length = valueString.length(); i < length; i++)
         {
            char c = valueString.charAt(i);
            switch (c)
            {
               case '%':
               case ':':
               case '"':
               case '=':
               case '?':
               case '*':
               case ',':
               case '\\':
               case '/':
               case '.':
               case '\'':
                  return true;
               default:
                  continue;
            }
         }
         return false;
      }

      String resolve(Object instance)
      {
         Object value;
         try
         {
            value = getter.invoke(instance);
         }
         catch (IllegalAccessException e)
         {
            throw new IllegalArgumentException("Getter for property " + key + " on class "
               + getter.getClass().getName() + " cannot be invoked", e);
         }
         catch (InvocationTargetException e)
         {
            throw new IllegalArgumentException("Getter for property " + key + " on class "
               + getter.getClass().getName() + " threw an exception during invocation", e);
         }
         if (value == null)
         {
            throw new IllegalArgumentException("Getter for property " + key + " on class "
               + getter.getClass().getName() + " returned a null value");
         }
         String valueString = value.toString();
         return hasSpecialChars(valueString) ? ObjectName.quote(valueString) : valueString;
      }
   }

   private static class LitteralValue extends Value
   {

      /** . */
      private final String litteral;

      private LitteralValue(String litteral)
      {
         this.litteral = litteral;
      }

      String resolve(Object instance)
      {
         return litteral;
      }
   }

}
