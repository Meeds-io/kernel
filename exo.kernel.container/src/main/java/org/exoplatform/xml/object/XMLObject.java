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

import org.exoplatform.commons.utils.ClassLoading;
import org.exoplatform.commons.utils.SecurityHelper;
import org.exoplatform.container.xml.Configuration;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Tuan Nguyen (tuan08@users.sourceforge.net)
 * @since Apr 10, 2005
 * @version $Id: XMLObject.java 11659 2007-01-05 15:35:06Z geaz $
 */
public class XMLObject
{
   
   /**
    * The logger
    */
   private static final Log LOG = ExoLogger.getLogger("exo.kernel.container.XMLObject");
   
   public static String CURRENT_VERSION = "1.0";

   static Map<Class<?>, Map<String, Field>> cacheFields_ = new HashMap<Class<?>, Map<String, Field>>();

   private Map<String, XMLField> fields_ = new HashMap<String, XMLField>();

   private String type;

   public XMLObject()
   {
   }

   public XMLObject(Object obj) throws Exception
   {
      Class<?> clazz = obj.getClass();
      Map<String, Field> fields = getFields(clazz);
      setType(obj.getClass().getName());
      Iterator<Field> i = fields.values().iterator();
      while (i.hasNext())
      {
         Field field = i.next();
         Object value = field.get(obj);
         if (value == null
            || (!value.getClass().isPrimitive() && Configuration.hasComponent(field.getType().getName())))
         {
            // The current field is a component so we ignore it or its value is null
            continue;
         }
         addField(new XMLField(field.getName(), field.getType(), value));
      }
   }

   public String getType()
   {
      return type;
   }

   public void setType(String s)
   {
      type = s;
   }

   public XMLField getField(String name)
   {
      return fields_.get(name);
   }

   public void addField(Object o)
   {
      XMLField field = (XMLField)o;
      fields_.put(field.getName(), field);
   }

   public void addField(XMLField field)
   {
      fields_.put(field.getName(), field);
   }

   public Iterator<XMLField> getFieldIterator()
   {
      return fields_.values().iterator();
   }

   public Collection<XMLField> getFields()
   {
      return fields_.values();
   }

   public void setFields(Collection<XMLField> fields)
   {
      Iterator<XMLField> i = fields.iterator();
      while (i.hasNext())
      {
         XMLField field = i.next();
         fields_.put(field.getName(), field);
      }
   }

   public void setFields(Map<String, XMLField> fields)
   {
      fields_.putAll(fields);
   }

   public Object getFieldValue(String fieldName) throws Exception
   {
      XMLField field = fields_.get(fieldName);
      if (field != null)
         return field.getObjectValue();
      return null;
   }

   public void renameField(String oldName, String newName)
   {
      XMLField field = fields_.remove(oldName);
      field.setName(newName);
      fields_.put(newName, field);
   }

   public void removeField(String name)
   {
      fields_.remove(name);
   }

   public void addField(String name, Class<?> fieldType, Object obj) throws Exception
   {
      addField(new XMLField(name, fieldType, obj));
   }

   @Override
   public String toString()
   {
      StringBuffer b = new StringBuffer();
      b.append("type: ").append(type).append("\n");
      Iterator<XMLField> i = fields_.values().iterator();
      while (i.hasNext())
      {
         XMLField field = i.next();
         b.append(field.toString()).append("\n");
      }
      return b.toString();
   }

   public Object toObject() throws Exception
   {
      Class<?> clazz = ClassLoading.forName(type, this);
      Map<String, Field> fields = getFields(clazz);
      Object instance = clazz.newInstance();
      Iterator<XMLField> i = fields_.values().iterator();
      while (i.hasNext())
      {
         XMLField xmlfield = i.next();
         try
         {
            Object value = xmlfield.getObjectValue();
            Field field = fields.get(xmlfield.getName());
            field.set(instance, value);
         }
         catch (Exception ex)
         {
            LOG.error("ERROR: Cannot set  field: " + xmlfield.getName() + " of " + type, ex);
            throw ex;
         }
      }
      return instance;
   }

   public String toXML() throws Exception
   {
      return toXML("UTF-8");
   }

   public String toXML(String encoding) throws Exception
   {
      return new String(toByteArray(encoding), encoding);
   }

   public byte[] toByteArray() throws Exception
   {
      return toByteArray("UTF-8");
   }

   public byte[] toByteArray(String encoding) throws Exception
   {
      IBindingFactory bfact = getBindingFactoryInPriviledgedMode(XMLObject.class);
      IMarshallingContext mctx = bfact.createMarshallingContext();
      mctx.setIndent(2);
      ByteArrayOutputStream os = new ByteArrayOutputStream();
      mctx.marshalDocument(this, encoding, null, os);
      return os.toByteArray();
   }

   public static XMLObject getXMLObject(InputStream is) throws Exception
   {
      IBindingFactory bfact = getBindingFactoryInPriviledgedMode(XMLObject.class);
      IUnmarshallingContext uctx = bfact.createUnmarshallingContext();
      XMLObject xmlobject = (XMLObject)uctx.unmarshalDocument(is, "UTF-8");
      return xmlobject;
   }

   public static Object getObject(InputStream is) throws Exception
   {
      return getXMLObject(is).toObject();
   }

   static Map<String, Field> getFields(Class<?> clazz)
   {
      Map<String, Field> fields = (Map<String, Field>)cacheFields_.get(clazz);
      if (fields != null)
         return fields;
      synchronized (cacheFields_)
      {
         fields = new HashMap<String, Field>();
         findFields(fields, clazz);
         cacheFields_.put(clazz, fields);
      }
      return fields;
   }

   static void findFields(Map<String, Field> fields, Class<?> clazz)
   {
      if (clazz.getName().startsWith("java.lang"))
         return;
      findFields(fields, clazz.getSuperclass());
      Field[] field = clazz.getDeclaredFields();
      for (int i = 0; i < field.length; i++)
      {
         int modifier = field[i].getModifiers();
         if (Modifier.isStatic(modifier) || Modifier.isTransient(modifier))
            continue;
         
         final Field fld = field[i];

         SecurityHelper.doPrivilegedAction(new PrivilegedAction<Void>()
         {
            public Void run()
            {
               fld.setAccessible(true);
               return null;
            }
         });

         fields.put(field[i].getName(), field[i]);
      }
   }

   protected static IBindingFactory getBindingFactoryInPriviledgedMode(final Class<?> clazz) throws JiBXException
   {
      try
      {
         return SecurityHelper.doPrivilegedExceptionAction(new PrivilegedExceptionAction<IBindingFactory>()
         {
            public IBindingFactory run() throws Exception
            {
               return BindingDirectory.getFactory(clazz);
            }
         });
      }
      catch (PrivilegedActionException pae)
      {
         Throwable cause = pae.getCause();
         if (cause instanceof JiBXException)
         {
            throw (JiBXException)cause;
         }
         else if (cause instanceof RuntimeException)
         {
            throw (RuntimeException)cause;
         }
         else
         {
            throw new RuntimeException(cause);
         }
      }
   }
}
