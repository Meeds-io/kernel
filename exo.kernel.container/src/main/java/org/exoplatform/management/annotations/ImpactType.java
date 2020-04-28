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

package org.exoplatform.management.annotations;

/**
 * The type of the impact of a managed method.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public enum ImpactType
{

   /**
    * Read operation, does not affect state of the managed resource.
    * Equivalent of {@link javax.management.MBeanOperationInfo#INFO} for JMX and GET method for Rest.
    */
   READ,

   /**
    * Write operation, changes the state of the managed resource.
    * Equivalent of {@link javax.management.MBeanOperationInfo#INFO} for JMX and POST method for Rest.
    */
   WRITE,

   /**
    * Write operation, changes the state of the managed resource in an idempotent manner.
    * Equivalent of {@link javax.management.MBeanOperationInfo#INFO} for JMX and PUT method for Rest.
    */
   IDEMPOTENT_WRITE

}
