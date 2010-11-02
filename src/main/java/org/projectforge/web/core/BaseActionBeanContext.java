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

package org.projectforge.web.core;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.Resolution;

import org.apache.log4j.Logger;
import org.projectforge.common.DateHelper;
import org.projectforge.user.PFUserContext;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserXmlPreferencesCache;
import org.projectforge.web.wicket.WicketApplication;

import de.micromata.user.ContextHolder;

/**
 */
public class BaseActionBeanContext extends ActionBeanContext
{
  private final static Logger log = Logger.getLogger(BaseActionBeanContext.class);

  private SessionStorage sessionStorage;

  private UserXmlPreferencesCache userXmlPreferencesCache;

  private Resolution sourcePageResolution;

  /**
   * @param name The request parameter name
   * @return The value of the request parameter or null if not found in request.
   */
  public String getRequestParameter(String name)
  {
    return getRequest().getParameter(name);
  }

  public Locale getLocale()
  {
    return PFUserContext.getLocale(getRequestLocale());
  }

  public Locale getRequestLocale()
  {
    return getRequest().getLocale();
  }

  public TimeZone getTimeZone()
  {
    return PFUserContext.getTimeZone();
  }

  /**
   * Get current time considering the users locale.
   * @see DateHelper#getCalendar()
   */
  public Date now()
  {
    Calendar cal = DateHelper.getCalendar();
    return cal.getTime();
  }

  /** Logs the user out by invalidating the session. */
  public void logout()
  {
    PFUserDO user = getUser();
    if (user != null) {
      flushToDB();
    }
    getRequest().getSession().removeAttribute("de.micromata.user.effectiveUserId");
    getRequest().getSession().removeAttribute("de.micromata.user.realUserId");
    sessionStorage.clearSession(getRequest().getSession());
    userXmlPreferencesCache.clear(user.getId());
    getRequest().getSession().invalidate();
    ContextHolder.setUserInfo(null);
    if (user != null) {
      log.info("User logged out: " + user.getShortDisplayName());
    }
  }

  /**
   * Needed for displaying current user on every page.
   * @return
   */
  public boolean isLoggedIn()
  {
    return getUser() != null;
  }

  /**
   * @return The current logged in user (stored in the context).
   */
  public PFUserDO getUser()
  {
    return (PFUserDO) ContextHolder.getUserInfo();
  }

  /**
   * @see WicketApplication#getAlertMessage()
   */
  public String getAlertMessage()
  {
    return WicketApplication.getAlertMessage();
  }

  /**
   * @see WicketApplication#setAlertMessage(String)
   */
  public static void setAlertMessage(String msg)
  {
    WicketApplication.setAlertMessage(msg);
  }

  public void setSessionStorage(SessionStorage sessionStorage)
  {
    this.sessionStorage = sessionStorage;
  }

  public void setUserXmlPreferencesCache(UserXmlPreferencesCache userXmlPreferencesCache)
  {
    this.userXmlPreferencesCache = userXmlPreferencesCache;
  }

  /**
   * Stores the given value for the current user.
   * @param key
   * @param value
   * @param persistent If true, the object will be persisted in the database.
   * @see UserXmlPreferencesCache#putEntry(Integer, String, Object, boolean)
   */
  public void putEntry(String key, Object value, boolean persistent)
  {
    if (getUser() == null) {
      // Should only occur, if user is not logged in.
      return;
    }
    Integer userId = getUser().getId();
    userXmlPreferencesCache.putEntry(userId, key, value, persistent);
  }

  /**
   * Gets the stored user preference entry.
   * @param key
   * @return Return a persistent object with this key, if existing, or if not a volatile object with this key, if existing, otherwise null;
   * @see UserXmlPreferencesCache#getEntry(Integer, String)
   */
  public Object getEntry(String key)
  {
    if (getUser() == null) {
      // Should only occur, if user is not logged in.
      return null;
    }
    Integer userId = getUser().getId();
    return userXmlPreferencesCache.getEntry(userId, key);
  }

  /**
   * Removes the entry under the given key.
   * @param key
   * @return The removed entry if found.
   */
  public Object removeEntry(String key) {
    if (getUser() == null) {
      // Should only occur, if user is not logged in.
      return null;
    }
    Integer userId = getUser().getId();
    return userXmlPreferencesCache.removeEntry(userId, key);
  }

  /**
   * @see UserXmlPreferencesCache#flushToDB(Integer)
   */
  public void flushToDB()
  {
    userXmlPreferencesCache.flushToDB(getUser().getId());
  }

  /**
   * Forces to flush all user preferences to database.
   */
  public void flushAllToDB()
  {
    userXmlPreferencesCache.forceReload();
  }

  public void setSourcePageResolution(Resolution res)
  {
    sourcePageResolution = res;
  }

  @Override
  public Resolution getSourcePageResolution()
  {
    if (sourcePageResolution != null) {
      return sourcePageResolution;
    }
    return super.getSourcePageResolution();
  }
}
