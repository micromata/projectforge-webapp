/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2012 Kai Reinhard (k.reinhard@micromata.de)
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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.log4j.Logger;

/**
 * 
 * @author Wolfgang Jung (W.Jung@micromata.de)
 * 
 */
public class Crypt
{

  private final static Logger log = Logger.getLogger(Crypt.class);

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
