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
package org.exoplatform.services.net.impl;

import org.exoplatform.commons.utils.SecurityHelper;
import org.exoplatform.services.net.NetService;

import java.net.Socket;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

/**
 * Created by The eXo Platform SAS Author : HoaPham phamvuxuanhoa@yahoo.com Jan
 * 10, 2006
 */
public class NetServiceImpl implements NetService
{

   public long ping(final String host, final int port) throws Exception
   {
      long startTime = 0;
      long endTime = 0;
      try
      {
         startTime = System.currentTimeMillis();
         Socket socket = SecurityHelper.doPrivilegedExceptionAction(new PrivilegedExceptionAction<Socket>()
         {
            public Socket run() throws Exception
            {
               return new Socket(host, port);
            }
         });
         endTime = System.currentTimeMillis();
         socket.close();
      }
      catch (PrivilegedActionException e)
      {
         // e.printStackTrace() ;
         return -1;
      }
      return endTime - startTime;
   }
}
