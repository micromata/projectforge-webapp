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

import org.apache.commons.lang.StringUtils;
import org.projectforge.common.StringHelper;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class LdapUtils
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LdapUtils.class);;

  private static final String ATTRIBUTE_SEPARATOR_CHAR = ",";

  public static String getOu(final String... organizationalUnit)
  {
    if (organizationalUnit == null) {
      return "";
    }
    final StringBuffer buf = new StringBuffer();
    buildOu(buf, organizationalUnit);
    return buf.toString();
  }

  public static void buildOu(final StringBuffer buf, final String... organizationalUnit)
  {
    if (organizationalUnit == null) {
      return;
    }
    boolean first = true;
    for (final String ou : organizationalUnit) {
      if (first == true) {
        first = false;
      } else {
        buf.append(',');
      }
      buf.append("ou=").append(ou);
    }
  }

  public static String[] getOrganizationalUnit(final String dn)
  {
    if (dn == null || dn.indexOf("ou=") < 0) {
      return null;
    }
    log.warn("getOrganizationalUnit(String) not yet implemented.");
    return null;
  }

  public static String[] getOrganizationalUnit(final String dn, final String ou)
  {
    if (StringUtils.isNotBlank(ou) == true) {
      return getOrganizationalUnit(dn + "," + ou);
    } else {
      return getOrganizationalUnit(dn);
    }
  }

  public static String getAttribute(final Attributes attributes, final String attrId) throws NamingException
  {
    final Attribute attr = attributes.get(attrId);
    if (attr == null) {
      return null;
    }
    return (String) attr.get();
  }

  public static Attribute putAttribute(final Attributes attributes, final String attrId, final String attrValue)
  {
    final Attribute attr = attributes.get(attrId);
    if (attrValue == null) {
      return attr;
    }
    if (attr == null) {
      return attributes.put(attrId, attrValue);
    }
    attr.add(attrValue);
    return attr;
  }

  /**
   * "customers,users" -> "ou=customers,ou=users".
   * @param attrId
   * @param value
   * @return
   */
  public static String splitMultipleAttribute(final String attrId, final String value)
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
