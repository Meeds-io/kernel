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
package org.exoplatform.services.cache.impl.infinispan.generic;

import org.exoplatform.management.annotations.Managed;
import org.exoplatform.management.annotations.ManagedDescription;
import org.exoplatform.management.annotations.ManagedName;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.cache.ExoCacheConfig;
import org.exoplatform.services.cache.ExoCacheInitException;
import org.exoplatform.services.cache.impl.infinispan.AbstractExoCache;
import org.exoplatform.services.cache.impl.infinispan.ExoCacheCreator;
import org.infinispan.Cache;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.eviction.EvictionStrategy;

import java.io.Serializable;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * The generic {@link ExoCacheCreator} for all the expiration available in infinispan.
 * 
 * @author <a href="mailto:nicolas.filotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public class GenericExoCacheCreator implements ExoCacheCreator
{

   /**
    * The default value for the eviction strategy
    */
   protected String defaultStrategy = "LRU";

   /**
    * The default value for maxIdle
    */
   protected long defaultMaxIdle = -1;

   /**
    * The default value for wakeUpInterval
    */
   protected long defaultWakeUpInterval = 5000;

   /**
    * A set of all the implementations supported by this creator
    */
   protected Set<String> implementations;

   /**
    * {@inheritDoc}
    */
   public Set<String> getExpectedImplementations()
   {
      return implementations;
   }

   /**
    * {@inheritDoc}
    */
   public Class<? extends ExoCacheConfig> getExpectedConfigType()
   {
      return GenericExoCacheConfig.class;
   }

   /**
    * {@inheritDoc}
    */
   public ExoCache<Serializable, Object> create(ExoCacheConfig config, ConfigurationBuilder confBuilder,
      Callable<Cache<Serializable, Object>> cacheGetter) throws ExoCacheInitException
   {
      if (config instanceof GenericExoCacheConfig)
      {
         final GenericExoCacheConfig gConfig = (GenericExoCacheConfig)config;
         return create(config, confBuilder, cacheGetter, gConfig.getStrategy(), gConfig.getMaxSize(),
            gConfig.getLiveTime(), gConfig.getMaxIdle() == 0 ? defaultMaxIdle : gConfig.getMaxIdle(),
            gConfig.getWakeUpInterval() == 0 ? defaultWakeUpInterval : gConfig.getWakeUpInterval());
      }
      else
      {
         final long period = config.getLiveTime();
         return create(config, confBuilder, cacheGetter,
            config.getImplementation() == null ? defaultStrategy : config.getImplementation(), config.getMaxSize(),
            period > 0 ? period * 1000 : -1, defaultMaxIdle, defaultWakeUpInterval);
      }
   }

   /**
    * Creates a new ExoCache instance with the relevant parameters
    * @throws ExoCacheInitException If any exception occurs while creating the cache
    */
   private ExoCache<Serializable, Object> create(ExoCacheConfig config, ConfigurationBuilder confBuilder,
      Callable<Cache<Serializable, Object>> cacheGetter, String strategy, int maxEntries, long lifespan, long maxIdle,
      long wakeUpInterval) throws ExoCacheInitException
   {
      EvictionStrategy es =
         strategy == null || strategy.length() == 0 ? null : EvictionStrategy.valueOf(strategy
            .toUpperCase(Locale.ENGLISH));
      if (es == null)
      {
         es = EvictionStrategy.LRU;
      }
      confBuilder.eviction().strategy(EvictionStrategy.valueOf(strategy)).maxEntries(maxEntries).expiration()
         .lifespan(lifespan).maxIdle(maxIdle).wakeUpInterval(wakeUpInterval);
      try
      {
         return new GenericExoCache(config, cacheGetter.call());
      }
      catch (Exception e)//NOSONAR
      {
         throw new ExoCacheInitException("Cannot create the cache '" + config.getName() + "'", e);
      }
   }

   /**
    * The Generic implementation of an ExoCache
    */
   public static class GenericExoCache extends AbstractExoCache<Serializable, Object>
   {

      public GenericExoCache(ExoCacheConfig config, Cache<Serializable, Object> cache)
      {
         super(config, cache);
      }

      public void setMaxSize(int max)
      {
         throw new UnsupportedOperationException("The configuration of the cache cannot not be modified");
      }

      public void setLiveTime(long period)
      {
         throw new UnsupportedOperationException("The configuration of the cache cannot not be modified");
      }

      public int getMaxSize()
      {
         return Math.toIntExact(cache.getCacheConfiguration().eviction().maxEntries());
      }

      public long getLiveTime()
      {
         return cache.getCacheConfiguration().expiration().lifespan();
      }

      @Managed
      @ManagedName("MaxIdle")
      @ManagedDescription("Maximum idle time a cache entry will be maintained in the cache. "
         + "If the idle time is exceeded, the entry will be expired cluster-wide. -1 means the entries never expire.")
      public long getMaxIdle()
      {
         return cache.getCacheConfiguration().expiration().maxIdle();
      }

      @Managed
      @ManagedName("WakeUpInterval")
      @ManagedDescription("Interval between subsequent eviction runs. If you wish to disable the periodic eviction "
         + "process altogether, set wakeupInterval to -1.")
      public long getWakeUpInterval()
      {
         return cache.getCacheConfiguration().expiration().wakeUpInterval();
      }
   }
}