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

import org.exoplatform.management.spi.ManagedTypeMetaData;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ManagedResource
{

   final ResourceKey key;

   /** . */
   final Object resource;

   /** . */
   final org.exoplatform.management.spi.ManagedResource context;

   /** . */
   final ManagedTypeMetaData metaData;

   /** . */
   final ScopedData data;

   public ManagedResource(Object resource, org.exoplatform.management.spi.ManagedResource context, ManagedTypeMetaData metaData)
   {
      this.key = new ResourceKey();
      this.resource = resource;
      this.context = context;
      this.metaData = metaData;
      this.data = new ScopedData();
   }

   public void register()
   {
      context.setScopingData(ScopedData.class, data);
   }

   @Override
   public String toString()
   {
      return "ManagedResource[key=" + key + ",resource=" + resource + "]";
   }
}
