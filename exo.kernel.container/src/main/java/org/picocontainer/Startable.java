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
package org.picocontainer;

/**
 * <p>An interface which is implemented by components that can be started and stopped. The {@link Startable#start()}
 * must be called at the begin of the component lifecycle. It can be called again only after a call to
 * {@link Startable#stop()}. The {@link Startable#stop()} method must be called at the end of the component lifecycle,
 * and can further be called after every {@link Startable#start()}. If a component implements the {@link Disposable}
 * interface as well, {@link Startable#stop()} should be called before {@link Disposable#dispose()}.</p>
 * <br>
 * <p>For more advanced and pluggable lifecycle support, see the functionality offered by the nanocontainer-proxytoys
 * subproject.</p>
 * 
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public interface Startable
{
   /**
    * Start this component. Called initially at the begin of the lifecycle. It can be called again after a stop.
    */
   void start();

   /**
    * Stop this component. Called near the end of the lifecycle. It can be called again after a further start. Implement
    * {@link Disposable} if you need a single call at the definite end of the lifecycle.
    */
   void stop();
}
