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
package org.exoplatform.container.jmx;

import org.exoplatform.container.RootContainer;
import org.exoplatform.container.jmx.support.ManagedByManager;
import org.exoplatform.container.jmx.support.Manager;

import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TestManagedBy extends AbstractTestContainer
{

   public void testFoo() throws Exception
   {
      RootContainer root = createRootContainer("managedby-configuration.xml");

      MBeanServer server = root.getMBeanServer();

      ManagedByManager mbm = (ManagedByManager)root.getComponentInstance("ManagedByManager");

      assertNotNull(mbm);

      ObjectInstance instance = server.getObjectInstance(new ObjectName("exo:object=ManagedByManager"));

      Manager manager = (Manager)server.getAttribute(instance.getObjectName(), "Reference");

      assertNotNull(manager);

   }

}