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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

public class DkimSignerTest {

  private static KeyPair pair;

  // Line ends, trailing spaces and the tab are part of what the signature has to survive
  private static final String MESSAGE = """
      Received: from a.example (a.example [192.0.2.1])
      \tby mx.example; Fri, 20 Nov 2026 10:00:00 +0000
      From: Sender <sender@example.org>
      To: a@example.com,
       b@example.com
      Subject:  Hello   world\s
      Date: Fri, 20 Nov 2026 10:00:00 +0000
      Message-ID: <1@example.org>
      X-Unsigned: not in the default list

      Line one \s
      \tindented\t line


      """.replace("\n", "\r\n");

  @BeforeClass
  public static void generateKey() throws Exception {
    pair = DkimTestSupport.generateKeyPair();
  }

  private DkimSigner signer(String... headers) {
    return new DkimSigner("example.org", "s1", pair.getPrivate(), headers.length == 0 ? null : List.of(headers));
  }

  private String sign(String message, DkimSigner signer) {
    return new String(signer.sign(message.getBytes(StandardCharsets.ISO_8859_1)), StandardCharsets.ISO_8859_1);
  }

  @Test
  public void signatureVerifiesAgainstTheSpecification() throws Exception {
    String signed = sign(MESSAGE, signer());
    assertTrue(signed.startsWith("DKIM-Signature: v=1;"));
    assertTrue(signed.endsWith(MESSAGE));
    assertNull(DkimTestSupport.verify(signed, pair.getPublic()));
  }

  @Test
  public void aChangedBodyBreaksTheSignature() throws Exception {
    String signed = sign(MESSAGE, signer()).replace("Line one", "Line 1");
    assertEquals("body hash mismatch", DkimTestSupport.verify(signed, pair.getPublic()).substring(0, 18));
  }

  @Test
  public void aChangedSignedHeaderBreaksTheSignature() throws Exception {
    String signed = sign(MESSAGE, signer()).replace("Subject:  Hello", "Subject:  Bye");
    assertEquals("signature does not verify", DkimTestSupport.verify(signed, pair.getPublic()));
  }

  @Test
  public void aChangedUnsignedHeaderKeepsTheSignature() throws Exception {
    String signed = sign(MESSAGE, signer()).replace("not in the default list", "changed in transit");
    assertNull(DkimTestSupport.verify(signed, pair.getPublic()));
  }

  @Test
  public void whitespaceChangesInTransitAreTolerated() throws Exception {
    // relaxed canonicalization: folding, runs of spaces, trailing spaces and trailing empty lines
    String signed = sign(MESSAGE, signer()).replace("Subject:  Hello   world \r\n", "Subject: Hello world\r\n")
                                           .replace("Line one  \r\n", "Line one\r\n")
                                           .replace("a@example.com,\r\n b@example.com", "a@example.com, b@example.com")
                                           .replaceAll("\r\n\r\n$", "\r\n");
    assertNull(DkimTestSupport.verify(signed, pair.getPublic()));
  }

  @Test
  public void aWrongPublicKeyDoesNotVerify() throws Exception {
    String signed = sign(MESSAGE, signer());
    assertEquals("signature does not verify", DkimTestSupport.verify(signed, DkimTestSupport.generateKeyPair().getPublic()));
  }

  @Test
  public void fromIsAlwaysSignedAndAbsentHeadersAreSkipped() {
    String signed = sign(MESSAGE, signer("subject", "x-absent"));
    String h = DkimTestSupport.signedHeaders(signed);
    assertEquals("from:subject", h);
  }

  @Test
  public void aRepeatedHeaderIsSignedFromTheBottom() throws Exception {
    String twice = "To: first@example.com\r\n" + MESSAGE.replace("To: a@example.com,", "To: last@example.com,\r\n x,");
    String signed = sign(twice, signer("to", "from"));
    assertNull(DkimTestSupport.verify(signed, pair.getPublic()));
    // The first To, which is not covered, can change
    assertNull(DkimTestSupport.verify(signed.replace("first@example.com", "other@example.com"), pair.getPublic()));
  }

  @Test
  public void anEmptyBodyIsSigned() throws Exception {
    String signed = sign("From: a@example.org\r\nSubject: s\r\n\r\n", signer());
    assertNull(DkimTestSupport.verify(signed, pair.getPublic()));
    assertTrue(signed.contains("bh=47DEQpj8HBSa+/TImW+5JCeuQeRkm5NMpJWZG3hSuFU="));
  }

  @Test
  public void aMessageWithoutFromIsRefused() {
    DkimSigner signer = signer();
    assertThrows(IllegalArgumentException.class, () -> sign("Subject: s\r\n\r\nbody\r\n", signer));
  }

  @Test
  public void alignmentFollowsTheFromDomain() {
    DkimSigner signer = signer();
    assertTrue(signer.isAligned("From: a@example.org\r\n\r\n".getBytes(StandardCharsets.ISO_8859_1)));
    assertTrue(signer.isAligned("From: \"A\" <a@Mail.Example.ORG>\r\n\r\n".getBytes(StandardCharsets.ISO_8859_1)));
    assertFalse(signer.isAligned("From: a@notexample.org\r\n\r\n".getBytes(StandardCharsets.ISO_8859_1)));
    assertFalse(signer.isAligned("From: a@example.org.evil.com\r\n\r\n".getBytes(StandardCharsets.ISO_8859_1)));
    assertFalse(signer.isAligned("Subject: no from\r\n\r\n".getBytes(StandardCharsets.ISO_8859_1)));
  }

  @Test
  public void canonicalizationFollowsTheRfcExamples() {
    // RFC 6376 section 3.4.5
    assertEquals("a:X", DkimSigner.canonicalHeader("A", " X"));
    assertEquals("b:Y Z", DkimSigner.canonicalHeader("B ", " Y\t\r\n\tZ  "));
    assertEquals(" C\r\nD E\r\n", DkimSigner.canonicalBody(" C \r\nD \t E\r\n\r\n\r\n"));
    assertEquals("", DkimSigner.canonicalBody(""));
    assertEquals("", DkimSigner.canonicalBody("\r\n\r\n"));
  }

  @Test
  public void privateKeyParsing() {
    PrivateKey parsed = DkimSigner.parsePrivateKey(DkimTestSupport.pem(pair));
    assertNotNull(parsed);
    assertEquals(pair.getPrivate(), parsed);
    IllegalArgumentException pkcs1 = assertThrows(IllegalArgumentException.class,
                                                  () -> DkimSigner.parsePrivateKey("-----BEGIN RSA PRIVATE KEY-----\nAAAA\n-----END RSA PRIVATE KEY-----"));
    assertTrue(pkcs1.getMessage().contains("PKCS#1"));
    assertThrows(IllegalArgumentException.class, () -> DkimSigner.parsePrivateKey("not a key"));
  }

  @Test
  public void aKeyUnderTheRecommendedSizeIsAcceptedButWeak() throws Exception {
    assertEquals(2048, signer().getKeyBits());
    assertFalse(signer().isWeakKey());
    java.security.KeyPairGenerator generator = java.security.KeyPairGenerator.getInstance("RSA");
    generator.initialize(1024);
    DkimSigner weak = new DkimSigner("example.org", "s1", generator.generateKeyPair().getPrivate(), null);
    assertEquals(1024, weak.getKeyBits());
    assertTrue(weak.isWeakKey());
  }

  @Test
  public void aKeyFileReadableByOthersIsReported() throws Exception {
    java.nio.file.Path file = java.nio.file.Files.createTempFile("dkim", ".pem");
    try {
      java.nio.file.Files.setPosixFilePermissions(file, java.nio.file.attribute.PosixFilePermissions.fromString("rw-------"));
      assertFalse(DkimSigner.isReadableByOthers(file));
      java.nio.file.Files.setPosixFilePermissions(file, java.nio.file.attribute.PosixFilePermissions.fromString("rw-r-----"));
      assertTrue(DkimSigner.isReadableByOthers(file));
      java.nio.file.Files.setPosixFilePermissions(file, java.nio.file.attribute.PosixFilePermissions.fromString("rw----r--"));
      assertTrue(DkimSigner.isReadableByOthers(file));
      java.nio.file.Files.setPosixFilePermissions(file, java.nio.file.attribute.PosixFilePermissions.fromString("r--------"));
      assertFalse(DkimSigner.isReadableByOthers(file));
      java.nio.file.Files.delete(file);
      assertFalse(DkimSigner.isReadableByOthers(file));
    } finally {
      java.nio.file.Files.deleteIfExists(file);
    }
  }

  @Test
  public void anEncryptedKeyIsRefusedWithAClearMessage() {
    IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                                                  () -> DkimSigner.parsePrivateKey("-----BEGIN ENCRYPTED PRIVATE KEY-----\nAAAA\n-----END ENCRYPTED PRIVATE KEY-----"));
    assertTrue(error.getMessage().contains("encrypted"));
  }

  @Test
  public void aFromWithoutAnAddressIsNotAligned() {
    assertFalse(signer().isAligned("From: nobody\r\n\r\n".getBytes(StandardCharsets.ISO_8859_1)));
  }

  @Test
  public void aMessageWithoutBodySeparatorOrWithAMalformedHeaderLineIsStillSigned() throws Exception {
    // No blank line: the whole text is the header section and the body is empty
    String headersOnly = "From: a@example.org\r\nSubject: s\r\n";
    assertNull(DkimTestSupport.verify(sign(headersOnly + "\r\n", signer()), pair.getPublic()));
    String signed = sign(headersOnly, signer());
    assertTrue(signed.startsWith("DKIM-Signature:"));
    assertTrue(signed.endsWith(headersOnly));
    // A line without a colon is kept as a header with an empty value and does not break the signing
    assertTrue(sign("From: a@example.org\r\nnot a header\r\n\r\nbody\r\n", signer()).startsWith("DKIM-Signature:"));
  }

  @Test
  public void aKeyThatIsNotRsaOrTooSmallIsRefused() throws Exception {
    java.security.KeyPairGenerator small = java.security.KeyPairGenerator.getInstance("RSA");
    small.initialize(512);
    PrivateKey weak = small.generateKeyPair().getPrivate();
    assertThrows(IllegalArgumentException.class, () -> new DkimSigner("example.org", "s1", weak, null));
    PrivateKey strong = pair.getPrivate();
    assertThrows(IllegalArgumentException.class, () -> new DkimSigner(" ", "s1", strong, null));
    assertThrows(IllegalArgumentException.class, () -> new DkimSigner("example.org", null, strong, null));
  }

  @Test
  public void aDomainOrASelectorThatDnsRefusesIsRefused() {
    PrivateKey key = pair.getPrivate();
    for (String bad : new String[] { "exa mple.org", "example..org", "-example.org", "example-.org", "exämple.org", "example.org;", "a_b.org", "x".repeat(64) + ".org" }) {
      assertThrows(bad, IllegalArgumentException.class, () -> new DkimSigner(bad, "s1", key, null));
      assertThrows(bad, IllegalArgumentException.class, () -> new DkimSigner("example.org", bad, key, null));
    }
    assertEquals("xn--bcher-kva.example", new DkimSigner("xn--bcher-kva.example", "mail.2026-11", key, null).getDomain());
  }

  @Test
  public void aHeaderNameThatCannotBeSignedIsRefused() {
    PrivateKey key = pair.getPrivate();
    for (String bad : new String[] { "to:cc", "sub ject", "x;y", "bcc", "BCC", "é" }) {
      List<String> names = List.of("from", bad);
      assertThrows(bad, IllegalArgumentException.class, () -> new DkimSigner("example.org", "s1", key, names));
    }
    assertNotNull(signer("X-Custom_1.a"));
  }

  @Test
  public void everyLineEndIsTurnedIntoCrlf() {
    assertEquals("a\r\nb\r\nc\r\nd\r\n\r\n",
                 new String(DkimSigner.toCrlf("a\nb\r\nc\rd\n\r".getBytes(StandardCharsets.ISO_8859_1)), StandardCharsets.ISO_8859_1));
    assertEquals("", new String(DkimSigner.toCrlf(new byte[0]), StandardCharsets.ISO_8859_1));
  }

  @Test
  public void permissionsAreNotCheckableOnAFileSystemWithoutPosixPermissions() throws Exception {
    java.nio.file.Path zip = java.nio.file.Files.createTempFile("dkim", ".zip");
    java.nio.file.Files.delete(zip);
    try (java.nio.file.FileSystem fileSystem = java.nio.file.FileSystems.newFileSystem(zip, java.util.Map.of("create", "true"))) {
      java.nio.file.Path inZip = fileSystem.getPath("/key.pem");
      java.nio.file.Files.writeString(inZip, "x");
      assertFalse(DkimSigner.canCheckPermissions(inZip));
      assertFalse(DkimSigner.isReadableByOthers(inZip));
    } finally {
      java.nio.file.Files.deleteIfExists(zip);
    }
  }

  @Test
  public void aMissingKeyFileIsLeftToTheLoadingOfTheKey() throws Exception {
    java.nio.file.Path file = java.nio.file.Files.createTempFile("dkim", ".pem");
    assertTrue(DkimSigner.canCheckPermissions(file));
    java.nio.file.Files.delete(file);
    assertTrue(DkimSigner.canCheckPermissions(file));
    assertThrows(IllegalArgumentException.class, () -> DkimSigner.loadPrivateKey(file));
  }
}
