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

import java.io.IOException;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Created by The eXo Platform SAS
 * Author : Nicolas Filotto 
 *          nicolas.filotto@exoplatform.com
 * 29 sept. 2009  
 */
public abstract class AbstractHttpServlet extends HttpServlet
{
   /**
    * The logger
    */
   private static final Log LOG = ExoLogger.getLogger("exo.kernel.container.AbstractHttpServlet");

   /**
    * Serial Version ID.
    */
   private static final long serialVersionUID = -3302886470677004895L;

   /**
    * The filter configuration
    */
   protected ServletConfig config;

   /**
    * The Servlet context name
    */
   protected String servletContextName;

   /**
    * Indicates if we need a portal environment.
    */
   private volatile Boolean requirePortalEnvironment;

   /**
    * {@inheritDoc}
    */
   public final void init(ServletConfig config) throws ServletException
   {
      super.init(config);
      this.config = config;
      this.servletContextName = ContainerUtil.getServletContextName(config.getServletContext());
      afterInit(config);
   }

   /**
    * Allows sub-classes to initialize 
    * @param config the current servlet configuration
    */
   protected void afterInit(ServletConfig config) throws ServletException
   {
   }

   /**
    * @see jakarta.servlet.http.HttpServlet#service(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse)
    */
   public final void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
   {
      final ExoContainer oldContainer = ExoContainerContext.getCurrentContainer();
      // Keep the old ClassLoader
      final ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
      ExoContainer container = null;
      boolean hasBeenSet = false;
      try
      {
         container = getContainer();
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
            final String ctxName = ContainerUtil.getServletContextName(config.getServletContext());
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
               onPortalEnvironmentError(req, res);
               return;
            }
         }
         onService(container, req, res);
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
   protected boolean requirePortalEnvironment()
   {
      if (requirePortalEnvironment == null)
      {
         synchronized (this)
         {
            if (requirePortalEnvironment == null)
            {
               this.requirePortalEnvironment = PortalContainer.isPortalContainerName(servletContextName);
            }
         }
      }
      return requirePortalEnvironment.booleanValue();
   }

   /**
    * Allow the sub classes to execute a task when the method <code>service</code> is called 
    * @param container the eXo container
    * @param req the {@link HttpServletRequest}
    * @param res the {@link HttpServletResponse}
    */
   protected void onService(ExoContainer container, HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException
   {
      // Dispatches to the right HTTP method
      super.service(req, res);
   }

   /**
    * Allow the sub classed to execute a task when the portal environment could not be set
    * because no related portal container could be found
    * @param req the {@link HttpServletRequest}
    * @param res the {@link HttpServletResponse}
    */
   protected void onPortalEnvironmentError(HttpServletRequest req, HttpServletResponse res) throws ServletException,
      IOException
   {
      if (PropertyManager.isDevelopping())
      {
         LOG.info("The portal environment could not be set for the webapp '"
            + ContainerUtil.getServletContextName(config.getServletContext())
            + "' because this servlet context has not been defined as a "
            + "dependency of any portal container or it is a disabled portal"
            + " container, the target URI was " + req.getRequestURI());
      }
      res.sendError(HttpServletResponse.SC_NOT_FOUND);
   }

   /**
    * @return Gives the {@link ExoContainer} that fits best with the current context
    */
   protected final ExoContainer getContainer()
   {
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      if (container instanceof RootContainer)
      {
         // The top container is a RootContainer, thus we assume that we are in a portal mode
         container = PortalContainer.getCurrentInstance(config.getServletContext());
         if (container == null)
         {
            container = ExoContainerContext.getTopContainer();
         }
      }
      // The container is a PortalContainer or a StandaloneContainer
      return container;
   }

   /**
    * @return the current {@link ServletContext}
    */
   @Override
   public ServletContext getServletContext()
   {
      if (requirePortalEnvironment())
      {
         ExoContainer container = getContainer();
         if (container instanceof PortalContainer)
         {
            return ((PortalContainer)container).getPortalContext();
         }
      }
      return super.getServletContext();
   }
}
