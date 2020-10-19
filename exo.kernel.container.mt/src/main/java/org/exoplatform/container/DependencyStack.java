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
package org.exoplatform.container;

import java.util.LinkedList;

/**
 * This class is used to be able to manage properly use cases where the constructor or the methods used
 * to add and create plugin call {{getComponentInstanceOfType}} instead of properly adding it in the constructor
 * directly
 * 
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public class DependencyStack extends LinkedList<Dependency>
{
   /**
    * The serial version UID
    */
   private static final long serialVersionUID = -3748924423444424832L;

   /**
    * The task that is currently launched
    */
   private final ComponentTask<?> task;

   public DependencyStack(ComponentTask<?> task)
   {
      this.task = task;
   }

   @Override
   public boolean add(Dependency dep)
   {
      if (isEmpty())
         task.getCaller().callDependency(task, dep);
      return super.add(dep);
   }
}
