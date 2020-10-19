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
package org.exoplatform.container.component;

import java.security.Permission;

/**
 * <p>Defines a component that holds variables of type {@link ThreadLocal}
 * whose value is required by the component to work normally and cannot be recovered. 
 * This component is mainly used when we want to do a task asynchronously, in that case
 * to ensure that the task will be executed in the same conditions as if it would be 
 * executed synchronously we need to transfer the thread context from the original
 * thread to the executor thread.</p>
 * 
 * <p>Warning please note that this interface must be used with caution, only
 * the most important components that have {@link ThreadLocal} variables whose value
 * cannot be recovered should implement this interface.</p>
 * 
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public interface ThreadContextHolder
{
   public static final Permission MANAGE_THREAD_LOCAL = new RuntimePermission("manageThreadLocal"); 

   /**
    * Gives the value corresponding to the context of the thread
    * @return a new instance of {@link ThreadContext} if there are some
    * valuable {@link ThreadLocal} variables to share otherwise <code>null</code>
    * is expected
    */
   ThreadContext getThreadContext();
}
