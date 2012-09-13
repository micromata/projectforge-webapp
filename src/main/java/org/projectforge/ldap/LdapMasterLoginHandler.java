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

import java.util.List;

import javax.naming.NameNotFoundException;

import org.apache.commons.lang.StringUtils;
import org.projectforge.common.NumberHelper;
import org.projectforge.user.GroupDO;
import org.projectforge.user.LoginResult;
import org.projectforge.user.LoginResultStatus;
import org.projectforge.user.PFUserDO;

/**
 * This LDAP login handler has read-write access to the LDAP server and acts as master of the user and group data. All changes of
 * ProjectForge's users and groups will be written through. Any change of the LDAP server will be ignored and may be overwritten by
 * ProjectForge. <br/>
 * Use this login handler if you want to configure your LDAP users and LDAP groups via ProjectForge.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class LdapMasterLoginHandler extends LdapLoginHandler
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LdapMasterLoginHandler.class);

  /**
   * @see org.projectforge.ldap.LdapLoginHandler#initialize()
   */
  @Override
  public void initialize()
  {
    super.initialize();
    ldapOrganizationalUnitDao.createIfNotExist(userBase, "ProjectForge's user base.");
    ldapOrganizationalUnitDao.createIfNotExist(LdapUserDao.DEACTIVATED_SUB_CONTEXT, "ProjectForge's user base for deactivated users.",
        userBase);
  }

  /**
   * @see org.projectforge.user.LoginHandler#checkLogin(java.lang.String, java.lang.String, boolean)
   */
  @Override
  public LoginResult checkLogin(final String username, final String password)
  {
    final LoginResult loginResult = loginDefaultHandler.checkLogin(username, password);
    if (loginResult.getLoginResultStatus() != LoginResultStatus.SUCCESS) {
      return loginResult;
    }
    // User is now logged-in successfully.
    final boolean authenticated = ldapUserDao.authenticate(username, password, userBase);
    if (authenticated == false) {
      log.info("User's credentials in LDAP not up-to-date: " + username + ". Updating LDAP entry...");
      final PFUserDO user = loginResult.getUser();
      final LdapPerson ldapUser = PFUserDOConverter.convert(user);
      ldapUser.setOrganizationalUnit(userBase);
      ldapUserDao.createOrUpdate(userBase, ldapUser);
      ldapUserDao.changePassword(ldapUser, null, password);
    }
    return loginResult;
  }

  /**
   * @see org.projectforge.user.LoginHandler#getAllGroups()
   */
  @Override
  public List<GroupDO> getAllGroups()
  {
    final List<GroupDO> groups = loginDefaultHandler.getAllGroups();
    return groups;
  }

  /**
   * @see org.projectforge.user.LoginHandler#getAllUsers()
   */
  @Override
  public List<PFUserDO> getAllUsers()
  {
    final List<PFUserDO> users = loginDefaultHandler.getAllUsers();
    return users;
  }

  /**
   * Refreshes the LDAP.
   * @see org.projectforge.user.LoginHandler#afterUserGroupCacheRefresh(java.util.List, java.util.List)
   */
  @Override
  public void afterUserGroupCacheRefresh(final List<PFUserDO> users, final List<GroupDO> groups)
  {
    new Thread() {
      @Override
      public void run()
      {
        updateLdap(users, groups);
      }
    }.start();
  }

  private void updateLdap(final List<PFUserDO> users, final List<GroupDO> groups)
  {
    synchronized (this) {
      new LdapTemplate(ldapConnector) {
        @Override
        protected Object call() throws NameNotFoundException, Exception
        {
          log.info("Updating LDAP...");
          // First, get set of all ldap entries:
          final List<LdapPerson> ldapUsers = getAllLdapUsers(ctx);
          for (final PFUserDO user : users) {
            try {
              final LdapPerson updatedLdapUser = PFUserDOConverter.convert(user);
              final LdapPerson ldapUser = getLdapUser(ldapUsers, user);
              if (ldapUser == null) {
                updatedLdapUser.setOrganizationalUnit(userBase);
                if (user.isDeleted() == false && user.isLocalUser() == false) {
                  // Do not add deleted or local users.
                  ldapUserDao.create(ctx, userBase, updatedLdapUser);
                }
              } else {
                // Need to set organizational unit for detecting the change of deactivated flag. The updateLdapUser needs the organizational
                // unit of the original ldap object:
                updatedLdapUser.setOrganizationalUnit(ldapUser.getOrganizationalUnit());
                if (user.isDeleted() == true || user.isLocalUser() == true) {
                  // Deleted and local users shouldn't be synchronized with LDAP:
                  ldapUserDao.delete(ctx, updatedLdapUser);
                } else {
                  final boolean modified = PFUserDOConverter.copyUserFields(updatedLdapUser, ldapUser);
                  if (modified == true) {
                    ldapUserDao.update(ctx, userBase, updatedLdapUser);
                  }
                  if (updatedLdapUser.isDeactivated() && ldapUser.isPasswordGiven() == true) {
                    log.warn("User password for deactivated user is set: " + ldapUser);
                    ldapUserDao.deactivateUser(ctx, updatedLdapUser);
                  }
                }
              }
            } catch (final Exception ex) {
              log.error("Error while proceeding user '" + user.getUsername() + "'. Continuing with next user.", ex);
            }
          }
          // Now get all groups:
          final List<LdapGroup> ldapGroups = getAllLdapGroups(ctx);
          for (final GroupDO group : groups) {
            try {
              final LdapGroup updatedLdapGroup = GroupDOConverter.convert(group);
              final LdapGroup ldapGroup = getLdapGroup(ldapGroups, group);
              if (ldapGroup == null) {
                updatedLdapGroup.setOrganizationalUnit(groupBase);
                if (group.isDeleted() == false && group.isLocalGroup() == false) {
                  // Do not add deleted or local groups.
                  ldapGroupDao.create(ctx, groupBase, updatedLdapGroup);
                }
              } else {
                if (group.isDeleted() == true || group.isLocalGroup() == true) {
                  // Deleted and local users shouldn't be synchronized with LDAP:
                  ldapGroupDao.delete(ctx, updatedLdapGroup);
                } else {
                  final boolean modified = GroupDOConverter.copyUserFields(updatedLdapGroup, ldapGroup);
                  if (modified == true) {
                    ldapGroupDao.update(ctx, groupBase, updatedLdapGroup);
                  }
                }
              }

            } catch (final Exception ex) {
              log.error("Error while proceeding group '" + group.getName() + "'. Continuing with next group.", ex);
            }
          }
          log.info("LDAP update done.");
          return null;
        }
      }.excecute();
    }
  }

  private LdapPerson getLdapUser(final List<LdapPerson> ldapUsers, final PFUserDO user)
  {
    for (final LdapPerson ldapUser : ldapUsers) {
      if (StringUtils.equals(ldapUser.getUid(), user.getUsername()) == true
          || StringUtils.equals(ldapUser.getEmployeeNumber(), PFUserDOConverter.buildEmployeeNumber(user)) == true) {
        return ldapUser;
      }
    }
    return null;
  }

  private LdapGroup getLdapGroup(final List<LdapGroup> ldapGroups, final GroupDO group)
  {
    for (final LdapGroup ldapGroup : ldapGroups) {
      if (NumberHelper.isEqual(ldapGroup.getGidNumber(), group.getId()) == true) {
        return ldapGroup;
      }
    }
    return null;
  }
}
