/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2010 Kai Reinhard (k.reinhard@me.com)
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

package org.projectforge.user;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.hibernate.LockMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.projectforge.access.AccessChecker;
import org.projectforge.access.AccessException;
import org.projectforge.access.AccessType;
import org.projectforge.access.OperationType;
import org.projectforge.common.Crypt;
import org.projectforge.common.NumberHelper;
import org.projectforge.core.BaseDao;
import org.projectforge.core.BaseSearchFilter;
import org.projectforge.core.DisplayHistoryEntry;
import org.projectforge.core.QueryFilter;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import de.micromata.user.ContextHolder;

/**
 * 
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class UserDao extends BaseDao<PFUserDO>
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(UserDao.class);

  private static final String MESSAGE_KEY_OLD_PASSWORD_WRONG = "user.changePassword.error.oldPasswordWrong";

  static final String MESSAGE_KEY_PASSWORD_QUALITY_CHECK_FAILED = "user.changePassword.error.passwordQualityCheckFailed";

  public UserDao()
  {
    super(PFUserDO.class);
  }

  public UserGroupCache getUserGroupCache()
  {
    return userGroupCache;
  }

  public QueryFilter getDefaultFilter()
  {
    QueryFilter queryFilter = new QueryFilter();
    queryFilter.add(Restrictions.eq("deleted", false));
    return queryFilter;
  }

  @Override
  public List<PFUserDO> getList(BaseSearchFilter filter)
  {
    QueryFilter queryFilter = new QueryFilter(filter);
    queryFilter.addOrder(Order.asc("username"));
    return getList(queryFilter);
  }

  public String getGroupnames(PFUserDO user)
  {
    if (user == null) {
      return "";
    }
    return userGroupCache.getGroupnames(user.getId());
  }

  public Collection<Integer> getAssignedGroups(PFUserDO user)
  {
    return userGroupCache.getUserGroups(user);
  }

  public List<UserRightDO> getUserRights(final Integer userId)
  {
    return userGroupCache.getUserRights(userId);
  }

  public String[] getPersonalPhoneIdentifiers(PFUserDO user)
  {
    String[] tokens = StringUtils.split(user.getPersonalPhoneIdentifiers(), ", ;|");
    if (tokens == null) {
      return null;
    }
    int n = 0;
    for (String token : tokens) {
      if (StringUtils.isNotBlank(token) == true) {
        n++;
      }
    }
    if (n == 0) {
      return null;
    }
    String[] result = new String[n];
    n = 0;
    for (String token : tokens) {
      if (StringUtils.isNotBlank(token) == true) {
        result[n] = token.trim();
        n++;
      }
    }
    return result;
  }

  public String getNormalizedPersonalPhoneIdentifiers(PFUserDO user)
  {
    if (StringUtils.isNotBlank(user.getPersonalPhoneIdentifiers()) == true) {
      String[] ids = getPersonalPhoneIdentifiers(user);
      if (ids != null) {
        StringBuffer buf = new StringBuffer();
        boolean first = true;
        for (String id : ids) {
          if (first == true) {
            first = false;
          } else {
            buf.append(",");
          }
          buf.append(id);
        }
        return buf.toString();
      }
    }
    return null;
  }

  @Override
  protected void afterSaveOrModify(PFUserDO user)
  {
    super.afterSaveOrModify(user);
    userGroupCache.updateUser(user);
  }

  /**
   * @see org.projectforge.core.BaseDao#hasAccess(Object, OperationType)
   */
  @Override
  public boolean hasAccess(PFUserDO obj, PFUserDO oldObj, OperationType operationType, boolean throwException)
  {
    return accessChecker.isUserMemberOfAdminGroup(throwException);
  }

  /**
   * @return false, if no admin user and the context user is not at minimum in one groups assigned to the given user or false. Also deleted
   *         users are only visible for admin users.
   * @see org.projectforge.core.BaseDao#hasSelectAccess(org.projectforge.core.BaseDO, boolean)
   * @see AccessChecker#isContextUserInSameGroup(PFUserDO)
   */
  @Override
  public boolean hasSelectAccess(PFUserDO obj, boolean throwException)
  {
    boolean result = accessChecker.isUserMemberOfAdminGroup()
        || accessChecker.isUserMemberOfGroup(ProjectForgeGroup.FINANCE_GROUP, ProjectForgeGroup.CONTROLLING_GROUP);
    if (result == false && obj.isDeleted() == false) {
      result = accessChecker.isContextUserInSameGroup(obj);
    }
    if (throwException == true && result == false) {
      throw new AccessException(AccessType.GROUP, OperationType.SELECT);
    }
    return result;
  }
  
  @Override
  public boolean hasSelectAccess(boolean throwException)
  {
    return true;
  }

  @Override
  protected void onSaveOrModify(PFUserDO obj)
  {
    obj.checkAndFixPassword();
  }

  /**
   * Ohne Zugangsbegrenzung. Wird bei Anmeldung benötigt.
   * @param username
   * @param encryptedPassword
   * @return
   */
  @SuppressWarnings("unchecked")
  @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
  public PFUserDO authenticateUser(final String username, final String encryptedPassword)
  {
    Validate.notNull(username);
    Validate.notNull(encryptedPassword);

    List<PFUserDO> list = getHibernateTemplate().find("from PFUserDO u where u.username = ? and u.password = ?",
        new Object[] { username, encryptedPassword});
    PFUserDO user = getUser(username, encryptedPassword);
    if (user != null) {
      int loginFailures = user.getLoginFailures();
      Timestamp lastLogin = user.getLastLogin();
      user.setLastLogin(new Timestamp(new Date().getTime()));
      user.setLoginFailures(0);
      user.setMinorChange(true); // Avoid re-indexing of all dependent objects.
      internalUpdate(user);
      PFUserDO contextUser = new PFUserDO();
      contextUser.copyValuesFrom(user);
      contextUser.setLoginFailures(loginFailures); // Restore loginFailures for current user session.
      contextUser.setLastLogin(lastLogin); // Restore lastLogin for current user session.
      contextUser.setPassword(null);
      return contextUser;
    }
    list = getHibernateTemplate().find("from PFUserDO u where u.username = ?", username);
    if (list != null && list.isEmpty() == false && list.get(0) != null) {
      user = list.get(0);
      user.setLoginFailures(user.getLoginFailures() + 1);
      internalUpdate(user);
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  private PFUserDO getUser(final String username, final String encryptedPassword)
  {
    List<PFUserDO> list = getHibernateTemplate().find("from PFUserDO u where u.username = ? and u.password = ?",
        new Object[] { username, encryptedPassword});
    if (list != null && list.isEmpty() == false && list.get(0) != null) {
      return list.get(0);
    }
    return null;
  }

  /**
   * Does an user with the given username already exists? Works also for existing users (if username was modified).
   * @param username
   * @return
   */
  @SuppressWarnings("unchecked")
  public boolean doesUsernameAlreadyExist(final PFUserDO user)
  {
    Validate.notNull(user);
    List<PFUserDO> list = null;
    if (user.getId() == null) {
      // New user
      list = getHibernateTemplate().find("from PFUserDO u where u.username = ?", user.getUsername());
    } else {
      // user already exists. Check maybe changed username:
      list = getHibernateTemplate().find("from PFUserDO u where u.username = ? and pk <> ?",
          new Object[] { user.getUsername(), user.getId()});
    }
    if (CollectionUtils.isNotEmpty(list) == true) {
      return true;
    }
    return false;
  }

  /**
   * Encrypts the password.
   * @param password
   * @return
   * @see Crypt#digest(String)
   */
  public String encryptPassword(String password)
  {
    return Crypt.digest(password);
  }

  /**
   * Changes the user's password. Checks the password quality and the correct authentication for the old password before. Also the
   * stay-logged-in-key will be renewed, so any existing stay-logged-in cookie will be invalid.
   * @param username
   * @param oldPassword
   * @param newPassword
   * @return Error message key if any check failed or null, if successfully changed.
   */
  @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
  public String changePassword(PFUserDO user, String oldPassword, String newPassword)
  {
    Validate.notNull(user);
    Validate.notNull(oldPassword);
    Validate.notNull(newPassword);
    String errorMsgKey = checkPasswordQuality(newPassword);
    if (errorMsgKey != null) {
      return errorMsgKey;
    }
    accessChecker.checkDemoUser();
    String encryptedOldPassword = encryptPassword(oldPassword);
    String encryptedNewPassword = encryptPassword(newPassword);
    oldPassword = newPassword = "";
    user = getUser(user.getUsername(), encryptedOldPassword);
    if (user == null) {
      return MESSAGE_KEY_OLD_PASSWORD_WRONG;
    }
    user.setPassword(encryptedNewPassword);
    user.setStayLoggedInKey(createStayLoggedInKey());
    log.info("Password changed and stay-logged-key renewed for user: " + user.getId() + " - " + user.getUsername());
    return null;
  }

  /**
   * Returns the user's stay-logged-in key if exists (must be not blank with a size >= 10). If not, a new stay-logged-in key will be
   * generated.
   * @param userId
   * @return
   */
  @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
  public String getStayLoggedInKey(final Integer userId)
  {
    final PFUserDO user = internalGetById(userId);
    if (StringUtils.isBlank(user.getStayLoggedInKey()) || user.getStayLoggedInKey().trim().length() < 10) {
      user.setStayLoggedInKey(createStayLoggedInKey());
      log.info("Stay-logged-key renewed for user: " + userId + " - " + user.getUsername());
    }
    return user.getStayLoggedInKey();
  }

  /**
   * Renews the user's stay-logged-in key (random string sequence).
   */
  @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
  public void renewStayLoggedInKey(final Integer userId)
  {
    final PFUserDO user = internalGetById(userId);
    user.setStayLoggedInKey(createStayLoggedInKey());
    log.info("Stay-logged-key renewed for user: " + userId + " - " + user.getUsername());
  }

  private String createStayLoggedInKey()
  {
    return NumberHelper.getSecureRandomUrlSaveString(20);
  }

  /**
   * Checks the password quality of a new password. Password must have at least 6 characters and at minimum one letter and one non-letter
   * character.
   * @param newPassword
   * @return null if password quality is OK, otherwise the i18n message key of the password check failure.
   */
  public String checkPasswordQuality(String newPassword)
  {
    boolean letter = false;
    boolean nonLetter = false;
    if (newPassword == null || newPassword.length() < 6) {
      return MESSAGE_KEY_PASSWORD_QUALITY_CHECK_FAILED;
    }
    for (int i = 0; i < newPassword.length(); i++) {
      char ch = newPassword.charAt(i);
      if (letter == false && Character.isLetter(ch) == true) {
        letter = true;
      } else if (nonLetter == false && Character.isLetter(ch) == false) {
        nonLetter = true;
      }
    }
    if (letter == true && nonLetter == true) {
      return null;
    }
    return MESSAGE_KEY_PASSWORD_QUALITY_CHECK_FAILED;
  }

  @SuppressWarnings("unchecked")
  @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
  public PFUserDO getInternalByName(String username)
  {
    List<PFUserDO> list = getHibernateTemplate().find("from PFUserDO u where u.username = ?", username);
    if (list != null && list.size() > 0) {
      return list.get(0);
    }
    return null;
  }

  /**
   * User can modify own setting, this method ensures that only such properties will be updated, the user's are allowed to.
   * @param user
   */
  @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW, isolation = Isolation.REPEATABLE_READ)
  public void updateMyAccount(PFUserDO user)
  {
    accessChecker.checkDemoUser();
    final PFUserDO contextUser = ((PFUserDO) ContextHolder.getUserInfo());
    Validate.isTrue(user.getId().equals(contextUser.getId()) == true);
    PFUserDO dbUser = (PFUserDO) getHibernateTemplate().load(clazz, user.getId(), LockMode.PESSIMISTIC_WRITE);
    if (copyValues(user, dbUser, "deleted", "password", "lastLogin", "loginFailures", "orgUnit", "role", "username", "stayLoggedInKey", "rights") == true) {
      dbUser.setLastUpdate();
      log.info("Object updated: " + dbUser.toString());
      copyValues(user, contextUser, "deleted", "password", "lastLogin", "loginFailures", "orgUnit", "role", "username", "stayLoggedInKey", "rights");
    } else {
      log.info("No modifications detected (no update needed): " + dbUser.toString());
    }
    userGroupCache.updateUser(user);
}

  /**
   * Gets history entries of super and adds all history entries of the AuftragsPositionDO childs.
   * @see org.projectforge.core.BaseDao#getDisplayHistoryEntries(org.projectforge.core.ExtendedBaseDO)
   */
  @Override
  public List<DisplayHistoryEntry> getDisplayHistoryEntries(PFUserDO obj)
  {
    final List<DisplayHistoryEntry> list = super.getDisplayHistoryEntries(obj);
    if (hasHistoryAccess(obj, false) == false) {
      return list;
    }
    if (CollectionUtils.isNotEmpty(obj.getRights()) == true) {
      for (final UserRightDO right : obj.getRights()) {
        final List<DisplayHistoryEntry> entries = internalGetDisplayHistoryEntries(right);
        for (final DisplayHistoryEntry entry : entries) {
          final String propertyName = entry.getPropertyName();
          if (propertyName != null) {
            entry.setPropertyName(right.getRightId() + ":" + entry.getPropertyName()); // Prepend number of positon.
          } else {
            entry.setPropertyName(String.valueOf(right.getRightId()));
          }
        }
        list.addAll(entries);
      }
    }
    Collections.sort(list, new Comparator<DisplayHistoryEntry>() {
      public int compare(DisplayHistoryEntry o1, DisplayHistoryEntry o2)
      {
        return (o2.getTimestamp().compareTo(o1.getTimestamp()));
      }
    });
    return list;
  }

  @Override
  public boolean hasHistoryAccess(boolean throwException)
  {
    return accessChecker.isUserMemberOfAdminGroup(throwException);
  }

  @Override
  public PFUserDO newInstance()
  {
    return new PFUserDO();
  }
}
