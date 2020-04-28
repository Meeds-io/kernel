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
package org.exoplatform.container;

import org.exoplatform.container.spi.ComponentAdapter;

import java.util.Collection;

/**
 * This defines a kind of {@link ComponentAdapter} that is aware of its dependencies
 * 
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public interface ComponentAdapterDependenciesAware<T> extends ComponentAdapter<T>
{

   /**
    * Gives the create dependencies of the component
    * @return a {@link Collection} of {@link Dependency} objects representing the 
    * dependencies of the component for the creation phase
    */
   Collection<Dependency> getCreateDependencies();

   /**
    * Gives the initialization dependencies of the component
    * @return a {@link Collection} of {@link Dependency} objects representing the 
    * dependencies of the component for the initialization phase
    */
   Collection<Dependency> getInitDependencies();
}
