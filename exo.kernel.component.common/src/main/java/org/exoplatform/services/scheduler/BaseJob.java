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
package org.exoplatform.services.scheduler;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Created by The eXo Platform SAS Author : Hoa Pham hoapham@exoplatform.com Oct
 * 5, 2005
 */
abstract public class BaseJob implements Job
{

   public void execute(JobExecutionContext context) throws JobExecutionException
   {
      JobContext tcontext = new JobContext(context);
      try
      {
         execute(tcontext);
      }
      catch (Exception ex)
      {
         throw new JobExecutionException(ex);
      }
   }

   public void execute(JobContext context) throws Exception
   {
      throw new Exception("You  should overide this method");
   }
}
