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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.projectforge.access.AccessChecker;
import org.projectforge.common.AbstractCache;

/**
 * Stores all user persistent objects such as filter settings, personal settings and persists them to the database.
 * 
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class UserXmlPreferencesCache extends AbstractCache
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(UserXmlPreferencesCache.class);

  private final Map<Integer, UserXmlPreferencesMap> allPreferences = new HashMap<Integer, UserXmlPreferencesMap>();

  private UserXmlPreferencesDao userXmlPreferencesDao;

  private AccessChecker accessChecker;

  private static UserXmlPreferencesCache instance;

  public static void setInternalInstance(final UserXmlPreferencesCache cache)
  {
    instance = cache;
  }

  public static UserXmlPreferencesCache getDefaultInstance()
  {
    return instance;
  }

  public void setUserXmlPreferencesDao(final UserXmlPreferencesDao userXmlPreferencesDao)
  {
    this.userXmlPreferencesDao = userXmlPreferencesDao;
  }

  public void setAccessChecker(final AccessChecker accessChecker)
  {
    this.accessChecker = accessChecker;
  }

  /**
   * Put the entry for the current logged in user.
   * @see org.projectforge.user.UserXmlPreferencesMap#putEntry(String, Object, boolean)
   */
  public void putEntry(final String key, final Object value, final boolean persistent)
  {
    putEntry(PFUserContext.getUserId(), key, value, persistent);
  }

  /**
   * @see org.projectforge.user.UserXmlPreferencesMap#putEntry(String, Object, boolean)
   */
  public void putEntry(final Integer userId, final String key, final Object value, final boolean persistent)
  {
    final UserXmlPreferencesMap data = ensureAndGetUserPreferencesData(userId);
    if (accessChecker.isDemoUser(userId) == true) {
      data.putEntry(key, value, false);
    } else {
      data.putEntry(key, value, persistent);
    }
    checkRefresh(); // Should be called at the end of this method for considering changes inside this method.
  }

  /**
   * Gets the entry for the current logged in user.
   * @see #ensureAndGetUserPreferencesData(Integer)
   */
  public Object getEntry(final String key)
  {
    return getEntry(PFUserContext.getUserId(), key);
  }

  /**
   * @see #ensureAndGetUserPreferencesData(Integer)
   */
  public Object getEntry(final Integer userId, final String key)
  {
    final UserXmlPreferencesMap data = ensureAndGetUserPreferencesData(userId);
    checkRefresh();
    return data.getEntry(key);
  }

  /**
   * Remove the entry of the current logged in user.
   * @see #removeEntry(Integer, String)
   */
  public void removeEntry(final String key)
  {
    removeEntry(PFUserContext.getUserId(), key);
  }

  /**
   * @see org.projectforge.user.UserXmlPreferencesMap#removeEntry(String)
   */
  public Object removeEntry(final Integer userId, final String key)
  {
    final UserXmlPreferencesMap data = getUserPreferencesData(userId);
    if (data == null) {
      // Should only occur for the pseudo-first-login-user setting up the system.
      return null;
    }
    if (data.getPersistentData().containsKey(key) == true) {
      userXmlPreferencesDao.remove(userId, key);
    } else if (data.getVolatileData().containsKey(key) == false) {
      log.warn("Oups, user preferences object with key '" + key + "' is wether persistent nor volatile!");
    }
    checkRefresh();
    return data.removeEntry(key);
  }

  public synchronized UserXmlPreferencesMap ensureAndGetUserPreferencesData(final Integer userId)
  {
    UserXmlPreferencesMap data = getUserPreferencesData(userId);
    if (data == null) {
      data = new UserXmlPreferencesMap();
      data.setUserId(userId);
      final List<UserXmlPreferencesDO> userPrefs = userXmlPreferencesDao.getUserPreferencesByUserId(userId);
      for (final UserXmlPreferencesDO userPref : userPrefs) {
        final Object value = userXmlPreferencesDao.deserialize(userPref, true);
        data.putEntry(userPref.getKey(), value, true);
      }
      this.allPreferences.put(userId, data);
    }
    return data;
  }

  UserXmlPreferencesMap getUserPreferencesData(final Integer userId)
  {
    return this.allPreferences.get(userId);
  }

  void setUserPreferencesData(final Integer userId, final UserXmlPreferencesMap data)
  {
    this.allPreferences.put(userId, data);
  }

  /**
   * Flushes the user settings to the database (independent from the expire mechanism). Should be used after the user's logout. If the user
   * data isn't modified, then nothing will be done.
   */
  public void flushToDB(final Integer userId)
  {
    flushToDB(userId, true);
  }

  private synchronized void flushToDB(final Integer userId, final boolean checkAccess)
  {
    final UserXmlPreferencesMap data = allPreferences.get(userId);
    if (data == null || data.isModified() == false) {
      return;
    }
    userXmlPreferencesDao.saveOrUpdateUserEntries(userId, data, checkAccess);
  }

  /**
   * Stores the PersistentUserObjects in the database or on start up restores the persistent user objects from the database.
   * @see org.projectforge.common.AbstractCache#refresh()
   */
  @Override
  protected void refresh()
  {
    log.info("Flushing all user preferences to data-base....");
    for (final Integer userId : allPreferences.keySet()) {
      flushToDB(userId, false);
    }
    log.info("Flushing of user preferences to data-base done.");
  }

  /**
   * Clear all volatile data (after logout). Forces refreshing of volatile data after re-login.
   * @param userId
   */
  public void clear(final Integer userId)
  {
    final UserXmlPreferencesMap data = allPreferences.get(userId);
    if (data == null) {
      return;
    }
    data.clear();
  }
}
