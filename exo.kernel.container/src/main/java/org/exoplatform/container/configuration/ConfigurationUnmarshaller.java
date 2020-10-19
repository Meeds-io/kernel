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

import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.commons.utils.SecurityHelper;
import org.exoplatform.container.util.Utils;
import org.exoplatform.container.xml.Configuration;
import org.exoplatform.container.xml.Deserializer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;
import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Collections;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

/**
 * Unmarshall a configuration.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ConfigurationUnmarshaller
{

   private static final Log LOG = ExoLogger.getLogger("exo.kernel.container.ConfigurationUnmarshaller");

   /**
    * A private copy of the list of kernel namespaces
    */
   private static final String[] KERNEL_NAMESPACES = Namespaces.getKernelNamespaces();

   private class Reporter implements ErrorHandler
   {

      private final URL url;

      private boolean valid;

      private Reporter(URL url)
      {
         this.url = url;
         this.valid = true;
      }

      public void warning(SAXParseException exception) throws SAXException
      {
         LOG.warn(exception.getMessage(), exception);
      }

      public void error(SAXParseException exception) throws SAXException
      {
         if (exception.getMessage().equals("cvc-elt.1: Cannot find the declaration of element 'configuration'."))
         {
            LOG.info("The document "
               + url
               + " does not contain a schema declaration, it should have an "
               + "XML declaration similar to\n"
               + "<configuration\n"
               + "   xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
                     + "   xsi:schemaLocation=\"http://www.exoplatform.org/xml/ns/kernel_1_1.xsd "
                     + "http://www.exoplatform.org/xml/ns/kernel_1_1.xsd\"\n"
               + "   xmlns=\"http://www.exoplatform.org/xml/ns/kernel_1_1.xsd\">");
         }
         else
         {
            LOG.error("In document " + url + "  at (" + exception.getLineNumber() + "," + exception.getColumnNumber()
               + ") :" + exception.getMessage());
         }
         valid = false;
      }

      public void fatalError(SAXParseException exception) throws SAXException
      {
         LOG.fatal("In document " + url + "  at (" + exception.getLineNumber() + "," + exception.getColumnNumber()
            + ") :" + exception.getMessage());
         valid = false;
      }
   }

   /** . */
   private final Set<String> profiles;

   public ConfigurationUnmarshaller(Set<String> profiles)
   {
      this.profiles = profiles;
   }

   public ConfigurationUnmarshaller()
   {
      this.profiles = Collections.emptySet();
   }

   /**
    * Returns true if the configuration file is valid according to its schema declaration. If the file
    * does not have any schema declaration, the file will be reported as valid.
    *
    * @param url the url of the configuration to validate
    * @return true if the configuration file is valid
    * @throws IOException any IOException thrown by using the provided URL
    * @throws NullPointerException if the provided URL is null
    */
   public boolean isValid(final URL url) throws NullPointerException, IOException
   {
      final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory
         .setAttribute("http://java.sun.com/xml/jaxp/properties/schemaLanguage", "http://www.w3.org/2001/XMLSchema");
      factory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaSource", KERNEL_NAMESPACES);
      factory.setNamespaceAware(true);
      factory.setValidating(true);
      return SecurityHelper.doPrivilegedIOExceptionAction(new PrivilegedExceptionAction<Boolean>()
      {
         public Boolean run() throws Exception
         {
            try
            {
               DocumentBuilder builder = factory.newDocumentBuilder();
               Reporter reporter = new Reporter(url);
               builder.setErrorHandler(reporter);
               builder.setEntityResolver(Namespaces.resolver);
               String content = Deserializer.resolveVariables(Utils.readStream(url.openStream()));
               InputSource is = new InputSource(new StringReader(content));
               builder.parse(is);
               return reporter.valid;
            }
            catch (ParserConfigurationException e)
            {
               LOG.error("Got a parser configuration exception when doing XSD validation");
               return false;
            }
            catch (SAXException e)
            {
               LOG.error("Got a sax exception when doing XSD validation");
               return false;
            }
         }
      });
   }
   
   public Configuration unmarshall(final URL url) throws Exception
   {
      if (PropertyManager.isDevelopping())
      {
         boolean valid = isValid(url);
         if (!valid)
         {
            LOG.info("The configuration file " + url + " was not found valid according to its XSD");
         }
      }

      //
      DocumentBuilderFactory factory = null;
      try
      {
         // With Java 6, it's safer to precise the builder factory class name as it may result:
         // java.lang.AbstractMethodError: org.apache.xerces.dom.DeferredDocumentImpl.getXmlStandalone()Z
         // at com.sun.org.apache.xalan.internal.xsltc.trax.DOM2TO.setDocumentInfo(Unknown Source) 
         Method dbfniMethod = DocumentBuilderFactory.class.getMethod("newInstance", String.class, ClassLoader.class);
         factory =
            (DocumentBuilderFactory)dbfniMethod.invoke(null,
               "com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl", Thread.currentThread()
                  .getContextClassLoader());
      }
      catch (InvocationTargetException e)
      {
         Throwable cause = e.getCause();
         if (cause instanceof FactoryConfigurationError)
         {
            // do nothing and let try to instantiate later
            LOG.debug("Was not able to find document builder factory class in Java > 5, will use default", cause);
         }
         else
         {
            // Rethrow
            throw e;
         }
      }
      catch (NoSuchMethodException e)
      {
         if (LOG.isTraceEnabled())
         {
            LOG.trace("An exception occurred: " + e.getMessage());
         }
      }

      //
      if (factory == null)
      {
         factory = DocumentBuilderFactory.newInstance();
      }

      //
      factory.setNamespaceAware(true);

      final DocumentBuilderFactory builderFactory = factory;
      try
      {
         return SecurityHelper.doPrivilegedExceptionAction(new PrivilegedExceptionAction<Configuration>()
         {
            public Configuration run() throws Exception
            {
               DocumentBuilder builder = builderFactory.newDocumentBuilder();
               Document doc = builder.parse(url.openStream());

               // Filter DOM
               ProfileDOMFilter filter = new ProfileDOMFilter(profiles);
               filter.process(doc.getDocumentElement());

               // SAX event stream -> String
               StringWriter buffer = new StringWriter();
               SAXTransformerFactory tf = (SAXTransformerFactory)SAXTransformerFactory.newInstance();
               TransformerHandler hd = tf.newTransformerHandler();
               StreamResult result = new StreamResult(buffer);
               hd.setResult(result);
               Transformer serializer = tf.newTransformer();
               serializer.setOutputProperty(OutputKeys.ENCODING, "UTF8");
               serializer.setOutputProperty(OutputKeys.INDENT, "yes");

               // Transform -> SAX event stream
               SAXResult saxResult = new SAXResult(new NoKernelNamespaceSAXFilter(hd));

               // DOM -> Transform
               serializer.transform(new DOMSource(doc), saxResult);

               // Reuse the parsed document
               String document = buffer.toString();

               // Debug
               if (LOG.isTraceEnabled())
                  LOG.trace("About to parse configuration file " + document);

               //
               IBindingFactory bfact = BindingDirectory.getFactory(Configuration.class);
               IUnmarshallingContext uctx = bfact.createUnmarshallingContext();

               return (Configuration)uctx.unmarshalDocument(new StringReader(document), null);
            }
         });
      }
      catch (PrivilegedActionException pae)
      {
         Throwable cause = pae.getCause();
         if (cause instanceof JiBXException)
         {
            throw (JiBXException)cause;
         }
         else if (cause instanceof ParserConfigurationException)
         {
            throw (ParserConfigurationException)cause;
         }
         else if (cause instanceof IOException)
         {
            throw (IOException)cause;
         }
         else if (cause instanceof SAXException)
         {
            throw (SAXException)cause;
         }
         else if (cause instanceof IllegalArgumentException)
         {
            throw (IllegalArgumentException)cause;
         }
         else if (cause instanceof TransformerException)
         {
            throw (TransformerException)cause;
         }
         else if (cause instanceof TransformerConfigurationException)
         {
            throw (TransformerConfigurationException)cause;
         }
         else if (cause instanceof TransformerFactoryConfigurationError)
         {
            throw (TransformerFactoryConfigurationError)cause;
         }
         else if (cause instanceof RuntimeException)
         {
            throw (RuntimeException)cause;
         }
         else
         {
            throw new RuntimeException(cause);
         }
      }
   }
}
