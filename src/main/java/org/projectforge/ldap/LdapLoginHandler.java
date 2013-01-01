/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2013 Kai Reinhard (k.reinhard@micromata.de)
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

import java.util.List;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;

import org.projectforge.access.AccessChecker;
import org.projectforge.core.ConfigXml;
import org.projectforge.registry.Registry;
import org.projectforge.user.LoginDefaultHandler;
import org.projectforge.user.LoginHandler;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserDao;
import org.projectforge.user.UserRights;

public abstract class LdapLoginHandler implements LoginHandler
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LdapSlaveLoginHandler.class);

  UserDao userDao;

  AccessChecker accessChecker;

  LdapConnector ldapConnector;

  LdapGroupDao ldapGroupDao;

  LdapUserDao ldapUserDao;

  LdapConfig ldapConfig;

  LdapOrganizationalUnitDao ldapOrganizationalUnitDao;

  protected LoginDefaultHandler loginDefaultHandler;

  protected String baseDN, userBase, groupBase;

  /**
   * @see org.projectforge.user.LoginHandler#initialize()
   */
  @Override
  public void initialize()
  {
    if (ldapConfig == null) {
      // May-be already set by test class.
      this.ldapConfig = ConfigXml.getInstance().getLdapConfig();
      if (ldapConfig == null || ldapConfig.getServer() == null) {
        log.warn("No LDAP configured in config.xml, so any login will be impossible!");
      }
    }
    baseDN = ldapConfig.getBaseDN();
    userBase = ldapConfig.getUserBase();
    groupBase = ldapConfig.getGroupBase();
    ldapConnector = new LdapConnector(ldapConfig);
    ldapGroupDao = new LdapGroupDao();
    ldapGroupDao.setLdapConnector(ldapConnector);
    if (ldapUserDao == null) {
      // May-be already set by test class.
      ldapUserDao = new LdapUserDao();
      ldapUserDao.setLdapConnector(ldapConnector);
      ldapUserDao.setLdapPersonDao((LdapPersonDao)new LdapPersonDao().setLdapConnector(ldapConnector));
    }
    if (ldapOrganizationalUnitDao == null) {
      // May-be already set by test class.
      ldapOrganizationalUnitDao = new LdapOrganizationalUnitDao();
      ldapOrganizationalUnitDao.setLdapConnector(ldapConnector);
    }
    final Registry registry = Registry.instance();
    userDao = (UserDao) registry.getDao(UserDao.class);
    accessChecker = UserRights.getAccessChecker();
    loginDefaultHandler = new LoginDefaultHandler();
    loginDefaultHandler.initialize();
  }

  /**
   * Calls {@link LoginDefaultHandler#checkStayLoggedIn(PFUserDO)}.
   * @see org.projectforge.user.LoginHandler#checkStayLoggedIn(org.projectforge.user.PFUserDO)
   */
  @Override
  public boolean checkStayLoggedIn(final PFUserDO user)
  {
    return loginDefaultHandler.checkStayLoggedIn(user);
  }

  /**
   * Does nothing at default.
   * @see org.projectforge.user.LoginHandler#passwordChanged(org.projectforge.user.PFUserDO, java.lang.String)
   */
  @Override
  public void passwordChanged(final PFUserDO user, final String newPassword)
  {
  }

  public boolean isAdminUser(final PFUserDO user)
  {
    return loginDefaultHandler.isAdminUser(user);
  }

  protected List<LdapUser> getAllLdapUsers()
  {
    final String organizationalUnits = ldapConfig.getUserBase();
    final List<LdapUser> ldapUsers = ldapUserDao.findAll(organizationalUnits);
    return ldapUsers;
  }

  protected List<LdapUser> getAllLdapUsers(final DirContext ctx) throws NamingException
  {
    final String organizationalUnits = ldapConfig.getUserBase();
    final List<LdapUser> ldapUsers = ldapUserDao.findAll(ctx, organizationalUnits);
    return ldapUsers;
  }

  protected List<LdapGroup> getAllLdapGroups()
  {
    final String organizationalUnits = ldapConfig.getGroupBase();
    final List<LdapGroup> ldapGroups = ldapGroupDao.findAll(organizationalUnits);
    return ldapGroups;
  }

  protected List<LdapGroup> getAllLdapGroups(final DirContext ctx) throws NamingException
  {
    final String organizationalUnits = ldapConfig.getGroupBase();
    final List<LdapGroup> ldapGroups = ldapGroupDao.findAll(ctx, organizationalUnits);
    return ldapGroups;
  }

  /**
   * @return true (ldap as an external user management system is supported).
   * @see org.projectforge.user.LoginHandler#hasExternalUsermanagementSystem()
   */
  @Override
  public boolean hasExternalUsermanagementSystem()
  {
    return true;
  }
}
