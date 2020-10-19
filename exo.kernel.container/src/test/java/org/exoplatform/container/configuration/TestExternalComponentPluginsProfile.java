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
package org.exoplatform.container.configuration;

import org.exoplatform.container.xml.Configuration;
import org.exoplatform.container.xml.ExternalComponentPlugins;

import java.util.Iterator;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TestExternalComponentPluginsProfile extends AbstractProfileTest
{

   public void testNoProfile() throws Exception
   {
      Configuration config = getConfiguration("external-component-plugins.xml");
      int size = 0;
      for (Iterator<ExternalComponentPlugins> it = config.getExternalComponentPluginsIterator();it.hasNext();)
      {
         ExternalComponentPlugins ecp = it.next();
         assertEquals(1, ecp.getComponentPlugins().size());
         size++;
      }
      assertEquals(1, size);
   }

   public void testFooProfile() throws Exception
   {
      Configuration config = getConfiguration("external-component-plugins.xml", "foo");
      int size = 0;
      for (Iterator<ExternalComponentPlugins> it = config.getExternalComponentPluginsIterator();it.hasNext();)
      {
         ExternalComponentPlugins ecp = it.next();
         assertEquals(2, ecp.getComponentPlugins().size());
         size++;
      }
      assertEquals(2, size);
   }

   public void testFooBarProfiles() throws Exception
   {
      Configuration config = getConfiguration("external-component-plugins.xml", "foo", "bar");
      int size = 0;
      for (Iterator<ExternalComponentPlugins> it = config.getExternalComponentPluginsIterator();it.hasNext();)
      {
         ExternalComponentPlugins ecp = it.next();
         assertEquals(3, ecp.getComponentPlugins().size());
         size++;
      }
      assertEquals(3, size);
   }
}