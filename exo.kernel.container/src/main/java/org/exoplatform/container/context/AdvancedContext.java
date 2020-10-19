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
package org.exoplatform.container.context;

import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.Contextual;

/**
 * This interface defines all the additional methods needed to easily implement 
 * an AlterableContext
 * 
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public interface AdvancedContext<K> extends Context
{
   /**
    * Registers a new key to the context
    * @param key the key to register
    */
   void register(K key);

   /**
    * Unregisters a given key from the context
    * @param key the key to unregister
    */
   void unregister(K key);

   /**
    * Activates the current context using the given key within the context of the thread
    * @param key the key to use to activate the context
    */
   void activate(K key);

   /**
    * Deactivates the current context using the given key from the context of the thread
    * @param key the key to use to deactivate the context
    */
   void deactivate(K key);

   /**
    * <p>
    * Destroy the existing contextual instance. If there is no existing instance, no action is taken.
    * </p>
    * 
    * @param contextual the contextual type
    * @throws ContextNotActiveException if the context is not active
    */
   void destroy(Contextual<?> contextual);
}
