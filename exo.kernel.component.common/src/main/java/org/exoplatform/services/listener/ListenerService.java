/*
 * Copyright (C) 2009 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.exoplatform.services.listener;

import org.picocontainer.Startable;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.container.component.ThreadContextHandler;
import org.exoplatform.container.spi.DefinitionByType;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.naming.InitialContextInitializer;

import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by The eXo Platform SAS<br>
 * Listener Service is reponsible for notifying the {@link Listener} when a
 * given event is broadcasted.
 *
 * @author : <a href="nhudinhthuan@exoplatform.com">Nhu Dinh Thuan</a>.
 * @LevelAPI Platform
 */
@DefinitionByType
@SuppressWarnings({ "rawtypes", "unchecked" })
public class ListenerService implements Startable {

  private static final Log                      LOG       = ExoLogger.getLogger("exo.kernel.component.common.ListenerService");

  /**
   * This executor used for asynchronously event broadcast.
   */
  private final ExecutorService                 executor;

  /**
   * Listeners by name map.
   */
  private final Map<String, List<ListenerBase>> listeners = new ConcurrentHashMap<>();

  private final ExoContainer                    container;

  /**
   * Construct a listener service.
   */
  public ListenerService(ExoContainerContext ctx) {
    this(ctx, null, null);
  }

  /**
   * Construct a listener service.
   */
  public ListenerService(ExoContainerContext ctx, InitialContextInitializer initializer) {
    this(ctx, initializer, null);
  }

  /**
   * Construct a listener service.
   */
  public ListenerService(ExoContainerContext ctx, InitParams params) {
    this(ctx, null, params);
  }

  /**
   * Construct a listener service.
   */
  public ListenerService(ExoContainerContext ctx,
                         InitialContextInitializer initializer, // NOSONAR
                         InitParams params) {
    container = ctx.getContainer();
    int poolSize = 1;

    if (params != null && params.getValueParam("asynchPoolSize") != null) {
      poolSize = Integer.parseInt(params.getValueParam("asynchPoolSize").getValue());
    }
    executor = Executors.newFixedThreadPool(poolSize, new ListenerThreadFactory());
  }

  /**
   * This method is used to register a {@link Listener} to the events of the
   * same name. It is similar to addListener(listener.getName(), listener)
   * 
   * @param listener the listener to notify any time an even of the same name is
   *          triggered
   */
  public <S, D> void addListener(Listener<S, D> listener) {
    addListener(listener.getName(), listener);
  }

  /**
   * This method is used to register a new {@link Listener}. Any time an event
   * of the given event name has been triggered, the {@link Listener} will be
   * notified. This method will:
   * <ol>
   * <li>Check if it exists a list of listeners that have been registered for
   * the given event name, create a new list if no list exists</li>
   * <li>Add the listener to the list</li>
   * </ol>
   * 
   * @param eventName The name of the event to listen to
   * @param listener The Listener to notify any time the event with the given
   *          name is triggered
   */
  public void addListener(String eventName, ListenerBase listener) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Adding listener {} on event {}", listener.getName(), eventName);
    }
    // Check is Listener or its superclass asynchronous, if so - wrap it in
    // AsynchronousListener.
    Class listenerClass = listener.getClass();
    do {
      if (listenerClass.isAnnotationPresent(Asynchronous.class)) {
        listener = new AsynchronousListener(listener);
        break;
      } else {
        listenerClass = listenerClass.getSuperclass();
      }
    } while (listenerClass != null);
    listeners.computeIfAbsent(eventName, k -> new Vector<>())
             .add(listener);
  }

  /**
   * This method is used to broadcast an event. This method should: 1. Check if
   * there is a list of listener that listen to the event name. 2. If there is a
   * list of listener, create the event object with the given name , source and
   * data 3. For each listener in the listener list, invoke the method
   * onEvent(Event)
   * 
   * @param <S> The type of the source that broacast the event
   * @param <D> The type of the data that the source object is working on
   * @param name The name of the event
   * @param source The source object instance
   * @param data The data object instance
   */
  public <S, D> void broadcast(String name, S source, D data) {
    List<ListenerBase> list = listeners.get(name);
    if (list == null)
      return;
    for (ListenerBase<S, D> listener : list) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("broadcasting event " + name + " on " + listener.getName());
      }
      try {
        listener.onEvent(new Event<>(name, source, data));
      } catch (Exception e) {
        LOG.warn("Exception on broadcasting events occurred while broadcasting event {}. Continue braodcasting events.",
                 name,
                 e);
      }
    }
  }

  /**
   * This method is used when a developer want to implement his own event object
   * and broadcast the event. The method should: 1. Check if there is a list of
   * listener that listen to the event name. 2. If there is a list of the
   * listener, ror each listener in the listener list, invoke the method
   * onEvent(Event)
   * 
   * @param <T> The type of the event object, the type of the event object has
   *          to be extended from the Event type
   * @param event The event instance
   */
  public <T extends Event> void broadcast(T event) {
    List<ListenerBase> list = listeners.get(event.getEventName());
    if (list == null) {
      return;
    }
    for (ListenerBase listener : list) {
      try {
        listener.onEvent(event);
      } catch (Exception e) {
        LOG.warn("Exception on broadcasting events occurred while broadcasting event {}. Continue braodcasting events.",
                 event.getEventName(),
                 e);
      }
    }
  }

  /**
   * This AsynchronousListener is a wrapper for original listener, that executes
   * wrapped listeners onEvent() in separate thread.
   */
  protected class AsynchronousListener<S, D> implements ListenerBase<S, D> {
    private ListenerBase<S, D> listener;

    public AsynchronousListener(ListenerBase<S, D> listener) {
      this.listener = listener;
    }

    @Override
    public void onEvent(Event<S, D> event) {
      executor.execute(new RunListener<S, D>(listener, event));
    }
  }

  /**
   * This thread executes listener.onEvent(event) method.
   */
  protected class RunListener<S, D> implements Runnable {

    private ListenerBase<S, D>         listener;

    private Event<S, D>                event;

    private final ThreadContextHandler handler;

    public RunListener(ListenerBase<S, D> listener, Event<S, D> event) {
      this.listener = listener;
      this.event = event;
      this.handler = new ThreadContextHandler(container);
      handler.store();
    }

    /**
     * {@inheritDoc}
     */
    public void run() {
      ExoContainerContext.setCurrentContainer(container);
      RequestLifeCycle.begin(container);
      try {
        handler.push();
        listener.onEvent(event);
      } catch (Exception e) {
        // Do not throw exception. Event is asynchronous so just report error.
        // Must say that exception will be ignored even in synchronous events.
        LOG.error("Exception on broadcasting events occurs: " + e.getMessage(), e);
      } finally {
        try {
          handler.restore();
          RequestLifeCycle.end();
        } finally {
          ExoContainerContext.setCurrentContainer(null);
        }
      }
    }
  }

  @Override
  public void stop() {
    executor.shutdown();
  }
}
