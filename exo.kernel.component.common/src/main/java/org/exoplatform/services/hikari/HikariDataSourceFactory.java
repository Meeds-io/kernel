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
package org.exoplatform.services.hikari;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariConfig;
import java.util.Hashtable;
import java.util.Properties;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;
import javax.sql.DataSource;


/**
 * @author aboughzela@exoplatform.com
 */
public class HikariDataSourceFactory implements ObjectFactory
{
   /**
    * {@inheritDoc}
    */
   public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment) throws Exception
   {
      if ((obj == null) || !(obj instanceof Reference))
      {
         return null;
      }

      Reference ref = (Reference)obj;
      if (!"javax.sql.DataSource".equals(ref.getClassName()))
      {
         return null;
      }

      Properties properties = new Properties();
      for (int i = 0; i < ref.size(); i++)
      {
         String propertyName = ref.get(i).getType();
         RefAddr ra = ref.get(propertyName);
         if (ra != null)
         {
            String propertyValue = ra.getContent().toString();
            properties.setProperty(propertyName, propertyValue);
         }
      }
      return createDataSource(properties);
   }

   public static DataSource createDataSource(Properties properties) throws Exception
   {
      HikariConfig config = new HikariConfig(properties);

      return new HikariDataSource(config);
   }

}