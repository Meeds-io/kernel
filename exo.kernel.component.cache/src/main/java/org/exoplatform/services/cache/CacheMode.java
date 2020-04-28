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
package org.exoplatform.services.cache;

/**
 * Cache topology mode.
 */
public enum CacheMode
{
    /**
     * Data is not replicated.
     */
    NONE("", true, CacheMode.CACHE_MODE_LOCAL),

    /**
     * Data is not replicated.
     */
    LOCAL("local", true, CacheMode.CACHE_MODE_LOCAL),

    /**
     * Data replicated synchronously.
     */
    REPLICATION("replication", true, CacheMode.CACHE_MODE_REPLICATED),

    /**
     * Data replicated asynchronously.
     */
    ASYNCREPLICATION("asyncReplication", false, CacheMode.CACHE_MODE_REPLICATED),

    /**
     * Data invalidated asynchronously.
     */
    ASYNCINVALIDATION("asyncInvalidation", false, CacheMode.CACHE_MODE_INVALIDATED),

    /**
     * Data invalidated synchronously.
     */
    SYNCINVALIDATION("syncInvalidation", true, CacheMode.CACHE_MODE_INVALIDATED),

    /**
     * Synchronous DIST
     */
    DISTRIBUTED("distributed", true, CacheMode.CACHE_MODE_DISTRIBUTED);

    /**
     * replicated mode const
     */
    private final static int CACHE_MODE_REPLICATED = 0;

    /**
     * invalidated mode const
     */
    private final static int CACHE_MODE_INVALIDATED = 1;

    /**
     * distributed mode const
     */
    private final static int CACHE_MODE_DISTRIBUTED = 2;

    /**
     * local mode const
     */
    private final static int CACHE_MODE_LOCAL = 3;

    private final String name;

    private final boolean sync;

    private final int topology;

    CacheMode(String name, boolean sync, int topology)
    {
        this.name = name;
        this.sync = sync;
        this.topology = topology;
    }

    public boolean isReplicated()
    {
        return topology == CACHE_MODE_REPLICATED;
    }

    public boolean isInvalidated()
    {
        return topology == CACHE_MODE_INVALIDATED;
    }

    public boolean isDistributed()
    {
        return topology == CACHE_MODE_DISTRIBUTED;
    }

    public boolean isLocal()
    {
        return topology == CACHE_MODE_LOCAL;
    }

    public String getName()
    {
        return name;
    }

    public boolean isSync()
    {
        return sync;
    }

}
