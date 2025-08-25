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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import org.exoplatform.services.log.Log;

/**
 * Really the cache state (we need it because of the clear cache consistency).
 */
public class CacheState<K extends Serializable, V> {

  private static final long                   MAX_ITEMS_REACHED_TRACE_PERIODICITY = 60000l; // 60s

  private static final long                   MIN_MISS_COUNT                      = 1000l;

  private final Log                           log;

  private final ConcurrentFIFOExoCache<K, V>  config;

  final ConcurrentHashMap<K, ObjectRef<K, V>> map;

  final Queue<ObjectRef<K, V>>                queue;

  long                                        lastMaxItemsReachedLogTime;

  public CacheState(ConcurrentFIFOExoCache<K, V> config, Log log) {
    this.log = log;
    this.config = config;
    this.map = new ConcurrentHashMap<>();
    this.queue = new SynchronizedQueue<>(log);
  }

  public void assertConsistency() {
    if (queue instanceof SynchronizedQueue synchronizedQueue) { // NOSONAR
      synchronizedQueue.assertConsistency();
    }
    int mapSize = map.size();
    int effectiveQueueSize = queue.size();
    if (effectiveQueueSize != mapSize) {
      throw new AssertionError("The map size is " + mapSize + " is different from the queue size " + effectiveQueueSize);
    }
  }

  public V get(Serializable name) {
    ObjectRef<K, V> entry = map.get(name);
    if (entry != null) {
      V o = entry.getObject();
      if (entry.isValid()) {
        config.hits.incrementAndGet();
        config.onGet(entry.name, o);
        return o;
      } else {
        config.misses.incrementAndGet();
        if (map.remove(name, entry)) {
          queue.remove(entry);
        }
        config.onExpire(entry.name, o);
        traceExcessiveMissCountRatio();
      }
    } else {
      config.misses.incrementAndGet();
      traceExcessiveMissCountRatio();
    }
    return null;
  }

  private boolean isTraceEnabled() {
    return log != null && log.isTraceEnabled();
  }

  private void trace(String message) {
    log.trace(message + " [" + Thread.currentThread().getName() + "]");
  }

  /**
   * Do a put with the provided expiration time.
   *
   * @param expirationTime the expiration time
   * @param name the cache key
   * @param obj the cached value
   */
  void put(long expirationTime, K name, V obj, boolean local) {
    boolean trace = isTraceEnabled();
    ObjectRef<K, V> nextRef = new SimpleObjectRef<>(expirationTime, name, obj);
    ObjectRef<K, V> previousRef = map.put(name, nextRef);

    // Remove previous (promoted as first element)
    if (previousRef != null) {
      queue.remove(previousRef);
      if (trace) {
        trace("Replaced item=" + previousRef.serial + " with item=" + nextRef.serial + " in the map");
      }
    } else if (trace) {
      trace("Added item=" + nextRef.serial + " to map");
    }

    // Add to the queue
    queue.add(nextRef);

    // Perform eviction from queue
    ArrayList<ObjectRef<K, V>> evictedRefs = queue.trim(config.maxSize);
    if (evictedRefs != null && !evictedRefs.isEmpty()) {
      for (ObjectRef<K, V> evictedRef : evictedRefs) {
        // We remove it from the map only if it was the same entry
        // it could have been removed concurrently by an explicit remove
        // or by a promotion
        map.remove(evictedRef.name, evictedRef);

        // Expiration callback
        config.onExpire(evictedRef.name, evictedRef.getObject());
      }
      traceMaxItemsReached();
    }

    // Put callback
    if (local) {
      config.onPutLocal(name, obj);
    } else {
      config.onPut(name, obj);
    }
  }

  public V remove(Serializable name) {
    boolean trace = isTraceEnabled();
    ObjectRef<K, V> item = map.remove(name);
    if (item != null) {
      if (trace) {
        trace("Removed item=" + item.serial + " from the map going to remove it");
      }
      boolean removed = queue.remove(item);
      boolean valid = removed && item.isValid();
      V object = item.getObject();
      if (valid) {
        config.onRemove(item.name, object);
        return object;
      } else {
        config.onExpire(item.name, object);
        return null;
      }
    } else {
      return null;
    }
  }

  private void traceExcessiveMissCountRatio() {
    int missCount = config.misses.get();
    int maxSize = config.getMaxSize();
    int hitCount = config.hits.get();
    if (missCount > MIN_MISS_COUNT
        && missCount % Math.max(MIN_MISS_COUNT, Math.min(maxSize, hitCount)) == 0) { // NOSONAR
      log.warn("Cache '{}' seems to have an excessive miss count '{}' (hits count: '{}'), please consider reviewing Max Size '{}' and TTL '{}s' configurations.",
               config.getName(),
               String.format("%,d", missCount),
               String.format("%,d", hitCount),
               String.format("%,d", config.getMaxSize()),
               String.format("%,d", config.getLiveTimeMillis() / 1000l));
    }
  }

  private void traceMaxItemsReached() {
    if (lastMaxItemsReachedLogTime < (System.currentTimeMillis() - MAX_ITEMS_REACHED_TRACE_PERIODICITY)) {
      lastMaxItemsReachedLogTime = System.currentTimeMillis();
      log.warn("Cache '{}' Max items '{}' reached, please consider reviewing Max Size and TTL configurations.",
               config.getName(),
               config.getMaxSize());
    }
  }

}
