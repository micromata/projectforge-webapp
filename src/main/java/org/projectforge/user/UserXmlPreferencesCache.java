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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

  private Map<Integer, UserXmlPreferencesMap> allPreferences = new HashMap<Integer, UserXmlPreferencesMap>();

  UserXmlPreferencesDao userXmlPreferencesDao;

  public void setUserXmlPreferencesDao(UserXmlPreferencesDao userXmlPreferencesDao)
  {
    this.userXmlPreferencesDao = userXmlPreferencesDao;
  }

  /**
   * @see org.projectforge.user.UserXmlPreferencesMap#putEntry(String, Object, boolean)
   */
  public void putEntry(Integer userId, String key, Object value, boolean persistent)
  {
    UserXmlPreferencesMap data = ensureAndGetUserPreferencesData(userId);
    data.putEntry(key, value, persistent);
    checkRefresh(); // Should be called at the end of this method for considering changes inside this method.
  }

  /**
   * @see #ensureAndGetUserPreferencesData(Integer)
   */
  public Object getEntry(Integer userId, String key)
  {
    UserXmlPreferencesMap data = ensureAndGetUserPreferencesData(userId);
    checkRefresh();
    return data.getEntry(key);
  }

  /**
   * @see org.projectforge.user.UserXmlPreferencesMap#removeEntry(String)
   */
  public Object removeEntry(Integer userId, String key)
  {
    UserXmlPreferencesMap data = getUserPreferencesData(userId);
    if (data == null) {
      // Should only occur for the pseudo-first-login-user setting up the system.
      return null;
    }
    if (data.getPersistentData().containsKey(key) == true) {
      userXmlPreferencesDao.remove(userId, key);
    } else if (data.getVolatileData().containsKey(key) == false) {
      log.warn("Oups, user preferences object with key '" + key + "' is wether persistent nor volatile!");
    }
    if (data != null) {
      checkRefresh();
      return data.removeEntry(key);
    }
    return null;
  }

  public synchronized UserXmlPreferencesMap ensureAndGetUserPreferencesData(Integer userId)
  {
    UserXmlPreferencesMap data = getUserPreferencesData(userId);
    if (data == null) {
      data = new UserXmlPreferencesMap();
      data.setUserId(userId);
      final List<UserXmlPreferencesDO> userPrefs = userXmlPreferencesDao.getUserPreferencesByUserId(userId);
      for (UserXmlPreferencesDO userPref : userPrefs) {
        Object value = userXmlPreferencesDao.deserialize(userPref, true);
        data.putEntry(userPref.getKey(), value, true);
      }
      this.allPreferences.put(userId, data);
    }
    return data;
  }

  UserXmlPreferencesMap getUserPreferencesData(Integer userId)
  {
    return this.allPreferences.get(userId);
  }

  void setUserPreferencesData(Integer userId, UserXmlPreferencesMap data)
  {
    this.allPreferences.put(userId, data);
  }

  /**
   * Flushes the user settings to the database (independent from the expire mechanism). Should be used after the user's logout. If the user
   * data isn't modified, then nothing will be done.
   */
  public void flushToDB(Integer userId)
  {
    flushToDB(userId, true);
  }

  private synchronized void flushToDB(Integer userId, boolean checkAccess)
  {
    UserXmlPreferencesMap data = allPreferences.get(userId);
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
    for (Integer userId : allPreferences.keySet()) {
      flushToDB(userId, false);
    }
  }

  /**
   * Clear all volatile data (after logout). Forces refreshing of volatile data after re-login.
   * @param userId
   */
  public void clear(Integer userId)
  {
    UserXmlPreferencesMap data = allPreferences.get(userId);
    if (data == null) {
      return;
    }
    data.clear();
  }
}
