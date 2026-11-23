/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2026 Meeds Association contact@meeds.io
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
package org.exoplatform.services.mail.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.services.mail.Message;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetup;

import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

/**
 * The optional DKIM signing of {@link MailServiceImpl}, with a real SMTP
 * exchange: what the server receives is checked against the specification.
 */
public class DkimMailServiceTest {

  private static final int    PORT   = 2626;

  @Rule
  public TemporaryFolder      folder = new TemporaryFolder();

  private GreenMail           smtp;

  private KeyPair             pair;

  private Path                keyFile;

  @Before
  public void startServer() throws Exception {
    smtp = new GreenMail(new ServerSetup(PORT, "127.0.0.1", ServerSetup.PROTOCOL_SMTP));
    smtp.start();
    pair = DkimTestSupport.generateKeyPair();
    keyFile = folder.newFile("dkim.pem").toPath();
    Files.writeString(keyFile, DkimTestSupport.pem(pair), StandardCharsets.US_ASCII);
  }

  @After
  public void stopServer() {
    smtp.stop();
  }

  private MailServiceImpl service(String... extra) throws Exception {
    PropertiesParam config = new PropertiesParam();
    config.setName("config");
    config.setProperty("mail.smtp.host", "127.0.0.1");
    config.setProperty("mail.smtp.port", String.valueOf(PORT));
    config.setProperty("mail.transport.protocol", "smtp");
    for (int i = 0; i < extra.length; i += 2) {
      config.setProperty(extra[i], extra[i + 1]);
    }
    InitParams params = new InitParams();
    params.addParam(config);
    return new MailServiceImpl(params, new ExoContainerContext(PortalContainer.getInstance()));
  }

  private MailServiceImpl signingService() throws Exception {
    return service("mail.dkim.enabled", "true",
                   "mail.dkim.domain", "example.org",
                   "mail.dkim.selector", "s1",
                   "mail.dkim.private-key", keyFile.toString());
  }

  private Message message(String from) {
    Message message = new Message();
    message.setFrom(from);
    message.setTo("to@example.com");
    message.setBCC("hidden@example.com");
    message.setSubject("Signed subject é");
    message.setBody("<p>Héllo wörld, a body long enough to be encoded and folded: " + "x".repeat(200) + "</p>");
    message.setMimeType("text/html");
    return message;
  }

  private String received(int index) {
    MimeMessage[] messages = smtp.getReceivedMessages();
    return GreenMailUtil.getWholeMessage(messages[index]).replaceAll("\r?\n", "\r\n");
  }

  @Test
  public void aMessageIsNotSignedByDefault() throws Exception {
    service().sendMessage(message("Sender <sender@example.org>"));
    assertTrue(smtp.waitForIncomingEmail(5000, 2));
    assertTrue(!received(0).contains("DKIM-Signature"));
  }

  @Test
  public void aMessageIsSignedWhenDkimIsEnabled() throws Exception {
    signingService().sendMessage(message("Sender <sender@example.org>"));
    assertTrue(smtp.waitForIncomingEmail(5000, 2));
    // To and Bcc recipients both receive the message
    assertEquals(2, smtp.getReceivedMessages().length);
    for (int i = 0; i < 2; i++) {
      assertNull(DkimTestSupport.verify(received(i), pair.getPublic()));
    }
    assertEquals("Signed subject é", smtp.getReceivedMessages()[0].getSubject());
    assertTrue(Arrays.stream(smtp.getReceivedMessages()).anyMatch(m -> {
      try {
        return m.getAllRecipients().length >= 1;
      } catch (Exception _) {
        return false;
      }
    }));
  }

  @Test
  public void theSignedHeadersCanBeChosen() throws Exception {
    service("mail.dkim.enabled", "true", "mail.dkim.domain", "example.org", "mail.dkim.selector", "s1",
            "mail.dkim.private-key", keyFile.toString(), "mail.dkim.headers", "subject, date ,x-absent").sendMessage(message("Sender <sender@example.org>"));
    assertTrue(smtp.waitForIncomingEmail(5000, 2));
    String source = received(0);
    assertNull(DkimTestSupport.verify(source, pair.getPublic()));
    String h = DkimTestSupport.signedHeaders(source);
    assertEquals("from:subject:date", h);
  }

  @Test
  public void anEightBitMimeSettingIsIgnoredWhileSigning() throws Exception {
    // A transport that re-encodes after the signature would invalidate it: the setting is forced off
    MailServiceImpl service = service("mail.dkim.enabled", "true", "mail.dkim.domain", "example.org",
                                      "mail.dkim.selector", "s1", "mail.dkim.private-key", keyFile.toString(),
                                      "mail.smtp.allow8bitmime", "true");
    assertEquals("false", service.getMailSession().getProperty("mail.smtp.allow8bitmime"));
    service.sendMessage(message("Sender <sender@example.org>"));
    assertTrue(smtp.waitForIncomingEmail(5000, 2));
    assertNull(DkimTestSupport.verify(received(0), pair.getPublic()));
  }

  @Test
  public void anEightBitMimeSettingOfTheSecureProtocolIsIgnoredWhileSigning() throws Exception {
    MailServiceImpl service = service("mail.dkim.enabled", "true", "mail.dkim.domain", "example.org",
                                      "mail.dkim.selector", "s1", "mail.dkim.private-key", keyFile.toString(),
                                      "mail.smtps.allow8bitmime", "true");
    assertEquals("false", service.getMailSession().getProperty("mail.smtps.allow8bitmime"));
  }

  @Test
  public void aBodyWithBareLineFeedsIsSignedAsItIsReceived() throws Exception {
    // The transport writes every bare LF as CRLF: the body hash must be the one of the received lines
    Message message = message("Sender <sender@example.org>");
    message.setBody("<html>\n<body>\n<p>Hello world</p>\n  \n</body>\n</html>");
    signingService().sendMessage(message);
    assertTrue(smtp.waitForIncomingEmail(5000, 2));
    assertNull(DkimTestSupport.verify(received(0), pair.getPublic()));
    assertTrue(received(0).contains("Content-Transfer-Encoding: 7bit"));
  }

  @Test
  public void aMimeMessageWithBareLineFeedsAndACarriageReturnIsSignedAsItIsReceived() throws Exception {
    MailServiceImpl service = signingService();
    MimeMessage mime = new MimeMessage(service.getMailSession());
    mime.setFrom(new InternetAddress("sender@example.org"));
    mime.setRecipients(jakarta.mail.Message.RecipientType.TO, "to@example.com");
    mime.setSubject("Plain");
    mime.setContent("line one\nline two\rline three\r\nline four", "text/plain; charset=utf-8");
    service.sendMessage(mime);
    assertTrue(smtp.waitForIncomingEmail(5000, 1));
    assertNull(DkimTestSupport.verify(received(0), pair.getPublic()));
  }

  @Test
  public void anEightBitMimeSettingIsKeptWhenNotSigning() throws Exception {
    MailServiceImpl service = service("mail.smtp.allow8bitmime", "true");
    assertEquals("true", service.getMailSession().getProperty("mail.smtp.allow8bitmime"));
  }

  @Test
  public void aMessageWithoutSessionIsSignedAndSentThroughTheServiceSession() throws Exception {
    MailServiceImpl service = signingService();
    MimeMessage mime = new MimeMessage((jakarta.mail.Session) null);
    mime.setFrom("sender@example.org");
    mime.setRecipients(jakarta.mail.Message.RecipientType.TO, "to@example.com");
    mime.setSubject("no session");
    mime.setText("text");
    service.sendMessage(mime);
    assertTrue(smtp.waitForIncomingEmail(5000, 1));
    assertNull(DkimTestSupport.verify(received(0), pair.getPublic()));
  }

  @Test
  public void aMimeMessageIsSignedToo() throws Exception {
    MailServiceImpl service = signingService();
    MimeMessage mime = new MimeMessage(service.getMailSession());
    mime.setFrom("sender@example.org");
    mime.setRecipients(jakarta.mail.Message.RecipientType.TO, "to@example.com");
    mime.setSubject("plain");
    mime.setText("plain text");
    service.sendMessage(mime);
    assertTrue(smtp.waitForIncomingEmail(5000, 1));
    assertNull(DkimTestSupport.verify(received(0), pair.getPublic()));
  }

  @Test
  public void aSignedMessageIsSentThroughTheSessionItWasCreatedWith() throws Exception {
    // The service points at a closed port: only the session of the message reaches the server
    MailServiceImpl service = service("mail.smtp.port", "1",
                                      "mail.dkim.enabled", "true",
                                      "mail.dkim.domain", "example.org",
                                      "mail.dkim.selector", "s1",
                                      "mail.dkim.private-key", keyFile.toString());
    java.util.Properties own = new java.util.Properties();
    own.put("mail.smtp.host", "127.0.0.1");
    own.put("mail.smtp.port", String.valueOf(PORT));
    MimeMessage mime = new MimeMessage(jakarta.mail.Session.getInstance(own));
    mime.setFrom("sender@example.org");
    mime.setRecipients(jakarta.mail.Message.RecipientType.TO, "to@example.com");
    mime.setSubject("own session");
    mime.setText("text");
    service.sendMessage(mime);
    assertTrue(smtp.waitForIncomingEmail(5000, 1));
    assertNull(DkimTestSupport.verify(received(0), pair.getPublic()));
  }

  @Test
  public void aMultipartMessageWithAnAttachmentIsSigned() throws Exception {
    byte[] binary = new byte[5000];
    new java.util.Random(7).nextBytes(binary);
    org.exoplatform.services.mail.Attachment attachment = new org.exoplatform.services.mail.Attachment();
    attachment.setName("data.bin");
    attachment.setMimeType("application/octet-stream");
    attachment.setInputStream(new java.io.ByteArrayInputStream(binary));
    Message message = message("Sender <sender@example.org>");
    message.addAttachment(attachment);
    signingService().sendMessage(message);
    assertTrue(smtp.waitForIncomingEmail(5000, 2));
    String source = received(0);
    assertTrue(source.contains("multipart/mixed"));
    assertTrue(source.contains("data.bin"));
    assertNull(DkimTestSupport.verify(source, pair.getPublic()));
  }

  @Test
  public void aMessageFromAnotherDomainIsSentUnsigned() throws Exception {
    signingService().sendMessage(message("Sender <sender@other.example>"));
    assertTrue(smtp.waitForIncomingEmail(5000, 2));
    assertTrue(!received(0).contains("DKIM-Signature"));
  }

  @Test
  public void aSubdomainOfTheSigningDomainIsSigned() throws Exception {
    signingService().sendMessage(message("Sender <sender@news.example.org>"));
    assertTrue(smtp.waitForIncomingEmail(5000, 2));
    assertNull(DkimTestSupport.verify(received(0), pair.getPublic()));
  }

  @Test
  public void enabledWithoutKeyFailsAtStartup() {
    IllegalStateException error = assertThrows(IllegalStateException.class,
                                               () -> service("mail.dkim.enabled", "true", "mail.dkim.domain", "example.org",
                                                             "mail.dkim.selector", "s1"));
    assertTrue(error.getMessage().contains("mail.dkim.private-key"));
  }

  @Test
  public void enabledWithAnUnreadableKeyFailsAtStartup() {
    String missing = folder.getRoot().toPath().resolve("missing.pem").toString();
    assertThrows(IllegalArgumentException.class,
                 () -> service("mail.dkim.enabled", "true", "mail.dkim.domain", "example.org", "mail.dkim.selector", "s1",
                               "mail.dkim.private-key", missing));
  }

  private MailServiceImpl strictService(Path key) throws Exception {
    return service("mail.dkim.enabled", "true", "mail.dkim.strict", "true", "mail.dkim.domain", "example.org",
                   "mail.dkim.selector", "s1", "mail.dkim.private-key", key.toString());
  }

  private void restrict(Path file, String permissions) throws Exception {
    Files.setPosixFilePermissions(file, java.nio.file.attribute.PosixFilePermissions.fromString(permissions));
  }

  @Test
  public void strictModeAcceptsARestrictedStrongKey() throws Exception {
    restrict(keyFile, "rw-------");
    strictService(keyFile).sendMessage(message("Sender <sender@example.org>"));
    assertTrue(smtp.waitForIncomingEmail(5000, 2));
    assertNull(DkimTestSupport.verify(received(0), pair.getPublic()));
  }

  @Test
  public void strictModeRefusesAKeyFileReadableByOthers() throws Exception {
    restrict(keyFile, "rw-r--r--");
    IllegalStateException error = assertThrows(IllegalStateException.class, () -> strictService(keyFile));
    assertTrue(error.getMessage().contains("mail.dkim.strict"));
    // The default, lenient mode starts with the same file
    restrict(keyFile, "rw-r--r--");
    signingService();
  }

  @Test
  public void strictModeRefusesAWeakKeyAndLenientModeAcceptsIt() throws Exception {
    java.security.KeyPairGenerator generator = java.security.KeyPairGenerator.getInstance("RSA");
    generator.initialize(1024);
    KeyPair weak = generator.generateKeyPair();
    Path weakFile = folder.newFile("weak.pem").toPath();
    Files.writeString(weakFile, DkimTestSupport.pem(weak), StandardCharsets.US_ASCII);
    restrict(weakFile, "rw-------");
    assertThrows(IllegalStateException.class, () -> strictService(weakFile));
    service("mail.dkim.enabled", "true", "mail.dkim.domain", "example.org", "mail.dkim.selector", "s1",
            "mail.dkim.private-key", weakFile.toString()).sendMessage(message("Sender <sender@example.org>"));
    assertTrue(smtp.waitForIncomingEmail(5000, 2));
    assertNull(DkimTestSupport.verify(received(0), weak.getPublic()));
  }

  @Test
  public void aKeyUnder1024BitsIsRefusedInBothModes() throws Exception {
    java.security.KeyPairGenerator generator = java.security.KeyPairGenerator.getInstance("RSA");
    generator.initialize(512);
    Path tiny = folder.newFile("tiny.pem").toPath();
    Files.writeString(tiny, DkimTestSupport.pem(generator.generateKeyPair()), StandardCharsets.US_ASCII);
    restrict(tiny, "rw-------");
    String tinyPath = tiny.toString();
    assertThrows(IllegalArgumentException.class, () -> strictService(tiny));
    assertThrows(IllegalArgumentException.class,
                 () -> service("mail.dkim.enabled", "true", "mail.dkim.domain", "example.org", "mail.dkim.selector", "s1",
                               "mail.dkim.private-key", tinyPath));
  }

  @Test
  public void aDomainSelectorOrHeaderListThatCannotBeSignedStopsTheStartup() {
    for (String[] bad : new String[][] { { "mail.dkim.selector", "bad selector" },
                                         { "mail.dkim.domain", "exa;mple.org" },
                                         { "mail.dkim.headers", "from,bcc" },
                                         { "mail.dkim.headers", "from,to:cc" } }) {
      String[] settings = { "mail.dkim.enabled", "true", "mail.dkim.domain", "example.org", "mail.dkim.selector", "s1",
                            "mail.dkim.private-key", keyFile.toString(), bad[0], bad[1] };
      assertThrows(bad[1], IllegalArgumentException.class, () -> service(settings));
    }
  }

  /** An RSA key that the JDK refuses to sign with. */
  private static final class BrokenKey implements java.security.PrivateKey, java.security.interfaces.RSAKey {
    private static final long serialVersionUID = 1L;

    private final java.math.BigInteger modulus;

    BrokenKey(java.math.BigInteger modulus) {
      this.modulus = modulus;
    }

    @Override
    public java.math.BigInteger getModulus() {
      return modulus;
    }

    @Override
    public java.security.spec.AlgorithmParameterSpec getParams() {
      return null;
    }

    @Override
    public String getAlgorithm() {
      return "RSA";
    }

    @Override
    public String getFormat() {
      return null;
    }

    @Override
    public byte[] getEncoded() {
      return null;
    }
  }

  private MailServiceImpl serviceWithBrokenSigner(boolean strict) throws Exception {
    MailServiceImpl service = service();
    DkimSigner broken = new DkimSigner("example.org", "s1", new BrokenKey(((java.security.interfaces.RSAKey) pair.getPrivate()).getModulus()), null);
    service.setDkimSigner(broken, strict);
    return service;
  }

  @Test
  public void lenientModeSendsAMailThatCannotBeSignedUnsigned() throws Exception {
    serviceWithBrokenSigner(false).sendMessage(message("Sender <sender@example.org>"));
    assertTrue(smtp.waitForIncomingEmail(5000, 2));
    assertTrue(!received(0).contains("DKIM-Signature"));
  }

  @Test
  public void strictModeDoesNotSendAMailThatCannotBeSigned() throws Exception {
    MailServiceImpl service = serviceWithBrokenSigner(true);
    Message mail = message("Sender <sender@example.org>");
    assertThrows(RuntimeException.class, () -> service.sendMessage(mail));
    assertEquals(0, smtp.getReceivedMessages().length);
  }

  @Test
  public void disabledIgnoresTheDkimSettings() throws Exception {
    service("mail.dkim.enabled", "false", "mail.dkim.private-key", "/does/not/exist").sendMessage(message("a@example.org"));
    assertTrue(smtp.waitForIncomingEmail(5000, 2));
  }
}
