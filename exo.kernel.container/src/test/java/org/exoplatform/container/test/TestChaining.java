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
package org.exoplatform.container.test;

import junit.framework.TestCase;

import org.exoplatform.container.component.ExecutionContext;
import org.exoplatform.container.component.ExecutionUnit;

/**
 * Created by the Exo Development team. Author : Mestrallet Benjamin
 * benjamin.mestrallet@exoplatform.com
 */
public class TestChaining extends TestCase
{
   public void testChain() throws Throwable
   {
      TextExecutionUnit chain = new TextExecutionUnit("unit-1");
      chain.addExecutionUnit(new TextExecutionUnit("unit-2"));
      chain.addExecutionUnit(new TextExecutionUnit("unit-3"));
      TextExcutionContext context = new TextExcutionContext("context");
      context.setCurrentExecutionUnit(chain);
      context.execute();
   }

   static class TextExecutionUnit extends ExecutionUnit
   {
      String name_;

      public TextExecutionUnit(String name)
      {
         name_ = name;
      }

      public Object execute(ExecutionContext context) throws Throwable
      {
         return context.executeNextUnit();
      }
   }

   static class TextExcutionContext extends ExecutionContext
   {
      String name_;

      public TextExcutionContext(String name)
      {
         name_ = name;
      }
   }
}
