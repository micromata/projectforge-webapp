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

package org.projectforge.web.wicket;

import java.util.TimeZone;

import org.apache.wicket.Request;
import org.apache.wicket.Session;
import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.protocol.http.request.WebClientInfo;
import org.apache.wicket.request.ClientInfo;
import org.projectforge.core.Configuration;
import org.projectforge.user.PFUserContext;
import org.projectforge.user.PFUserDO;

import de.micromata.user.ContextHolder;

public class MySession extends WebSession
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MySession.class);

  private static final long serialVersionUID = -1783696379234637066L;

  private PFUserDO user;

  public MySession(final Request request)
  {
    super(request);
    setLocale(PFUserContext.getLocale(request.getLocale()));
    final ClientInfo info = getClientInfo();
    if (info instanceof WebClientInfo) {
      ((WebClientInfo) info).getProperties().setTimeZone(PFUserContext.getTimeZone());
    } else {
      log.error("Oups, ClientInfo is not from type WebClientInfo: " + info);
    }
    setUser(PFUserContext.getUser());
  }

  public static MySession get()
  {
    return (MySession) Session.get();
  }

  public synchronized PFUserDO getUser()
  {
    return user;
  }

  public synchronized void setUser(PFUserDO user)
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

  public void login(final PFUserDO user)
  {
    if (user == null) {
      log.warn("Oups, no user given to log in.");
      return;
    }
    this.user = user;
    log.info("User logged in: " + user.getShortDisplayName());
    PFUserContext.setUser(user);
  }

  public void logout()
  {
    if (user != null) {
      log.info("User logged out: " + user.getShortDisplayName());
      user = null;
    }
    ContextHolder.setUserInfo(null);
    super.clear();
    super.invalidate();
  }

  public void put(final String name, final Object value)
  {
    super.setAttribute(name, value);
  }

  public Object get(final String name)
  {
    return super.getAttribute(name);
  }
}
