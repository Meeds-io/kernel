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
 * This class allows you to create a new instance of {@link org.exoplatform.services.cache.ExoCache}
 * 
 * @author <a href="mailto:dmitry.kataev@exoplatform.com">Dmytro Katayev</a>
 *
 */
public interface ExoCacheFactory
{

   /**
    * Creates a new instance of {@link org.exoplatform.services.cache.ExoCache}
    * @param config the cache to create
    * @return the new instance of {@link org.exoplatform.services.cache.ExoCache}
    * @throws ExoCacheInitException if an exception happens while initializing the cache
    */
   public ExoCache createCache(ExoCacheConfig config) throws ExoCacheInitException;

}
