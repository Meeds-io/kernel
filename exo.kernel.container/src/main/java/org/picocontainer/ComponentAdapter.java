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
package org.picocontainer;

/**
 * This is used for backward compatibility, now you are supposed to use 
 * {@link org.exoplatform.container.spi.ComponentAdapter}
 * 
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 * @deprecated use {@link org.exoplatform.container.spi.ComponentAdapter} instead
 */
public interface ComponentAdapter<T>
{
   /**
    * Retrieve the key associated with the component.
    * 
    * @return the component's key. Should either be a class type (normally an interface) or an identifier that is
    *         unique (within the scope of the current Container).
    */
   Object getComponentKey();

   /**
    * Retrieve the class of the component.
    * 
    * @return the component's implementation class. Should normally be a concrete class (ie, a class that can be
    *         instantiated).
    */
   Class<? extends T> getComponentImplementation();
}
