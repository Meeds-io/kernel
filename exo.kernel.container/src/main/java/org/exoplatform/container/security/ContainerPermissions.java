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
package org.exoplatform.container.security;

import java.security.Permission;

/**
 * This interface defines all the permissions used by the project kernel.container
 * 
 * @author <a href="mailto:nicolas.filotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public interface ContainerPermissions
{
   /**
    * Any operations applied on a container have to be protected with this permission
    */
   public static final Permission MANAGE_CONTAINER_PERMISSION = new RuntimePermission("manageContainer");
   
   /**
    * Any component modifications have to be protected with this permission
    */
   public static final Permission MANAGE_COMPONENT_PERMISSION = new RuntimePermission("manageComponent"); 
}
