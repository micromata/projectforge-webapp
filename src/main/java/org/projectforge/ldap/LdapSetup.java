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
import org.projectforge.core.ConfigXml;
import org.projectforge.core.ConfigurationListener;
import org.springframework.ldap.core.support.LdapContextSource;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class LdapSetup implements ConfigurationListener
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LdapSetup.class);
  private static final LdapSetup instance = new LdapSetup();

  private LdapContextSource ldapContextSource;

  public static LdapSetup getInstance()
  {
    return instance;
  }

  public void init(final LdapContextSource ldapContextSource)
  {
    this.ldapContextSource = ldapContextSource;
    afterRead();
  }

  private LdapSetup()
  {
    ConfigXml.getInstance().register(this);
  }

  /**
   * @see org.projectforge.core.ConfigurationListener#afterRead()
   */
  @Override
  public void afterRead()
  {
    if (ldapContextSource == null) {
      // Not yet initialized.
      return;
    }
    final LdapConfig config = ConfigXml.getInstance().getLdapConfig();
    if (config == null || StringUtils.isBlank(config.getUrl()) == true) {
      log.info("LDAP not configured (OK).");
      return;
    }
    log.info("Initializing LDAP: url=[" + config.getUrl() + "], userDN=[" + config.getUserDn() + "], base=[" + config.getBase() + "], password=[***].");
    ldapContextSource.setUrl(config.getUrl());
    ldapContextSource.setUserDn(config.getUserDn());
    ldapContextSource.setPassword(config.getPassword());
    ldapContextSource.setBase(config.getBase());
  }
}
