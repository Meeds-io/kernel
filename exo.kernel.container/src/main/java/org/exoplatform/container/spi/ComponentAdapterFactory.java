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
package org.exoplatform.container.spi;

/**
 * A component adapter factory is responsible for creating
 * {@link ComponentAdapter component adapters}.
 * explicitly.
 * 
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public interface ComponentAdapterFactory
{
   /**
    * Create a new component adapter based on the specified arguments.
    *
    * @param componentKey            the key to be associated with this adapter. This value should be returned
    *                                from a call to {@link ComponentAdapter#getComponentKey()} on the created adapter.
    * @param componentImplementation the implementation class to be associated with this adapter.
    *                                This value should be returned from a call to
    *                                {@link ComponentAdapter#getComponentImplementation()} on the created adapter. Should not
    *                                be null.
    * @return a new component adapter based on the specified arguments. Should not return null.
    * @throws ContainerException if the creation of the component adapter results in a
    *                                    {@link ContainerException}.
    */
   <T> ComponentAdapter<T> createComponentAdapter(Object componentKey, Class<T> componentImplementation)
      throws ContainerException;
}