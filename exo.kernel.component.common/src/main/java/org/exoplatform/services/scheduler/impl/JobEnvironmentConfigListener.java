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
package org.exoplatform.services.scheduler.impl;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.RootContainer;
import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.container.component.ComponentRequestLifecycle;
import org.exoplatform.container.component.RequestLifeCycle;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;

import java.util.List;

/**
 * Created by The eXo Platform SAS Author : Hoa Pham hoapham@exoplatform.com Oct
 * 5, 2005
 */
@SuppressWarnings("unchecked")
public class JobEnvironmentConfigListener implements JobListener, ComponentPlugin
{
   static final String NAME = "JobContextConfigListener";

   public void jobToBeExecuted(JobExecutionContext context)
   {
      String containerName = extractContainerName(context);
      ExoContainer container = null;
      if (containerName != null)
      {
         if (containerName.equals(JobSchedulerServiceImpl.STANDALONE_CONTAINER_NAME))
         {
            container = ExoContainerContext.getTopContainer();
         }
         else
         {
            RootContainer rootContainer = RootContainer.getInstance();
            container = (ExoContainer)rootContainer.getComponentInstance(containerName);
         }
      }
      if (container != null)
      {
         ExoContainerContext.setCurrentContainer(container);
         RequestLifeCycle.begin(container);
      }
   }

   public void jobExecutionVetoed(JobExecutionContext context)
   {

   }

   public void jobWasExecuted(JobExecutionContext context, JobExecutionException exception)
   {
      String containerName = extractContainerName(context);
      ExoContainer container = null;
      if (containerName != null)
      {
         if (containerName.equals(JobSchedulerServiceImpl.STANDALONE_CONTAINER_NAME))
         {
            container = ExoContainerContext.getTopContainer();
         }
         else
         {
            RootContainer rootContainer = RootContainer.getInstance();
            container = (ExoContainer)rootContainer.getComponentInstance(containerName);
         }
      }
      if (container != null)
      {
         List<ComponentRequestLifecycle> components =
            container.getComponentInstancesOfType(ComponentRequestLifecycle.class);
         for (ComponentRequestLifecycle component : components)
         {
            component.endRequest(container);
         }
         RequestLifeCycle.end();
         ExoContainerContext.setCurrentContainer(null);
      }
   }

   public String getName()
   {
      return NAME;
   }

   public void setName(String s)
   {
   }

   public String getDescription()
   {
      return "This JobListener is used to setup the eXo environment properly";
   }

   public void setDescription(String s)
   {
   }

   /**
    * Extracts the container name from the {@link JobExecutionContext}.
    * The container name is extracted from the group name
    */
   private static String extractContainerName(JobExecutionContext context)
   {
      String group = context.getJobDetail().getKey().getGroup();
      if (group == null || (group = group.trim()).length() == 0)
      {
         return null;
      }
      int index = group.indexOf(':');
      return index == -1 ? group : group.substring(0, index);
   }
}
