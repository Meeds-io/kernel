/*
 * Copyright (C) 2013 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.exoplatform.container;

import org.exoplatform.container.spi.ComponentAdapter;

import javax.inject.Provider;

/**
 * This defines a dependency by provider
 *
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public class DependencyByProvider extends Dependency
{

   private final Provider<Object> provider;
   private final ComponentAdapter<?> adapter;

   public DependencyByProvider(Object key, Class<?> bindType, Provider<Object> provider, ComponentAdapter<?> adapter)
   {
      super(key, bindType, true);
      this.provider = provider;
      this.adapter = adapter;
   }

   /**
    * {@inheritDoc}
    */
   protected Object load(ExoContainer holder)
   {
      return provider;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected ComponentAdapter<?> getAdapter(ExoContainer holder)
   {
      return adapter;
   }

   /**
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString()
   {
      return "DependencyByProvider [key=" + key + ", bindType=" + bindType + "]";
   }
}
