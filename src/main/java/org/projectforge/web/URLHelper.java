/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2010 Kai Reinhard (k.reinhard@me.com)
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

package org.projectforge.web;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class URLHelper
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(URLHelper.class);

  /**
   * Uses UTF-8
   * @param str
   * @see URLEncoder#encode(String, String)
   */
  public static String encode(final String str)
  {
    if (str == null) {
      return "";
    }
    try {
      return URLEncoder.encode(str, "UTF-8");
    } catch (UnsupportedEncodingException ex) {
      log.info("Can't URL-encode '" + str + "': " + ex.getMessage());
      return "";
    }
  }
}
