/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2013 Kai Reinhard (k.reinhard@micromata.de)
//
// ProjectForge is dual-licensed.
//
// This community edition is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License as published
// by the Free Software Foundation; version 3 of the License.
//
// This community edition is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
// Public License for more details.
//
// You should have received a copy of the GNU General Public License along
// with this program; if not, see http://www.gnu.org/licenses/.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.common;

import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

/**
 * 
 * @author Wolfgang Jung (W.Jung@micromata.de)
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class Crypt
{
  static String cryptoAlgorithm;

  private final static Logger log = Logger.getLogger(Crypt.class);

  /**
   * Encrypts the given str with AES. The password is first converted using SHA-256.
   * @param password
   * @param str
   * @return The base64 encoded result (url safe).
   */
  public static String encrypt(final String password, final String data)
  {
    if ("NONE".equals(getEncryptionAlgorithm()) == false) {
      try {
        final Cipher cipher = Cipher.getInstance(cryptoAlgorithm);
        final byte[] keyValue = getPassword(password);
        final Key key = new SecretKeySpec(keyValue, cryptoAlgorithm);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        final byte[] encVal = cipher.doFinal(data.getBytes("UTF-8"));
        final String encryptedValue = Base64.encodeBase64URLSafeString(encVal);
        return encryptedValue;
      } catch (final Exception ex) {
        log.error("Exception encountered while trying to encrypt with Algorithm 'AES' and the given password: " + ex.getMessage(), ex);
        return null;
      }
    }
    // Using base 64 at least:
    try {
      final String encryptedValue = Base64.encodeBase64URLSafeString(data.getBytes("UTF-8"));
      return encryptedValue;
    } catch (final UnsupportedEncodingException ex) {
      // Bummer, can't be true.
      throw new RuntimeException("UTF-8 not supported as encoding: " + ex.getMessage(), ex);
    }
  }

  /**
   * @param password
   * @param encryptedString
   * @return
   */
  public static String decrypt(final String password, final String encryptedString)
  {
    if ("NONE".equals(getEncryptionAlgorithm()) == false) {
      try {
        final Cipher cipher = Cipher.getInstance(cryptoAlgorithm);
        final byte[] keyValue = getPassword(password);
        final Key key = new SecretKeySpec(keyValue, cryptoAlgorithm);
        cipher.init(Cipher.DECRYPT_MODE, key);
        final byte[] decordedValue = Base64.decodeBase64(encryptedString);
        final byte[] decValue = cipher.doFinal(decordedValue);
        final String decryptedValue = new String(decValue);
        return decryptedValue;
      } catch (final Exception ex) {
        log.error("Exception encountered while trying to encrypt with Algorithm '"
            + cryptoAlgorithm
            + "' and the user's authentication token: "
            + ex.getMessage(), ex);
      }
    }
    // Trying to use base 64 at least:
    final byte[] decodedValue = Base64.decodeBase64(encryptedString);
    final String decryptedValue = new String(decodedValue);
    return decryptedValue;
  }

  private static byte[] getPassword(final String password)
  {
    try {
      final MessageDigest digester = MessageDigest.getInstance("SHA-256");
      digester.update(password.getBytes("UTF-8"));
      final byte[] key = digester.digest();
      log.info("Key length is " + key.length + ", cryptoAlgorithm is " + cryptoAlgorithm);
      if ("DES".equals(cryptoAlgorithm) == true) {
        final byte[] shortKey = new byte[8];
        for (int i = 0; i < 8; i++) {
          shortKey[i] = key[i];
        }
        return shortKey;
      }
      return key;
    } catch (final NoSuchAlgorithmException ex) {
      log.error("Exception encountered while trying to create a SHA-256 password: " + ex.getMessage(), ex);
      return null;
    } catch (final UnsupportedEncodingException ex) {
      log.error("Exception encountered while trying to get bytes in UTF-8: " + ex.getMessage(), ex);
      return null;
    }
  }

  static String getEncryptionAlgorithm()
  {
    if (cryptoAlgorithm != null) {
      return cryptoAlgorithm;
    }
    if (isAlgorithmAvailable("AES") == true) {
      cryptoAlgorithm = "AES";
    } else if (isAlgorithmAvailable("DES") == true) {
      cryptoAlgorithm = "DES";
    } else {
      log.warn("Weather AEs nor DES algorithm found. Can't use any crypto algorithm.");
      cryptoAlgorithm = "NONE";
    }
    return cryptoAlgorithm;
  }

  private static boolean isAlgorithmAvailable(final String algorithm)
  {
    try {
      Cipher.getInstance(algorithm);
      return true;
    } catch (final NoSuchAlgorithmException ex) {
      log.warn(algorithm + " encryption is not available in your Java runtime environment. Switching to more (unsafe) algorithms.");
      return false;
    } catch (final NoSuchPaddingException ex) {
      log.warn(algorithm + " encryption is not available in your Java runtime environment. Switching to more (unsafe) algorithms.");
      return false;
    }
  }

  /**
   * Encrypts the given String via SHA crypt algorithm.
   * @param s
   * @return
   */
  public static String digest(final String s)
  {
    return encode(s, "SHA");
  }

  public static String digest(final String s, final String alg)
  {
    return encode(s, alg);
  }

  public static boolean check(final String pass, final String encoded)
  {
    final String alg = encoded.substring(0, encoded.indexOf('{'));
    return encoded.equals(encode(pass, alg));
  }

  private static String encode(final String s, final String alg)
  {
    try {
      final MessageDigest md = MessageDigest.getInstance(alg);
      md.reset();
      md.update(s.getBytes());
      final byte[] d = md.digest();

      String ret = "";

      for (int val : d) {
        final char[] hex = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        if (val < 0) {
          val = 256 + val;
        }
        final char hi = hex[val / 16];
        final char lo = hex[val % 16];
        ret = hi + "" + lo + ret;
      }
      return md.getAlgorithm() + '{' + ret + '}';
    } catch (final NoSuchAlgorithmException ex) {
      log.fatal(ex);
      return "NONE{" + s + "}";
    }
  }
}
