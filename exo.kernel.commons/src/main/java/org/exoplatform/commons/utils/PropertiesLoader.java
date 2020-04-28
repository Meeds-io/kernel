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

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Properties;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class PropertiesLoader
{

   private static class LinkedProperties extends Properties
   {

      /** A list that contains each element at most once. */
      private LinkedHashMap<String, String> list = new LinkedHashMap<String, String>();

      @Override
      public Object put(Object key, Object value)
      {
         if (list.containsKey(key))
         {
            list.remove(key);
         }
         list.put((String)key, (String)value);
         return super.put(key, value);
      }

      @Override
      public Object remove(Object key)
      {
         list.remove(key);
         return super.remove(key);
      }

   }

   public static LinkedHashMap<String, String> load(InputStream in) throws IOException
   {
      LinkedProperties props = new LinkedProperties();
      props.load(in);
      return props.list;
   }

   public static LinkedHashMap<String, String> loadFromXML(InputStream in) throws IOException
   {
      LinkedProperties props = new LinkedProperties();
      props.loadFromXML(in);
      return props.list;
   }
}
