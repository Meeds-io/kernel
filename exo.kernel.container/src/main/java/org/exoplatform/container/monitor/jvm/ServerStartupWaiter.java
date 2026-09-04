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

import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * Holds a caller until the <em>full</em> server startup: the given
 * {@link ExoContainer} started, then a JMX-managed HTTP connector in STARTED
 * state. Tomcat starts its connectors only once every webapp is deployed and
 * every container is started, and their 'stateName' JMX attribute switches to
 * STARTED as the last server startup step — so that state is the one signal
 * that the server can answer the HTTP requests startup-time work may need to
 * send to itself (OAuth/OIDC discovery, token issuance, MCP, REST).
 * <p>
 * Degrades to the container-startup-only wait when no JMX-managed HTTP
 * connector exists (unit tests, embedded runs, AJP-only servers) or when the
 * JMX read fails, escapes with a warning when every HTTP connector reached a
 * terminal state without starting (e.g. port already bound), and gives up with
 * a warning past {@value #TIMEOUT_PROPERTY} seconds
 * ({@value #DEFAULT_TIMEOUT_SECONDS} by default) — the timeout is the safety
 * net for any thread the server startup transitively depends on that this
 * class cannot recognize (see below), turning a potential boot deadlock into a
 * bounded delay.
 * <p>
 * The server startup thread itself is refused with an
 * {@link IllegalStateException}: the connector starts only after that thread
 * finishes the startup, so waiting there can never end. That detection is by
 * thread name ({@code "main"}) — the reliable signal for the standard Tomcat
 * bootstrap, but blind to renamed bootstrap threads and to the
 * {@code catalina-utility-*} threads Tomcat uses when {@code startStopThreads}
 * &gt; 1; those cases are what the timeout bounds instead.
 */
public final class ServerStartupWaiter {

  private static final Log         LOG                       = ExoLogger.getLogger(ServerStartupWaiter.class);

  private static final String      CONNECTOR_OBJECT_NAME     = "Catalina:type=Connector,*";

  private static final String      PROTOCOL_ATTRIBUTE        = "protocol";

  private static final String      STATE_NAME_ATTRIBUTE      = "stateName";

  private static final String      STARTED_LIFECYCLE_STATE   = "STARTED";

  private static final Set<String> TERMINAL_LIFECYCLE_STATES = Set.of("FAILED", "STOPPED", "DESTROYED");

  private static final String      STARTUP_THREAD_NAME       = "main";

  private static final String      TIMEOUT_PROPERTY          = "exo.server.startup.wait.timeoutSeconds";

  private static final long        DEFAULT_TIMEOUT_SECONDS   = 600;

  private static final String      POLL_PERIOD_PROPERTY      = "exo.server.startup.wait.pollPeriodMs";

  private static final long        DEFAULT_POLL_PERIOD_MS    = 500;

  // One 'still waiting' log each minute of polling at the default period
  private static final long        LOG_PERIOD_MS             = 60 * 1000L;

  private ServerStartupWaiter() {
    // Static utility
  }

  /**
   * Waits until the container is started and one HTTP connector is up, per the
   * class contract above.
   *
   * @param container the container whose startup ends the first wait phase
   * @param subject what is being held, for the wait logs and the startup-thread
   *          error message (e.g. "Quartz scheduled jobs", "MCP client OAuth
   *          registrations")
   * @throws InterruptedException when the wait is interrupted (server shutdown)
   * @throws IllegalStateException when called on the server startup thread
   *           while a wait is actually needed — waiting there would deadlock
   *           the boot, the work must move to an async-init or request thread
   */
  public static void awaitServerStartup(ExoContainer container, String subject) throws InterruptedException {
    if (container.isStarted() && isHttpConnectorStarted(container, subject)) {
      return;
    }
    if (STARTUP_THREAD_NAME.equals(Thread.currentThread().getName())) {
      throw new IllegalStateException(String.format(
          "'%s' requires the full server startup, but is running on the server startup thread ('main'), which is"
              + " precisely what completes that startup: waiting here would deadlock the boot. Move this work off"
              + " the startup path — an async context/bean initialization, a dedicated startup thread, or the first"
              + " request/job thread that actually needs it.",
          subject));
    }
    long pollPeriodMs = getLongProperty(POLL_PERIOD_PROPERTY, DEFAULT_POLL_PERIOD_MS);
    long deadline = System.currentTimeMillis() + getLongProperty(TIMEOUT_PROPERTY, DEFAULT_TIMEOUT_SECONDS) * 1000L;
    long lastLog = System.currentTimeMillis();
    while (!container.isStarted()) {
      if (giveUpPastDeadline(deadline, subject)) {
        return;
      }
      lastLog = logStillWaiting(lastLog, "the container '" + container.getContext().getName() + "' startup", subject);
      Thread.sleep(pollPeriodMs);
    }
    while (!isHttpConnectorStarted(container, subject)) {
      if (giveUpPastDeadline(deadline, subject)) {
        return;
      }
      lastLog = logStillWaiting(lastLog, "the server HTTP connector startup", subject);
      Thread.sleep(pollPeriodMs);
    }
  }

  /**
   * Whether an HTTP connector exists and has not started yet — i.e. whether
   * {@link #awaitServerStartup} would actually have something to wait for
   * beyond the container startup. Lets a caller keep its legacy synchronous
   * path when there is nothing observable to wait for (unit tests, embedded
   * runs, AJP-only servers) and pay the asynchronous wait only when the wait
   * is real.
   *
   * @param container the container providing the JMX server
   * @param subject what would be held, for the degrade-path logs
   * @return {@code true} when an HTTP connector is observable and not STARTED
   */
  public static boolean isHttpConnectorPending(ExoContainer container, String subject) {
    return !isHttpConnectorStarted(container, subject);
  }

  private static boolean giveUpPastDeadline(long deadline, String subject) {
    if (System.currentTimeMillis() > deadline) {
      LOG.warn("'{}' waited {} seconds ({}) for the full server startup without observing it: continuing anyway."
          + " If this server legitimately starts slower than that, raise the property",
               subject,
               getLongProperty(TIMEOUT_PROPERTY, DEFAULT_TIMEOUT_SECONDS),
               TIMEOUT_PROPERTY);
      return true;
    }
    return false;
  }

  private static long logStillWaiting(long lastLog, String waitedFor, String subject) {
    long now = System.currentTimeMillis();
    if (now - lastLog >= LOG_PERIOD_MS) {
      LOG.info("'{}' is waiting for {}", subject, waitedFor);
      return now;
    }
    return lastLog;
  }

  private static boolean isHttpConnectorStarted(ExoContainer container, String subject) {
    try {
      J2EEServerInfo serverInfo = container.getComponentInstanceOfType(J2EEServerInfo.class);
      MBeanServer mBeanServer = serverInfo == null ? null : serverInfo.getMBeanServer();
      if (mBeanServer == null) {
        return true;
      }
      return isHttpConnectorStarted(mBeanServer, subject);
    } catch (JMException | RuntimeException e) {
      LOG.warn("Error checking the server HTTP connector state through JMX."
          + " '{}' continues relying on the container startup only", subject, e);
      return true;
    }
  }

  /**
   * The connector-state decision, on an explicit {@link MBeanServer} so unit
   * tests can drive it with stub connector MBeans.
   *
   * @param mBeanServer the server holding the Catalina MBeans
   * @param subject what is being held, for the degrade-path logs
   * @return {@code true} when the wait must end: one HTTP connector STARTED,
   *         no HTTP connector at all, or every HTTP connector in a terminal
   *         state
   * @throws JMException when a JMX read fails (the caller degrades)
   */
  static boolean isHttpConnectorStarted(MBeanServer mBeanServer, String subject) throws JMException {
    Set<ObjectName> httpConnectorNames = mBeanServer.queryNames(new ObjectName(CONNECTOR_OBJECT_NAME), null)
                                                    .stream()
                                                    .filter(name -> containsIgnoreCase(getStringAttribute(mBeanServer,
                                                                                                          name,
                                                                                                          PROTOCOL_ATTRIBUTE),
                                                                                       "http"))
                                                    .collect(Collectors.toSet());
    if (httpConnectorNames.isEmpty()) {
      // No JMX-managed HTTP connector: nothing more than the container
      // startup to wait for
      return true;
    }
    Set<String> connectorStates = httpConnectorNames.stream()
                                                    .map(name -> getStringAttribute(mBeanServer, name, STATE_NAME_ATTRIBUTE))
                                                    .filter(Objects::nonNull)
                                                    .collect(Collectors.toSet());
    if (connectorStates.contains(STARTED_LIFECYCLE_STATE)) {
      // One started HTTP connector is enough: Tomcat starts its connectors
      // sequentially as the last server startup step
      return true;
    } else if (TERMINAL_LIFECYCLE_STATES.containsAll(connectorStates)) {
      LOG.warn("No server HTTP connector could start (states: {}). '{}' stops waiting and continues anyway",
               connectorStates,
               subject);
      return true;
    } else {
      return false;
    }
  }

  private static String getStringAttribute(MBeanServer mBeanServer, ObjectName name, String attribute) {
    try {
      return Objects.toString(mBeanServer.getAttribute(name, attribute), null);
    } catch (JMException e) {
      throw new IllegalStateException(String.format("Error retrieving JMX attribute '%s' of MBean '%s'", attribute, name), e);
    }
  }

  private static boolean containsIgnoreCase(String value, String token) {
    return value != null && value.toLowerCase(Locale.ROOT).contains(token);
  }

  private static long getLongProperty(String propertyName, long defaultValue) {
    String value = PropertyManager.getProperty(propertyName);
    if (value == null || value.isBlank()) {
      return defaultValue;
    }
    try {
      long parsed = Long.parseLong(value.trim());
      return parsed > 0 ? parsed : defaultValue;
    } catch (NumberFormatException e) {
      LOG.warn("Invalid value '{}' for property {}; using default {}", value, propertyName, defaultValue);
      return defaultValue;
    }
  }

}
