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
package org.exoplatform.container;

import org.exoplatform.container.jmx.AbstractTestContainer;

import java.net.URL;

/**
 * @author <a href="anatoliy.bazko@exoplatform.com">Anatoliy Bazko</a>
 * @version $Id: TestScopingObjectName.java 34360 2009-07-22 23:58:59Z tolusha $
 */
public class TestScopingObjectName extends AbstractTestContainer
{
   public void testHasScopingObjectName()
   {
      URL rootURL = getClass().getResource("empty-config.xml");
      URL portalURL = getClass().getResource("empty-config.xml");
      assertNotNull(rootURL);
      assertNotNull(portalURL);
      //
      new ContainerBuilder().withRoot(rootURL).withPortal(portalURL).build();
      assertNull(RootContainer.getInstance().getScopingObjectName());
      assertNotNull(PortalContainer.getInstance().getScopingObjectName());
   }

}
