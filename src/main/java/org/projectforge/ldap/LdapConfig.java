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

  private String adminUser;

  private String adminPassword;

  private String authentication = "simple";

  public String getUrl()
  {
    return url;
  }

  /**
   * @return url/base or url if base is not given.
   */
  public String getCompleteUrl() {
    if (StringUtils.isBlank(this.base) == true) {
      return url;
    }
    if (url.endsWith("/") == true) {
      return url + this.base;
    }
    return url + "/" + this.base;
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

  public String getAdminUser()
  {
    return adminUser;
  }

  public LdapConfig setAdminUser(final String adminUser)
  {
    this.adminUser = adminUser;
    return this;
  }

  public String getAdminPassword()
  {
    return adminPassword;
  }

  public LdapConfig setAdminPassword(final String password)
  {
    this.adminPassword = password;
    return this;
  }

  /**
   * The authentication, can be a list of algorithms.<br/>
   * "none" - means anonymous<br/>
   * "simple" - user/password authentication without any encryption.
   * "DIGEST-MD5 CRAM-MD5" - space separated list of supported algorithms.
   * @return the authentication
   */
  public String getAuthentication()
  {
    return authentication;
  }

  /**
   * @param authentication the authentication to set
   * @return this for chaining.
   */
  public LdapConfig setAuthentication(final String authentication)
  {
    this.authentication = authentication;
    return this;
  }
}
