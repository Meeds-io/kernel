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

import org.exoplatform.container.RootContainer;
import org.exoplatform.container.xml.Component;
import org.exoplatform.container.xml.Configuration;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ObjectParameter;
import org.exoplatform.xml.object.XMLCollection;
import org.exoplatform.xml.object.XMLField;
import org.exoplatform.xml.object.XMLObject;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TestField extends AbstractProfileTest
{

   public void testNoProfile() throws Exception
   {
      XMLCollection xc = getConfiguredCollection();
      assertEquals(Arrays.asList("manager"), xc.getCollection());
   }

   public void testNoProfileKernel() throws Exception
   {
      List l = getCollection();
      assertEquals(Arrays.asList("manager"), l);
   }

   public void testFooProfile() throws Exception
   {
      XMLCollection xc = getConfiguredCollection("foo");
      assertEquals(Arrays.asList("foo_manager"), xc.getCollection());
   }

   public void testFooProfileKernel() throws Exception
   {
      List l = getCollection("foo");
      assertEquals(Arrays.asList("foo_manager"), l);
   }

   public void testBarProfile() throws Exception
   {
      XMLCollection xc = getConfiguredCollection("bar");
      assertEquals(Arrays.asList("foo_bar_manager"), xc.getCollection());
   }

   public void testBarProfileKernel() throws Exception
   {
      List l = getCollection("bar");
      assertEquals(Arrays.asList("foo_bar_manager"), l);
   }

   public void testFooBarProfile() throws Exception
   {
      XMLCollection xc = getConfiguredCollection("foo", "bar");
      assertEquals(Arrays.asList("foo_manager"), xc.getCollection());
   }

   public void testFooBarProfileKernel() throws Exception
   {
      List l = getCollection("foo", "bar");
      assertEquals(Arrays.asList("foo_manager"), l);
   }

   private XMLCollection getConfiguredCollection(String... profiles)
      throws Exception
   {
      Configuration config = getConfiguration("field-configuration.xml", profiles);
      Component a = config.getComponent(InitParamsHolder.class.getName());
      ObjectParameter op = a.getInitParams().getObjectParam("test.configuration");
      XMLObject o = op.getXMLObject();
      XMLField xf = o.getField("role");
      return xf.getCollection();
   }

   private List getCollection(String... profiles)
      throws Exception
   {
      RootContainer config = getKernel("field-configuration.xml", profiles);
      InitParamsHolder holder = (InitParamsHolder)config.getComponentInstanceOfType(InitParamsHolder.class);
      InitParams params = holder.getParams();
      ObjectParameter op = params.getObjectParam("test.configuration");
      ConfigParam cp = (ConfigParam)op.getObject();
      return cp.getRole();
   }
}