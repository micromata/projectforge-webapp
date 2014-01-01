/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2014 Kai Reinhard (k.reinhard@micromata.de)
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

package org.projectforge.web.user;

import java.io.Serializable;

import org.projectforge.common.CloneHelper;
import org.projectforge.user.PFUserContext;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserRights;
import org.projectforge.user.UserXmlPreferencesCache;
import org.projectforge.web.wicket.MySession;

public class UserPreferencesHelper
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(UserPreferencesHelper.class);

  /**
   * Stores the given value for the current user.
   * @param key
   * @param value
   * @param persistent If true, the object will be persisted in the database.
   * @see UserXmlPreferencesCache#putEntry(Integer, String, Object, boolean)
   */
  public static void putEntry(final String key, final Object value, final boolean persistent)
  {
    final PFUserDO user = PFUserContext.getUser();
    if (user == null || value == null) {
      // Should only occur, if user is not logged in.
      return;
    }
    final Integer userId = user.getId();
    if (UserRights.getAccessChecker().isDemoUser(userId) == true && value instanceof Serializable) {
      // Store user pref for demo user only in user's session.
      MySession.get().setAttribute(key, (Serializable) value);
      return;
    }
    final UserXmlPreferencesCache userXmlPreferencesCache = UserXmlPreferencesCache.getDefaultInstance();
    userXmlPreferencesCache.putEntry(userId, key, value, persistent);
  }

  /**
   * Gets the stored user preference entry.
   * @param key
   * @return Return a persistent object with this key, if existing, or if not a volatile object with this key, if existing, otherwise null;
   * @see UserXmlPreferencesCache#getEntry(Integer, String)
   */
  public static Object getEntry(final String key)
  {
    final PFUserDO user = PFUserContext.getUser();
    if (user == null) {
      // Should only occur, if user is not logged in.
      return null;
    }
    final UserXmlPreferencesCache userXmlPreferencesCache = UserXmlPreferencesCache.getDefaultInstance();
    final Integer userId = user.getId();
    if (UserRights.getAccessChecker().isDemoUser(userId) == true) {
      // Store user pref for demo user only in user's session.
      Object value = MySession.get().getAttribute(key);
      if (value != null) {
        return value;
      }
      value = userXmlPreferencesCache.getEntry(userId, key);
      if (value == null || value instanceof Serializable == false) {
        return null;
      }
      value = CloneHelper.cloneBySerialization(value);
      MySession.get().setAttribute(key, (Serializable) value);
      return value;
    }
    return userXmlPreferencesCache.getEntry(userId, key);
  }

  /**
   * Gets the stored user preference entry.
   * @param key
   * @param expectedType Checks the type of the user pref entry (if found) and returns only this object if the object is from the expected
   *          type, otherwise null is returned.
   * @return Return a persistent object with this key, if existing, or if not a volatile object with this key, if existing, otherwise null;
   * @see UserXmlPreferencesCache#getEntry(Integer, String)
   */
  public static Object getEntry(final Class< ? > expectedType, final String key)
  {
    final Object entry = getEntry(key);
    if (entry == null) {
      return null;
    }
    if (expectedType.isAssignableFrom(entry.getClass()) == true) {
      return entry;
    }
    // Probably a new software release results in an incompability of old and new object format.
    log.info("Could not get user preference entry: (old) type "
        + entry.getClass().getName()
        + " is not assignable to (new) required type "
        + expectedType.getName()
        + " (OK, probably new software release).");
    return null;
  }

  /**
   * Removes the entry under the given key.
   * @param key
   * @return The removed entry if found.
   */
  public static Object removeEntry(final String key)
  {
    final PFUserDO user = PFUserContext.getUser();
    if (user == null) {
      // Should only occur, if user is not logged in.
      return null;
    }
    final Integer userId = user.getId();
    if (UserRights.getAccessChecker().isDemoUser(userId) == true) {
      MySession.get().removeAttribute(key);
    }
    final UserXmlPreferencesCache userXmlPreferencesCache = UserXmlPreferencesCache.getDefaultInstance();
    return userXmlPreferencesCache.removeEntry(userId, key);
  }
}
