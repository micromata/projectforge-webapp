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

import org.apache.commons.lang.StringUtils;
import org.projectforge.common.NumberHelper;
import org.projectforge.user.PFUserDO;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class PFUserDOConverter
{
  public static final String UID_PREFIX = "pf-";

  public static PFUserDO convert(final LdapPerson person) {
    final PFUserDO user = new PFUserDO();
    user.setLastname(person.getSurname());
    user.setFirstname(person.getGivenName());
    final String uid = person.getUid();
    if (uid != null && uid.startsWith(UID_PREFIX) == true && uid.length() > UID_PREFIX.length()) {
      final String id = uid.substring(UID_PREFIX.length());
      user.setId(NumberHelper.parseInteger(id));
    }
    user.setOrganization(person.getOrganization());
    user.setDescription(person.getDescription());
    final String[] mails = person.getMail();
    if (mails != null) {
      for (final String mail : mails) {
        if (StringUtils.isNotEmpty(mail) == true) {
          user.setEmail(mail);
          break;
        }
      }
    }
    return user;
  }

  public static LdapPerson convert(final PFUserDO user)
  {
    final LdapPerson person = new LdapPerson();
    person.setSurname(user.getLastname());
    person.setGivenName(user.getFirstname());
    if (user.getId() != null) {
      person.setUid(UID_PREFIX + user.getId());
    }
    person.setOrganization(user.getOrganization());
    person.setDescription(user.getDescription());
    person.setMail(user.getEmail());
    return person;
  }
}
