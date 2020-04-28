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
package org.exoplatform.services.cache.impl.memcached;

import net.spy.memcached.BinaryConnectionFactory;
import net.spy.memcached.ConnectionFactory;
import net.spy.memcached.DefaultConnectionFactory;
import net.spy.memcached.DefaultHashAlgorithm;

/**
 * The {@link ConnectionFactoryCreator} that will create {@link BinaryConnectionFactory} instances
 * 
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public class BinaryConnectionFactoryCreator implements ConnectionFactoryCreator
{
   /**
    * The length of the queue
    */
   protected int queueLength = DefaultConnectionFactory.DEFAULT_OP_QUEUE_LEN;

   /**
    * The buffer size
    */
   protected int bufferSize = DefaultConnectionFactory.DEFAULT_READ_BUFFER_SIZE;

   /**
    * The algorithm to use for hashing
    */
   protected String hash;

   /**
    * {@inheritDoc}
    */
   public ConnectionFactory create()
   {
      return new BinaryConnectionFactory(queueLength, bufferSize, hash == null ? DefaultConnectionFactory.DEFAULT_HASH
         : DefaultHashAlgorithm.valueOf(hash));
   }
}
