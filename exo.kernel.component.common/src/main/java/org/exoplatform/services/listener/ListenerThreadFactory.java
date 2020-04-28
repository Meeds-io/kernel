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
package org.exoplatform.services.listener;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This ThreadFactory implementation is a copy of Executors$DefaultThreadFactory.
 * Its copied for single purpose - make readable thread name. 
 * 
 * Created by The eXo Platform SAS.
 * 
 * <br>Date:
 *
 * @author <a href="karpenko.sergiy@gmail.com">Karpenko Sergiy</a> 
 * @version $Id: ListenerThreadFactory.java 111 2008-11-11 11:11:11Z serg $
 */
public class ListenerThreadFactory implements ThreadFactory
{
   static final AtomicInteger poolNumber = new AtomicInteger(1);

   final ThreadGroup group;

   final AtomicInteger threadNumber = new AtomicInteger(1);

   final String namePrefix;

   ListenerThreadFactory()
   {
      SecurityManager s = System.getSecurityManager();
      group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
      namePrefix = "asynch-event-" + poolNumber.getAndIncrement() + "-thread-";
   }

   /**
    * {@inheritDoc}
    */
   public Thread newThread(Runnable r)
   {
      Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
      if (t.isDaemon())
         t.setDaemon(false);
      if (t.getPriority() != Thread.NORM_PRIORITY)
         t.setPriority(Thread.NORM_PRIORITY);
      return t;
   }
}
