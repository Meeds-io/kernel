/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io
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
package org.exoplatform.container.monitor.jvm;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.container.ExoContainer;

import junit.framework.TestCase;

/**
 * Tests of {@link ServerStartupWaiter}: the connector-state decision against
 * stub Catalina connector MBeans, the startup-thread refusal, the timeout
 * escape and the interruption path.
 */
public class TestServerStartupWaiter extends TestCase
{

   private static final String TIMEOUT_PROPERTY     = "exo.server.startup.wait.timeoutSeconds";

   private static final String POLL_PERIOD_PROPERTY = "exo.server.startup.wait.pollPeriodMs";

   private MBeanServer mBeanServer;

   @Override
   protected void setUp() throws Exception
   {
      // An isolated MBean server: nothing here touches the platform one
      mBeanServer = MBeanServerFactory.newMBeanServer();
   }

   @Override
   protected void tearDown() throws Exception
   {
      // No releaseMBeanServer: newMBeanServer() creates an unregistered
      // server, garbage-collected with the test
      mBeanServer = null;
      PropertyManager.setProperty(TIMEOUT_PROPERTY, "");
      PropertyManager.setProperty(POLL_PERIOD_PROPERTY, "");
   }

   public void testNoHttpConnectorMeansNothingToWaitFor() throws Exception
   {
      assertTrue("An empty MBean server holds nothing to wait for",
                 ServerStartupWaiter.isHttpConnectorStarted(mBeanServer, "test"));

      registerConnector("port=8009", "AJP/1.3", "INITIALIZED");
      assertTrue("A non-HTTP connector is not waited for",
                 ServerStartupWaiter.isHttpConnectorStarted(mBeanServer, "test"));
   }

   public void testConnectorLifecycleStates() throws Exception
   {
      ConnectorStub http = registerConnector("port=8080", "HTTP/1.1", "INITIALIZED");

      assertFalse("An HTTP connector not yet started must be waited for",
                  ServerStartupWaiter.isHttpConnectorStarted(mBeanServer, "test"));

      http.stateName = "STARTING";
      assertFalse("A starting HTTP connector must still be waited for",
                  ServerStartupWaiter.isHttpConnectorStarted(mBeanServer, "test"));

      http.stateName = "STARTED";
      assertTrue("A started HTTP connector ends the wait",
                 ServerStartupWaiter.isHttpConnectorStarted(mBeanServer, "test"));

      http.stateName = "FAILED";
      assertTrue("All-terminal connector states end the wait (escape, not success)",
                 ServerStartupWaiter.isHttpConnectorStarted(mBeanServer, "test"));
   }

   public void testOneStartedConnectorAmongSeveralIsEnough() throws Exception
   {
      registerConnector("port=8080", "HTTP/1.1", "STARTED");
      registerConnector("port=8443", "HTTP/1.1", "INITIALIZED");
      assertTrue("One STARTED HTTP connector is enough: Tomcat starts them sequentially last",
                 ServerStartupWaiter.isHttpConnectorStarted(mBeanServer, "test"));
   }

   public void testRefusesToWaitOnTheStartupThread() throws Exception
   {
      ExoContainer unstarted = new ExoContainer();
      AtomicReference<Throwable> thrown = new AtomicReference<>();
      Thread fakeMain = new Thread(() -> {
         try
         {
            ServerStartupWaiter.awaitServerStartup(unstarted, "test");
         }
         catch (Throwable t)
         {
            thrown.set(t);
         }
      }, "main");
      fakeMain.start();
      fakeMain.join(10_000);
      assertFalse("The refusal must be immediate, not a wait", fakeMain.isAlive());
      assertTrue("Waiting on the startup thread must be refused with an IllegalStateException, got: " + thrown.get(),
                 thrown.get() instanceof IllegalStateException);
      assertTrue("The error must explain the deadlock and the way out",
                 thrown.get().getMessage().contains("deadlock"));
   }

   public void testTimeoutEndsTheWait() throws Exception
   {
      PropertyManager.setProperty(TIMEOUT_PROPERTY, "1");
      PropertyManager.setProperty(POLL_PERIOD_PROPERTY, "50");
      ExoContainer neverStarted = new ExoContainer();
      AtomicReference<Throwable> thrown = new AtomicReference<>();
      Thread waiter = new Thread(() -> {
         try
         {
            ServerStartupWaiter.awaitServerStartup(neverStarted, "test");
         }
         catch (Throwable t)
         {
            thrown.set(t);
         }
      }, "test-waiter");
      waiter.start();
      waiter.join(10_000);
      assertFalse("The wait must give up at the configured timeout instead of holding forever", waiter.isAlive());
      assertNull("The timeout escape returns normally, it does not throw", thrown.get());
   }

   public void testInterruptionSurfaces() throws Exception
   {
      ExoContainer neverStarted = new ExoContainer();
      AtomicBoolean interrupted = new AtomicBoolean();
      Thread waiter = new Thread(() -> {
         try
         {
            ServerStartupWaiter.awaitServerStartup(neverStarted, "test");
         }
         catch (InterruptedException e)
         {
            interrupted.set(true);
         }
      }, "test-waiter");
      waiter.start();
      Thread.sleep(200);
      waiter.interrupt();
      waiter.join(10_000);
      assertFalse(waiter.isAlive());
      assertTrue("An interruption must surface as InterruptedException to the caller", interrupted.get());
   }

   private ConnectorStub registerConnector(String nameSuffix, String protocol, String stateName) throws Exception
   {
      ConnectorStub stub = new ConnectorStub(protocol, stateName);
      mBeanServer.registerMBean(stub, new ObjectName("Catalina:type=Connector," + nameSuffix));
      return stub;
   }

   /**
    * A stub of Tomcat's Connector MBean. Dynamic on purpose: the real MBean
    * exposes its attributes with lowercase names ('protocol', 'stateName'),
    * which a standard MBean's getter-derived attributes cannot reproduce.
    */
   private static final class ConnectorStub implements DynamicMBean
   {
      private final String    protocol;

      private volatile String stateName;

      private ConnectorStub(String protocol, String stateName)
      {
         this.protocol = protocol;
         this.stateName = stateName;
      }

      public Object getAttribute(String attribute) throws AttributeNotFoundException
      {
         if ("protocol".equals(attribute))
         {
            return protocol;
         }
         else if ("stateName".equals(attribute))
         {
            return stateName;
         }
         throw new AttributeNotFoundException(attribute);
      }

      public AttributeList getAttributes(String[] attributes)
      {
         return new AttributeList();
      }

      public MBeanInfo getMBeanInfo()
      {
         return new MBeanInfo(ConnectorStub.class.getName(), "Tomcat Connector stub", null, null, null, null);
      }

      public void setAttribute(Attribute attribute)
      {
         // Read-only stub
      }

      public AttributeList setAttributes(AttributeList attributes)
      {
         return new AttributeList();
      }

      public Object invoke(String actionName, Object[] params, String[] signature)
      {
         return null;
      }
   }

}
