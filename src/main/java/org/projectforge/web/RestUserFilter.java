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

package org.projectforge.web;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.projectforge.user.PFUserContext;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserDao;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author Daniel Ludwig (d.ludwig@micromata.de)
 * 
 */
public class RestUserFilter implements Filter
{
  @Autowired
  private UserDao userDao;

  @Override
  public void init(final FilterConfig filterConfig) throws ServletException
  {
    // NOOP
  }

  @Override
  public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException,
  ServletException
  {
    final HttpServletRequest req = (HttpServletRequest) request;
    final String header = req.getHeader("Authorization");
    try {
      final String[] split = StringUtils.split(header, ":");
      if (split == null || split.length != 2) {
        final HttpServletResponse resp = (HttpServletResponse) response;
        resp.sendError(HttpServletResponse.SC_FORBIDDEN);
        return;
      }
      final String username = split[0];
      final String encryptedPassword = userDao.encryptPassword(split[1]);
      final PFUserDO user = userDao.authenticateUser(username, encryptedPassword);
      if (user == null) {
        final HttpServletResponse resp = (HttpServletResponse) response;
        resp.sendError(HttpServletResponse.SC_FORBIDDEN);
        return;
      }

      PFUserContext.setUser(user);
      chain.doFilter(request, response);
    } finally {
      PFUserContext.setUser(null);
    }
  }

  @Override
  public void destroy()
  {
    // NOOP
  }

  public UserDao getUserDao()
  {
    return userDao;
  }

  public void setUserDao(final UserDao userDao)
  {
    this.userDao = userDao;
  }

}
