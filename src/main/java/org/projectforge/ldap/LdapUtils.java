/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2012 Kai Reinhard (k.reinhard@micromata.com)
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

package org.projectforge.ldap;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

import org.projectforge.common.StringHelper;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class LdapUtils
{
  private static final String ATTRIBUTE_SEPARATOR_CHAR = ",";

  public static String getAttribute(final Attributes attributes, final String attrId) throws NamingException
  {
    final Attribute attr = attributes.get(attrId);
    if (attr == null) {
      return null;
    }
    return (String) attr.get();
  }

  public static void putAttribute(final Attributes attributes, final String attrId, final String attrValue)
  {
    if (attrValue == null) {
      return;
    }
    attributes.put(attrId, attrValue);
  }

  /**
   * separator
   * @param attrId
   * @param value
   * @return
   */
  public static String buildAttribute(final String attrId, final String value)
  {
    if (value == null) {
      return null;
    }
    final int pos = value.indexOf(ATTRIBUTE_SEPARATOR_CHAR);
    if (pos < 0) {
      return attrId + "=" + value;
    }
    final String[] strs = StringHelper.splitAndTrim(value, ATTRIBUTE_SEPARATOR_CHAR);
    final StringBuffer buf = new StringBuffer();
    boolean first = true;
    for (final String str : strs) {
      if (first == true) {
        first = false;
      } else {
        buf.append(", ");
      }
      buf.append(attrId).append('=').append(str);
    }
    return buf.toString();
  }
}
