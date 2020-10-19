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

import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * This enumeration defines all the possible mode supported by the kernel.
 * 
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public enum Mode {

   /**
    * Use this mode when you want to delegate several threads to the kernel to create, initialize and start components.
    */
   MULTI_THREADED,

   /**
    * Use this mode when you want to see the kernel automatically fixes dependency issues such as unexpected call to
    * getComponentInstanceOfType() and/or getComponentInstance()
    */
   AUTO_SOLVE_DEP_ISSUES,

   /**
    * Indicates whether or not the multi-threading should be disabled on startup complete.
    */
   DISABLE_MT_ON_STARTUP_COMPLETE;

   /**
    * The logger
    */
   private static final Log LOG = ExoLogger.getLogger("exo.kernel.container.mt.Mode");

   /**
    * The name of the system parameter to indicate that we want to enable the <i>multi-threaded</i> mode of the kernel
    */
   public static final String MULTI_THREADED_PARAM_NAME = "org.exoplatform.container.mt.enabled";

   /**
    * The name of the system parameter to indicate that we want to enable the <i>auto solve dependency issues</i> mode
    * of the kernel
    */
   public static final String AUTO_SOLVE_DEP_ISSUES_PARAM_NAME = "org.exoplatform.container.as.enabled";

   /**
    * The name of the system parameter to indicate that we want to disable the <i>multi-threaded</i> mode
    * once the {@link TopExoContainer} is fully started
    */
   public static final String DISABLE_MT_ON_STARTUP_COMPLETE_PARAM_NAME = "org.exoplatform.container.dmtosc.enabled";

   private static volatile Set<Mode> MODES;

   static void setModes(Mode... modes)
   {
      Set<Mode> sModes;
      if (modes == null || modes.length == 0)
      {
         sModes = Collections.emptySet();
      }
      else
      {
         sModes = new HashSet<Mode>(Arrays.asList(modes));
      }
      synchronized (Mode.class)
      {
         MODES = Collections.unmodifiableSet(sModes);
      }
   }

   static void clearModes()
   {
      // Clear to enforce reloading the default configuration
      synchronized (Mode.class)
      {
         MODES = null;
      }
   }

   /**
    * Indicates whether or not the given mode has been activated
    */
   static boolean hasMode(Mode mode)
   {
      return getModes().contains(mode);
   }

   /**
    * Removes the provided modes if they are all defined, does nothing otherwise
    * @param modes the modes to be removed
    * @return <code>true</code> if the modes have been removed, <code>false</code> otherwise.
    */
   static boolean removeModes(Mode... modes)
   {
      if (modes == null || modes.length == 0)
         return false;
      synchronized (Mode.class)
      {
         Set<Mode> modesSet = new HashSet<Mode>(getModes());
         for (Mode m : modes)
         {
            if (!modesSet.remove(m))
            {
               return false;
            }
         }
         MODES = Collections.unmodifiableSet(modesSet);
      }
      return true;
   }

   private static Set<Mode> getModes()
   {
      Set<Mode> modes = MODES;
      if (modes == null)
      {
         synchronized (Mode.class)
         {
            modes = MODES;
            if (modes == null)
            {
               Set<Mode> sModes = new HashSet<Mode>();
               String sValue = PropertyManager.getProperty(MULTI_THREADED_PARAM_NAME);
               if ((sValue == null || Boolean.valueOf(sValue)) && Runtime.getRuntime().availableProcessors() > 1)
               {
                  sModes.add(MULTI_THREADED);
                  if (LOG.isDebugEnabled())
                  {
                     LOG.debug("The 'multi-threaded' mode of the kernel has been enabled");
                  }
                  sValue = PropertyManager.getProperty(DISABLE_MT_ON_STARTUP_COMPLETE_PARAM_NAME);
                  if (sValue == null || Boolean.valueOf(sValue))
                  {
                     sModes.add(DISABLE_MT_ON_STARTUP_COMPLETE);
                     if (LOG.isDebugEnabled())
                     {
                        LOG.debug("The 'multi-threaded' mode of the kernel will be disabled once fully started");
                     }
                  }
                  else if (LOG.isDebugEnabled())
                  {
                     LOG.debug("The 'multi-threaded' mode of the kernel won't be disabled once fully started");
                  }
               }
               else if (LOG.isDebugEnabled())
               {
                  if (Runtime.getRuntime().availableProcessors() == 1)
                  {
                     LOG.debug("The 'multi-threaded' mode of the kernel is disabled since you must have more than one processor");
                  }
                  else
                  {
                     LOG.debug("The 'multi-threaded' mode of the kernel is disabled");
                  }
               }
               sValue = PropertyManager.getProperty(AUTO_SOLVE_DEP_ISSUES_PARAM_NAME);
               if (sValue == null || Boolean.valueOf(sValue))
               {
                  sModes.add(AUTO_SOLVE_DEP_ISSUES);
                  if (LOG.isDebugEnabled())
                  {
                     LOG.debug("The 'auto solve dependency issues' mode of the kernel has been enabled");
                  }
               }
               else if (LOG.isDebugEnabled())
               {
                  LOG.debug("The 'auto solve dependency issues' mode of the kernel is disabled");
               }
               modes = Collections.unmodifiableSet(sModes);
               MODES = modes;
            }
         }
      }
      return modes;
   }
}
