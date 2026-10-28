/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.exoplatform.container;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;

import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.container.configuration.ConfigurationManagerImpl;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValuesParam;

import junit.framework.TestCase;

public class TestExtendedPropertyConfiguratorSecretFile extends TestCase {

  public void testResolveSecretFile() throws Exception {
    assertEquals("value", ExtendedPropertyConfigurator.resolveSecretFile("p", "value"));
    assertEquals("file:/tmp/x", ExtendedPropertyConfigurator.resolveSecretFile("p", "file:/tmp/x"));
    assertNull(ExtendedPropertyConfigurator.resolveSecretFile("p", null));
    assertNull(ExtendedPropertyConfigurator.resolveSecretFile("p", "secret-file:/does/not/exist"));
    File secret = File.createTempFile("secret", ".txt");
    try {
      Files.write(secret.toPath(), "s3cr3t pass\r\n".getBytes(StandardCharsets.UTF_8));
      assertEquals("s3cr3t pass", ExtendedPropertyConfigurator.resolveSecretFile("p", "secret-file:" + secret.getAbsolutePath()));
    } finally {
      secret.delete();
    }
  }

  public void testResolveSecretFileEdgeCases() throws Exception {
    File secret = File.createTempFile("secret", ".txt");
    try {
      // Only trailing whitespace is removed, inner and leading whitespace is kept
      Files.write(secret.toPath(), " a\nb \r\n\n".getBytes(StandardCharsets.UTF_8));
      assertEquals(" a\nb", ExtendedPropertyConfigurator.resolveSecretFile("p", "secret-file:" + secret.getAbsolutePath()));
      // Whitespace around the path is ignored
      assertEquals(" a\nb",
                   ExtendedPropertyConfigurator.resolveSecretFile("p", "secret-file: " + secret.getAbsolutePath() + " "));
      // An empty file gives an empty value
      Files.write(secret.toPath(), new byte[0]);
      assertEquals("", ExtendedPropertyConfigurator.resolveSecretFile("p", "secret-file:" + secret.getAbsolutePath()));
    } finally {
      secret.delete();
    }
    // A directory or an empty path cannot be read
    assertNull(ExtendedPropertyConfigurator.resolveSecretFile("p", "secret-file:" + System.getProperty("java.io.tmpdir")));
    assertNull(ExtendedPropertyConfigurator.resolveSecretFile("p", "secret-file:"));
    // The prefix is only recognized at the start of the value
    assertEquals("x secret-file:/a", ExtendedPropertyConfigurator.resolveSecretFile("p", "x secret-file:/a"));
  }

  public void testPropertiesFileWithSecretFile() throws Exception {
    File secret = File.createTempFile("secret", ".txt");
    File props = File.createTempFile("exo-test", ".properties");
    String key = "test.secret.file.password";
    String missing = "test.secret.file.missing";
    try {
      Files.write(secret.toPath(), "s3cr3t\n".getBytes(StandardCharsets.UTF_8));
      Files.write(props.toPath(),
                  (key + "=secret-file:" + secret.getAbsolutePath() + "\n" + missing + "=secret-file:/does/not/exist\n")
                      .getBytes(StandardCharsets.UTF_8));
      InitParams params = new InitParams();
      ValuesParam urls = new ValuesParam();
      urls.setName("properties.urls");
      urls.setValues(new ArrayList<>(Collections.singletonList(props.toURI().toURL().toString())));
      params.addParam(urls);
      new ExtendedPropertyConfigurator(params, new ConfigurationManagerImpl(null));
      assertEquals("s3cr3t", PropertyManager.getProperty(key));
      assertNull(PropertyManager.getProperty(missing));
    } finally {
      System.clearProperty(key);
      System.clearProperty(missing);
      secret.delete();
      props.delete();
    }
  }
}
