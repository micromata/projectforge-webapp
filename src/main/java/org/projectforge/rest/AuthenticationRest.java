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

package org.projectforge.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.projectforge.AppVersion;
import org.projectforge.Version;
import org.projectforge.user.PFUserContext;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserDao;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * REST interface for authentication (tests) and getting the authentication token on initial contact.
 * 
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
@Path("authenticate")
public class AuthenticationRest
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AuthenticationRest.class);

  @Autowired
  private UserDao userDao;

  /**
   * For getting the user's authentication token. This token can be stored in the client (e. g. mobile app). The user's password shouldn't
   * be stored in the client for security reasons. The authentication token is renewable through the ProjectForge's web app (my account).
   * @return {@link UserObject}
   */
  @GET
  @Path("getToken")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getToken()
  {
    final PFUserDO user = PFUserContext.getUser();
    if (user == null) {
      log.error("No user given for rest call.");
      throw new IllegalArgumentException("No user given for the rest call: authenticate/getToken.");
    }
    final UserObject userObject = new UserObject(user);
    user.setAuthenticationToken(user.getAuthenticationToken());
    return Response.ok(userObject).build();
  }

  @GET
  @Path("initialContact")
  @Produces(MediaType.APPLICATION_JSON)
  public Response initialContact(@QueryParam("clientVersion") final String clientVersionString)
  {
    final PFUserDO user = PFUserContext.getUser();
    if (user == null) {
      log.error("No user given for rest call.");
      throw new IllegalArgumentException("No user given for the rest call: authenticate/getToken.");
    }
    final ServerInfo info = new ServerInfo(user, AppVersion.VERSION.toString());
    Version clientVersion = null;
    if (clientVersionString != null) {
      clientVersion = new Version(clientVersionString);
    }
    if (clientVersion == null) {
      info.setStatus(ServerInfo.STATUS_UNKNOWN);
    } else if (clientVersion.compareTo(new Version("5.0")) < 0) {
      info.setStatus(ServerInfo.STATUS_CLIENT_TO_OLD);
    } else if (clientVersion.compareTo(AppVersion.VERSION) > 0) {
      info.setStatus(ServerInfo.STATUS_CLIENT_NEWER_THAN_SERVER);
    } else {
      info.setStatus(ServerInfo.STATUS_OK);
    }
    return Response.ok(info).build();
  }

  /**
   * @param userDao the userDao to set
   * @return this for chaining.
   */
  public void setUserDao(final UserDao userDao)
  {
    this.userDao = userDao;
  }
}
