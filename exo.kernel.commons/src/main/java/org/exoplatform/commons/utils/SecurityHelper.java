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
package org.exoplatform.commons.utils;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.security.AccessController;
import java.security.Permission;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.sql.SQLException;

import javax.naming.NamingException;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Helps running code in privileged 
 * 
 * @author <a href="mailto:nikolazius@gmail.com">Nikolay Zamosenchuk</a>
 * @version $Id: SecurityHelper.java 34360 2009-07-22 23:58:59Z nzamosenchuk $
 *
 */
public class SecurityHelper
{

   /**
    * Launches action in privileged mode. Can throw only IO exception.
    * 
    * @param <E>
    * @param action
    * @return
    * @throws IOException
    */
   public static <E> E doPrivilegedIOExceptionAction(PrivilegedExceptionAction<E> action) throws IOException
   {
      try
      {
         return doPrivilegedExceptionAction(action);
      }
      catch (PrivilegedActionException pae)
      {
         Throwable cause = pae.getCause();
         if (cause instanceof IOException)
         {
            throw (IOException)cause;
         }
         else if (cause instanceof RuntimeException)
         {
            throw (RuntimeException)cause;
         }
         else
         {
            throw new RuntimeException(cause);
         }
      }
   }

   /**
    * Launches action in privileged mode. Can throw only NamingException.
    * 
    * @param <E>
    * @param action
    * @return
    * @throws NamingException
    */
   public static <E> E doPrivilegedNamingExceptionAction(PrivilegedExceptionAction<E> action) throws NamingException
   {
      try
      {
         return doPrivilegedExceptionAction(action);
      }
      catch (PrivilegedActionException pae)
      {
         Throwable cause = pae.getCause();
         if (cause instanceof NamingException)
         {
            throw (NamingException)cause;
         }
         else if (cause instanceof RuntimeException)
         {
            throw (RuntimeException)cause;
         }
         else
         {
            throw new RuntimeException(cause);
         }
      }
   }

   /**
    * Launches action in privileged mode. Can throw only SQL exception.
    * 
    * @param <E>
    * @param action
    * @return
    * @throws SQLException
    */
   public static <E> E doPrivilegedSQLExceptionAction(PrivilegedExceptionAction<E> action) throws SQLException
   {
      try
      {
         return doPrivilegedExceptionAction(action);
      }
      catch (PrivilegedActionException pae)
      {
         Throwable cause = pae.getCause();
         if (cause instanceof SQLException)
         {
            throw (SQLException)cause;
         }
         else if (cause instanceof RuntimeException)
         {
            throw (RuntimeException)cause;
         }
         else
         {
            throw new RuntimeException(cause);
         }
      }
   }

   /**
    * Launches action in privileged mode. Can throw only ParserConfigurationException, SAXException.
    * 
    * @param <E>
    * @param action
    * @return
    * @throws ParserConfigurationException
    * @throws SAXException
    */
   public static <E> E doPrivilegedParserConfigurationOrSAXExceptionAction(PrivilegedExceptionAction<E> action)
      throws ParserConfigurationException, SAXException
   {
      try
      {
         return doPrivilegedExceptionAction(action);
      }
      catch (PrivilegedActionException pae)
      {
         Throwable cause = pae.getCause();
         if (cause instanceof ParserConfigurationException)
         {
            throw (ParserConfigurationException)cause;
         }
         else if (cause instanceof SAXException)
         {
            throw (SAXException)cause;
         }
         else if (cause instanceof RuntimeException)
         {
            throw (RuntimeException)cause;
         }
         else
         {
            throw new RuntimeException(cause);
         }
      }
   }

   /**
    * Launches action in privileged mode. Can throw only ParserConfigurationException.
    * 
    * @param <E>
    * @param action
    * @return
    * @throws ParserConfigurationException
    */
   public static <E> E doPrivilegedParserConfigurationAction(PrivilegedExceptionAction<E> action)
      throws ParserConfigurationException
   {
      try
      {
         return doPrivilegedExceptionAction(action);
      }
      catch (PrivilegedActionException pae)
      {
         Throwable cause = pae.getCause();
         if (cause instanceof ParserConfigurationException)
         {
            throw (ParserConfigurationException)cause;
         }
         else if (cause instanceof RuntimeException)
         {
            throw (RuntimeException)cause;
         }
         else
         {
            throw new RuntimeException(cause);
         }
      }
   }

   /**
    * Launches action in privileged mode. Can throw only SAXException.
    * 
    * @param <E>
    * @param action
    * @return
    * @throws SAXException
    */
   public static <E> E doPrivilegedSAXExceptionAction(PrivilegedExceptionAction<E> action) throws SAXException
   {
      try
      {
         return doPrivilegedExceptionAction(action);
      }
      catch (PrivilegedActionException pae)
      {
         Throwable cause = pae.getCause();
         if (cause instanceof SAXException)
         {
            throw (SAXException)cause;
         }
         else if (cause instanceof RuntimeException)
         {
            throw (RuntimeException)cause;
         }
         else
         {
            throw new RuntimeException(cause);
         }
      }
   }

   /**
    * Launches action in privileged mode. Can throw only SAXException.
    * 
    * @param <E>
    * @param action
    * @return
    * @throws MalformedURLException
    */
   public static <E> E doPrivilegedMalformedURLExceptionAction(PrivilegedExceptionAction<E> action)
      throws MalformedURLException
   {
      try
      {
         return doPrivilegedExceptionAction(action);
      }
      catch (PrivilegedActionException pae)
      {
         Throwable cause = pae.getCause();
         if (cause instanceof MalformedURLException)
         {
            throw (MalformedURLException)cause;
         }
         else if (cause instanceof RuntimeException)
         {
            throw (RuntimeException)cause;
         }
         else
         {
            throw new RuntimeException(cause);
         }
      }
   }

   /**
    * Launches action in privileged mode. Can throw only runtime exceptions.
    * 
    * @param <E>
    * @param action
    * @return
    */
   public static <E> E doPrivilegedAction(PrivilegedAction<E> action)
   {
      if (System.getSecurityManager() != null)
      {
         // A security manager has been established
         return AccessController.doPrivileged(action);
      }
      return action.run();
   }

   /**
    * Launches action in privileged mode. 
    * 
    * @param <E>
    * @param action
    * @return
    */
   public static <E> E doPrivilegedExceptionAction(PrivilegedExceptionAction<E> action)
      throws PrivilegedActionException
   {
      if (System.getSecurityManager() != null)
      {
         // A security manager has been established
         return AccessController.doPrivileged(action);
      }
      try
      {
         return action.run();
      }
      catch (RuntimeException e)
      {
         throw e;
      }
      catch (Exception e)
      {
         throw new PrivilegedActionException(e);
      }
   }

   /**
    * Validate permissions.
    */
   public static void validateSecurityPermission(Permission permission)
   {
      SecurityManager security = System.getSecurityManager();
      if (security != null)
      {
            security.checkPermission(permission);
      }
   }
}
