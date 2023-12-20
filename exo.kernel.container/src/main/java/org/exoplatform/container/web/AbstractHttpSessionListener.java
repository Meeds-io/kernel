/*
 * Copyright (C) 2009 eXo Platform SAS.
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
package org.exoplatform.container.web;

import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.RootContainer;
import org.exoplatform.container.util.ContainerUtil;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;

/**
 * Created by The eXo Platform SAS
 * Author : Nicolas Filotto 
 *          nicolas.filotto@exoplatform.com
 * 29 sept. 2009  
 */
public abstract class AbstractHttpSessionListener implements HttpSessionListener
{
   /**
    * The logger
    */
   private static final Log LOG = ExoLogger.getLogger("exo.kernel.container.AbstractHttpSessionListener");

   /**
    * @see jakarta.servlet.http.HttpSessionListener#sessionCreated(jakarta.servlet.http.HttpSessionEvent)
    */
   public final void sessionCreated(HttpSessionEvent event)
   {
      final ExoContainer oldContainer = ExoContainerContext.getCurrentContainerIfPresent();
      // Keep the old ClassLoader
      final ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
      ExoContainer container = null;
      boolean hasBeenSet = false;
      try
      {
         container = getContainer(event);
         if (container == null)
            return;
         if (!container.equals(oldContainer))
         {
            if (container instanceof PortalContainer)
            {
               PortalContainer.setInstance((PortalContainer)container);
            }
            ExoContainerContext.setCurrentContainer(container);
            hasBeenSet = true;
         }
         if (requirePortalEnvironment())
         {
            final String ctxName = ContainerUtil.getServletContextName(event.getSession().getServletContext());
            if (!PortalContainer.isPortalContainerNameDisabled(ctxName) && container instanceof PortalContainer)
            {
               if (PortalContainer.getInstanceIfPresent() == null)
               {
                  // The portal container has not been set
                  PortalContainer.setInstance((PortalContainer)container);
                  hasBeenSet = true;
               }
               // Set the full classloader of the portal container
               Thread.currentThread().setContextClassLoader(((PortalContainer)container).getPortalClassLoader());
            }
            else
            {
               if (PropertyManager.isDevelopping())
               {
                  LOG.info("The portal environment could not be set for the webapp '" + ctxName
                     + "' because this servlet context has not been defined as a "
                     + "dependency of any portal container or it is a disabled portal"
                     + " container, the sessionCreated event will be ignored");
               }
               return;
            }
         }
         onSessionCreated(container, event);
      }
      finally
      {
         if (hasBeenSet)
         {
            if (container instanceof PortalContainer)
            {
               // Remove the current Portal Container and the current ExoContainer
               PortalContainer.setInstance(null);
            }
            // Re-set the old container
            ExoContainerContext.setCurrentContainer(oldContainer);
         }
         if (requirePortalEnvironment())
         {
            // Re-set the old classloader
            Thread.currentThread().setContextClassLoader(currentClassLoader);
         }
      }
   }

   /**
    * @see jakarta.servlet.http.HttpSessionListener#sessionDestroyed(jakarta.servlet.http.HttpSessionEvent)
    */
   public final void sessionDestroyed(HttpSessionEvent event)
   {
      final ExoContainer oldContainer = ExoContainerContext.getCurrentContainerIfPresent();
      // Keep the old ClassLoader
      final ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
      ExoContainer container = null;
      boolean hasBeenSet = false;
      try
      {
         container = getContainer(event);
         if (container == null)
            return;
         if (!container.equals(oldContainer))
         {
            if (container instanceof PortalContainer)
            {
               PortalContainer.setInstance((PortalContainer)container);
            }
            ExoContainerContext.setCurrentContainer(container);
            hasBeenSet = true;
         }
         if (requirePortalEnvironment())
         {
            final String ctxName = ContainerUtil.getServletContextName(event.getSession().getServletContext());
            if (!PortalContainer.isPortalContainerNameDisabled(ctxName) && container instanceof PortalContainer)
            {
               if (PortalContainer.getInstanceIfPresent() == null)
               {
                  // The portal container has not been set
                  PortalContainer.setInstance((PortalContainer)container);
                  hasBeenSet = true;
               }
               // Set the full classloader of the portal container
               Thread.currentThread().setContextClassLoader(((PortalContainer)container).getPortalClassLoader());
            }
            else
            {
               if (PropertyManager.isDevelopping())
               {
                  LOG.info("The portal environment could not be set for the webapp '" + ctxName
                     + "' because this servlet context has not been defined as a "
                     + "dependency of any portal container or it is a disabled portal"
                     + " container, the sessionDestroyed event will be ignored");
               }
               return;
            }
         }
         onSessionDestroyed(container, event);
      }
      finally
      {
         if (hasBeenSet)
         {
            if (container instanceof PortalContainer)
            {
               // Remove the current Portal Container and the current ExoContainer
               PortalContainer.setInstance(null);
            }
            // Re-set the old container
            ExoContainerContext.setCurrentContainer(oldContainer);
         }
         if (requirePortalEnvironment())
         {
            // Re-set the old classloader
            Thread.currentThread().setContextClassLoader(currentClassLoader);
         }
      }
   }

   /**
    * Indicates if it requires that a full portal environment must be set
    * @return <code>true</code> if it requires the portal environment <code>false</code> otherwise.
    */
   protected abstract boolean requirePortalEnvironment();

   /**
    * Allow sub-classes to execute an action when a session is created
    * @param container the eXo container
    * @param event the {@link HttpSessionEvent}
    */
   protected abstract void onSessionCreated(ExoContainer container, HttpSessionEvent event);

   /**
    * Allow sub-classes to execute an action when a session is destroyed
    * @param container the eXo container
    * @param event the {@link HttpSessionEvent}
    */
   protected abstract void onSessionDestroyed(ExoContainer container, HttpSessionEvent event);

   /**
    * @return Gives the {@link ExoContainer} that fits best with the current context
    */
   protected final ExoContainer getContainer(HttpSessionEvent event)
   {
      ExoContainer container = ExoContainerContext.getCurrentContainerIfPresent();
      if (container instanceof RootContainer)
      {
         // The top container is a RootContainer, thus we assume that we are in a portal mode
         container = PortalContainer.getCurrentInstance(event.getSession().getServletContext());
         if (container == null)
         {
            container = ExoContainerContext.getTopContainer();
         }
      }
      // The container is a PortalContainer or a StandaloneContainer
      return container;
   }
}
