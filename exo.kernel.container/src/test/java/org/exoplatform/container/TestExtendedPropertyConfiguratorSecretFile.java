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
    assertEquals("value", ExtendedPropertyConfigurator.resolveSecretFile("p", "value", false));
    assertEquals("file:/tmp/x", ExtendedPropertyConfigurator.resolveSecretFile("p", "file:/tmp/x", false));
    assertNull(ExtendedPropertyConfigurator.resolveSecretFile("p", null, false));
    assertNull(ExtendedPropertyConfigurator.resolveSecretFile("p", "secret-file:/does/not/exist", false));
    File secret = File.createTempFile("secret", ".txt");
    try {
      Files.write(secret.toPath(), "s3cr3t pass\r\n".getBytes(StandardCharsets.UTF_8));
      assertEquals("s3cr3t pass", ExtendedPropertyConfigurator.resolveSecretFile("p", "secret-file:" + secret.getAbsolutePath(), false));
    } finally {
      secret.delete();
    }
  }

  public void testResolveSecretFileEdgeCases() throws Exception {
    File secret = File.createTempFile("secret", ".txt");
    try {
      // Only trailing line breaks are removed, inner, leading and trailing spaces are kept
      Files.write(secret.toPath(), " a\nb \r\n\n".getBytes(StandardCharsets.UTF_8));
      assertEquals(" a\nb ", ExtendedPropertyConfigurator.resolveSecretFile("p", "secret-file:" + secret.getAbsolutePath(), false));
      // Whitespace around the path is ignored
      assertEquals(" a\nb ",
                   ExtendedPropertyConfigurator.resolveSecretFile("p", "secret-file: " + secret.getAbsolutePath() + " ", false));
      // An empty file gives an empty value
      Files.write(secret.toPath(), new byte[0]);
      assertEquals("", ExtendedPropertyConfigurator.resolveSecretFile("p", "secret-file:" + secret.getAbsolutePath(), false));
    } finally {
      secret.delete();
    }
    // A directory or an empty path cannot be read
    assertNull(ExtendedPropertyConfigurator.resolveSecretFile("p", "secret-file:" + System.getProperty("java.io.tmpdir"), false));
    assertNull(ExtendedPropertyConfigurator.resolveSecretFile("p", "secret-file:", false));
    // The prefix is only recognized at the start of the value
    assertEquals("x secret-file:/a", ExtendedPropertyConfigurator.resolveSecretFile("p", "x secret-file:/a", false));
  }

  public void testPropertiesFileWithSecretFile() throws Exception {
    File secret = File.createTempFile("secret", ".txt");
    File props = File.createTempFile("exo-test", ".properties");
    String key = "test.secret.file.password";
    String missing = "test.secret.file.missing";
    String after = "test.secret.file.after";
    try {
      Files.write(secret.toPath(), "s3cr3t\n".getBytes(StandardCharsets.UTF_8));
      Files.write(props.toPath(),
                  (missing + "=secret-file:/does/not/exist\n" + key + "=secret-file:"
                      + secret.getAbsolutePath().replace('\\', '/') + "\n" + after + "=x\n")
                      .getBytes(StandardCharsets.UTF_8));
      InitParams params = new InitParams();
      ValuesParam urls = new ValuesParam();
      urls.setName("properties.urls");
      urls.setValues(new ArrayList<>(Collections.singletonList(props.toURI().toURL().toString())));
      params.addParam(urls);
      new ExtendedPropertyConfigurator(params, new ConfigurationManagerImpl(null));
      assertEquals("s3cr3t", PropertyManager.getProperty(key));
      assertNull(PropertyManager.getProperty(missing));
      // An unreadable secret must not drop the entries that follow it
      assertEquals("x", PropertyManager.getProperty(after));
    } finally {
      System.clearProperty(key);
      System.clearProperty(missing);
      System.clearProperty(after);
      secret.delete();
      props.delete();
    }
  }

  public void testStrictResolveSecretFile() {
    try {
      ExtendedPropertyConfigurator.resolveSecretFile("p", "secret-file:/does/not/exist", true);
      fail("An unreadable secret must fail in strict mode");
    } catch (ExtendedPropertyConfigurator.SecretFileException e) {
      assertTrue(e.getMessage().contains("/does/not/exist"));
    }
    // Strict mode doesn't change what a readable secret or a plain value gives
    assertEquals("value", ExtendedPropertyConfigurator.resolveSecretFile("p", "value", true));
  }

  public void testStrictPropertiesFileFailsStartup() throws Exception {
    File props = File.createTempFile("exo-test", ".properties");
    String earlier = "test.secret.file.strict.earlier";
    try {
      System.setProperty(earlier, "kept");
      // Default mode: the earlier value survives an unreadable secret
      Files.write(props.toPath(), (earlier + "=secret-file:/does/not/exist\n").getBytes(StandardCharsets.UTF_8));
      newConfigurator(props);
      assertEquals("kept", PropertyManager.getProperty(earlier));
      // Strict mode: the creation fails, whatever the per-file error handling
      PropertyManager.setProperty(ExtendedPropertyConfigurator.SECRET_FILE_STRICT_PROPERTY, "true");
      try {
        newConfigurator(props);
        fail("An unreadable secret must fail the startup in strict mode");
      } catch (ExtendedPropertyConfigurator.SecretFileException e) {
        assertTrue(e.getMessage().contains(earlier));
        assertEquals("kept", PropertyManager.getProperty(earlier));
      }
    } finally {
      PropertyManager.setProperty(ExtendedPropertyConfigurator.SECRET_FILE_STRICT_PROPERTY, "false");
      System.clearProperty(earlier);
      props.delete();
    }
  }

  public void testStrictFlagFromAnEarlierFile() throws Exception {
    File first = File.createTempFile("exo-test-first", ".properties");
    File second = File.createTempFile("exo-test-second", ".properties");
    String missing = "test.secret.file.flag.missing";
    try {
      // The flag and an unreadable secret in the same file: the flag is read before the file, so the entry is ignored
      Files.write(first.toPath(),
                  (ExtendedPropertyConfigurator.SECRET_FILE_STRICT_PROPERTY + "=true\n" + missing
                      + "=secret-file:/does/not/exist\n").getBytes(StandardCharsets.UTF_8));
      newConfigurator(first);
      assertNull(PropertyManager.getProperty(missing));
      PropertyManager.setProperty(ExtendedPropertyConfigurator.SECRET_FILE_STRICT_PROPERTY, "false");

      // The flag in a first file applies to the unreadable secret of a second one
      Files.write(first.toPath(),
                  (ExtendedPropertyConfigurator.SECRET_FILE_STRICT_PROPERTY + "=true\n").getBytes(StandardCharsets.UTF_8));
      Files.write(second.toPath(), (missing + "=secret-file:/does/not/exist\n").getBytes(StandardCharsets.UTF_8));
      try {
        newConfigurator(first, second);
        fail("An unreadable secret must fail the startup when an earlier file sets the strict flag");
      } catch (ExtendedPropertyConfigurator.SecretFileException e) {
        assertTrue(e.getMessage().contains(missing));
      }
    } finally {
      PropertyManager.setProperty(ExtendedPropertyConfigurator.SECRET_FILE_STRICT_PROPERTY, "false");
      System.clearProperty(missing);
      first.delete();
      second.delete();
    }
  }

  private static void newConfigurator(File... files) throws Exception {
    InitParams params = new InitParams();
    ValuesParam urls = new ValuesParam();
    urls.setName("properties.urls");
    ArrayList<String> values = new ArrayList<>();
    for (File file : files) {
      values.add(file.toURI().toURL().toString());
    }
    urls.setValues(values);
    params.addParam(urls);
    new ExtendedPropertyConfigurator(params, new ConfigurationManagerImpl(null));
  }
}
