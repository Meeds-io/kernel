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
package org.exoplatform.services.cache.invalidation;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * We use this class to propagate the hash code of the value efficiently over the network
 */
public final class  HashCode<V> implements Externalizable
{
    /**
     * The hash code of the value
     */
    private int hashCode;

    /**
     * The corresponding value
     */
    private V value;

    public HashCode() {}

    public HashCode(V value)
    {
        this.hashCode = value.hashCode();
        this.value = value;
    }

    /**
     * @return the value
     */
    public V getValue()
    {
        return value;
    }

    /**
     * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
     */
    public void writeExternal(ObjectOutput out) throws IOException
    {
        out.writeInt(hashCode);
    }

    /**
     * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
     */
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
    {
        this.hashCode = in.readInt();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return hashCode;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        @SuppressWarnings("rawtypes")
        HashCode other = (HashCode)obj;
        if (hashCode != other.hashCode)
            return false;
        if (value != null && other.value != null)
        {
            return value.equals(other.value);
        }
        return true;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "HashCode [hashCode=" + hashCode + ", value=" + value + "]";
    }
}
