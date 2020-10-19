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
package org.exoplatform.services.scheduler.test;

import org.exoplatform.services.scheduler.BaseJob;
import org.exoplatform.services.scheduler.JobContext;

import java.util.Date;

/**
 * Created by The eXo Platform SAS Author : Hoa Pham
 * hoapham@exoplatform.com,phamvuxuanhoa@yahoo.com Oct 7, 2005
 */
public class AJob extends BaseJob
{
   static int counter_ = 0;

   static Date expectExecuteTime_;

   static int errorCounter_ = 0;

   static int repeatCounter_ = 0;

   static void reset()
   {
      counter_ = 0;
      expectExecuteTime_ = null;
      errorCounter_ = 0;
      repeatCounter_ = 0;
   }

   public void execute(JobContext context) throws Exception
   {
      counter_++;
      repeatCounter_++;
      if (expectExecuteTime_ != null)
      {
         if (expectExecuteTime_.getTime() + 500 < System.currentTimeMillis())
         {
            errorCounter_++;
         }
      }
   }
}
