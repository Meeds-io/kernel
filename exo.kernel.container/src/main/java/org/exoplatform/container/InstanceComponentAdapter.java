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

import org.exoplatform.container.spi.ContainerException;

/**
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public class InstanceComponentAdapter<T> extends AbstractComponentAdapter<T>
{
   /**
    * The serial version UID
    */
   private static final long serialVersionUID = 94127189297829247L;

   private final T componentInstance;

   @SuppressWarnings("unchecked")
   public InstanceComponentAdapter(Object componentKey, T componentInstance) throws ContainerException
   {
      super(componentKey, (Class<T>)componentInstance.getClass());
      this.componentInstance = componentInstance;
   }

   /**
    * {@inheritDoc}
    */
   public T getComponentInstance()
   {
      return componentInstance;
   }

   /**
    * {@inheritDoc}
    */
   public boolean isSingleton()
   {
      return true;
   }
}
