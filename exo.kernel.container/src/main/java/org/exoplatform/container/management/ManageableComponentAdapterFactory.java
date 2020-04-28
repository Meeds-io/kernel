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
package org.exoplatform.container.management;

import org.exoplatform.container.ConcurrentContainer;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.spi.ComponentAdapter;
import org.exoplatform.container.spi.ComponentAdapterFactory;
import org.exoplatform.container.spi.ContainerException;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ManageableComponentAdapterFactory implements ComponentAdapterFactory
{

   /** . */
   private final ExoContainer holder;

   /** . */
   private final ConcurrentContainer container;

   public ManageableComponentAdapterFactory(ExoContainer holder, ConcurrentContainer container)
   {
      this.holder = holder;
      this.container = container;
   }

   public <T> ComponentAdapter<T> createComponentAdapter(Object componentKey, Class<T> componentImplementation) 
            throws ContainerException
   {
      return new ManageableComponentAdapter<T>(holder, container, componentKey, componentImplementation);
   }
}
