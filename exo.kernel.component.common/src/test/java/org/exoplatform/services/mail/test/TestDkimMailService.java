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
package org.exoplatform.services.mail.test;

import junit.framework.TestCase;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.services.mail.impl.MailServiceImpl;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;

import java.io.File;
import java.io.FileOutputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;

import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

/**
 * Verifies that {@link MailServiceImpl} only signs outgoing mail with a
 * DKIM-Signature header when mail.dkim.enabled=true is explicitly set.
 */
public class TestDkimMailService extends TestCase
{
   private static final int SMTP_PORT = 2526;

   private GreenMail mailServer;

   private File privateKeyFile;

   public TestDkimMailService(String name)
   {
      super(name);
   }

   public void setUp() throws Exception
   {
      mailServer = new GreenMail(new ServerSetup(SMTP_PORT, null, ServerSetup.PROTOCOL_SMTP));
      mailServer.start();

      KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
      keyPairGenerator.initialize(2048);
      KeyPair keyPair = keyPairGenerator.generateKeyPair();
      // DkimSigner expects a raw PKCS#8 DER-encoded private key, not a PEM file
      privateKeyFile = File.createTempFile("dkim-test-key", ".der");
      privateKeyFile.deleteOnExit();
      try (FileOutputStream out = new FileOutputStream(privateKeyFile))
      {
         out.write(keyPair.getPrivate().getEncoded());
      }
   }

   public void tearDown() throws Exception
   {
      mailServer.stop();
      privateKeyFile.delete();
   }

   public void testSendMessageSignsWithDkimWhenEnabled() throws Exception
   {
      PropertiesParam config = baseConfig();
      config.setProperty("mail.dkim.enabled", "true");
      config.setProperty("mail.dkim.domain", "example.com");
      config.setProperty("mail.dkim.selector", "test");
      config.setProperty("mail.dkim.privateKeyPath", privateKeyFile.getAbsolutePath());
      config.setProperty("mail.dkim.checkDomainKey", "false");
      MailServiceImpl service = newMailService(config);

      sendTestMessage(service, "DKIM enabled test");
      Thread.sleep(500);

      MimeMessage[] received = mailServer.getReceivedMessages();
      assertEquals(1, received.length);
      String[] dkimHeaders = received[0].getHeader("DKIM-Signature");
      assertNotNull("Message should carry a DKIM-Signature header", dkimHeaders);
      assertTrue(dkimHeaders[0].contains("d=example.com"));
      assertTrue(dkimHeaders[0].contains("s=test"));
   }

   public void testSendMessageNotSignedWhenDkimDisabled() throws Exception
   {
      MailServiceImpl service = newMailService(baseConfig());

      sendTestMessage(service, "DKIM disabled test");
      Thread.sleep(500);

      MimeMessage[] received = mailServer.getReceivedMessages();
      assertEquals(1, received.length);
      assertNull("Message should not carry a DKIM-Signature header", received[0].getHeader("DKIM-Signature"));
   }

   private void sendTestMessage(MailServiceImpl service, String subject) throws Exception
   {
      MimeMessage message = new MimeMessage(service.getMailSession());
      message.setFrom(new InternetAddress("sender@example.com"));
      message.setRecipients(jakarta.mail.Message.RecipientType.TO, new InternetAddress[]{new InternetAddress("recipient@localhost")});
      message.setSubject(subject);
      message.setContent("DKIM test content", "text/plain");
      service.sendMessage(message);
   }

   private PropertiesParam baseConfig()
   {
      PropertiesParam config = new PropertiesParam();
      config.setName("config");
      config.setProperty("mail.smtp.host", "localhost");
      config.setProperty("mail.smtp.port", String.valueOf(SMTP_PORT));
      config.setProperty("mail.smtp.auth", "false");
      return config;
   }

   private MailServiceImpl newMailService(PropertiesParam config) throws Exception
   {
      InitParams params = new InitParams();
      params.addParameter(config);
      PortalContainer pcontainer = PortalContainer.getInstance();
      return new MailServiceImpl(params, pcontainer.getContext());
   }
}
