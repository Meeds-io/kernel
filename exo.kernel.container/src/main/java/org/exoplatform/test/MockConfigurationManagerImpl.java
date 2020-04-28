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
package org.exoplatform.test;

import org.exoplatform.commons.utils.PrivilegedSystemHelper;
import org.exoplatform.commons.utils.SecurityHelper;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.configuration.ConfigurationManagerImpl;

import java.net.URL;
import java.security.PrivilegedAction;

import javax.servlet.ServletContext;

/**
 * Jul 19, 2004
 * 
 * @author: Tuan Nguyen
 * @version: $Id: MockConfigurationManagerImpl.java 5799 2006-05-28 17:55:42Z
 *           geaz $
 */
public class MockConfigurationManagerImpl extends ConfigurationManagerImpl
{
   private String confDir_;

   public MockConfigurationManagerImpl(ServletContext context) throws Exception
   {
      super(context, ExoContainer.getProfilesFromProperty());
      confDir_ = PrivilegedSystemHelper.getProperty("mock.portal.dir") + "/WEB-INF";
   }

   @Override
   public URL getURL(String uri) throws Exception
   {
      if (uri.startsWith("jar:"))
      {
         String path = removePrefix("jar:", uri);
         if (path.startsWith("/"))
         {
            path = path.substring(1);
         }
         final ClassLoader cl = Thread.currentThread().getContextClassLoader();
         final String finalPath = path;
         return SecurityHelper.doPrivilegedAction(new PrivilegedAction<URL>()
         {
            public URL run()
            {
               return cl.getResource(finalPath);
            }
         });
      }
      else if (uri.startsWith("classpath:"))
      {
         String path = removePrefix("classpath:", uri);
         if (path.startsWith("/"))
         {
            path = path.substring(1);
         }
         return PrivilegedSystemHelper.getResource(path);
      }
      else if (uri.startsWith("war:"))
      {
         String path = removePrefix("war:", uri);
         URL url = new URL("file:" + confDir_ + path);
         return url;
      }
      else if (uri.startsWith("file:"))
      {
         return new URL(uri);
      }
      return null;
   }
}
