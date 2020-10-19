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
package org.exoplatform.management.spi;

/**
 * This interface is implemented by a management provider such a JMX.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public interface ManagementProvider
{

   /**
    * Instruct the management provider to manage the provided managed resource. If any registration is done
    * the provider should return an unique key that will be used later for unregistration purpose in the
    * {@link #unmanage(Object)} method. If no registration is performed then null should be returned.
    *
    * @param managedResource the managed resource
    * @return the key under which the resource is registered
    */
   Object manage(ManagedResource managedResource);

   /**
    * Instruct the management provider to remove the specifed resource from management.
    *
    * @param key the key under which the resource is registered
    */
   void unmanage(Object key);

}
