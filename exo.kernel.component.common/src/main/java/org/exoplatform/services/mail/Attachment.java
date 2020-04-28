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
package org.exoplatform.services.mail;

import java.io.InputStream;

/**
 * Created by The eXo Platform SAS Author : Phung Hai Nam phunghainam@gmail.com
 * Dec 28, 2005
 */
public class Attachment
{
   private String name = null;

   private InputStream inputStream = null;

   private String mimeType = "";

   public InputStream getInputStream()
   {
      return inputStream;
   }

   public String getName()
   {
      return name;
   }

   public void setName(String name_)
   {
      this.name = name_;
   }

   public void setInputStream(InputStream value)
   {
      inputStream = value;
   }

   public String getMimeType()
   {
      return mimeType;
   }

   public void setMimeType(String value)
   {
      mimeType = value;
   }
}
