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

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;

import java.util.HashSet;
import java.util.Set;

/**
 * Removes kernel namespace declaration from the document to not confuse the jibx thing.
 * It also filters the active profiles from the XML stream.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
class NoKernelNamespaceSAXFilter extends DefaultHandler
{

   /** . */
   private static final Log LOG = ExoLogger.getExoLogger(NoKernelNamespaceSAXFilter.class);

   /** . */
   private static final String XSI_URI = "http://www.w3.org/2001/XMLSchema-instance";

   /** . */
   private ContentHandler contentHandler;

   /** . */
   private final Set<String> blackListedPrefixes;

   NoKernelNamespaceSAXFilter(ContentHandler contentHandler)
   {
      this.contentHandler = contentHandler;
      this.blackListedPrefixes = new HashSet<String>();
   }

   public void setDocumentLocator(Locator locator)
   {
      contentHandler.setDocumentLocator(locator);
   }

   public void startDocument() throws SAXException
   {
      contentHandler.startDocument();
   }

   public void endDocument() throws SAXException
   {
      contentHandler.endDocument();
   }

   public void startPrefixMapping(String prefix, String uri) throws SAXException
   {
      if (Namespaces.isKernelNamespace(uri) || XSI_URI.equals(uri))
      {
         blackListedPrefixes.add(prefix);
         if (LOG.isTraceEnabled())
            LOG.trace("Black listing prefix " + prefix + " with uri " + uri);
      }
      else
      {
         contentHandler.startPrefixMapping(prefix, uri);
         if (LOG.isTraceEnabled())
            LOG.trace("Start prefix mapping " + prefix + " with uri " + uri);
      }
   }

   public void endPrefixMapping(String prefix) throws SAXException
   {
      if (!blackListedPrefixes.remove(prefix))
      {
         if (LOG.isTraceEnabled())
            LOG.trace("Ending prefix mapping " + prefix);
         contentHandler.endPrefixMapping(prefix);
      }
      else
      {
         if (LOG.isTraceEnabled())
            LOG.trace("Removed prefix mapping " + prefix + " from black list ");
      }
   }

   public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException
   {
      AttributesImpl noNSAtts = new AttributesImpl();
      for (int i = 0; i < atts.getLength(); i++)
      {
         String attQName = atts.getQName(i);
         if ((attQName.equals("xmlns")) && blackListedPrefixes.contains(""))
         {
            // Skip
            if (LOG.isTraceEnabled())
               LOG.trace("Skipping black listed xmlns attribute");
         }
         else if (attQName.startsWith("xmlns:") && blackListedPrefixes.contains(attQName.substring(6)))
         {
            // Skip
            if (LOG.isTraceEnabled())
               LOG.trace("Skipping black listed " + attQName + " attribute");
         }
         else
         {
            String attURI = atts.getURI(i);
            String attLocalName = atts.getLocalName(i);
            String attType = atts.getType(i);
            String attValue = atts.getValue(i);

            //
            if (XSI_URI.equals(attURI))
            {
               // Skip
               if (LOG.isTraceEnabled())
                  LOG.trace("Skipping XSI " + attQName + " attribute");
               continue;
            }
            else if (Namespaces.isKernelNamespace(attURI))
            {
               if (LOG.isTraceEnabled())
                  LOG.trace("Requalifying prefixed attribute " + attQName + " attribute to " + localName);
               attURI = null;
               attQName = localName;
            }

            //
            noNSAtts.addAttribute(attURI, attLocalName, attQName, attType, attValue);
         }
      }

      //
      if (Namespaces.isKernelNamespace(uri))
      {
         if (LOG.isTraceEnabled())
            LOG.trace("Requalifying active profile " + qName + " start element to " + localName);
         qName = localName;
         uri = null;
      }

      //
      contentHandler.startElement(uri, localName, qName, noNSAtts);
   }

   public void endElement(String uri, String localName, String qName) throws SAXException
   {
      if (Namespaces.isKernelNamespace(uri))
      {
         if (LOG.isTraceEnabled())
            LOG.trace("Requalifying " + qName + " end element");
         qName = localName;
         uri = null;
      }

      //
      if (LOG.isTraceEnabled())
         LOG.trace("Propagatting " + qName + " end element");
      contentHandler.endElement(uri, localName, qName);
   }

   public void characters(char[] ch, int start, int length) throws SAXException
   {
      contentHandler.characters(ch, start, length);
   }

   public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException
   {
      contentHandler.ignorableWhitespace(ch, start, length);
   }

   public void processingInstruction(String target, String data) throws SAXException
   {
      contentHandler.processingInstruction(target, data);
   }

   public void skippedEntity(String name) throws SAXException
   {
      contentHandler.skippedEntity(name);
   }
}
