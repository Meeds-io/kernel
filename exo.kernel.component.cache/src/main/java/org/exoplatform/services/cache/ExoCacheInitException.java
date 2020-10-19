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
package org.exoplatform.services.cache;

/**
 * An exception that represents any type of exception that prevent the initialization of
 * the {@link org.exoplatform.services.cache.ExoCache}
 * 
 * @author <a href="mailto:dmitry.kataev@exoplatform.com">Dmytro Katayev</a>
 * @version $Id$
 *
 */
public class ExoCacheInitException extends Exception
{

   /**
    * The serial version UID
    */
   private static final long serialVersionUID = -5612051266167302943L;

   /**
    * {@inheritDoc}
    */
   public ExoCacheInitException(String message)
   {
      super(message);
   }

   /**
    * {@inheritDoc}
    */
   public ExoCacheInitException(Throwable cause)
   {
      super(cause);
   }

   /**
    * {@inheritDoc}
    */
   public ExoCacheInitException(String message, Throwable cause)
   {
      super(message, cause);
   }

}
