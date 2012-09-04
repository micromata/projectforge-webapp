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

import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.ModificationItem;

import org.apache.commons.lang.StringUtils;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class LdapPersonDao extends LdapDao<String, LdapPerson>
{
  private static final String[] ADDITIONAL_OBJECT_CLASSES = { "inetOrgPerson"};

  /**
   * @see org.projectforge.ldap.LdapDao#getObjectClass()
   */
  @Override
  protected String getObjectClass()
  {
    return "person";
  }

  @Override
  protected String[] getAdditionalObjectClasses()
  {
    return ADDITIONAL_OBJECT_CLASSES;
  }

  /**
   * @see org.projectforge.ldap.LdapDao#getIdAttrId()
   */
  @Override
  public String getIdAttrId()
  {
    return "uid";
  }

  /**
   * @see org.projectforge.ldap.LdapDao#getId(org.projectforge.ldap.LdapObject)
   */
  @Override
  public String getId(final LdapPerson obj)
  {
    return obj.getUid();
  }

  /**
   * @see org.projectforge.ldap.LdapDao#mapToObject(java.lang.String, javax.naming.directory.Attributes)
   */
  @Override
  protected LdapPerson mapToObject(final Attributes attributes) throws NamingException
  {
    final LdapPerson person = new LdapPerson();
    person.setSurname(LdapUtils.getAttributeStringValue(attributes, "sn"));
    person.setGivenName(LdapUtils.getAttributeStringValue(attributes, "givenName"));
    person.setUid(LdapUtils.getAttributeStringValue(attributes, "uid"));
    person.setEmployeeNumber(LdapUtils.getAttributeStringValue(attributes, "employeeNumber"));
    person.setOrganization(LdapUtils.getAttributeStringValue(attributes, "o"));
    person.setMail(LdapUtils.getAttributeStringValues(attributes, "mail"));
    person.setDescription(LdapUtils.getAttributeStringValue(attributes, "description"));
    person.setTelephoneNumber(LdapUtils.getAttributeStringValue(attributes, "telephoneNumber"));
    person.setMobilePhoneNumber(LdapUtils.getAttributeStringValues(attributes, "mobile"));
    person.setHomePhoneNumber(LdapUtils.getAttributeStringValue(attributes, "homePhone"));
    return person;
  }

  /**
   * Used for bind and update.
   * @param person
   * @return
   * @see org.projectforge.ldap.LdapDao#getModificationItems(org.projectforge.ldap.LdapObject)
   */
  @Override
  protected ModificationItem[] getModificationItems(final LdapPerson person)
  {
    final List<ModificationItem> list = new ArrayList<ModificationItem>();
    createAndAddModificationItems(list, "sn", person.getSurname());
    createAndAddModificationItems(list, "givenName", person.getGivenName());
    createAndAddModificationItems(list, "uid", person.getUid());
    createAndAddModificationItems(list, "employeeNumber", person.getEmployeeNumber());
    createAndAddModificationItems(list, "o", person.getOrganization());
    createAndAddModificationItems(list, "mail", person.getMail());
    createAndAddModificationItems(list, "description", StringUtils.isEmpty(person.getDescription()) == true ? "-" : person.getDescription());
    createAndAddModificationItems(list, "telephoneNumber", person.getTelephoneNumber());
    createAndAddModificationItems(list, "mobile", person.getMobilePhoneNumber());
    createAndAddModificationItems(list, "homePhone", person.getHomePhoneNumber());
    return list.toArray(new ModificationItem[list.size()]);
  }
}
