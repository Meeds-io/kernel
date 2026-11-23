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

import static org.junit.Assert.assertNotNull;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.Signature;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * A DKIM verifier written from RFC 6376 sections 3.4, 3.5 and 5.4. It is
 * written apart from the signer but by the same author, and its canonicalization
 * follows the same reading, so a misreading shared by both would pass: it does
 * not replace a check with an independent implementation such as dkimpy or
 * OpenDKIM.
 */
final class DkimTestSupport {

  private DkimTestSupport() {
    // Utility class
  }

  static KeyPair generateKeyPair() throws Exception {
    KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
    generator.initialize(2048);
    return generator.generateKeyPair();
  }

  static String pem(KeyPair pair) {
    return "-----BEGIN PRIVATE KEY-----\n"
        + Base64.getMimeEncoder(64, "\n".getBytes(StandardCharsets.US_ASCII)).encodeToString(pair.getPrivate().getEncoded())
        + "\n-----END PRIVATE KEY-----\n";
  }

  /**
   * @return {@code null} when the signature is valid, the reason otherwise
   */
  static String verify(String message, PublicKey publicKey) throws Exception {
    int blank = message.indexOf("\r\n\r\n");
    String headerSection = blank < 0 ? message : message.substring(0, blank + 2);
    String body = blank < 0 ? "" : message.substring(blank + 4);

    List<String[]> headers = new ArrayList<>();
    for (String line : headerSection.split("\r\n")) {
      if (line.startsWith(" ") || line.startsWith("\t")) {
        String[] last = headers.get(headers.size() - 1);
        last[1] = last[1] + "\r\n" + line;
      } else {
        int colon = line.indexOf(':');
        headers.add(new String[] { line.substring(0, colon), line.substring(colon + 1) });
      }
    }
    String[] dkim = headers.stream().filter(h -> h[0].equalsIgnoreCase("DKIM-Signature")).findFirst().orElse(null);
    assertNotNull("No DKIM-Signature header", dkim);

    Map<String, String> tags = new HashMap<>();
    for (String tag : dkim[1].replaceAll("\\s", "").split(";")) {
      int eq = tag.indexOf('=');
      if (eq > 0) {
        tags.put(tag.substring(0, eq), tag.substring(eq + 1));
      }
    }
    if (!"rsa-sha256".equals(tags.get("a")) || !"relaxed/relaxed".equals(tags.get("c")) || !"1".equals(tags.get("v"))) {
      return "unexpected v/a/c tags " + tags;
    }

    // Body hash, section 3.4.4 and 3.7
    String canonicalBody = relaxedBody(body);
    String bodyHash = Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-256").digest(
        canonicalBody.getBytes(StandardCharsets.ISO_8859_1)));
    if (!bodyHash.equals(tags.get("bh"))) {
      return "body hash mismatch: " + bodyHash + " vs " + tags.get("bh");
    }

    // Header hash, section 3.7 and 5.4.2: each name takes the next instance from the bottom
    StringBuilder data = new StringBuilder();
    List<String[]> pool = new ArrayList<>(headers);
    for (String name : tags.get("h").split(":")) {
      for (int i = pool.size() - 1; i >= 0; i--) {
        if (pool.get(i)[0].equalsIgnoreCase(name)) {
          data.append(relaxedHeader(pool.get(i)[0], pool.get(i)[1])).append("\r\n");
          pool.remove(i);
          break;
        }
      }
    }
    // The signature header itself, with the b= value empty and no trailing line end
    String withoutValue = dkim[1].replaceAll("(?s)(;\\s*b=)[^;]*$", "$1");
    data.append(relaxedHeader(dkim[0], withoutValue));

    Signature rsa = Signature.getInstance("SHA256withRSA");
    rsa.initVerify(publicKey);
    rsa.update(data.toString().getBytes(StandardCharsets.ISO_8859_1));
    return rsa.verify(Base64.getDecoder().decode(tags.get("b"))) ? null : "signature does not verify";
  }

  /**
   * @return the names in the h= tag of the DKIM-Signature header, without folding
   */
  static String signedHeaders(String message) {
    java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("[ ;]h=([^;]*);").matcher(message);
    if (!matcher.find()) {
      throw new AssertionError("No h= tag in " + message);
    }
    return matcher.group(1).replace("\r\n", "").replace(" ", "");
  }

  static String relaxedHeader(String name, String value) {
    return name.toLowerCase(Locale.ROOT) + ":" + value.replace("\r\n", "").replaceAll("[ \\t]+", " ").trim();
  }

  static String relaxedBody(String body) {
    List<String> lines = new ArrayList<>();
    for (String line : body.split("\r\n", -1)) {
      lines.add(line.replaceAll("[ \\t]+", " ").replaceAll(" $", ""));
    }
    while (!lines.isEmpty() && lines.get(lines.size() - 1).isEmpty()) {
      lines.remove(lines.size() - 1);
    }
    StringBuilder out = new StringBuilder();
    lines.forEach(l -> out.append(l).append("\r\n"));
    return out.toString();
  }
}
