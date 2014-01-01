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

package org.projectforge.web.rest;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.mockito.Mockito;
import org.projectforge.core.ProjectForgeApp;
import org.projectforge.rest.Authentication;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserDao;
import org.projectforge.user.UserGroupCache;
import org.projectforge.web.wicket.WicketApplication;

public class RestUserFilterTest
{
  @Test
  public void testAuthentication() throws IOException, ServletException, InterruptedException
  {
    ProjectForgeApp.init(null, null);
    WicketApplication.internalSetUpAndRunning(true);
    final HttpServletResponse response = mock(HttpServletResponse.class);
    final UserDao userDao = mock(UserDao.class);
    when(userDao.authenticateUser(Mockito.eq("successUser"), Mockito.eq("successPassword"))).thenReturn(
        new PFUserDO().setUsername("successUser"));
    when(userDao.getCachedAuthenticationToken(Mockito.eq(2))).thenReturn("token");
    final UserGroupCache userGroupCache = mock(UserGroupCache.class);
    when(userDao.getUserGroupCache()).thenReturn(userGroupCache);
    when(userGroupCache.getUser(Mockito.eq(2))).thenReturn(new PFUserDO().setUsername("testuser"));
    final RestUserFilter filter = new RestUserFilter();
    filter.userDao = userDao;

    // Wrong password
    HttpServletRequest request = mockRequest("successUser", "failed", null, null);
    FilterChain chain = mock(FilterChain.class);
    filter.doFilter(request, response, chain);
    verify(chain, never()).doFilter(Mockito.any(HttpServletRequest.class), Mockito.any(HttpServletResponse.class));
    Thread.sleep(1100); // Login penalty.
    // Correct user name and password
    request = mockRequest("successUser", "successPassword", null, null);
    chain = mock(FilterChain.class);
    filter.doFilter(request, response, chain);
    verify(chain).doFilter(Mockito.eq(request), Mockito.eq(response));

    // Wrong token
    request = mockRequest(null, null, 2, "wrongToken");
    chain = mock(FilterChain.class);
    filter.doFilter(request, response, chain);
    verify(chain, never()).doFilter(Mockito.any(HttpServletRequest.class), Mockito.any(HttpServletResponse.class));
    Thread.sleep(2100); // Login penalty.
    // Correct user name and password
    request = mockRequest(null, null, 2, "token");
    chain = mock(FilterChain.class);
    filter.doFilter(request, response, chain);
    verify(chain).doFilter(Mockito.eq(request), Mockito.eq(response));
  }

  private HttpServletRequest mockRequest(final String username, final String password, final Integer userId,
      final String authenticationToken)
  {
    final HttpServletRequest request = mock(HttpServletRequest.class);
    if (username != null) {
      when(request.getHeader(Mockito.eq(Authentication.AUTHENTICATION_USERNAME))).thenReturn(username);
    }
    if (password != null) {
      when(request.getHeader(Mockito.eq(Authentication.AUTHENTICATION_PASSWORD))).thenReturn(password);
    }
    if (userId != null) {
      when(request.getHeader(Mockito.eq(Authentication.AUTHENTICATION_USER_ID))).thenReturn(userId.toString());
    }
    if (authenticationToken != null) {
      when(request.getHeader(Mockito.eq(Authentication.AUTHENTICATION_TOKEN))).thenReturn(authenticationToken);
    }
    return request;
  }
}
