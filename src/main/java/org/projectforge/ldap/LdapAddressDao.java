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
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.projectforge.address.AddressDO;
import org.projectforge.common.StringHelper;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class LdapAddressDao extends LdapDao<AddressDO>
{
  /**
   * @see org.projectforge.ldap.LdapDao#getObjectClass()
   */
  @Override
  protected String getObjectClass()
  {
    return "contact";
  }

  /**
   * @see org.projectforge.ldap.LdapDao#buildDn(java.lang.Object)
   */
  @Override
  protected String buildDn(final AddressDO address)
  {
    final StringBuffer buf = new StringBuffer();
    buf.append("cn=");
    boolean first = true;
    if (StringUtils.isNotBlank(address.getFirstName()) == true) {
      first = StringHelper.append(buf, first, address.getFirstName(), " ");
    }
    if (StringUtils.isNotBlank(address.getName()) == true) {
      first = StringHelper.append(buf, first, address.getName(), " ");
    }
    buf.append(", ou=").append("contacts");
    return buf.toString();
  }

  /**
   * @see org.projectforge.ldap.LdapDao#getAttributesToBind(java.lang.Object)
   */
  @Override
  protected Attributes getAttributesToBind(final AddressDO address)
  {
    final Attributes attrs = new BasicAttributes();
    final BasicAttribute ocattr = new BasicAttribute("objectclass");
    ocattr.add("top");
    ocattr.add("person");
    ocattr.add("inetOrgPerson");
    // ocattr.add("organisationalPerson");
    attrs.put(ocattr);
    LdapUtils.putAttribute(attrs, "sn", address.getName());
    LdapUtils.putAttribute(attrs, "givenName", address.getFirstName());
    LdapUtils.putAttribute(attrs, "uid", String.valueOf(address.getId()));
    LdapUtils.putAttribute(attrs, "o", address.getOrganization());
    LdapUtils.putAttribute(attrs, "mail", address.getEmail());
    LdapUtils.putAttribute(attrs, "mail", address.getPrivateEmail());
    LdapUtils.putAttribute(attrs, "telephoneNumber", address.getBusinessPhone());
    LdapUtils.putAttribute(attrs, "mobile", address.getMobilePhone());
    LdapUtils.putAttribute(attrs, "homePhone", address.getPrivatePhone());
    LdapUtils.putAttribute(attrs, "mobile", address.getMobilePhone());
    LdapUtils.putAttribute(attrs, "mobile", address.getPrivateMobilePhone());
    return attrs;
  }

  /**
   * @see org.projectforge.ldap.LdapDao#mapToObject(java.lang.String, javax.naming.directory.Attributes)
   */
  @Override
  protected AddressDO mapToObject(final String dn, final Attributes attributes) throws NamingException
  {
    final AddressDO person = new AddressDO();
    person.setId(NumberUtils.createInteger(LdapUtils.getAttribute(attributes, "cn")));
    person.setName(LdapUtils.getAttribute(attributes, "sn"));
    person.setFirstName(LdapUtils.getAttribute(attributes, "givenName"));
    return person;
  }
}
