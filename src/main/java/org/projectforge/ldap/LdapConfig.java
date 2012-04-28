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
 * Bean used by ConfigXML (config.xml).
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class LdapConfig
{
  /**
   * ldap://localhost:389
   */
  private String url;

  /**
   * uid=admin,ou=system
   */
  private String userDn;

  /**
   * dc=jayway,dc=se
   */
  private String base;

  private String password;

  public String getUrl()
  {
    return url;
  }

  public LdapConfig setUrl(final String url)
  {
    this.url = url;
    return this;
  }

  public String getUserDn()
  {
    return userDn;
  }

  public LdapConfig setUserDn(final String userDn)
  {
    this.userDn = userDn;
    return this;
  }

  public String getBase()
  {
    return base;
  }

  public LdapConfig setBase(final String base)
  {
    this.base = base;
    return this;
  }

  public String getPassword()
  {
    return password;
  }

  public LdapConfig setPassword(final String password)
  {
    this.password = password;
    return this;
  }
}
