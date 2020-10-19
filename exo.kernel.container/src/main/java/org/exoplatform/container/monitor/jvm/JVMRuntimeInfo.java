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
package org.exoplatform.container.monitor.jvm;

import org.exoplatform.container.RootContainer;
import org.exoplatform.container.StandaloneContainer;
import org.exoplatform.container.spi.DefinitionByType;

import java.util.List;
import java.util.Map;

/**
 * @author Tuan Nguyen (tuan08@users.sourceforge.net)
 * @since Nov 8, 2004
 * @version $Id: JVMRuntimeInfo.java 5799 2006-05-28 17:55:42Z geaz $
 */
@DefinitionByType(type = JVMRuntimeInfoImpl.class, target = {RootContainer.class, StandaloneContainer.class})
public interface JVMRuntimeInfo
{
   final static public String MEMORY_MANAGER_MXBEANS = "MemoryManagerMXBean";

   final static public String MEMORY_POOL_MXBEANS = "MemoryPoolMXBeans";

   final static public String GARBAGE_COLLECTOR_MXBEANS = "GarbageCollectorMXBeans";

   String getName();

   String getSpecName();

   String getSpecVendor();

   String getSpecVersion();

   String getManagementSpecVersion();

   String getVmName();

   String getVmVendor();

   String getVmVersion();

   List getInputArguments();

   Map getSystemProperties();

   boolean getBootClassPathSupported();

   String getBootClassPath();

   String getClassPath();

   String getLibraryPath();

   long getStartTime();

   long getUptime();

   public boolean isManagementSupported();

   public String getSystemPropertiesAsText();
}
