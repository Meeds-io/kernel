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
package org.exoplatform.container.configuration;

import org.xml.sax.EntityResolver;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class Namespaces
{

   /** . */
   public static final String KERNEL_1_0_URI = "http://www.exoplatform.org/xml/ns/kernel_1_0.xsd";
   
   // We keep it for backward compatibility
   public static final String KERNEL_1_0_URI_OLD = "http://www.exoplaform.org/xml/ns/kernel_1_0.xsd";

   /** . */
   public static final String KERNEL_1_1_URI = "http://www.exoplatform.org/xml/ns/kernel_1_1.xsd";
   
   // We keep it for backward compatibility
   public static final String KERNEL_1_1_URI_OLD = "http://www.exoplaform.org/xml/ns/kernel_1_1.xsd";

   /** . */
   public static final String KERNEL_1_2_URI = "http://www.exoplatform.org/xml/ns/kernel_1_2.xsd";
   
   // We keep it for backward compatibility
   public static final String KERNEL_1_2_URI_OLD = "http://www.exoplaform.org/xml/ns/kernel_1_2.xsd";

   /** . */
   public static final String KERNEL_1_3_URI = "http://www.exoplatform.org/xml/ns/kernel_1_3.xsd";

   /**
    * All the namespaces related to the kernel
    */
   public static final Set<String> KERNEL_NAMESPACES_SET;

   static
   {
      Set<String> tmp = new LinkedHashSet<String>();
      tmp.add(KERNEL_1_0_URI);
      tmp.add(KERNEL_1_1_URI);
      tmp.add(KERNEL_1_2_URI);
      tmp.add(KERNEL_1_3_URI);
      // we add the namespaces badly spelled for backward compatibility
      tmp.add(KERNEL_1_0_URI_OLD);
      tmp.add(KERNEL_1_1_URI_OLD);
      tmp.add(KERNEL_1_2_URI_OLD);
      KERNEL_NAMESPACES_SET = Collections.unmodifiableSet(tmp);
   }

   /** . */
   static final EntityResolver resolver;

   static
   {
      Map<String, String> resourceMap = new HashMap<String, String>();
      resourceMap.put(KERNEL_1_0_URI, "org/exoplatform/container/configuration/kernel-configuration_1_0.xsd");
      resourceMap.put(KERNEL_1_1_URI, "org/exoplatform/container/configuration/kernel-configuration_1_1.xsd");
      resourceMap.put(KERNEL_1_2_URI, "org/exoplatform/container/configuration/kernel-configuration_1_2.xsd");
      resourceMap.put(KERNEL_1_3_URI, "org/exoplatform/container/configuration/kernel-configuration_1_3.xsd");
      // we add the namespaces badly spelled for backward compatibility
      resourceMap.put(KERNEL_1_0_URI_OLD, "org/exoplatform/container/configuration/kernel-configuration_1_0_OLD.xsd");
      resourceMap.put(KERNEL_1_1_URI_OLD, "org/exoplatform/container/configuration/kernel-configuration_1_1_OLD.xsd");
      resourceMap.put(KERNEL_1_2_URI_OLD, "org/exoplatform/container/configuration/kernel-configuration_1_2_OLD.xsd");
      resolver = new EntityResolverImpl(Namespaces.class.getClassLoader(), resourceMap);
   }

   /**
    * @return A safe copy of the list of kernel namespaces
    */
   public static String[] getKernelNamespaces()
   {
      return new String[]{KERNEL_1_0_URI, KERNEL_1_1_URI, KERNEL_1_2_URI, KERNEL_1_3_URI, KERNEL_1_0_URI_OLD,
         KERNEL_1_1_URI_OLD, KERNEL_1_2_URI_OLD};
   }
   
   /**
    * Indicates whether the given uri is a kernel namespace or not
    * @param uri the uri to check
    * @return <code>true</code> if it is a kernel namespace, <code>false</code> otherwise.
    */
   public static boolean isKernelNamespace(String uri)
   {
      return KERNEL_NAMESPACES_SET.contains(uri);
   }
}
