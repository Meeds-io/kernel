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
package org.exoplatform.commons.utils.secure;

import java.security.Permission;
import java.util.List;
import java.util.Set;

/**
 * @author <a href="mailto:nikolazius@gmail.com">Nikolay Zamosenchuk</a>
 * @version $Id: SecureCollections.java 34360 2009-07-22 23:58:59Z nzamosenchuk $
 *
 */
public class SecureCollections
{
   /**
    * Private constructor, used to disallow instance creation  
    */
   private SecureCollections()
   {
   }

   /**
    * Creates {@link SecureSet}, which will require given {@link Permission} for it's modification
    * 
    * @param <E>
    * @param set
    *    Base List instance
    * @param permission
    *    Required permission
    * @return
    */
   public static <E> Set<E> secureSet(Set<E> set, Permission permission)
   {
      return new SecureSet<E>(set, permission);
   }

   /**
    * Creates {@link SecureList}, which will require given {@link Permission} for it's modification
    * 
    * @param <E>
    * @param list
    *    Base list instance
    * @param permission
    *    Required permission
    * @return
    */
   public static <E> List<E> secureList(List<E> list, Permission permission)
   {
      return new SecureList<E>(list, permission);
   }
}
