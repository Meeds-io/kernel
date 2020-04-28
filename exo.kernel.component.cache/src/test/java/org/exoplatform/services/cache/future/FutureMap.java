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
package org.exoplatform.services.cache.future;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class FutureMap<C> extends FutureCache<String, String, C>
{

   /** . */
   final Map<String, String> data;

   public FutureMap(Loader<String, String, C> loader)
   {
      super(loader);

      //
      this.data = Collections.synchronizedMap(new HashMap<String, String>());
   }

   @Override
   protected String get(String key)
   {
      return data.get(key);
   }

   @Override
   protected void put(String key, String value)
   {
      data.put(key, value);
   }

   @Override
   protected void putOnly(String key, String value)
   {
      put(key, value);
   }
}
