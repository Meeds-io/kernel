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

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.interfaces.RSAKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Signs a serialized RFC 5322 message with a DKIM signature (RFC 6376),
 * {@code rsa-sha256} with {@code relaxed/relaxed} canonicalization. It needs
 * only the JDK. The signature covers the bytes as they are written on the
 * wire, so the message must be serialized once, signed, and sent as is.
 */
public final class DkimSigner {

  /** Headers signed by default, when they are present in the message. */
  public static final List<String> DEFAULT_SIGNED_HEADERS =
                                                           List.of("from",
                                                                   "to",
                                                                   "cc",
                                                                   "subject",
                                                                   "date",
                                                                   "message-id",
                                                                   "reply-to",
                                                                   "mime-version",
                                                                   "content-type",
                                                                   "content-transfer-encoding",
                                                                   "list-unsubscribe",
                                                                   "list-unsubscribe-post");

  private static final int      MIN_RSA_BITS = 1024;

  /** Dot separated labels of letters, digits and hyphens: a domain name, an IDN in its A-label form, or a selector (RFC 6376 section 3.1). */
  private static final Pattern  DNS_LABEL    = Pattern.compile("[A-Za-z0-9]([A-Za-z0-9-]{0,61}[A-Za-z0-9])?");

  /**
   * A header field name made of letters, digits and the token characters
   * {@code !#$%&'*+.^_`|~-}. It is narrower than RFC 5322 on purpose: ':' and ';'
   * would end the name or its tag in the {@code h=} tag.
   */
  private static final Pattern  HEADER_NAME  = Pattern.compile("[A-Za-z0-9!#$%&'*+.^_`|~-]+");

  /** RFC 8301 section 3.2: signers must use at least 1024 bits and should use 2048 bits. */
  public static final int       RECOMMENDED_RSA_BITS = 2048;

  private static final int      FOLD_WIDTH   = 76;

  private static final int      SIGNATURE_LINE = 64;

  private static final String   CRLF         = "\r\n";

  private final String          domain;

  private final String          selector;

  private final PrivateKey      key;

  private final List<String>    headers;

  /**
   * Creates a signer for a domain and a selector.
   *
   * @param domain the signing domain, the {@code d=} tag, as dot separated
   *          labels of letters, digits and hyphens (an IDN in its A-label form)
   * @param selector the selector of the DNS record that holds the public key,
   *          written like a domain name
   * @param key an RSA private key of at least 1024 bits, the floor of RFC 8301
   *          section 3.2 that no mode relaxes
   * @param headers the names of the headers to sign when present, or
   *          {@code null} for {@link #DEFAULT_SIGNED_HEADERS}; From is always
   *          signed, and Bcc cannot be signed because the transport removes it
   *          before sending
   * @throws IllegalArgumentException when the domain, the selector, a header
   *           name or the key is not usable
   */
  public DkimSigner(String domain, String selector, PrivateKey key, List<String> headers) {
    if (domain == null || domain.isBlank() || selector == null || selector.isBlank()) {
      throw new IllegalArgumentException("DKIM needs a signing domain and a selector");
    }
    if (!isDnsName(domain.trim()) || !isDnsName(selector.trim())) {
      throw new IllegalArgumentException("The DKIM domain and selector must be dot separated labels of letters, digits and "
          + "hyphens (write an internationalized domain in its xn-- form)");
    }
    if (!(key instanceof RSAKey rsaKey) || rsaKey.getModulus().bitLength() < MIN_RSA_BITS) {
      throw new IllegalArgumentException("DKIM needs an RSA private key of at least " + MIN_RSA_BITS + " bits");
    }
    this.domain = domain.trim().toLowerCase(Locale.ROOT);
    this.selector = selector.trim();
    this.key = key;
    this.headers = signedHeaderNames(headers);
  }

  private static List<String> signedHeaderNames(List<String> requested) {
    List<String> names = new ArrayList<>();
    for (String header : requested == null || requested.isEmpty() ? DEFAULT_SIGNED_HEADERS : requested) {
      String name = header.trim().toLowerCase(Locale.ROOT);
      if (name.isEmpty()) {
        continue;
      }
      if (!HEADER_NAME.matcher(name).matches()) {
        throw new IllegalArgumentException("'" + header.trim() + "' is not a header name that can be signed");
      }
      if ("bcc".equals(name)) {
        throw new IllegalArgumentException("Bcc cannot be signed: the transport removes it before the message is sent");
      }
      if (!names.contains(name)) {
        names.add(name);
      }
    }
    if (!names.contains("from")) {
      names.add(0, "from"); // RFC 6376 section 5.4: From must always be signed
    }
    return List.copyOf(names);
  }

  /**
   * Reads an unencrypted RSA private key in PKCS#8 PEM format
   * ({@code -----BEGIN PRIVATE KEY-----}).
   *
   * @param file the PEM file
   * @return the key
   * @throws IllegalArgumentException when the file is not a PKCS#8 RSA key
   */
  public static PrivateKey loadPrivateKey(Path file) {
    try {
      return parsePrivateKey(Files.readString(file, StandardCharsets.US_ASCII));
    } catch (java.io.IOException e) {
      throw new IllegalArgumentException("Cannot read the DKIM private key " + file, e);
    }
  }

  private static boolean isDnsName(String value) {
    return value.length() <= 253 && Stream.of(value.split("\\.", -1)).allMatch(label -> DNS_LABEL.matcher(label).matches());
  }

  /**
   * Tells whether the group or the other users can read or write the file.
   *
   * @param file the private key file
   * @return {@code true} when the group or the others can read or write the
   *         file; {@code false} when only its owner can, or when the file
   *         system has no POSIX permissions to tell (see
   *         {@link #canCheckPermissions(Path)})
   */
  public static boolean isReadableByOthers(Path file) {
    try {
      Set<PosixFilePermission> permissions = Files.getPosixFilePermissions(file);
      return Stream.of(PosixFilePermission.GROUP_READ,
                       PosixFilePermission.GROUP_WRITE,
                       PosixFilePermission.OTHERS_READ,
                       PosixFilePermission.OTHERS_WRITE)
                   .anyMatch(permissions::contains);
    } catch (UnsupportedOperationException | java.io.IOException _) {
      return false;
    }
  }

  /**
   * Tells whether {@link #isReadableByOthers(Path)} can tell anything about the
   * file.
   *
   * @param file the private key file
   * @return {@code false} when the file system has no POSIX permissions, so that
   *         the permission check is skipped; {@code true} otherwise, also when the
   *         file cannot be read, which the loading of the key reports
   */
  public static boolean canCheckPermissions(Path file) {
    try {
      Files.getPosixFilePermissions(file);
    } catch (UnsupportedOperationException _) {
      return false;
    } catch (java.io.IOException _) {
      // A missing or unreadable file is reported with its real cause when the key is loaded
    }
    return true;
  }

  /**
   * Parses an unencrypted RSA private key in PKCS#8 PEM format.
   *
   * @param pem the content of the PEM file
   * @return the key
   * @throws IllegalArgumentException when it is not a PKCS#8 RSA key
   */
  public static PrivateKey parsePrivateKey(String pem) {
    if (pem.contains("BEGIN RSA PRIVATE KEY")) {
      throw new IllegalArgumentException("The DKIM private key is PKCS#1; convert it with "
          + "'openssl pkcs8 -topk8 -nocrypt -in key.pem -out key-pkcs8.pem'");
    }
    if (pem.contains("ENCRYPTED PRIVATE KEY")) {
      throw new IllegalArgumentException("The DKIM private key must not be encrypted");
    }
    String base64 = pem.replaceAll("-----[A-Z ]+-----", "").replaceAll("\\s", "");
    try {
      return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(base64)));
    } catch (InvalidKeySpecException | IllegalArgumentException | java.security.NoSuchAlgorithmException e) {
      throw new IllegalArgumentException("The DKIM private key is not a PKCS#8 RSA key", e);
    }
  }

  /**
   * Gives the size of the signing key.
   *
   * @return the size of the RSA modulus, in bits
   */
  public int getKeyBits() {
    return ((RSAKey) key).getModulus().bitLength();
  }

  /**
   * Tells whether the signing key is weaker than recommended.
   *
   * @return {@code true} when the key is accepted but shorter than
   *         {@link #RECOMMENDED_RSA_BITS}
   */
  public boolean isWeakKey() {
    return getKeyBits() < RECOMMENDED_RSA_BITS;
  }

  /**
   * @return the signing domain, the {@code d=} tag
   */
  public String getDomain() {
    return domain;
  }

  /**
   * @return the selector, the {@code s=} tag
   */
  public String getSelector() {
    return selector;
  }

  /**
   * Tells whether the From address of a message is in the signing domain.
   *
   * @param message a serialized message, with CRLF line ends
   * @return {@code true} when the From address belongs to the signing domain
   *         or one of its subdomains, so that the signature is aligned
   */
  public boolean isAligned(byte[] message) {
    String from = firstHeaderValue(parseHeaders(headerSection(message)), "from");
    if (from == null) {
      return false;
    }
    int at = from.lastIndexOf('@');
    if (at < 0) {
      return false;
    }
    String fromDomain = from.substring(at + 1).replaceAll("[>\\s\"].*$", "").toLowerCase(Locale.ROOT);
    return fromDomain.equals(domain) || fromDomain.endsWith("." + domain);
  }

  /**
   * Turns every lone CR and lone LF into CRLF, as the SMTP transport does when
   * it writes the message, so that a signature covers the bytes a receiver gets.
   *
   * @param message a serialized message
   * @return the message with CRLF line ends only
   */
  public static byte[] toCrlf(byte[] message) {
    ByteArrayOutputStream out = new ByteArrayOutputStream(message.length + 64);
    boolean afterCr = false;
    for (byte b : message) {
      if (b == '\r' || b == '\n' && !afterCr) {
        out.write('\r');
        out.write('\n');
      } else if (b != '\n') { // the LF of a CRLF is already written with its CR
        out.write(b);
      }
      afterCr = b == '\r';
    }
    return out.toByteArray();
  }

  /**
   * Signs a message.
   *
   * @param message a serialized message, with CRLF line ends (see
   *          {@link #toCrlf(byte[])})
   * @return the same message with a DKIM-Signature header in front of it
   */
  public byte[] sign(byte[] message) {
    return sign(message, Instant.now());
  }

  byte[] sign(byte[] message, Instant now) {
    int split = bodyStart(message);
    List<String[]> messageHeaders = parseHeaders(new String(message, 0, split, StandardCharsets.ISO_8859_1));
    if (firstHeaderValue(messageHeaders, "from") == null) {
      throw new IllegalArgumentException("A message without From header cannot be signed");
    }
    String body = canonicalBody(new String(message, split, message.length - split, StandardCharsets.ISO_8859_1));
    String bodyHash = Base64.getEncoder().encodeToString(sha256(body.getBytes(StandardCharsets.ISO_8859_1)));

    List<String> signedNames = new ArrayList<>();
    StringBuilder signedData = new StringBuilder();
    Set<Integer> used = new HashSet<>();
    for (String name : headers) {
      int index = lastUnusedHeader(messageHeaders, name, used);
      if (index >= 0) {
        used.add(index);
        signedNames.add(name);
        signedData.append(canonicalHeader(messageHeaders.get(index)[0], messageHeaders.get(index)[1])).append(CRLF);
      }
    }

    // Tokens are separated by a space where the header may be folded; the signature covers the header as it is written
    List<String> tokens = new ArrayList<>(List.of("v=1;", "a=rsa-sha256;", "c=relaxed/relaxed;", "d=" + domain + ";",
                                                  "s=" + selector + ";", "t=" + now.getEpochSecond() + ";"));
    for (int i = 0; i < signedNames.size(); i++) {
      boolean last = i == signedNames.size() - 1;
      tokens.add((i == 0 ? "h=" : "") + signedNames.get(i) + (last ? ";" : ":"));
    }
    tokens.add("bh=" + bodyHash + ";");
    tokens.add("b=");
    String unsigned = foldTokens("DKIM-Signature:", tokens);
    signedData.append(canonicalHeader("DKIM-Signature", unsigned.substring("DKIM-Signature:".length())));
    String signature = Base64.getEncoder().encodeToString(rsaSign(signedData.toString().getBytes(StandardCharsets.ISO_8859_1)));

    StringBuilder wrapped = new StringBuilder(unsigned);
    for (int i = 0; i < signature.length(); i += SIGNATURE_LINE) {
      wrapped.append(i == 0 ? "" : CRLF + " ").append(signature, i, Math.min(signature.length(), i + SIGNATURE_LINE));
    }
    ByteArrayOutputStream out = new ByteArrayOutputStream(message.length + 512);
    byte[] header = (wrapped + CRLF).getBytes(StandardCharsets.ISO_8859_1);
    out.write(header, 0, header.length);
    out.write(message, 0, message.length);
    return out.toByteArray();
  }

  private byte[] rsaSign(byte[] data) {
    try {
      Signature rsa = Signature.getInstance("SHA256withRSA");
      rsa.initSign(key);
      rsa.update(data);
      return rsa.sign();
    } catch (GeneralSecurityException e) {
      throw new IllegalStateException("The DKIM signature failed", e);
    }
  }

  private static byte[] sha256(byte[] data) {
    try {
      return MessageDigest.getInstance("SHA-256").digest(data);
    } catch (GeneralSecurityException e) {
      throw new IllegalStateException(e);
    }
  }

  /** Index just after the blank line that ends the header section. */
  private static int bodyStart(byte[] message) {
    for (int i = 0; i + 3 < message.length; i++) {
      if (message[i] == '\r' && message[i + 1] == '\n' && message[i + 2] == '\r' && message[i + 3] == '\n') {
        return i + 4;
      }
    }
    return message.length;
  }

  private static String headerSection(byte[] message) {
    return new String(message, 0, bodyStart(message), StandardCharsets.ISO_8859_1);
  }

  /** Each entry is {name, raw value with its folds}. */
  private static List<String[]> parseHeaders(String section) {
    List<String[]> result = new ArrayList<>();
    String[] lines = section.split("\r\n", -1);
    StringBuilder current = null;
    for (String line : lines) {
      if (line.isEmpty()) {
        break;
      }
      if ((line.charAt(0) == ' ' || line.charAt(0) == '\t') && current != null) {
        current.append(CRLF).append(line);
      } else {
        if (current != null) {
          result.add(splitHeader(current.toString()));
        }
        current = new StringBuilder(line);
      }
    }
    if (current != null) {
      result.add(splitHeader(current.toString()));
    }
    return result;
  }

  private static String[] splitHeader(String header) {
    int colon = header.indexOf(':');
    return colon < 0 ? new String[] { header, "" } : new String[] { header.substring(0, colon), header.substring(colon + 1) };
  }

  private static String firstHeaderValue(List<String[]> list, String name) {
    for (String[] header : list) {
      if (header[0].trim().equalsIgnoreCase(name)) {
        return header[1].replace(CRLF, "");
      }
    }
    return null;
  }

  /** Headers are chosen from the bottom up (RFC 6376 section 5.4.2). */
  private static int lastUnusedHeader(List<String[]> list, String name, Set<Integer> used) {
    for (int i = list.size() - 1; i >= 0; i--) {
      if (!used.contains(i) && list.get(i)[0].trim().equalsIgnoreCase(name)) {
        return i;
      }
    }
    return -1;
  }

  /** RFC 6376 section 3.4.2, the line end is added by the caller. */
  static String canonicalHeader(String name, String value) {
    String unfolded = value.replace(CRLF, "").replaceAll("[ \\t]+", " ").strip();
    return name.trim().toLowerCase(Locale.ROOT) + ":" + unfolded;
  }

  /** RFC 6376 section 3.4.4. */
  static String canonicalBody(String body) {
    StringBuilder out = new StringBuilder(body.length());
    List<String> cleaned = new ArrayList<>();
    for (String line : body.split("\r\n", -1)) {
      cleaned.add(line.replaceAll("[ \\t]+", " ").replaceAll(" $", ""));
    }
    int end = cleaned.size();
    while (end > 0 && cleaned.get(end - 1).isEmpty()) {
      end--;
    }
    for (int i = 0; i < end; i++) {
      out.append(cleaned.get(i)).append(CRLF);
    }
    return out.toString();
  }

  /** Joins the tokens with a space, or with a fold when the line would be too long. */
  private static String foldTokens(String name, List<String> tokens) {
    StringBuilder out = new StringBuilder(name);
    int lineLength = name.length();
    for (String token : tokens) {
      if (lineLength + 1 + token.length() > FOLD_WIDTH) {
        out.append(CRLF).append(' ').append(token);
        lineLength = 1 + token.length();
      } else {
        out.append(' ').append(token);
        lineLength += 1 + token.length();
      }
    }
    return out.toString();
  }
}
