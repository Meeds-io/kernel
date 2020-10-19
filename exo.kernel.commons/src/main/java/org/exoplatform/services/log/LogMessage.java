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
package org.exoplatform.services.log;

/**
 * Created by the Exo Development team. Author : Tuan Nguyen
 * @version $Id: LogMessage.java 34394 2009-07-23 09:23:31Z dkatayev $
 */
public class LogMessage
{

   static public int FATAL = 0;

   static public int ERROR = 1;

   static public int WARN = 2;

   static public int INFO = 3;

   static public int DEBUG = 4;

   static public int TRACE = 5;

   private String name_;

   private int type_;

   private String message_;

   private String detail_;

   public LogMessage(String name, int type, String message, String detail)
   {
      name_ = name;
      type_ = type;
      message_ = message;
      detail_ = detail;
   }

   public String getName()
   {
      return name_;
   }

   public int getType()
   {
      return type_;
   }

   public String getMessage()
   {
      return message_;
   }

   public String getDetail()
   {
      return detail_;
   }

}
