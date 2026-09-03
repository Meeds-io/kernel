/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.exoplatform.services.scheduler.impl;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.Properties;
import org.exoplatform.container.BaseContainerLifecyclePlugin;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.monitor.jvm.ServerStartupWaiter;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.picocontainer.Startable;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;

import javax.naming.InitialContext;
import javax.sql.DataSource;

/**
 * Created by The eXo Platform SAS Author : Tuan Nguyen
 * tuan08@users.sourceforge.net Dec 13, 2005
 * 
 * @version $Id: QuartzSheduler.java 34394 2009-07-23 09:23:31Z dkatayev $
 */
public class QuartzSheduler implements Startable
{
   private static final Log    LOG                        = ExoLogger.getLogger("exo.kernel.component.common.QuartzSheduler");

   private static final String defaultDriverDelegateClass = "org.quartz.impl.jdbcjobstore.StdJDBCDelegate";

   private static final String PGSQLDriverDelegateClass   = "org.quartz.impl.jdbcjobstore.PostgreSQLDelegate";

   private static final String MSSQLlDriverDelegateClass  = "org.quartz.impl.jdbcjobstore.MSSQLDelegate";

   private static final String datasourceProperty         = "org.quartz.dataSource.quartzDS.jndiURL";

   private static final String delegateClassProperty      = "org.quartz.jobStore.driverDelegateClass";

   private static final String QUARTZ_STARTUP_SUBJECT     = "Quartz scheduled jobs";

   private final Scheduler scheduler_;

   private volatile Thread starterThread;

   private volatile boolean stopping;

   public QuartzSheduler(ExoContainerContext ctx, InitParams params) throws Exception
   {
      final SchedulerFactory sf;

      if (params != null && !params.isEmpty())
      {
         final Properties props = new Properties();
         for (String key : params.keySet())
         {
            props.setProperty(key, params.getValueParam(key).getValue());
         }
         String oldValue = props.getProperty(delegateClassProperty);
         if (oldValue== null || oldValue.isEmpty() || oldValue.equals(defaultDriverDelegateClass))
         {
           String datasourceName = props.getProperty(datasourceProperty);
           if (datasourceName != null && !datasourceName.isEmpty())
           {
             try (Connection conn = getConnection(datasourceName);)
             {
               DatabaseMetaData meta = conn.getMetaData();
               String newValue = getDriverDelegateClass(meta);
               props.setProperty(delegateClassProperty, newValue);
             }
           }
         }
         sf = new StdSchedulerFactory(props);
      }
      /*Use default quartz configuration (utilizes RAM as its storage device,
           exo.quartz.jobStore.class=org.quartz.simpl.RAMJobStore).*/
      else
      {
         sf = new StdSchedulerFactory();
      }
      scheduler_ = sf.getScheduler();

      // If the scheduler has already been started, it is necessary to put the scheduler
      // in standby mode to ensure that the jobs of the ExoContainer won't launched too early
      scheduler_.standby();
      // This will launch the scheduler once the FULL server startup is observed: not only
      // this container's components, but also the sibling lifecycle plugins run after this
      // one (the Kernel <-> Spring bridge finishing every Spring context) and the server
      // HTTP connector, which Tomcat starts as the last startup step. Jobs released on
      // "components started" alone run against a server that cannot answer the HTTP
      // requests they may send to itself (OAuth/OIDC discovery, token issuance, MCP) —
      // observed with the email sync jobs firing seconds before the connector was up.
      // The wait MUST leave the startup thread: the connector starts only after the
      // container startup completes, so waiting inline would deadlock the boot. When no
      // HTTP connector is observable (unit tests, embedded runs, AJP-only servers) the
      // release stays the legacy synchronous one — the asynchronous path is paid only
      // where the wait is real, so environments without a connector keep the exact
      // deterministic startup they always had.
      ctx.getContainer().addContainerLifecylePlugin(new BaseContainerLifecyclePlugin()
      {

         @Override
         public void startContainer(ExoContainer container) throws Exception
         {
            if (ServerStartupWaiter.isHttpConnectorPending(container, QUARTZ_STARTUP_SUBJECT))
            {
               LOG.info("Quartz scheduler stays in standby until the full server startup, HTTP connector included");
               starterThread = new Thread(() -> startSchedulerAfterFullServerStartup(container),
                                          "quartz-scheduler-startup-" + ctx.getName());
               starterThread.setDaemon(true);
               starterThread.start();
            }
            else
            {
               scheduler_.start();
            }
         }
      });
   }

   private void startSchedulerAfterFullServerStartup(ExoContainer container)
   {
      try
      {
         ServerStartupWaiter.awaitServerStartup(container, QUARTZ_STARTUP_SUBJECT);
      }
      catch (InterruptedException e)
      {
         Thread.currentThread().interrupt();
         LOG.info("Interrupted while waiting for the full server startup: the server is stopping,"
             + " the Quartz scheduler stays in standby");
         return;
      }
      catch (Throwable t) // NOSONAR an escaping error would silently leave every job unscheduled forever
      {
         // Fail open: a broken wait must not cost the platform its scheduler — better a
         // job hitting a not-yet-ready endpoint than no scheduled job ever running
         LOG.error("Unexpected error while waiting for the full server startup. Starting the Quartz scheduler right away", t);
      }
      if (stopping)
      {
         LOG.info("The server is stopping: the Quartz scheduler stays in standby");
         return;
      }
      try
      {
         scheduler_.start();
         LOG.info("Quartz scheduler started after the full server startup");
      }
      catch (SchedulerException e)
      {
         if (stopping)
         {
            LOG.debug("The Quartz scheduler was shut down while its startup release was in flight: normal on a"
                + " server stopped before its startup completed", e);
         }
         else
         {
            LOG.error("Could not start the Quartz scheduler: no scheduled job will run", e);
         }
      }
   }

   public Scheduler getQuartzSheduler()
   {
      return scheduler_;
   }

   public void start()
   {
   }

   public void stop()
   {
      // Settle the race with the startup-release thread before shutting Quartz down: a
      // start() landing after shutdown() throws, and a start() landing during shutdown
      // would fire queued jobs against a stopping container. The bounded join is what
      // actually closes the window -- the interrupt alone misses a starter already past
      // its 'stopping' check and inside scheduler_.start()
      stopping = true;
      Thread starter = starterThread;
      if (starter != null)
      {
         starter.interrupt();
         try
         {
            starter.join(5000);
         }
         catch (InterruptedException e)
         {
            Thread.currentThread().interrupt();
         }
      }
      try
      {
         scheduler_.shutdown();
      }
      catch (SchedulerException ex)
      {
         LOG.warn("Could not shutdown the scheduler", ex);
      }
   }

   /**
    * Opens connection to quartz database.
    */
   private Connection getConnection(String dsName) throws Exception
   {
     final DataSource dsF = (DataSource) new InitialContext().lookup(dsName);
     return dsF.getConnection();
   }

    /**
     * Auto detect DriverDelegateClass  according to database name.
     */
   private String getDriverDelegateClass(final DatabaseMetaData metaData) throws Exception
   {
     String databaseName = metaData.getDatabaseProductName();
     if(databaseName == null || databaseName.isEmpty())
     {
         LOG.warn("The database name cannot be retrieve, the default DriverDelegateClass will be used for Quartz.");
         return defaultDriverDelegateClass;
     }
     if (databaseName.startsWith("Microsoft SQL Server"))
     {
        return MSSQLlDriverDelegateClass;
     }
     else if (databaseName.startsWith("PostgreSQL"))
     {
        return PGSQLDriverDelegateClass;
     }
     else
     {
        return defaultDriverDelegateClass;
     }
   }
}
