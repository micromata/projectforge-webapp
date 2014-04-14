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

package org.projectforge.web.wicket;

import java.io.Serializable;
import java.util.TimeZone;

import org.apache.wicket.Session;
import org.apache.wicket.core.request.ClientInfo;
import org.apache.wicket.protocol.http.ClientProperties;
import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.protocol.http.request.WebClientInfo;
import org.apache.wicket.request.Request;
import org.projectforge.Version;
import org.projectforge.common.NumberHelper;
import org.projectforge.core.Configuration;
import org.projectforge.user.PFUserContext;
import org.projectforge.user.PFUserDO;
import org.projectforge.web.UserAgentBrowser;
import org.projectforge.web.UserAgentDetection;
import org.projectforge.web.UserAgentDevice;
import org.projectforge.web.UserAgentOS;

public class MySession extends WebSession
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MySession.class);

  private static final long serialVersionUID = -1783696379234637066L;

  private PFUserDO user;

  private String userAgent;

  private UserAgentDevice userAgentDevice = UserAgentDevice.UNKNOWN;

  private UserAgentBrowser userAgentBrowser = UserAgentBrowser.UNKNOWN;

  private ClientProperties clientProperties;

  private String userAgentBrowserVersionString = null;

  private Version userAgentBrowserVersion = null;

  private UserAgentOS userAgentOS = UserAgentOS.UNKNOWN;

  private boolean mobileUserAgent;

  private boolean ignoreMobileUserAgent;

  /**
   * Random cross site request forgery token.
   */
  private final String csrfToken;

  public MySession(final Request request)
  {
    super(request);
    setLocale(request);
    final ClientInfo info = getClientInfo();
    if (info instanceof WebClientInfo) {
      clientProperties = ((WebClientInfo) clientInfo).getProperties();
      clientProperties.setTimeZone(PFUserContext.getTimeZone());
      userAgent = ((WebClientInfo) info).getUserAgent();
      userAgentDevice = UserAgentDevice.getUserAgentDevice(userAgent);
      userAgentOS = UserAgentOS.getUserAgentOS(userAgent);
      mobileUserAgent = userAgentDevice.isMobile();
      final UserAgentDetection userAgentDetection = UserAgentDetection.browserDetect(userAgent);
      userAgentBrowser = userAgentDetection.getUserAgentBrowser();
      userAgentBrowserVersionString = userAgentDetection.getUserAgentBrowserVersion();
    } else {
      log.error("Oups, ClientInfo is not from type WebClientInfo: " + info);
    }
    setUser(PFUserContext.getUser());
    this.csrfToken = NumberHelper.getSecureRandomUrlSaveString(20);
  }

  public static MySession get()
  {
    return (MySession) Session.get();
  }

  /**
   * @return The logged-in user or null if no user is logged-in.
   */
  public synchronized PFUserDO getUser()
  {
    return user;
  }

  /**
   * @return the randomized cross site request forgery token
   */
  public String getCsrfToken()
  {
    return csrfToken;
  }

  /**
   * @return The id of the logged-in user or null if no user is logged-in.
   */
  public synchronized Integer getUserId()
  {
    return user != null ? user.getId() : null;
  }

  public synchronized void setUser(final PFUserDO user)
  {
    this.user = user;
    dirty();
  }

  public synchronized boolean isAuthenticated()
  {
    return (user != null);
  }

  public synchronized TimeZone getTimeZone()
  {
    return user != null ? user.getTimeZoneObject() : Configuration.getInstance().getDefaultTimeZone();
  }

  public String getUserAgent()
  {
    return userAgent;
  }

  /**
   * @return the userAgentOS
   */
  public UserAgentOS getUserAgentOS()
  {
    return userAgentOS;
  }

  /**
   * @return true, if the user agent device is an iPad, iPhone or iPod.
   */
  public boolean isIOSDevice()
  {
    return this.userAgentDevice != null && this.userAgentDevice.isIn(UserAgentDevice.IPAD, UserAgentDevice.IPHONE, UserAgentDevice.IPOD);
  }

  /**
   * @return true, if the user agent is a mobile agent and ignoreMobileUserAgent isn't set, otherwise false.
   */
  public boolean isMobileUserAgent()
  {
    if (ignoreMobileUserAgent == true) {
      return false;
    }
    return mobileUserAgent;
  }

  /**
   * The user wants to ignore the mobile agent and wants to get the PC version (normal web version).
   * @return
   */
  public boolean isIgnoreMobileUserAgent()
  {
    return ignoreMobileUserAgent;
  }


  /**
   * @return the userAgentBrowser
   */
  public UserAgentBrowser getUserAgentBrowser()
  {
    return userAgentBrowser;
  }

  /**
   * @return the userAgentBrowserVersion
   */
  public String getUserAgentBrowserVersionString()
  {
    return userAgentBrowserVersionString;
  }

  /**
   * @return the userAgentBrowserVersion
   */
  public Version getUserAgentBrowserVersion()
  {
    if (userAgentBrowserVersion == null && userAgentBrowserVersionString != null) {
      userAgentBrowserVersion = new Version(userAgentBrowserVersionString);
    }
    return userAgentBrowserVersion;
  }

  /**
   * @return the userAgentDevice
   */
  public UserAgentDevice getUserAgentDevice()
  {
    return userAgentDevice;
  }

  public void setIgnoreMobileUserAgent(final boolean ignoreMobileUserAgent)
  {
    this.ignoreMobileUserAgent = ignoreMobileUserAgent;
  }

  public void login(final PFUserDO user, final Request request)
  {
    if (user == null) {
      log.warn("Oups, no user given to log in.");
      return;
    }
    this.user = user;
    log.debug("User logged in: " + user.getShortDisplayName());
    PFUserContext.setUser(user);
    setLocale(request);
  }

  /**
   * Sets or updates the locale of the user's session. Takes the locale of the user account or if not given the locale of the given request.
   * @param request
   */
  public void setLocale(final Request request)
  {
    setLocale(PFUserContext.getLocale(request.getLocale()));
  }

  public void logout()
  {
    if (user != null) {
      log.info("User logged out: " + user.getShortDisplayName());
      user = null;
    }
    PFUserContext.setUser(null);
    super.clear();
    super.invalidate();
  }

  public void put(final String name, final Serializable value)
  {
    super.setAttribute(name, value);
  }

  public Object get(final String name)
  {
    return super.getAttribute(name);
  }

  /**
   * @return the clientProperties
   */
  public ClientProperties getClientProperties()
  {
    return clientProperties;
  }
}
