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

import org.exoplatform.commons.reflect.AnnotationIntrospector;
import org.exoplatform.management.jmx.annotations.NameTemplate;
import org.exoplatform.management.jmx.annotations.NamingContext;
import org.exoplatform.management.jmx.annotations.Property;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class PropertiesInfo
{

   /** . */
   private final Map<String, PropertyInfo> properties;

   public PropertiesInfo(Map<String, PropertyInfo> properties)
   {
      this.properties = properties;
   }

   public static PropertiesInfo resolve(Class clazz, Class<? extends Annotation> annotationClass)
   {
      Annotation tpl2 = AnnotationIntrospector.resolveClassAnnotations(clazz, annotationClass);
      Property[] blah = null;
      if (tpl2 instanceof NamingContext)
      {
         blah = ((NamingContext)tpl2).value();
      }
      else if (tpl2 instanceof NameTemplate)
      {
         blah = ((NameTemplate)tpl2).value();
      }
      if (blah != null)
      {
         Map<String, PropertyInfo> properties = new LinkedHashMap<String, PropertyInfo>();
         for (Property property : blah)
         {
            PropertyInfo propertyInfo = new PropertyInfo(clazz, property);
            properties.put(propertyInfo.getKey(), propertyInfo);
         }
         return new PropertiesInfo(properties);
      }
      else
      {
         return null;
      }
   }

   public Collection<PropertyInfo> getProperties()
   {
      return properties.values();
   }

   public MBeanScopingData resolve(Object instance)
   {
      MBeanScopingData props = new MBeanScopingData();
      for (PropertyInfo propertyInfo : properties.values())
      {
         String key = propertyInfo.getKey();
         String value = propertyInfo.resolveValue(instance);
         props.put(key, value);
      }
      return props;
   }
}
