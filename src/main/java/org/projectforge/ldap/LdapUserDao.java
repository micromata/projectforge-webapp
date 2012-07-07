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
public class LdapUserDao extends LdapDao<LdapUser>
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LdapUserDao.class);

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
  public void update(final LdapUser person, final Object... objs)
  {
    modify(person, getModificationItems(person));
  }

  public void changePassword(final LdapUser person, final String userPassword)
  {
    log.info("Change password for " + getObjectClass() + ": " + buildDn(person));
    final ModificationItem[] modificationItems = new ModificationItem[1];
    modificationItems[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("userPassword", userPassword));
    modify(person, modificationItems);
  }

  public boolean authenticate(final String username, final String userPassword)
  {
    return false;
  }

  /**
   * @see org.projectforge.ldap.LdapDao#getAttributesToBind(java.lang.Object)
   */
  @Override
  protected Attributes getAttributesToBind(final LdapUser person)
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
  private ModificationItem[] getModificationItems(final LdapUser person)
  {
    final ModificationItem[] modificationItems = new ModificationItem[5];
    modificationItems[0] = createModificationItem("sn", person.getSurname());
    modificationItems[1] = createModificationItem("givenName", person.getGivenName());
    modificationItems[2] = createModificationItem("uid", person.getUid());
    modificationItems[3] = createModificationItem("mail", person.getMail());
    modificationItems[4] = createModificationItem("description", person.getDescription());
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
   * @see org.projectforge.ldap.LdapDao#mapToObject(java.lang.String, javax.naming.directory.Attributes)
   */
  @Override
  protected LdapUser mapToObject(final Attributes attributes) throws NamingException
  {
    final LdapUser person = new LdapUser();
    person.setSurname(LdapUtils.getAttribute(attributes, "sn"));
    person.setDescription(LdapUtils.getAttribute(attributes, "description"));
    return person;
  }
}
