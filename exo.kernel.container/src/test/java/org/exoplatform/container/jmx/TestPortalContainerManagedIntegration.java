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

import org.exoplatform.container.ContainerBuilder;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.RootContainer;
import org.exoplatform.container.jmx.support.SimpleManagementAware;
import org.exoplatform.container.management.ManagementContextImpl;

import java.net.URL;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TestPortalContainerManagedIntegration extends AbstractTestContainer
{

   public void testManagementContext()
   {
      URL rootURL = TestPortalContainerManagedIntegration.class.getResource("root-configuration.xml");
      URL portalURL = TestPortalContainerManagedIntegration.class.getResource("portal-configuration.xml");

      //
      RootContainer root = new ContainerBuilder().withRoot(rootURL).withPortal(portalURL).build();
      ManagementContextImpl rootManagementContext = (ManagementContextImpl)root.getManagementContext();

      //
      PortalContainer portal = PortalContainer.getInstance();
      ManagementContextImpl portalManagementContext = (ManagementContextImpl)portal.getManagementContext();
      assertSame(root.getManagementContext(), portalManagementContext.getParent());
      assertNotNull(portalManagementContext.findContainer());

      //
      SimpleManagementAware rootManagementAware = (SimpleManagementAware)root.getComponentInstance("RootManagementAware");
      ManagementContextImpl rootManagementAwareContext = (ManagementContextImpl)rootManagementAware.context;
      assertSame(rootManagementContext, rootManagementAwareContext.getParent());

      //
      SimpleManagementAware portalManagementAware = (SimpleManagementAware)portal.getComponentInstance("PortalManagementAware");
      ManagementContextImpl portalManagementAwareContext = (ManagementContextImpl)portalManagementAware.context;
      assertSame(portalManagementContext, portalManagementAwareContext.getParent());
   }

}
