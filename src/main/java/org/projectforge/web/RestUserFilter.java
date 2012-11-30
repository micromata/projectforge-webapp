/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
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
