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
import org.projectforge.core.ModificationStatus;
import org.projectforge.core.QueryFilter;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class UserDao extends BaseDao<PFUserDO>
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(UserDao.class);

  private static final String MESSAGE_KEY_OLD_PASSWORD_WRONG = "user.changePassword.error.oldPasswordWrong";

  private static final short AUTHENTICATION_TOKEN_LENGTH = 20;

  private static final short STAY_LOGGED_IN_KEY_LENGTH = 20;

  public static final String MESSAGE_KEY_PASSWORD_QUALITY_CHECK = "user.changePassword.error.passwordQualityCheck";

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
    final QueryFilter queryFilter = new QueryFilter();
    queryFilter.add(Restrictions.eq("deleted", false));
    return queryFilter;
  }

  @Override
  public List<PFUserDO> getList(final BaseSearchFilter filter)
  {
    final PFUserFilter myFilter;
    if (filter instanceof PFUserFilter) {
      myFilter = (PFUserFilter) filter;
    } else {
      myFilter = new PFUserFilter(filter);
    }
    final QueryFilter queryFilter = new QueryFilter(myFilter);
    if (myFilter.getDeactivatedUser() != null) {
      queryFilter.add(Restrictions.eq("deactivated", myFilter.getDeactivatedUser()));
    }
    if (Login.getInstance().hasExternalUsermanagementSystem() == true) {
      // Check hasExternalUsermngmntSystem because otherwise the filter is may-be preset for an user and the user can't change the filter
      // (because the fields aren't visible).
      if (myFilter.getRestrictedUser() != null) {
        queryFilter.add(Restrictions.eq("restrictedUser", myFilter.getRestrictedUser()));
      }
      if (myFilter.getLocalUser() != null) {
        queryFilter.add(Restrictions.eq("localUser", myFilter.getLocalUser()));
      }
    }
    if (myFilter.getHrPlanning() != null) {
      queryFilter.add(Restrictions.eq("hrPlanning", myFilter.getHrPlanning()));
    }
    queryFilter.addOrder(Order.asc("username"));
    return getList(queryFilter);
  }

  public String getGroupnames(final PFUserDO user)
  {
    if (user == null) {
      return "";
    }
    return userGroupCache.getGroupnames(user.getId());
  }

  public Collection<Integer> getAssignedGroups(final PFUserDO user)
  {
    return userGroupCache.getUserGroups(user);
  }

  public List<UserRightDO> getUserRights(final Integer userId)
  {
    return userGroupCache.getUserRights(userId);
  }

  public String[] getPersonalPhoneIdentifiers(final PFUserDO user)
  {
    final String[] tokens = StringUtils.split(user.getPersonalPhoneIdentifiers(), ", ;|");
    if (tokens == null) {
      return null;
    }
    int n = 0;
    for (final String token : tokens) {
      if (StringUtils.isNotBlank(token) == true) {
        n++;
      }
    }
    if (n == 0) {
      return null;
    }
    final String[] result = new String[n];
    n = 0;
    for (final String token : tokens) {
      if (StringUtils.isNotBlank(token) == true) {
        result[n] = token.trim();
        n++;
      }
    }
    return result;
  }

  public String getNormalizedPersonalPhoneIdentifiers(final PFUserDO user)
  {
    if (StringUtils.isNotBlank(user.getPersonalPhoneIdentifiers()) == true) {
      final String[] ids = getPersonalPhoneIdentifiers(user);
      if (ids != null) {
        final StringBuffer buf = new StringBuffer();
        boolean first = true;
        for (final String id : ids) {
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
  protected void afterSaveOrModify(final PFUserDO user)
  {
    userGroupCache.setExpired();
  }

  /**
   * @see org.projectforge.core.BaseDao#afterDelete(org.projectforge.core.ExtendedBaseDO)
   */
  @Override
  protected void afterDelete(final PFUserDO obj)
  {
    userGroupCache.setExpired();
  }

  /**
   * @see org.projectforge.core.BaseDao#hasAccess(Object, OperationType)
   */
  @Override
  public boolean hasAccess(final PFUserDO user, final PFUserDO obj, final PFUserDO oldObj, final OperationType operationType,
      final boolean throwException)
  {
    return accessChecker.isUserMemberOfAdminGroup(user, throwException);
  }

  /**
   * @return false, if no admin user and the context user is not at minimum in one groups assigned to the given user or false. Also deleted
   *         and deactivated users are only visible for admin users.
   * @see org.projectforge.core.BaseDao#hasSelectAccess(org.projectforge.core.BaseDO, boolean)
   * @see AccessChecker#areUsersInSameGroup(PFUserDO, PFUserDO)
   */
  @Override
  public boolean hasSelectAccess(final PFUserDO user, final PFUserDO obj, final boolean throwException)
  {
    boolean result = accessChecker.isUserMemberOfAdminGroup(user)
        || accessChecker.isUserMemberOfGroup(user, ProjectForgeGroup.FINANCE_GROUP, ProjectForgeGroup.CONTROLLING_GROUP);
    if (result == false && obj.hasSystemAccess() == true) {
      result = accessChecker.areUsersInSameGroup(user, obj);
    }
    if (throwException == true && result == false) {
      throw new AccessException(user, AccessType.GROUP, OperationType.SELECT);
    }
    return result;
  }

  @Override
  public boolean hasSelectAccess(final PFUserDO user, final boolean throwException)
  {
    return true;
  }

  /**
   * @see org.projectforge.core.BaseDao#hasInsertAccess(org.projectforge.user.PFUserDO)
   */
  @Override
  public boolean hasInsertAccess(final PFUserDO user)
  {
    return accessChecker.isUserMemberOfAdminGroup(user, false);
  }

  @Override
  protected void onSaveOrModify(final PFUserDO obj)
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
      final int loginFailures = user.getLoginFailures();
      final Timestamp lastLogin = user.getLastLogin();
      user.setLastLogin(new Timestamp(new Date().getTime()));
      user.setLoginFailures(0);
      user.setMinorChange(true); // Avoid re-indexing of all dependent objects.
      internalUpdate(user, false);
      if (user.hasSystemAccess() == false) {
        log.warn("Deleted/deactivated user tried to login: " + user);
        return null;
      }
      final PFUserDO contextUser = new PFUserDO();
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
    final List<PFUserDO> list = getHibernateTemplate().find("from PFUserDO u where u.username = ? and u.password = ?",
        new Object[] { username, encryptedPassword});
    if (list != null && list.isEmpty() == false && list.get(0) != null) {
      return list.get(0);
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  public PFUserDO getUserByStayLoggedInKey(final String username, final String stayLoggedInKey)
  {
    final List<PFUserDO> list = getHibernateTemplate().find("from PFUserDO u where u.username = ? and u.stayLoggedInKey = ?",
        new Object[] { username, stayLoggedInKey});
    PFUserDO user = null;
    if (list != null && list.isEmpty() == false && list.get(0) != null) {
      user = list.get(0);
    }
    if (user != null && user.hasSystemAccess() == false) {
      log.warn("Deleted/deactivated user tried to login (via stay-logged-in): " + user);
      return null;
    }
    return user;
  }

  /**
   * Does an user with the given username already exists? Works also for existing users (if username was modified).
   * @param user
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
  public String encryptPassword(final String password)
  {
    return Crypt.digest(password);
  }

  /**
   * Changes the user's password. Checks the password quality and the correct authentication for the old password before. Also the
   * stay-logged-in-key will be renewed, so any existing stay-logged-in cookie will be invalid.
   * @param user
   * @param oldPassword
   * @param newPassword
   * @return Error message key if any check failed or null, if successfully changed.
   */
  @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
  public String changePassword(PFUserDO user, final String oldPassword, final String newPassword)
  {
    Validate.notNull(user);
    Validate.notNull(oldPassword);
    Validate.notNull(newPassword);
    final String errorMsgKey = checkPasswordQuality(newPassword);
    if (errorMsgKey != null) {
      return errorMsgKey;
    }
    accessChecker.checkRestrictedOrDemoUser();
    final String encryptedOldPassword = encryptPassword(oldPassword);
    final String encryptedNewPassword = encryptPassword(newPassword);
    user = getUser(user.getUsername(), encryptedOldPassword);
    if (user == null) {
      return MESSAGE_KEY_OLD_PASSWORD_WRONG;
    }
    user.setPassword(encryptedNewPassword);
    user.setStayLoggedInKey(createStayLoggedInKey());
    Login.getInstance().passwordChanged(user, newPassword);
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
    return NumberHelper.getSecureRandomUrlSaveString(STAY_LOGGED_IN_KEY_LENGTH);
  }

  /**
   * Get authentication key by user. ; )
   * 
   * @param userName
   * @param authKey
   * @return
   */
  @SuppressWarnings("unchecked")
  public PFUserDO getUserByAuthenticationToken(final Integer userId, final String authKey)
  {
    final List<PFUserDO> list = getHibernateTemplate().find("from PFUserDO u where u.id = ? and u.authenticationToken = ?",
        new Object[] { userId, authKey});
    PFUserDO user = null;
    if (list != null && list.isEmpty() == false && list.get(0) != null) {
      user = list.get(0);
    }
    if (user != null && user.hasSystemAccess() == false) {
      log.warn("Deleted user tried to login (via authentication token): " + user);
      return null;
    }
    return user;
  }

  /**
   * Returns the user's authentication token if exists (must be not blank with a size >= 10). If not, a new token key will be generated.
   * @param userId
   * @return
   */
  @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
  public String getAuthenticationToken(final Integer userId)
  {
    final PFUserDO user = internalGetById(userId);
    if (StringUtils.isBlank(user.getAuthenticationToken()) || user.getAuthenticationToken().trim().length() < 10) {
      user.setAuthenticationToken(createAuthenticationToken());
      log.info("Authentication token renewed for user: " + userId + " - " + user.getUsername());
    }
    return user.getAuthenticationToken();
  }

  /**
   * Renews the user's authentication token (random string sequence).
   */
  @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
  public void renewAuthenticationToken(final Integer userId)
  {
    final PFUserDO user = internalGetById(userId);
    user.setAuthenticationToken(createAuthenticationToken());
    log.info("Authentication token renewed for user: " + userId + " - " + user.getUsername());
  }

  private String createAuthenticationToken()
  {
    return NumberHelper.getSecureRandomUrlSaveString(AUTHENTICATION_TOKEN_LENGTH);
  }

  /**
   * Uses the context user.
   * @param data
   * @return
   * @see #encrypt(Integer, String)
   */
  public String encrypt(final String data)
  {
    return encrypt(PFUserContext.getUserId(), data);
  }

  /**
   * Encrypts the given str with AES. The key is the current authenticationToken of the given user (by id) (first 16 bytes of it).
   * @param userId
   * @param data
   * @return The base64 encoded result (url safe).
   * @see Crypt#encrypt(String, String)
   */
  public String encrypt(final Integer userId, final String data)
  {
    final String authenticationToken = StringUtils.rightPad(getCachedAuthenticationToken(userId), 32, "x");
    return Crypt.encrypt(authenticationToken, data);
  }

  /**
   * for faster access (due to permanent usage e. g. by subscription of calendars
   * @param userId
   * @return
   */
  private String getCachedAuthenticationToken(final Integer userId)
  {
    final PFUserDO user = userGroupCache.getUser(userId);
    final String authenticationToken = user.getAuthenticationToken();
    if (StringUtils.isBlank(authenticationToken) == false && authenticationToken.trim().length() >= 10) {
      return authenticationToken;
    }
    return getAuthenticationToken(userId);
  }

  /**
   * @param userId
   * @param encryptedString
   * @return The decrypted string.
   * @see Crypt#decrypt(String, String)
   */
  public String decrypt(final Integer userId, final String encryptedString)
  {
    final PFUserDO user = userGroupCache.getUser(userId); // for faster access (due to permanent usage e. g. by subscription of calendars
    // (ics).
    final String authenticationToken = StringUtils.rightPad(getCachedAuthenticationToken(userId), 32, "x");
    return Crypt.decrypt(authenticationToken, encryptedString);
  }

  /**
   * Checks the password quality of a new password. Password must have at least 6 characters and at minimum one letter and one non-letter
   * character.
   * @param newPassword
   * @return null if password quality is OK, otherwise the i18n message key of the password check failure.
   */
  public String checkPasswordQuality(final String newPassword)
  {
    boolean letter = false;
    boolean nonLetter = false;
    if (newPassword == null || newPassword.length() < 6) {
      return MESSAGE_KEY_PASSWORD_QUALITY_CHECK;
    }
    for (int i = 0; i < newPassword.length(); i++) {
      final char ch = newPassword.charAt(i);
      if (letter == false && Character.isLetter(ch) == true) {
        letter = true;
      } else if (nonLetter == false && Character.isLetter(ch) == false) {
        nonLetter = true;
      }
    }
    if (letter == true && nonLetter == true) {
      return null;
    }
    return MESSAGE_KEY_PASSWORD_QUALITY_CHECK;
  }

  @SuppressWarnings("unchecked")
  @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
  public PFUserDO getInternalByName(final String username)
  {
    final List<PFUserDO> list = getHibernateTemplate().find("from PFUserDO u where u.username = ?", username);
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
  public void updateMyAccount(final PFUserDO user)
  {
    accessChecker.checkRestrictedOrDemoUser();
    final PFUserDO contextUser = PFUserContext.getUser();
    Validate.isTrue(user.getId().equals(contextUser.getId()) == true);
    final PFUserDO dbUser = getHibernateTemplate().load(clazz, user.getId(), LockMode.PESSIMISTIC_WRITE);
    if (copyValues(user, dbUser, "deleted", "password", "lastLogin", "loginFailures", "username", "stayLoggedInKey", "authenticationToken",
        "rights") != ModificationStatus.NONE) {
      dbUser.setLastUpdate();
      log.info("Object updated: " + dbUser.toString());
      copyValues(user, contextUser, "deleted", "password", "lastLogin", "loginFailures", "username", "stayLoggedInKey",
          "authenticationToken", "rights");
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
  public List<DisplayHistoryEntry> getDisplayHistoryEntries(final PFUserDO obj)
  {
    final List<DisplayHistoryEntry> list = super.getDisplayHistoryEntries(obj);
    if (hasLoggedInUserHistoryAccess(obj, false) == false) {
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
      public int compare(final DisplayHistoryEntry o1, final DisplayHistoryEntry o2)
      {
        return (o2.getTimestamp().compareTo(o1.getTimestamp()));
      }
    });
    return list;
  }

  @Override
  public boolean hasHistoryAccess(final PFUserDO user, final boolean throwException)
  {
    return accessChecker.isUserMemberOfAdminGroup(user, throwException);
  }

  /**
   * Re-index all dependent objects only if the username, first or last name was changed.
   * @see org.projectforge.core.BaseDao#wantsReindexAllDependentObjects(org.projectforge.core.ExtendedBaseDO,
   *      org.projectforge.core.ExtendedBaseDO)
   */
  @Override
  protected boolean wantsReindexAllDependentObjects(final PFUserDO obj, final PFUserDO dbObj)
  {
    if (super.wantsReindexAllDependentObjects(obj, dbObj) == false) {
      return false;
    }
    return StringUtils.equals(obj.getUsername(), dbObj.getUsername()) == false
        || StringUtils.equals(obj.getFirstname(), dbObj.getFirstname()) == false
        || StringUtils.equals(obj.getLastname(), dbObj.getLastname()) == false;
  }

  @Override
  public PFUserDO newInstance()
  {
    return new PFUserDO();
  }
}
