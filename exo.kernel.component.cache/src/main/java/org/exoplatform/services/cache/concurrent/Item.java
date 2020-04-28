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
package org.exoplatform.services.cache.concurrent;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class Item
{

   private static final AtomicLong generator = new AtomicLong(0);

   final long serial = generator.incrementAndGet();

   final int hashCode = (int)(serial % Integer.MAX_VALUE);

   Item previous;

   Item next;

   /**
    * This is final on purpose, we rely on object equality in the concurrent has
    */
   public final boolean equals(Object obj)
   {
      return obj != null && ((Item)obj).serial == serial;
   }

   /**
    * This is final on purpose, we rely on object equality in the concurrent has
    */
   public final int hashCode()
   {
      return hashCode;
   }
}
