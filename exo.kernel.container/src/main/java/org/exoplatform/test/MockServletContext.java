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
package org.exoplatform.test;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterRegistration;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRegistration;
import jakarta.servlet.ServletRegistration.Dynamic;
import jakarta.servlet.SessionCookieConfig;
import jakarta.servlet.SessionTrackingMode;
import jakarta.servlet.descriptor.JspConfigDescriptor;

/**
 * Created by The eXo Platform SARL . Author : Mestrallet Benjamin
 * benjmestrallet@users.sourceforge.net Date: Jul 25, 2003 Time: 12:26:58 AM
 */
public class MockServletContext implements ServletContext
{

   private static final Log LOG = ExoLogger.getLogger("org.exoplatform.test.MockServletContext");

   private String name_;

   private Map<String, String> initParams_;

   private Map<String, Object> attributes_;

   private String contextPath_;

   private StringBuffer logBuffer = new StringBuffer();

   public MockServletContext()
   {
      this("portlet_app_1");
   }

   public MockServletContext(String name)
   {
      this(name, "/" + name);
   }

   public MockServletContext(String name, String path)
   {
      this.name_ = name;
      this.contextPath_ = path;
      this.initParams_ = new HashMap<String, String>();
      this.attributes_ = new HashMap<String, Object>();
      this.attributes_.put("jakarta.servlet.context.tempdir", path);
   }

   public void setName(String name)
   {
      name_ = name;
   }

   public String getLogBuffer()
   {
      try
      {
         return logBuffer.toString();
      }
      finally
      {
         logBuffer = new StringBuffer();
      }
   }

   public ServletContext getContext(String s)
   {
      return null;
   }

   public int getMajorVersion()
   {
      return 3;
   }

   public int getMinorVersion()
   {
      return 0;
   }

   public String getMimeType(String s)
   {
      return "text/html";
   }

   public Set<String> getResourcePaths(String s)
   {
      Set<String> set = new HashSet<String>();
      set.add("/test1");
      set.add("/WEB-INF");
      set.add("/test2");
      return set;
   }

   public URL getResource(String s) throws MalformedURLException
   {
      String path = "file:" + contextPath_ + s;
      URL url = new URL(path);
      return url;
   }

   public InputStream getResourceAsStream(String s)
   {
      try
      {
         return getResource(s).openStream();
      }
      catch (IOException e)
      {
         LOG.error(e);
      }
      return null;
   }

   public RequestDispatcher getRequestDispatcher(String s)
   {
      return null;
   }

   public RequestDispatcher getNamedDispatcher(String s)
   {
      return null;
   }

   public Servlet getServlet(String s) throws ServletException
   {
      return null;
   }

   public Enumeration<Servlet> getServlets()
   {
      return null;
   }

   public Enumeration<String> getServletNames()
   {
      return null;
   }

   public void log(String s)
   {
      logBuffer.append(s);
   }

   public void log(Exception e, String s)
   {
      logBuffer.append(s + e.getMessage());
   }

   public void log(String s, Throwable throwable)
   {
      logBuffer.append(s + throwable.getMessage());
   }

   public void setContextPath(String s)
   {
      contextPath_ = s;
   }

   public String getRealPath(String s)
   {
      return contextPath_ + s;
   }

   public String getServerInfo()
   {
      return null;
   }

   public boolean setInitParameter(String name, String value)
   {
      initParams_.put(name, value);
      return true;
   }

   public String getInitParameter(String name)
   {
      return (String)initParams_.get(name);
   }

   public Enumeration<String> getInitParameterNames()
   {
      Vector<String> keys = new Vector<String>(initParams_.keySet());
      return keys.elements();
   }

   public Object getAttribute(String name)
   {
      return attributes_.get(name);
   }

   public Enumeration<String> getAttributeNames()
   {
      Vector<String> keys = new Vector<String>(attributes_.keySet());
      return keys.elements();
   }

   public void setAttribute(String name, Object value)
   {
      attributes_.put(name, value);
   }

   public void removeAttribute(String name)
   {
      attributes_.remove(name);
   }

   public String getServletContextName()
   {
      return name_;
   }

   public String getContextPath()
   {
      return contextPath_;
   }

   // servlet 3.0 API

   public int getEffectiveMajorVersion()
   {
      return 3;
   }

   public int getEffectiveMinorVersion()
   {
      return 0;
   }

   public jakarta.servlet.ServletRegistration.Dynamic addServlet(String servletName, String className)
   {
      return null;
   }

   public jakarta.servlet.ServletRegistration.Dynamic addServlet(String servletName, Servlet servlet)
   {
      return null;
   }

   public jakarta.servlet.ServletRegistration.Dynamic addServlet(String servletName, Class<? extends Servlet> servletClass)
   {
      return null;
   }

   public <T extends Servlet> T createServlet(Class<T> clazz) throws ServletException
   {
      return null;
   }

   public ServletRegistration getServletRegistration(String servletName)
   {
      return null;
   }

   public Map<String, ? extends ServletRegistration> getServletRegistrations()
   {
      return null;
   }

   public jakarta.servlet.FilterRegistration.Dynamic addFilter(String filterName, String className)
   {
      return null;
   }

   public jakarta.servlet.FilterRegistration.Dynamic addFilter(String filterName, Filter filter)
   {
      return null;
   }

   public jakarta.servlet.FilterRegistration.Dynamic addFilter(String filterName, Class<? extends Filter> filterClass)
   {
      return null;
   }

   public <T extends Filter> T createFilter(Class<T> clazz) throws ServletException
   {
      return null;
   }

   public FilterRegistration getFilterRegistration(String filterName)
   {
      return null;
   }

   public Map<String, ? extends FilterRegistration> getFilterRegistrations()
   {
      return null;
   }

   public SessionCookieConfig getSessionCookieConfig()
   {
      return null;
   }

   public void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes)
   {
   }

   public Set<SessionTrackingMode> getDefaultSessionTrackingModes()
   {
      return null;
   }

   public Set<SessionTrackingMode> getEffectiveSessionTrackingModes()
   {
      return null;
   }

   public void addListener(String className)
   {
   }

   public <T extends EventListener> void addListener(T t)
   {
   }

   public void addListener(Class<? extends EventListener> listenerClass)
   {
   }

   public <T extends EventListener> T createListener(Class<T> clazz) throws ServletException
   {
      return null;
   }

   public JspConfigDescriptor getJspConfigDescriptor()
   {
      return null;
   }

   public ClassLoader getClassLoader()
   {
      return null;
   }

   public void declareRoles(String... roleNames)
   {
   }

   public String getVirtualServerName() {
      return null;
   }

  @Override
  public Dynamic addJspFile(String servletName, String jspFile) {
    return null;
  }

  @Override
  public int getSessionTimeout() {
    return 0;
  }

  @Override
  public void setSessionTimeout(int sessionTimeout) {
    
  }

  @Override
  public String getRequestCharacterEncoding() {
    return null;
  }

  @Override
  public void setRequestCharacterEncoding(String encoding) {
    
  }

  @Override
  public String getResponseCharacterEncoding() {
    return null;
  }

  @Override
  public void setResponseCharacterEncoding(String encoding) {
    
  }
}
