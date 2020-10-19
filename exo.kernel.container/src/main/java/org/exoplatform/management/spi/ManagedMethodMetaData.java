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
package org.exoplatform.management.spi;

import org.exoplatform.management.annotations.ImpactType;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Meta data that describes a managed method.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ManagedMethodMetaData extends ManagedMetaData
{

   /** . */
   private final Method method;

   /** . */
   private final Map<Integer, ManagedMethodParameterMetaData> parameters;

   /** . */
   private final ImpactType impact;

   /**
    * Build a new instance.
    *
    * @param method the method
    * @param impactType the access mode
    * @throws IllegalArgumentException if the method is null or the impact is null
    */
   public ManagedMethodMetaData(Method method, ImpactType impactType) throws IllegalArgumentException
   {
      if (method == null)
      {
         throw new IllegalArgumentException("The method cannot be null");
      }
      if (impactType == null)
      {
         throw new IllegalArgumentException("The impactType cannot be null");
      }

      //
      this.method = method;
      this.impact = impactType;
      this.parameters = new HashMap<Integer, ManagedMethodParameterMetaData>();
   }

   public String getName()
   {
      return method.getName();
   }

   public ImpactType getImpact()
   {
      return impact;
   }

   public Method getMethod()
   {
      return method;
   }

   public void addParameter(ManagedMethodParameterMetaData parameter)
   {
      if (parameter == null)
      {
         throw new IllegalArgumentException("No null parameter accepted");
      }
      parameters.put(parameter.getIndex(), parameter);
   }

   public Collection<ManagedMethodParameterMetaData> getParameters()
   {
      return Collections.unmodifiableCollection(parameters.values());
   }
}
