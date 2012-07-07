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

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class LdapPersonDao extends LdapDao<LdapPerson>
{
  /**
   * @see org.projectforge.ldap.LdapDao#getObjectClass()
   */
  @Override
  protected String getObjectClass()
  {
    return "person";
  }

  /**
   * @see org.projectforge.ldap.LdapDao#getAttributesToBind(java.lang.Object)
   */
  @Override
  protected Attributes getAttributesToBind(final LdapPerson address)
  {
    final Attributes attrs = new BasicAttributes();
    final BasicAttribute ocattr = new BasicAttribute("objectclass");
    ocattr.add("top");
    ocattr.add(getObjectClass());
    ocattr.add("inetOrgPerson");
    // ocattr.add("organisationalPerson");
    attrs.put(ocattr);
    LdapUtils.putAttribute(attrs, "sn", address.getSurname());
    LdapUtils.putAttribute(attrs, "givenName", address.getGivenName());
    LdapUtils.putAttribute(attrs, "uid", address.getUid());
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
  protected LdapPerson mapToObject(final Attributes attributes) throws NamingException
  {
    final LdapPerson address = new LdapPerson();
    address.setSurname(LdapUtils.getAttribute(attributes, "sn"));
    address.setGivenName(LdapUtils.getAttribute(attributes, "givenName"));
    return address;
  }
}
