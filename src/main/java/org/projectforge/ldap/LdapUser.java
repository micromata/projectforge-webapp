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

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class LdapUser extends LdapPerson
{
  private Integer posixAccountUidNumber, posixAccountGidNumber;

  private String posixAccountShell, posixAccountHomeDirectoriy;

  public Integer getPosixAccountUidNumber()
  {
    return posixAccountUidNumber;
  }

  public LdapUser setPosixAccountUidNumber(final Integer posixAccountUidNumber)
  {
    this.posixAccountUidNumber = posixAccountUidNumber;
    return this;
  }

  public Integer getPosixAccountGidNumber()
  {
    return posixAccountGidNumber;
  }

  public LdapUser setPosixAccountGidNumber(final Integer posixAccountGidNumber)
  {
    this.posixAccountGidNumber = posixAccountGidNumber;
    return this;
  }

  public String getPosixAccountShell()
  {
    return posixAccountShell;
  }

  public LdapUser setPosixAccountShell(final String posixAccountShell)
  {
    this.posixAccountShell = posixAccountShell;
    return this;
  }

  public String getPosixAccountHomeDirectoriy()
  {
    return posixAccountHomeDirectoriy;
  }

  public LdapUser setPosixAccountHomeDirectoriy(final String posixAccountHomeDirectoriy)
  {
    this.posixAccountHomeDirectoriy = posixAccountHomeDirectoriy;
    return this;
  }
}
