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

/**
 * Created by The eXo Platform SAS Author : Tuan Nguyen
 * tuan08@users.sourceforge.net Sep 17, 2005
 */
public class ExecutionContext
{
   private ExecutionUnit currentUnit_;

   public void setCurrentExecutionUnit(ExecutionUnit unit)
   {
      currentUnit_ = unit;
   }

   public Object execute() throws Throwable
   {
      if (currentUnit_ != null)
         return currentUnit_.execute(this);
      return null;
   }

   public Object executeNextUnit() throws Throwable
   {
      currentUnit_ = currentUnit_.getNextUnit();
      if (currentUnit_ != null)
         return currentUnit_.execute(this);
      return null;
   }
}
