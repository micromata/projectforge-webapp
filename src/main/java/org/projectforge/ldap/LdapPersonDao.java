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
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;

import org.apache.commons.lang.StringUtils;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class LdapPersonDao extends LdapDao<LdapPerson>
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LdapPersonDao.class);
  /**
   * @see org.projectforge.ldap.LdapDao#getObjectClass()
   */
  @Override
  protected String getObjectClass()
  {
    return "person";
  }

  /**
   * Uses modify instead of original update because otherwise any set password would be deleted.
   * @see org.projectforge.ldap.LdapDao#update(java.lang.Object, java.lang.Object[])
   */
  @Override
  public void update(final LdapPerson person, final Object... objs)
  {
    modify(person, getModificationItems(person));
  }

  public void changePassword(final LdapPerson person, final String userPassword)
  {
    log.info("Change password for " + getObjectClass() + ": " + buildDn(person));
    final ModificationItem[] modificationItems = new ModificationItem[1];
    modificationItems[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("userPassword", userPassword));
    modify(person, modificationItems);
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
    // ocattr.add("organisationalPerson");
    attrs.put(ocattr);
    final ModificationItem[] modificationItems = getModificationItems(person);
    for (final ModificationItem modItem : modificationItems) {
      final Attribute attr = modItem.getAttribute();
      attrs.put(attr);
    }
    return attrs;
  }

  /**
   * Used for bind and update.
   * @param person
   * @return
   */
  private ModificationItem[] getModificationItems(final LdapPerson person)
  {
    final ModificationItem[] modificationItems = new ModificationItem[6];
    modificationItems[0] = createModificationItem("cn", person.getCommonName());
    modificationItems[1] = createModificationItem("sn", person.getSurname());
    modificationItems[2] = createModificationItem("givenName", person.getGivenName());
    modificationItems[3] = createModificationItem("uid", person.getUid());
    modificationItems[4] = createModificationItem("mail", person.getMail());
    modificationItems[5] = createModificationItem("description", person.getDescription());
    return modificationItems;
  }

  private ModificationItem createModificationItem(final String attrId, final String attrValue)
  {
    return new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute(attrId, StringUtils.defaultString(attrValue)));
  }

  /**
   * @see org.projectforge.ldap.LdapDao#onBeforeBind(java.lang.String, javax.naming.directory.Attributes, java.lang.Object[])
   */
  @Override
  protected void onBeforeBind(final String dn, final Attributes attrs, final Object... args)
  {
    if (args != null && args.length == 1 && args[0] instanceof String) {
      attrs.put("userPassword", args[0]);
    }
  }

  /**
   * @see org.projectforge.ldap.LdapDao#onBeforeRebind(java.lang.String, javax.naming.directory.Attributes, java.lang.Object[])
   */
  @Override
  protected void onBeforeRebind(final String dn, final Attributes attrs, final Object... objs)
  {
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
