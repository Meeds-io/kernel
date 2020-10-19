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
package org.exoplatform.services.idgenerator.impl;

import org.exoplatform.commons.utils.SecurityHelper;
import org.exoplatform.services.idgenerator.IDGeneratorService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.io.Serializable;
import java.net.InetAddress;
import java.security.PrivilegedAction;
import java.security.SecureRandom;
import java.util.Random;

/**
 * @author Tuan Nguyen (tuan08@users.sourceforge.net)
 * @since Oct 14, 2004
 * @version $Id: IDGeneratorServiceImpl.java 5332 2006-04-29 18:32:44Z geaz $
 */
public class IDGeneratorServiceImpl implements IDGeneratorService
{
   /**
    * The logger
    */
   private static final Log LOG = ExoLogger.getLogger("exo.kernel.component.common.IDGeneratorServiceImpl");

   private static String hexServerIP_;

   private static IntegerFormatter integerFormatter;

   static
   {
      SecurityHelper.doPrivilegedAction(new PrivilegedAction<Object>()
      {
         public Object run()
         {
            if ("IBM Corporation".equals(System.getProperty("java.vendor"))
               && "1.8.0".equals(System.getProperty("java.version")))
            {
               //IBM JDK 8 workaround KER-308
               integerFormatter = new J9IntegerFormatter();
            }
            else
            {
               integerFormatter = new IntegerFormatter();
            }
            return null;
         }
      });
      InetAddress localInetAddress = null;
      try
      {
         // get the inet address
         localInetAddress = InetAddress.getLocalHost();
         byte serverIP[] = localInetAddress.getAddress();
         hexServerIP_ = hexFormat(getInt(serverIP), 8);
      }
      catch (java.net.UnknownHostException uhe)
      {
         LOG.fatal(uhe.getLocalizedMessage(), uhe);
      }
   }

   private static final Random seeder_ = new SecureRandom();

   public IDGeneratorServiceImpl()
   {
      if (hexServerIP_ == null)
      {
         throw new IllegalStateException("The local inet address could not be found");
      }
   }

   public Serializable generateID(Object o)
   {
      return generateStringID(o);
   }

   public long generateLongID(Object o)
   {
      String uuid = generateStringID(o);
      return uuid.hashCode();
   }

   public int generatIntegerID(Object o)
   {
      String uuid = generateStringID(o);
      return uuid.hashCode();
   }

   public String generateStringID(Object o)
   {
      return generateStringID(o, System.currentTimeMillis(), hexServerIP_, seeder_.nextInt());
   }

   protected String generateStringID(Object o, long timeNow, String hexServerIP, int node)
   {
      StringBuilder guid = new StringBuilder(32);
      int timeLow = (int)timeNow & 0xFFFFFFFF;
      addHexFormat(guid, timeLow, 8);
      guid.append(hexServerIP);
      addHexFormat(guid, System.identityHashCode(o), 8);
      addHexFormat(guid, node, 8);
      return guid.toString();
   }

   private static int getInt(byte bytes[])
   {
      int i = 0;
      int j = 24;
      for (int k = 0; j >= 0; k++)
      {
         int l = bytes[k] & 0xff;
         i += l << j;
         j -= 8;
      }
      return i;
   }

   private static String hexFormat(int i, int j)
   {
      StringBuilder buffer = new StringBuilder(j);
      addHexFormat(buffer, i, j);
      return buffer.toString();
   }

   private static void addHexFormat(StringBuilder buffer, int i, int j)
   {
      String s = integerFormatter.toHexString(i);
      addPadHex(buffer, s, j);
      buffer.append(s);
   }

   private static void addPadHex(StringBuilder buffer, String s, int i)
   {
      int length = s.length();
      if (length < i)
      {
         for (int j = 0; j < i - length; j++)
         {
            buffer.append('0');
         }
      }
   }

   private static class IntegerFormatter
   {
      public String toHexString(int i)
      {
         return Integer.toHexString(i);
      }
   }

   private static class J9IntegerFormatter extends IntegerFormatter
   {
      public String toHexString(int i)
      {
         final String hex = Integer.toHexString(i);
         final int lastNul = hex.lastIndexOf('\u0000');
         if (lastNul >= 0)
         {
            return hex.substring(lastNul + 1);
         }
         else
         {
            return hex;
         }
      }
   }
}
