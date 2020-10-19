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
package org.exoplatform.container.configuration;

import junit.framework.TestCase;

import org.exoplatform.container.ContainerBuilder;
import org.exoplatform.container.RootContainer;

import java.io.File;
import java.io.FileFilter;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TestXSD_1_1 extends TestCase
{

   public void testValidation() throws Exception
   {
      ConfigurationUnmarshaller unmarshaller = new ConfigurationUnmarshaller();
      URL urlSampleConfig = getClass().getResource("../../../../xsd_1_1/sample-configuration-01.xml");
      File baseDir = new File(urlSampleConfig.toURI()).getParentFile();
      int count = 0;
      for (File f : baseDir.listFiles(new FileFilter()
      {
         public boolean accept(File pathname)
         {
            return pathname.getName().endsWith(".xml");
         }
      }))
      {
         count++;
         try
         {
            URL url = f.toURI().toURL();
            assertTrue("XML configuration file " + url + " is not valid", unmarshaller.isValid(url));
         }
         catch (MalformedURLException e)
         {
            fail("Was not expecting such exception " + e.getMessage());
         }
      }
      assertEquals(20, count);
      try
      {
         File f = new File(baseDir,"invalid-configuration.xml.bad");
         URL url = f.toURI().toURL();
         assertFalse("XML configuration file " + url + " is valid", unmarshaller.isValid(url));
      }
      catch (MalformedURLException e)
      {
         // Expected
      }
   }
   
   public void testInitParams() throws Exception
   {
      URL url = getClass().getResource("../../../../xsd_1_1/test-validation.xml");
      assertNotNull(url);
      RootContainer container = new ContainerBuilder().withRoot(url).build();
      container.getComponentInstanceOfType(TestValidation.class);
   }
}