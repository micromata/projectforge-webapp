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

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Locale;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.MDC;
import org.projectforge.user.PFUserContext;
import org.projectforge.web.meb.SMSReceiverServlet;
import org.projectforge.web.wicket.WicketUtils;

import de.micromata.user.ContextHolder;
import de.micromata.user.LogonFilter;
import de.micromata.user.UserInfo;

/**
 * Extends LogonFilter by ignoring login for: /ProjectForge/wa/resources/* with the suffixes: *.js, *.css, *.gif, *.png Don't forget to call
 * setServletContext on applications start-up.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class LoginFilter implements Filter
{
  private static String IGNORE_PREFIX;

  public static void setServletContextPath(final String contextPath)
  {
    LoginFilter.contextPath = contextPath;
    IGNORE_PREFIX = contextPath + '/' + WicketUtils.WICKET_APPLICATION_PATH + "resources";
  }

  /** The logger */
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LogonFilter.class);

  private static String contextPath = "";

  private LoginChecker loginChecker;

  private String loginUrl;

  private String loginFailedUrl;

  private String loginCheckUrl;

  private String timeOutUrl;

  public static final String USER_ATTRIBUTE = "de.micromata.user.LogonFilter.USER";

  public static final String TARGET_URL = "de.micromata.user.LogonFilter.TARGET_URL";

  public void setLoginChecker(final LoginChecker loginChecker)
  {
    this.loginChecker = loginChecker;
  }

  public void setLoginUrl(final String loginUrl)
  {
    this.loginUrl = loginUrl;
  }

  public void setSessionTimeoutUrl(final String timeOutUrl)
  {
    this.timeOutUrl = timeOutUrl;
  }

  public void setLoginFailedUrl(final String loginFailedUrl)
  {
    this.loginFailedUrl = loginFailedUrl;
  }

  public void setLoginCheckUrl(final String loginCheckUrl)
  {
    this.loginCheckUrl = loginCheckUrl;
  }

  public void destroy()
  {
    // do nothing
  }

  public void init(final FilterConfig cfg) throws ServletException
  {
    // do nothing
  }

  public void doFilter(final ServletRequest req, final ServletResponse resp, final FilterChain chain) throws IOException, ServletException
  {
    if (ignoreFilterFor(req) == false) {
      final HttpServletRequest request = (HttpServletRequest) req;
      final HttpServletResponse response = (HttpServletResponse) resp;
      final boolean sessionTimeout = !request.isRequestedSessionIdValid();
      MDC.put("ip", request.getRemoteAddr());
      MDC.put("session", request.getSession().getId());
      MDC.remove("user");
      ContextHolder.setUserInfo(null);
      doLogin(request, response, chain, sessionTimeout);
    } else {
      MDC.remove("ip");
      MDC.remove("session");
      MDC.remove("user");
      ContextHolder.setUserInfo(null);
      chain.doFilter(req, resp);
    }
  }

  /**
   * redirects to the login page, if not logged on.
   * 
   * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
   */
  private void doLogin(HttpServletRequest request, final HttpServletResponse response, final FilterChain chain, final boolean sessionTimeout)
      throws IOException, ServletException
  {
    UserInfo info = (UserInfo) request.getSession().getAttribute(USER_ATTRIBUTE);
    if (info == null) {
      info = loginChecker.checkLogon(request, response);
      if (info != null) {
        request.getSession().setAttribute(USER_ATTRIBUTE, info);
      }
    }
    // user is known, so lets do the main work
    if (info != null) {
      try {
        MDC.put("user", info.getDisplayUsername());
        ContextHolder.setUserInfo(info);
        request = decorateWithLocale(request, info);
        chain.doFilter(request, response);
      } finally {
        MDC.remove("user");
        ContextHolder.setUserInfo(info);
      }
      return;
    }
    // user is not logged in
    // first check, if the user has posted his credentials
    String fwdUrl = loginUrl; // rrk changed from loginFailedUrl
    if (log.isDebugEnabled() == true) {
      log.debug("requesting URL " + request.getRequestURI() + " for " + request.getSession().getId());
    }
    final String uri = request.getRequestURI();
    if ((uri.endsWith(loginCheckUrl) == true || uri.endsWith(loginUrl) == true || uri.endsWith(loginFailedUrl) == true)
        && request.getMethod().equals("POST")) {
      // posted to the check-page
      log.debug("Checking supplied credentials for user " + request.getParameter("username"));
      LoginChecker.AfterLoginAction action = new LoginChecker.AfterLoginAction();
      info = loginChecker.checkLogon(request, response, request.getParameter("username"), request.getParameter("password"), action);
      if (action.urlAfter != null) {
        fwdUrl = action.urlAfter;
      } else {
        fwdUrl = loginFailedUrl;
      }
      if (info == null) {
        log.info("login failed for user " + request.getParameter("username") + " redirect to " + fwdUrl);
        String encodeURL = response.encodeURL(request.getContextPath() + fwdUrl);
        log.debug("redirecting to failedLogon " + encodeURL);
        response.sendRedirect(encodeURL);
        return;
      }

      request.getSession().setAttribute(USER_ATTRIBUTE, info);
      ContextHolder.setUserInfo(info);
      String origRequest = (String) request.getSession().getAttribute(TARGET_URL);

      if (action.urlAfter == null) {
        request.getSession().removeAttribute(TARGET_URL);
        if (origRequest == null) {
          origRequest = request.getRequestURI();
        }
        origRequest = response.encodeURL(origRequest);
        log.info("login succeeded for user "
            + request.getParameter("username")
            + " redirect to "
            + origRequest
            + " for "
            + request.getSession().getId());
        response.sendRedirect(origRequest);
        return;
      } else {
        String encodeURL = response.encodeURL(request.getContextPath() + fwdUrl);
        log.info("login succeeded for user "
            + request.getParameter("username")
            + " redirect to "
            + encodeURL
            + " for "
            + request.getSession().getId());
        response.sendRedirect(encodeURL);
        return;
      }
    }
    if (request.getRequestURI().endsWith(loginUrl) == true) {
      log.debug("login page for " + request.getRequestURI() + " for " + request.getSession().getId());
      chain.doFilter(request, response);
      return;
    }
    // login page, remember the request and redirect to login
    if (log.isDebugEnabled() == true) {
      log.debug("login user for secured page " + request.getRequestURI() + " for " + request.getSession().getId());
    }
    if (request.getQueryString() != null) {
      request.getSession().setAttribute(TARGET_URL, response.encodeURL(request.getRequestURI()) + "?" + request.getQueryString());
    } else {
      request.getSession().setAttribute(TARGET_URL, response.encodeURL(request.getRequestURI()));
    }

    if (sessionTimeout == true && timeOutUrl != null) {
      log.debug("session timeout");
      fwdUrl = timeOutUrl;
    }
    final String encodeURL = response.encodeRedirectURL(request.getContextPath() + fwdUrl);
    log.debug("redirecting to page " + encodeURL);
    response.sendRedirect(encodeURL);
  }

  /**
   * @param request
   * @param info
   * @return
   */
  protected HttpServletRequest decorateWithLocale(HttpServletRequest request, UserInfo info)
  {
    final Locale locale = PFUserContext.getLocale(request.getLocale());
    request = new HttpServletRequestWrapper(request) {
      @Override
      public Locale getLocale()
      {
        return locale;
      }

      @Override
      public Enumeration< ? > getLocales()
      {
        return Collections.enumeration(Collections.singleton(locale));
      }
    };
    return request;
  }

  /**
   * Will be called by doFilter.
   * @param req from do Filter.
   * @return true, if the filter should ignore this request, otherwise false.
   */
  protected boolean ignoreFilterFor(final ServletRequest req)
  {
    final HttpServletRequest hreq = (HttpServletRequest) req;
    final String uri = hreq.getRequestURI();
    // If you have an NPE you have probably forgotten to call setServletContext on applications start-up.
    if (uri.startsWith(IGNORE_PREFIX)
        && (uri.endsWith(".js") == true || uri.endsWith(".css") == true || uri.endsWith(".gif") == true || uri.endsWith(".png") == true)) {
      return true;
    } else if (uri.startsWith(contextPath + "/" + LogoServlet.URL) == true
        || uri.startsWith(contextPath + "/" + SMSReceiverServlet.URL) == true) {
      return true;
    }
    return false;
  }
}
