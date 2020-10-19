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

import org.exoplatform.container.spi.ComponentAdapter;
import org.exoplatform.container.spi.ContainerException;

import java.io.Serializable;

/**
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public abstract class AbstractComponentAdapter<T> implements ComponentAdapter<T>, Serializable
{
   /**
    * The serial version UID
    */
   private static final long serialVersionUID = -8879158955898247836L;

   private Object componentKey;

   private Class<T> componentImplementation;

   /**
    * Constructs a new ComponentAdapter for the given key and implementation. 
    * @param componentKey the search key for this implementation
    * @param componentImplementation the concrete implementation
    * @throws ContainerException if the key is a type and the implementation cannot be assigned to.
    */
   protected AbstractComponentAdapter(Object componentKey, Class<T> componentImplementation)
      throws ContainerException
   {
      if (componentImplementation == null)
      {
         throw new NullPointerException("componentImplementation");
      }
      this.componentKey = componentKey;
      this.componentImplementation = componentImplementation;
      checkTypeCompatibility();
   }

   /**
    * {@inheritDoc}
    */
   public Object getComponentKey()
   {
      if (componentKey == null)
      {
         throw new NullPointerException("componentKey");
      }
      return componentKey;
   }

   /**
    * {@inheritDoc}
    */
   public Class<T> getComponentImplementation()
   {
      return componentImplementation;
   }

   protected void checkTypeCompatibility() throws ContainerException
   {
      if (componentKey instanceof Class<?>)
      {
         Class<?> componentType = (Class<?>)componentKey;
         if (!componentType.isAnnotation() && !componentType.isAssignableFrom(componentImplementation))
         {
            throw new ContainerException("The type:" + componentType.getName() + "  was not assignable from the class "
               + componentImplementation.getName());
         }
      }
   }

   /**
    * @return Returns the ComponentAdapter's class name and the component's key.
    * @see java.lang.Object#toString()
    */
   public String toString()
   {
      return getClass().getName() + "[" + getComponentKey() + "]";
   }
}
