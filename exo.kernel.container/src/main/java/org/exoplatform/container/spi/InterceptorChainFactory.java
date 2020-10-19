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
package org.exoplatform.container.spi;

import org.exoplatform.container.ExoContainer;

/**
 * This interface is used to create a new {@link Interceptor} chain
 * 
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public interface InterceptorChainFactory
{

   /**
    * Creates a new {@link Interceptor} chain with the provided {@link ExoContainer} instance as parent container
    * and with the provided {@link ExoContainer} as holder of the container. The holder is mostly
    * used when it is required to be able to go through the chain of {@link Interceptor}
    * @param holder the holder of the container
    * @param parent the parent container, if <code>null</code> the container will be considered
    * as the root container.
    * @return an {@link Interceptor} corresponding to the head of the {@link Interceptor} chain
    */
   Interceptor getInterceptorChain(ExoContainer holder, ExoContainer parent);
}
