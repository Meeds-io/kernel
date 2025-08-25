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
package org.exoplatform.services.cache.concurrent;

import org.exoplatform.services.cache.CacheListener;
import org.exoplatform.services.cache.CachedObjectSelector;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * An {@link org.exoplatform.services.cache.ExoCache} implementation based on
 * {@link java.util.concurrent.ConcurrentHashMap} that minimize locking. Cache
 * entries are maintained in a fifo list that is used for the fifo eviction
 * policy.
 */
public class ConcurrentFIFOExoCache<K extends Serializable, V> implements ExoCache<K, V> {

  private static final Log                            LOGGER                 =
                                                             ExoLogger.getExoLogger(ConcurrentFIFOExoCache.class);

  private static final int                            DEFAULT_MAX_SIZE       = 50;

  private static final String                         NULL_CACHE_KEY_MESSAGE = "No null cache key accepted";

  private final Log                                   log;

  private volatile long                               liveTimeMillis;

  volatile int                                        maxSize;

  private CopyOnWriteArrayList<ListenerContext<K, V>> listeners;

  private CacheState<K, V>                            state;

  AtomicInteger                                       hits                   = new AtomicInteger();

  AtomicInteger                                       misses                 = new AtomicInteger();

  private String                                      label;

  private String                                      name;

  private boolean                                     logEnabled             = false;

  public ConcurrentFIFOExoCache() {
    this(DEFAULT_MAX_SIZE);
  }

  public ConcurrentFIFOExoCache(Log log) {
    this(DEFAULT_MAX_SIZE, log);
  }

  public ConcurrentFIFOExoCache(int maxSize) {
    this(null, maxSize);
  }

  public ConcurrentFIFOExoCache(int maxSize, Log log) {
    this(null, maxSize, log);
  }

  public ConcurrentFIFOExoCache(String name, int maxSize) {
    this(name, maxSize, null);
  }

  public ConcurrentFIFOExoCache(String name, int maxSize, Log log) {
    this.maxSize = maxSize;
    this.name = name;
    if (log == null) {
      log = LOGGER;
    }
    this.state = new CacheState<>(this, log);
    this.liveTimeMillis = -1;
    this.log = log;
    this.listeners = new CopyOnWriteArrayList<>();
  }

  public void assertConsistent() {
    state.assertConsistency();
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(String s) {
    name = s;
  }

  @Override
  public String getLabel() {
    if (label == null) {
      if (name.length() > 30) {
        String shortLabel = name.substring(name.lastIndexOf(".") + 1);
        setLabel(shortLabel);
        return shortLabel;
      }
      return name;
    }
    return label;
  }

  @Override
  public void setLabel(String name) {
    label = name;
  }

  @Override
  public long getLiveTime() {
    long tmp = getLiveTimeMillis();
    return tmp == -1 ? -1 : tmp / 1000;
  }

  @Override
  public void setLiveTime(long period) {
    setLiveTimeMillis(period * 1000);
  }

  public long getLiveTimeMillis() {
    return liveTimeMillis;
  }

  public void setLiveTimeMillis(long liveTimeMillis) {
    if (liveTimeMillis < 0) {
      liveTimeMillis = -1;
    }
    this.liveTimeMillis = liveTimeMillis;
  }

  @Override
  public int getMaxSize() {
    return maxSize;
  }

  @Override
  public void setMaxSize(int max) {
    this.maxSize = max;
  }

  @Override
  public V get(Serializable name) {
    if (name == null) {
      return null;
    }
    return state.get(name);
  }

  @Override
  public void put(K name, V obj) {
    if (name == null) {
      throw new IllegalArgumentException(NULL_CACHE_KEY_MESSAGE);
    }
    if (liveTimeMillis != 0) {
      long expirationTime = liveTimeMillis > 0 ? System.currentTimeMillis() + liveTimeMillis : Long.MAX_VALUE;
      state.put(expirationTime, name, obj, false);
    }
  }

  @Override
  public void putLocal(K name, V obj) {
    if (name == null) {
      throw new IllegalArgumentException(NULL_CACHE_KEY_MESSAGE);
    }
    if (liveTimeMillis != 0) {
      long expirationTime = liveTimeMillis > 0 ? System.currentTimeMillis() + liveTimeMillis : Long.MAX_VALUE;
      state.put(expirationTime, name, obj, true);
    }
  }

  @Override
  public void putMap(Map<? extends K, ? extends V> objs) {
    if (objs == null) {
      throw new IllegalArgumentException("No null map accepted");
    }
    long expirationTime = liveTimeMillis > 0 ? System.currentTimeMillis() + liveTimeMillis : Long.MAX_VALUE;
    for (Serializable keyName : objs.keySet()) {
      if (keyName == null) {
        throw new IllegalArgumentException(NULL_CACHE_KEY_MESSAGE);
      }
    }
    for (Map.Entry<? extends K, ? extends V> entry : objs.entrySet()) {
      state.put(expirationTime, entry.getKey(), entry.getValue(), false);
    }
  }

  @Override
  public V remove(Serializable name) {
    if (name == null) {
      throw new IllegalArgumentException(NULL_CACHE_KEY_MESSAGE);
    }
    return state.remove(name);
  }

  @Override
  public List<? extends V> getCachedObjects() {
    LinkedList<V> list = new LinkedList<>();
    for (ObjectRef<K, V> objectRef : state.map.values()) {
      V object = objectRef.getObject();
      if (objectRef.isValid()) {
        list.add(object);
      }
    }
    return list;
  }

  @Override
  public List<? extends V> removeCachedObjects() {
    List<? extends V> list = getCachedObjects();
    clearCache();
    return list;
  }

  public void clearCache() {
    state = new CacheState<>(this, log);
  }

  @Override
  public void select(CachedObjectSelector<? super K, ? super V> selector) throws Exception {
    if (selector == null) {
      throw new IllegalArgumentException("No null selector");
    }
    for (Map.Entry<K, ObjectRef<K, V>> entry : state.map.entrySet()) {
      K key = entry.getKey();
      ObjectRef<K, V> info = entry.getValue();
      if (selector.select(key, info)) {
        selector.onSelect(this, key, info);
      }
    }
  }

  @Override
  public int getCacheSize() {
    return state.queue.size();
  }

  @Override
  public int getCacheHit() {
    return hits.get();
  }

  @Override
  public int getCacheMiss() {
    return misses.get();
  }

  @Override
  public synchronized void addCacheListener(CacheListener<? super K, ? super V> listener) {
    if (listener == null) {
      throw new IllegalArgumentException("The listener cannot be null");
    }
    listeners.add(new ListenerContext<>(listener, this));
  }

  @Override
  public boolean isLogEnabled() {
    return logEnabled;
  }

  @Override
  public void setLogEnabled(boolean logEnabled) {
    this.logEnabled = logEnabled;
  }

  //

  @Override
  public void onExpire(K key, V obj) {
    if (!listeners.isEmpty())
      for (ListenerContext<K, V> context : listeners)
        context.onExpire(key, obj);
  }

  @Override
  public void onRemove(K key, V obj) {
    if (!listeners.isEmpty())
      for (ListenerContext<K, V> context : listeners)
        context.onRemove(key, obj);
  }

  @Override
  public void onPut(K key, V obj) {
    if (!listeners.isEmpty())
      for (ListenerContext<K, V> context : listeners)
        context.onPut(key, obj);
  }

  @Override
  public void onPutLocal(K key, V obj) {
    if (!listeners.isEmpty())
      for (ListenerContext<K, V> context : listeners)
        context.onPutLocal(key, obj);
  }

  @Override
  public void onGet(K key, V obj) {
    if (!listeners.isEmpty())
      for (ListenerContext<K, V> context : listeners)
        context.onGet(key, obj);
  }

  @Override
  public void onClearCache() {
    if (!listeners.isEmpty())
      for (ListenerContext<K, V> context : listeners)
        context.onClearCache();
  }

  public List<ListenerContext<K, V>> getListeners() {
    return listeners;
  }

}
