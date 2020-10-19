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

import java.io.Serializable;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Benjamin Mestrallet benjamin.mestrallet@exoplatform.com
 */
public class MapResourceBundle extends ResourceBundle implements Serializable
{

   /**
    * The serial version UID
    */
   private static final long serialVersionUID = -7020823660841958748L;

   private final static Pattern PATTERN = Pattern.compile("#\\{.*\\}");

   private Map<String, String> props;

   private Locale locale;

   public MapResourceBundle(Locale l)
   {
      this.locale = l;
      this.props = new HashMap<String, String>();
   }

   public MapResourceBundle(ResourceBundle rB, Locale l)
   {
      Map<String, String> props = new HashMap<String, String>();
      doMerge(props, rB);

      //
      this.locale = l;
      this.props = props;
   }

   private static void doMerge(Map<String, String> props, ResourceBundle rB)
   {
      Enumeration<String> e = rB.getKeys();
      while (e.hasMoreElements())
      {
         String key = e.nextElement();
         if (props.get(key) == null)
         {
            Object o = rB.getObject(key);
            if (o instanceof String)
            {
               String value = (String)o;
               props.put(key.intern(), value.intern());
            }
         }
      }
   }

   protected Object handleGetObject(String key)
   {
      return props.get(key);
   }

   public Enumeration<String> getKeys()
   {
      final Iterator<String> i = props.keySet().iterator();
      return new Enumeration<String>()
      {
         public boolean hasMoreElements()
         {
            return i.hasNext();
         }
         public String nextElement()
         {
            return i.next();
         }
      };
   }

   public Locale getLocale()
   {
      return this.locale;
   }

   public void add(String key, Object o)
   {
      if (key != null && o instanceof String)
      {
         String value = (String)o;
         props.put(key.intern(), value.intern());
      }
   }

   public void remove(String key)
   {
      if (key != null)
      {
         props.remove(key);
      }
   }

   public void merge(ResourceBundle bundle)
   {
      doMerge(props, bundle);
   }

   public void resolveDependencies()
   {
      Map<String, String> tempMap = new HashMap<String ,String>(props);
      for (String element : props.keySet())
      {
         String value = lookupKey(tempMap, element, new HashSet<String>());
         if (value != null)
         {
            tempMap.put(element.intern(), value.intern());
         }
      }
      props = tempMap;
   }

   private String lookupKey(Map<String, String> props, String key, Set<String> callStack)
   {
      String s = props.get(key);
      if (s == null || callStack.contains(key))
      {
         // The value cannot be found or it has already been asked which means that
         // a loop has been detected
         return key;
      }
      callStack.add(key);
      Matcher matcher = PATTERN.matcher(s);
      if (matcher.find())
      {
         return recursivedResolving(props, s, callStack);
      }
      // The value could be resolved thus it can be removed from the callStack
      callStack.remove(key);
      return s;
   }

   private String recursivedResolving(Map props, String value, Set<String> callStack)
   {
      String resolved = value;
      StringBuilder sB = new StringBuilder();
      while (resolved.indexOf("#{") != -1)
      {
         sB.setLength(0);
         int firstIndex = resolved.indexOf('#');
         int lastIndex = resolved.indexOf('}', firstIndex);
         String realKey = resolved.substring(firstIndex + 2, lastIndex);
         sB.append(resolved.substring(0, firstIndex));
         sB.append(lookupKey(props, realKey, callStack));
         sB.append(resolved.substring(lastIndex + 1));
         resolved = sB.toString();
      }
      return resolved;
   }
}
