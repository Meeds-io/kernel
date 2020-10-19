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
package org.exoplatform.container.component;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>This component contains all the variables of type {@link ThreadLocal} hold by a {@link ThreadContextHolder}
 * This component has not been designed to be thread safe so ensure that you create
 * a new instance each time you use it. Moreover for security reason, when the 
 * {@link SecurityManager} is installed we check the {@link RuntimePermission}
 * <i>manageThreadLocal</i> only within the constructor of a ThreadContext.</p>
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public class ThreadContext
{
   private final ThreadLocal<Object>[] threadLocals;
   
   private Object[] values;
   private Object[] oldValues;
   
   /**
    * Default constructor
    * @param threadLocals the thread local variables whose value 
    * must be transfered from one thread to another to avoid critical issues.
    * 
    * @throws SecurityException In case the
    * security manager is installed and the calling stack doesn't have
    * the {@link RuntimePermission} <i>manageThreadLocal</i>
    */
   @SuppressWarnings("unchecked")
   public ThreadContext(ThreadLocal<?>... threadLocals)
   {
      SecurityManager security = System.getSecurityManager();
      if (security != null)
         security.checkPermission(ThreadContextHolder.MANAGE_THREAD_LOCAL);
      this.threadLocals = (ThreadLocal<Object>[])threadLocals;
   }

   /**
    * This constructor can be used to group several {@link ThreadContext} into one.
    * @param contexts the list of all the {@link ThreadContext} to merge
    */
   @SuppressWarnings("unchecked")
   private ThreadContext(List<ThreadContext> contexts)
   {
      SecurityManager security = System.getSecurityManager();
      if (security != null)
         security.checkPermission(ThreadContextHolder.MANAGE_THREAD_LOCAL);
      if (contexts == null)
      {
         this.threadLocals = null;
         return;
      }
      List<ThreadLocal<Object>> lThreadLocals = new ArrayList<ThreadLocal<Object>>();
      for (int i = 0, length = contexts.size(); i < length; i++)
      {
         ThreadContext ctx = contexts.get(i);
         if (ctx == null || ctx.threadLocals == null || ctx.threadLocals.length == 0)
            continue;
         for (int j = 0; j < ctx.threadLocals.length; j++)
         {
            ThreadLocal<Object> tl = ctx.threadLocals[j];
            if (tl == null)
               continue;
            lThreadLocals.add(tl);
         }
      }
      this.threadLocals = (ThreadLocal<Object>[])lThreadLocals.toArray(new ThreadLocal<?>[lThreadLocals.size()]);
   }

   /**
    * Merges all the provided {@link ThreadContext} into one
    * @param contexts the {@link ThreadContext} to merge
    * @return a {@link ThreadContext} representing all the provided {@link ThreadContext} instance
    */
   public static ThreadContext merge(List<ThreadContext> contexts)
   {
      return new ThreadContext(contexts);
   }

   /**
    * Stores into memory the current values of all the Thread Local variables
    */
   public void store()
   {
      this.values = new Object[threadLocals == null ? 0 : threadLocals.length];
      for (int i = 0; i < values.length; i++)
      {
         ThreadLocal<Object> tl = threadLocals[i];
         if (tl == null)
         {
            continue;
         }
         values[i] = tl.get();
      }
   }
   
   /**
    * Pushes values stored into memory into all the Thread Local variables
    * and keeps previous values to be able to restore old state
    */
   public void push()
   {
      if (values == null)
      {
         throw new IllegalStateException("No values have been set, the store method has not been called or failed");
      }
      this.oldValues = new Object[values.length];
      for (int i = 0; i < values.length; i++)
      {
         ThreadLocal<Object> tl = threadLocals[i];
         if (tl == null)
         {
            continue;
         }         
         oldValues[i] = tl.get();
         tl.set(values[i]);
      }      
      this.values = null;
   }
   
   /**
    * Restores the old values of all the Thread Local variables
    */
   public void restore()
   {
      if (oldValues == null)
      {
         throw new IllegalStateException("No old values have been set, the push method has not been called or failed");
      }
      for (int i = 0; i < oldValues.length; i++)
      {
         ThreadLocal<Object> tl = threadLocals[i];
         if (tl == null)
         {
            continue;
         }         
         tl.set(oldValues[i]);
      }
      this.oldValues = null;
   }
}
