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
   * @see org.projectforge.ldap.LdapDao#buildDn(java.lang.Object)
   */
  @Override
  protected String buildDn(final LdapPerson person)
  {
    final StringBuffer buf = new StringBuffer();
    buf.append("cn=").append(person.getCommonName());
    final String ou = LdapUtils.splitMultipleAttribute("ou", person.getOrganisationalUnitName());
    if (ou != null) {
      buf.append(", ").append(ou);
    }
    return buf.toString();
  }

  /**
   * @see org.projectforge.ldap.LdapDao#getAttributesToBind(java.lang.Object)
   */
  @Override
  protected Attributes getAttributesToBind(final LdapPerson person)
  {
    final Attributes attrs = new BasicAttributes();
    final BasicAttribute ocattr = new BasicAttribute("objectclass");
    ocattr.add("top");
    ocattr.add("person");
    ocattr.add("inetOrgPerson");
    //ocattr.add("organisationalPerson");
    attrs.put(ocattr);
    LdapUtils.putAttribute(attrs, "cn", person.getCommonName());
    LdapUtils.putAttribute(attrs, "sn", person.getSurname());
    LdapUtils.putAttribute(attrs, "givenName", person.getGivenName());
    LdapUtils.putAttribute(attrs, "uid", person.getUid());
    LdapUtils.putAttribute(attrs, "mail", person.getMail());
    LdapUtils.putAttribute(attrs, "description", person.getDescription());
    return attrs;
  }

  /**
   * @see org.projectforge.ldap.LdapDao#mapToObject(java.lang.String, javax.naming.directory.Attributes)
   */
  @Override
  protected LdapPerson mapToObject(final String dn, final Attributes attributes) throws NamingException
  {
    final LdapPerson person = new LdapPerson();
    person.setCommonName(LdapUtils.getAttribute(attributes, "cn"));
    person.setSurname(LdapUtils.getAttribute(attributes, "sn"));
    person.setDescription(LdapUtils.getAttribute(attributes, "description"));
    person.setOrganisationalUnitName(LdapUtils.getAttribute(attributes, "ou"));
    return person;
  }
}
