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
package org.exoplatform.container;

import org.exoplatform.container.util.Utils;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.ServletRegistration.Dynamic;
import javax.servlet.SessionCookieConfig;
import javax.servlet.SessionTrackingMode;
import javax.servlet.descriptor.JspConfigDescriptor;

/**
 * This class is used to merge all the {@link ServletContext} related to a given portal container.
 * It will used the {@link WebAppInitContext} that have been defined in the related portal container.
 * It will always consider that the {@link WebAppInitContext}
 * with the highest priority has always right, in other words for example in the method
 * getInitParameter, it will try to get the init parameter in the {@link WebAppInitContext} 
 * of the highest priority, if it cans not find it, it will try the {@link WebAppInitContext}
 *  with the second highest priority and so on. The priority of the {@link WebAppInitContext} is
 *  the order given by the method PortalContainer.getWebAppInitContexts(), 
 * the last {@link WebAppInitContext} is the one with the highest priority.
 * 
 * Created by The eXo Platform SAS
 * Author : Nicolas Filotto
 *          nicolas.filotto@exoplatform.com
 * 14 sept. 2009  
 */
class PortalContainerContext implements ServletContext {

   /**
    * The weak reference to the related portal container
    */
   private volatile WeakReference<PortalContainer> containerRef;

   /**
    * The name of the related portal container used in case of developing mode
    */
   private final String portalContainerName;

   PortalContainerContext(PortalContainer container)
   {
      // In case of developing mode we want to avoid to use hard reference in case 
      // we would like to reload the container
      this.containerRef = new WeakReference<PortalContainer>(container);
      this.portalContainerName = container.getName();
   }

   private WebAppInitContext[] getWebAppInitContexts()
   {
      final Set<WebAppInitContext> contexts = getPortalContainer().getWebAppInitContexts();
      final WebAppInitContext[] aContexts = new WebAppInitContext[contexts.size()];
      return (WebAppInitContext[])contexts.toArray(aContexts);
   }
   
   private PortalContainer getPortalContainer()
   {
      PortalContainer container = containerRef.get();
      if (container != null)
      {
         return container;
      }
      container = RootContainer.getInstance().getPortalContainer(portalContainerName);
      containerRef = new WeakReference<PortalContainer>(container);
      return container;
   }
   
   private ServletContext getPortalContext()
   {
      return getPortalContainer().portalContext;
   }

   /**
    * {@inheritDoc}
    */
   public Object getAttribute(String name)
   {
      return getPortalContext().getAttribute(name);
   }

   /**
    * {@inheritDoc}
    */
   public Enumeration<String> getAttributeNames()
   {
      return getPortalContext().getAttributeNames();
   }

   /**
    * {@inheritDoc}
    */
   public ServletContext getContext(String uripath)
   {
      return getPortalContext().getContext(uripath);
   }

   /**
    * {@inheritDoc}
    */
   public String getInitParameter(String name)
   {
      final WebAppInitContext[] contexts = getWebAppInitContexts();
      for (int i = contexts.length - 1; i >= 0; i--)
      {
         final ServletContext context = contexts[i].getServletContext();
         String param = context.getInitParameter(name);
         if (param != null)
         {
            return param;
         }
      }
      return null;
   }

   /**
    * {@inheritDoc}
    */
   public Enumeration<String> getInitParameterNames()
   {
      final Set<WebAppInitContext> contexts = getPortalContainer().getWebAppInitContexts();
      Set<String> names = null;
      for (WebAppInitContext context : contexts)
      {
         Enumeration<String> eNames = context.getServletContext().getAttributeNames();
         if (eNames != null)
         {
            if (names == null)
            {
               names = new HashSet<String>();
            }
            names.addAll(Collections.list(eNames));
         }
      }
      if (names == null)
      {
         return null;
      }
      return Collections.enumeration(names);
   }

   /**
    * {@inheritDoc}
    */
   public int getMajorVersion()
   {
      return getPortalContext().getMajorVersion();
   }

   /**
    * {@inheritDoc}
    */
   public String getMimeType(String file)
   {
      final WebAppInitContext[] contexts = getWebAppInitContexts();
      for (int i = contexts.length - 1; i >= 0; i--)
      {
         final ServletContext context = contexts[i].getServletContext();
         String mimeType = context.getMimeType(file);
         if (mimeType != null)
         {
            return mimeType;
         }
      }
      return null;
   }

   /**
    * {@inheritDoc}
    */
   public int getMinorVersion()
   {
      return getPortalContext().getMinorVersion();
   }

   /**
    * {@inheritDoc}
    */
   public RequestDispatcher getNamedDispatcher(String name)
   {
      return getPortalContext().getNamedDispatcher(name);
   }

   /**
    * {@inheritDoc}
    */
   public String getRealPath(String path)
   {
      final WebAppInitContext[] contexts = getWebAppInitContexts();
      for (int i = contexts.length - 1; i >= 0; i--)
      {
         final ServletContext context = contexts[i].getServletContext();
         final InputStream is = context.getResourceAsStream(path);
         if (is != null)
         {
            // The resource exists within this servlet context
            return context.getRealPath(path);
         }
      }
      return null;
   }

   /**
    * {@inheritDoc}
    */
   public RequestDispatcher getRequestDispatcher(String path)
   {
      final WebAppInitContext[] contexts = getWebAppInitContexts();
      for (int i = contexts.length - 1; i >= 0; i--)
      {
         final ServletContext context = contexts[i].getServletContext();
         final InputStream is = context.getResourceAsStream(Utils.getPathOnly(path));
         if (is != null)
         {
            // The resource exists within this servlet context
            return context.getRequestDispatcher(path);
         }
      }
      return null;
   }

   /**
    * {@inheritDoc}
    */
   public URL getResource(String path) throws MalformedURLException
   {
      final WebAppInitContext[] contexts = getWebAppInitContexts();
      for (int i = contexts.length - 1; i >= 0; i--)
      {
         final ServletContext context = contexts[i].getServletContext();
         final URL url = context.getResource(path);
         if (url != null)
         {
            return url;
         }
      }
      return null;
   }

   /**
    * {@inheritDoc}
    */
   public InputStream getResourceAsStream(String path)
   {
      final WebAppInitContext[] contexts = getWebAppInitContexts();
      for (int i = contexts.length - 1; i >= 0; i--)
      {
         final ServletContext context = contexts[i].getServletContext();
         final InputStream is = context.getResourceAsStream(path);
         if (is != null)
         {
            return is;
         }
      }
      return null;
   }

   /**
    * {@inheritDoc}
    */
   public Set<String> getResourcePaths(String path)
   {
      final Set<WebAppInitContext> contexts = getPortalContainer().getWebAppInitContexts();
      Set<String> paths = null;
      for (WebAppInitContext context : contexts)
      {
         Set<String> sPaths = context.getServletContext().getResourcePaths(path);
         if (sPaths != null)
         {
            if (paths == null)
            {
               paths = new LinkedHashSet<String>();
            }
            paths.addAll(sPaths);
         }
      }
      return paths;
   }

   /**
    * {@inheritDoc}
    */
   public String getServerInfo()
   {
      return getPortalContext().getServerInfo();
   }

   /**
    * {@inheritDoc}
    */
   @SuppressWarnings("deprecation")
   public Servlet getServlet(String name) throws ServletException
   {
      return getPortalContext().getServlet(name);
   }

   /**
    * {@inheritDoc}
    */
   public String getServletContextName()
   {
      return getPortalContext().getServletContextName();
   }

   /**
    * {@inheritDoc}
    */
   @SuppressWarnings("deprecation")
   public Enumeration<String> getServletNames()
   {
      return getPortalContext().getServletNames();
   }

   /**
    * {@inheritDoc}
    */
   @SuppressWarnings("deprecation")
   public Enumeration<Servlet> getServlets()
   {
      return getPortalContext().getServlets();
   }

   /**
    * {@inheritDoc}
    */
   public void log(String message)
   {
      getPortalContext().log(message);
   }

   /**
    * {@inheritDoc}
    */
   @SuppressWarnings("deprecation")
   public void log(Exception exception, String message)
   {
      getPortalContext().log(exception, message);
   }

   /**
    * {@inheritDoc}
    */
   public void log(String message, Throwable throwable)
   {
      getPortalContext().log(message, throwable);
   }

   /**
    * {@inheritDoc}
    */
   public void removeAttribute(String name)
   {
      getPortalContext().removeAttribute(name);
   }

   /**
    * {@inheritDoc}
    */
   public void setAttribute(String name, Object object)
   {
      getPortalContext().setAttribute(name, object);
   }

   /**
    * {@inheritDoc}
    */
   public String getContextPath()
   {
      return getPortalContext().getContextPath();
   }

   // servlet 3.0. API

   /**
    * {@inheritDoc}
    */
   public int getEffectiveMajorVersion()
   {
      return getPortalContext().getEffectiveMajorVersion();
   }

   /**
    * {@inheritDoc}
    */
   public int getEffectiveMinorVersion()
   {
      return getPortalContext().getEffectiveMinorVersion();
   }

   /**
    * {@inheritDoc}
    */
   public boolean setInitParameter(String name, String value)
   {
      return getPortalContext().setInitParameter(name, value);
   }

   /**
    * {@inheritDoc}
    */
   public Dynamic addServlet(String servletName, String className)
   {
      return getPortalContext().addServlet(servletName, className);
   }

   /**
    * {@inheritDoc}
    */
   public Dynamic addServlet(String servletName, Servlet servlet)
   {
      return getPortalContext().addServlet(servletName, servlet);
   }

   /**
    * {@inheritDoc}
    */
   public Dynamic addServlet(String servletName, Class<? extends Servlet> servletClass)
   {
      return getPortalContext().addServlet(servletName, servletClass);
   }

   /**
    * {@inheritDoc}
    */
   public <T extends Servlet> T createServlet(Class<T> clazz) throws ServletException
   {
      return getPortalContext().createServlet(clazz);
   }

   /**
    * {@inheritDoc}
    */
   public ServletRegistration getServletRegistration(String servletName)
   {
      return getPortalContext().getServletRegistration(servletName);
   }

   /**
    * {@inheritDoc}
    */
   public Map<String, ? extends ServletRegistration> getServletRegistrations()
   {
      return getPortalContext().getServletRegistrations();
   }

   /**
    * {@inheritDoc}
    */
   public javax.servlet.FilterRegistration.Dynamic addFilter(String filterName, String className)
   {
      return getPortalContext().addFilter(filterName, className);
   }

   /**
    * {@inheritDoc}
    */
   public javax.servlet.FilterRegistration.Dynamic addFilter(String filterName, Filter filter)
   {
      return getPortalContext().addFilter(filterName, filter);
   }

   /**
    * {@inheritDoc}
    */
   public javax.servlet.FilterRegistration.Dynamic addFilter(String filterName, Class<? extends Filter> filterClass)
   {
      return getPortalContext().addFilter(filterName, filterClass);
   }

   /**
    * {@inheritDoc}
    */
   public <T extends Filter> T createFilter(Class<T> clazz) throws ServletException
   {
      return getPortalContext().createFilter(clazz);
   }

   /**
    * {@inheritDoc}
    */
   public FilterRegistration getFilterRegistration(String filterName)
   {
      return getPortalContext().getFilterRegistration(filterName);
   }

   /**
    * {@inheritDoc}
    */
   public Map<String, ? extends FilterRegistration> getFilterRegistrations()
   {
      return getPortalContext().getFilterRegistrations();
   }

   /**
    * {@inheritDoc}
    */
   public SessionCookieConfig getSessionCookieConfig()
   {
      return getPortalContext().getSessionCookieConfig();
   }

   /**
    * {@inheritDoc}
    */
   public void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes)
   {
      getPortalContext().setSessionTrackingModes(sessionTrackingModes);
   }

   /**
    * {@inheritDoc}
    */
   public Set<SessionTrackingMode> getDefaultSessionTrackingModes()
   {
      return getPortalContext().getDefaultSessionTrackingModes();
   }

   /**
    * {@inheritDoc}
    */
   public Set<SessionTrackingMode> getEffectiveSessionTrackingModes()
   {
      return getPortalContext().getEffectiveSessionTrackingModes();
   }

   /**
    * {@inheritDoc}
    */
   public void addListener(String className)
   {
      getPortalContext().addListener(className);
   }

   /**
    * {@inheritDoc}
    */
   public <T extends EventListener> void addListener(T t)
   {
      getPortalContext().addListener(t);
   }

   /**
    * {@inheritDoc}
    */
   public void addListener(Class<? extends EventListener> listenerClass)
   {
      getPortalContext().addListener(listenerClass);
   }

   /**
    * {@inheritDoc}
    */
   public <T extends EventListener> T createListener(Class<T> clazz) throws ServletException
   {
      return getPortalContext().createListener(clazz);
   }

   /**
    * {@inheritDoc}
    */
   public JspConfigDescriptor getJspConfigDescriptor()
   {
      return getPortalContext().getJspConfigDescriptor();
   }

   /**
    * {@inheritDoc}
    */
   public ClassLoader getClassLoader()
   {
      return getPortalContainer().getPortalClassLoader();
   }

   /**
    * {@inheritDoc}
    */
   public void declareRoles(String... roleNames)
   {
      getPortalContext().declareRoles(roleNames);
   }

   /**
    * {@inheritDoc}
    */
   public String getVirtualServerName() {
     return getPortalContext().getVirtualServerName();
   }

  @Override
  public Dynamic addJspFile(String servletName, String jspFile) {
    return getPortalContext().addJspFile(servletName, jspFile);
  }

  @Override
  public int getSessionTimeout() {
    return getPortalContext().getSessionTimeout();
  }

  @Override
  public void setSessionTimeout(int sessionTimeout) {
    getPortalContext().setSessionTimeout(sessionTimeout);
  }

  @Override
  public String getRequestCharacterEncoding() {
    return getPortalContext().getRequestCharacterEncoding();
  }

  @Override
  public void setRequestCharacterEncoding(String encoding) {
    getPortalContext().setRequestCharacterEncoding(encoding);
  }

  @Override
  public String getResponseCharacterEncoding() {
    return getPortalContext().getResponseCharacterEncoding();
  }

  @Override
  public void setResponseCharacterEncoding(String encoding) {
    getPortalContext().setResponseCharacterEncoding(encoding);
  }
}
