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
import java.util.Iterator;
import java.util.Map;

/**
 * Class for avoiding brute force attacks by time offsets during login after failed login attempts. Usage:<br/>
 * 
 * <pre>
 * public boolean login(String clientIp, String username, String password)
 * {
 *   long offset = LoginProtection.instance().getFailedLoginTimeOffsetIfExists(clientIp);
 *   if (offset &gt; 0) {
 *     // setResponsePage(MessagePage.class, &quot;Your account is locked for &quot; + offset / 1000 +
 *     // &quot; seconds due to failed login attempts. Please try again later.&quot;);
 *     return false;
 *   }
 *   boolean success = checkLogin(username, password); // Check the login however you want.
 *   if (success == true) {
 *     LoginProtection.instance().clearLoginTimeOffset(clientIp);
 *     return true;
 *   } else {
 *     LoginProtection.instance().incrementFailedLoginTimeOffset(clientIp);
 *     return false;
 *   }
 * }
 * </pre>
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class LoginProtection
{
  /**
   * Login offset time after failed login attempts expires after 24h.
   */
  private static final long LOGIN_OFFSET_EXPIRES_AFTER_MS = 24 * 60 * 60 * 1000;

  /**
   * Login time offset will be number of failed logins multiplied by this value (in ms).
   */
  private static final long LOGIN_TIME_OFFSET_SCALE = 1000;

  private static final LoginProtection instance = new LoginProtection();

  public static LoginProtection instance()
  {
    return instance;
  }

  /**
   * Singleton.
   */
  private LoginProtection()
  {
  }

  /**
   * Number of failed logins per IP address.
   */
  private final Map<String, Integer> loginFailedAttemptsMap = new HashMap<String, Integer>();

  /**
   * Time stamp of last failed login per userId in ms since 01/01/1970.
   * @see System#currentTimeMillis()
   */
  private final Map<String, Long> lastFailedLoginMap = new HashMap<String, Long>();

  /**
   * Call this before checking the login credentials. If a long > 0 is returned please don't proceed the login-procedure. Please display a
   * user message that the login was denied due previous failed login attempts. The user should try it later again (after x seconds).
   * @param userId This could be the client's ip address, the login name etc.
   * @return 0 if no active time offset was found, otherwise the time offset left until the account is opened again for login.
   */
  public long getFailedLoginTimeOffsetIfExists(final String userId)
  {
    final Long lastFailedLoginInMs = this.lastFailedLoginMap.get(userId);
    if (lastFailedLoginInMs == null) {
      return 0;
    }
    final long offset = getFailedLoginTimeOffset(userId, false);
    final long currentTimeInMs = System.currentTimeMillis();
    if (lastFailedLoginInMs + offset < currentTimeInMs) {
      return 0;
    }
    return lastFailedLoginInMs + offset - currentTimeInMs;
  }

  /**
   * Increments the number of login failures.
   * @param userId This could be the client's ip address, the login name etc.
   * @return Login time offset in ms.
   */
  public long incrementFailedLoginTimeOffset(final String userId)
  {
    return getFailedLoginTimeOffset(userId, true);
  }

  /**
   * @param userId This could be the client's ip address, the login name etc.
   * @param increment If true the login fail counter will be incremented.
   * @return
   */
  private long getFailedLoginTimeOffset(final String userId, final boolean increment)
  {
    clearExpiredEntries();
    final long currentTimeInMillis = System.currentTimeMillis();
    Integer numberOfFailedLogins = this.loginFailedAttemptsMap.get(userId);
    if (numberOfFailedLogins == null) {
      if (increment == false) {
        return 0;
      }
      numberOfFailedLogins = 0;
    } else {
      final Long lastFailedLoginInMs = this.lastFailedLoginMap.get(userId);
      if (lastFailedLoginInMs != null && currentTimeInMillis - lastFailedLoginInMs > LOGIN_OFFSET_EXPIRES_AFTER_MS) {
        // Last failed login entry is to old, so we'll ignore and clear it:
        clearLoginTimeOffset(userId);
        if (increment == false) {
          return 0;
        }
        numberOfFailedLogins = 0;
      }
    }
    if (increment == true) {
      synchronized (this) {
        this.loginFailedAttemptsMap.put(userId, ++numberOfFailedLogins);
        this.lastFailedLoginMap.put(userId, currentTimeInMillis);
      }
    }
    return numberOfFailedLogins * LOGIN_TIME_OFFSET_SCALE;
  }

  /**
   * Call this method after successful authentication. The counter of failed logins will be cleared.
   * @param userId This could be the client's ip address, the login name etc.
   */
  public void clearLoginTimeOffset(final String userId)
  {
    synchronized (this) {
      this.loginFailedAttemptsMap.remove(userId);
      this.lastFailedLoginMap.remove(userId);
    }
  }

  /**
   * Clears (removes) all entries for userId's older than {@link #LOGIN_OFFSET_EXPIRES_AFTER_MS}.
   */
  public void clearExpiredEntries()
  {
    final long currentTimeInMillis = System.currentTimeMillis();
    synchronized (this) {
      final Iterator<String> it = this.lastFailedLoginMap.keySet().iterator();
      while (it.hasNext() == true) {
        final String key = it.next();
        final Long lastFailedLoginInMs = this.lastFailedLoginMap.get(key);
        if (lastFailedLoginInMs != null && currentTimeInMillis - lastFailedLoginInMs > LOGIN_OFFSET_EXPIRES_AFTER_MS) {
          // Last failed login entry is to old, so we'll ignore and clear it:
          this.loginFailedAttemptsMap.remove(key);
          it.remove();
        }
      }
    }
  }

  /**
   * Clears all entries of failed logins (counter and time stamps).
   */
  public void clearAll()
  {
    synchronized (this) {
      this.loginFailedAttemptsMap.clear();
      this.lastFailedLoginMap.clear();
    }
  }

  /**
   * For internal use by test cases.
   */
  int getSizeOfLastFailedLoginMap()
  {
    return this.lastFailedLoginMap.size();
  }

  /**
   * For internal use by test cases.
   */
  int getSizeOfLoginFailedAttemptsMap()
  {
    return this.loginFailedAttemptsMap.size();
  }

  /**
   * For internal use by test cases.
   */
  void setEntry(final String userId, final int numberOfFailedLoginAttempts, final long lastFailedAttemptTimestamp)
  {
    synchronized (this) {
      this.loginFailedAttemptsMap.put(userId, numberOfFailedLoginAttempts);
      this.lastFailedLoginMap.put(userId, lastFailedAttemptTimestamp);
    }
  }

  /**
   * @return The number of failed login attempts (not expired ones) if exist, otherwise 0.
   */
  public int getNumberOfFailedLoginAttempts(final String userId)
  {
    final Integer result = this.loginFailedAttemptsMap.get(userId);
    return result != null ? result : 0;
  }
}
