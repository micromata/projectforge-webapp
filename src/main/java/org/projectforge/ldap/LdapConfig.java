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
import org.projectforge.common.ReflectionToString;

/**
 * Bean used by ConfigXML (config.xml).
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class LdapConfig
{
  private String server;

  private Integer port;

  private String userBase, userBaseFilter, groupBase;

  private String baseDN;

  private String managerUser;

  private String managerPassword;

  private String authentication = "simple";

  /**
   * e.g. ldap.acme.com
   */
  public String getServer()
  {
    return server;
  }

  /**
   * @return ldap://{server}/{base or url if base is not given.
   */
  public String getCompleteServerUrl()
  {
    final StringBuffer buf = new StringBuffer();
    buf.append("ldap://").append(this.server);
    if (port != null) {
      buf.append(':').append(port);
    }
    if (StringUtils.isBlank(this.baseDN) == false) {
      buf.append('/').append(this.baseDN);
    }
    return buf.toString();
  }

  public LdapConfig setServer(final String server)
  {
    this.server = server;
    return this;
  }

  /**
   * Optional.
   * @return the port if given.
   */
  public Integer getPort()
  {
    return port;
  }

  /**
   * @param port the port to set
   * @return this for chaining.
   */
  public LdapConfig setPort(final Integer port)
  {
    this.port = port;
    return this;
  }

  /**
   * e. g. ou=users
   */
  public String getUserBase()
  {
    return userBase;
  }

  public LdapConfig setUserBase(final String userBase)
  {
    this.userBase = userBase;
    return this;
  }

  /**
   * Filter to search the user to login, e. g. "uid={0}". '{0}' is replaced by the login name (user name).
   * @return the userBaseFilter
   */
  public String getUserBaseFilter()
  {
    return userBaseFilter;
  }

  /**
   * @param userBaseFilter the userBaseFilter to set
   * @return this for chaining.
   */
  public LdapConfig setUserBaseFilter(final String userBaseFilter)
  {
    this.userBaseFilter = userBaseFilter;
    return this;
  }

  /**
   * e. g. ou=groups
   * @return the groupBase
   */
  public String getGroupBase()
  {
    return groupBase;
  }

  /**
   * @param groupBase the groupBase to set
   * @return this for chaining.
   */
  public LdapConfig setGroupBase(final String groupBase)
  {
    this.groupBase = groupBase;
    return this;
  }

  /**
   * e. g. dc=acme,dc=com
   * @return
   */
  public String getBaseDN()
  {
    return baseDN;
  }

  public LdapConfig setBaseDN(final String baseDN)
  {
    this.baseDN = baseDN;
    return this;
  }

  public String getManagerUser()
  {
    return managerUser;
  }

  public LdapConfig setManagerUser(final String managerUser)
  {
    this.managerUser = managerUser;
    return this;
  }

  public String getManagerPassword()
  {
    return managerPassword;
  }

  public LdapConfig setManagerPassword(final String password)
  {
    this.managerPassword = password;
    return this;
  }

  /**
   * The authentication, can be a list of algorithms.<br/>
   * "none" - means anonymous<br/>
   * "simple" - user/password authentication without any encryption. "DIGEST-MD5 CRAM-MD5" - space separated list of supported algorithms.
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

  /**
   * @see ReflectionToString#asString(Object)
   */
  @Override
  public String toString()
  {
    return ReflectionToString.asString(this);
  }
}
