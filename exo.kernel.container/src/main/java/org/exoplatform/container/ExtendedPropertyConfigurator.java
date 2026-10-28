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

import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.container.PropertyConfigurator;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.util.ContainerUtil;
import org.exoplatform.container.xml.Deserializer;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.container.xml.Property;
import org.exoplatform.container.xml.ValuesParam;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Map;
import org.picocontainer.Startable;

/**
 * Allow set system properties.
 * <br>
 * Example of configuration:
 * <pre>
 * {@code
 * <component>
 *   <key>ExtendedPropertyConfigurator</key>
 *   <type>org.exoplatform.container.ExtendedPropertyConfigurator</type>
 *   <init-params>
 *     <properties-param>
 *      <name>MyPropertySet1</name>
 *      <property name="myproperty" value="myvalue" />
 *     </properties-param>
 *     <properties-param profile="myProfile">
 *      <name>MyPropertySet1</name>
 *      <property name="myproperty" value="myvalue2" />
 *     </properties-param>
 *     <properties-param>
 *      <name>MyPropertySet1</name>
 *      <property name="myproperty2" value="myothervalue" />
 *     </properties-param>
 *     <values-param>
 *       <name>properties.urls</name>
 *       <value>cloud.properties</value>
 *       <value>cluster.properties</value>
 *     </values-param>
 *   </init-params>
 * </component>
 * }
 * </pre>
 * <br>
 * Note that <i>properties-param</i> entries and <i>properties.url</i> optional and can be omitted.
 * Note that if a path in <i>properties.url</i> doesn't exist it will be skipped with an info message
 * <br>
 * A property value of the form <i>secret-file:/path/to/file</i> in a file of <i>properties.urls</i>
 * is replaced by the content of that file (trailing whitespace removed), which allows to use
 * Docker secrets, e.g. <i>exo.db.password=secret-file:/run/secrets/exo_db_password</i>.
 * A missing or unreadable file is logged and the property is not set.
 *
 */
public class ExtendedPropertyConfigurator extends PropertyConfigurator implements Startable {

  /**
   * The logger.
   */
  private static final Log LOG = ExoLogger.getExoLogger(ExtendedPropertyConfigurator.class);

  /**
   * Prefix of a property value referencing a file (ex: a Docker secret) holding the real value.
   */
  public static final String SECRET_FILE_PREFIX = "secret-file:";

  /**
   * Constructor used by ExoContainer.
   *
   * @param params
   * @param confManager
   */
  public ExtendedPropertyConfigurator(InitParams params,
                                      ConfigurationManager confManager) {
    super(loadPropertiesParams(params), confManager);
    ValuesParam exts = params.getValuesParam("properties.urls");
    if (exts != null) {
      for (Object val : exts.getValues()) {
        String path = (String) val;
        if (path != null) {
          URL url = null;
          try {
            url = confManager.getURL(path);
            // Test if the Url is available before going further
            // We don't want an error reported by ContainerUtil.loadProperties when it doesn't exist
            url.openStream().close();
            LOG.info("Using property file " + url + " to set configuration properties.");
            Map<String, String> props = ContainerUtil.loadProperties(url);
            if (props != null) {
              for (Map.Entry<String, String> entry : props.entrySet()) {
                String propertyName = entry.getKey();
                String propertyValue = resolveSecretFile(propertyName, entry.getValue());
                if (propertyValue != null) {
                  PropertyManager.setProperty(propertyName, propertyValue);
                }
              }
            }
          } catch (FileNotFoundException fne) {
            LOG.info("Skipping configuration file " + path + ". File doesn't exist.");
          } catch (Exception e) {
            LOG.error("Cannot load extension property file " + path
                          + (url != null ? " resolved as " + url : ""), e);
          }
        }
      }
    }
  }

  /**
   * Replaces a <i>secret-file:</i> value by the content of the referenced file.
   * The content is never logged.
   * @param name the property name, used for logging only
   * @param value the raw property value
   * @return the value unchanged if it isn't a secret file reference, the file content
   *         without trailing whitespace otherwise, or null if the file cannot be read
   */
  static String resolveSecretFile(String name, String value) {
    if (value == null || !value.startsWith(SECRET_FILE_PREFIX)) {
      return value;
    }
    String path = value.substring(SECRET_FILE_PREFIX.length()).trim();
    try {
      String content = new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
      LOG.info("Loaded the value of property " + name + " from secret file " + path);
      return content.stripTrailing();
    } catch (IOException | RuntimeException e) {
      LOG.error("Cannot read secret file " + path + " for property " + name + ", the property is not set", e);
      return null;
    }
  }

  /**
   * Register all properties defined as <i>properties-param</i> in the {@link org.exoplatform.commons.utils.PropertyManager}.
   * <i>properties</i> entry isn't processed here but done by the parent {@link org.exoplatform.container.PropertyConfigurator}.
   * @param params The list of InitParams to register
   * @return The <i>params</i> list passed in parameter
   */
  private static InitParams loadPropertiesParams(InitParams params) {
    if (params != null) {
      Iterator<PropertiesParam> it = params.getPropertiesParamIterator();
      while (it.hasNext()) {
        PropertiesParam propertiesParam = it.next();
        LOG.debug("Going to initialize properties from init param");
        for (Iterator<Property> i = propertiesParam.getPropertyIterator(); i.hasNext(); ) {
          Property property = i.next();
          String name = property.getName();
          String value = Deserializer.resolveString(property.getValue());
          LOG.debug("Adding property from init param " + name + " = " + value);
          PropertyManager.setProperty(name, value);
        }
        if ("properties".equals(propertiesParam.getName()))
          it.remove();
      }
    }
    return params;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void start() {
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void stop() {
  }

}
