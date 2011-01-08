/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2011 Kai Reinhard (k.reinhard@me.com)
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
import java.net.URLEncoder;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Locale;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.MDC;
import org.projectforge.common.NumberHelper;
import org.projectforge.user.PFUserContext;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserDao;
import org.projectforge.web.core.LogoServlet;
import org.projectforge.web.meb.SMSReceiverServlet;
import org.projectforge.web.registry.WebRegistry;
import org.projectforge.web.wicket.WicketUtils;

/**
 * Ensures that an user is logged in and put the user id, locale and ip to the logging mdc.<br/>
 * Ignores login for: /ProjectForge/wa/resources/* with the suffixes: *.js, *.css, *.gif, *.png. <br/>
 * Don't forget to call setServletContext on applications start-up!
 */
public class UserFilter implements Filter
{
  /**
   * Set after stay-logged-in functionality (used by MenuMobilePage).
   */
  public static String USER_ATTR_STAY_LOGGED_IN = "stayLoggedIn";

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(UserFilter.class);

  private final static String SESSION_KEY_USER = "UserFilter.user";

  private final static String SESSION_KEY_TARGET_URL = "UserFilter.targetUrl";

  private static final String COOKIE_NAME_FOR_STAY_LOGGED_IN = "stayLoggedIn";

  private static final int COOKIE_MAX_AGE = 30 * 24 * 3600; // 30 days.

  private static String IGNORE_PREFIX;

  private static String contextPath = "";

  private static UserDao userDao;

  private final static String LOGIN_URL = "/wa/" + WebRegistry.BOOKMARK_LOGIN;

  private final static String MOBILE_LOGIN_URL = "/wa/" + WebRegistry.BOOKMARK_MOBILE_LOGIN;

  public static void initialize(final UserDao userDao, final String contextPath)
  {
    UserFilter.userDao = userDao;
    UserFilter.contextPath = contextPath;
    IGNORE_PREFIX = contextPath + '/' + WicketUtils.WICKET_APPLICATION_PATH + "resources";
  }

  public static String getTargetUrlAfterLogin(final HttpServletRequest request)
  {
    return (String) request.getParameter(SESSION_KEY_TARGET_URL);
  }

  public static Cookie getStayLoggedInCookie(final HttpServletRequest request)
  {
    return getCookie(request, COOKIE_NAME_FOR_STAY_LOGGED_IN);
  }

  public static Cookie getCookie(final HttpServletRequest request, final String name)
  {
    final Cookie[] cookies = request.getCookies();
    if (cookies != null) {
      for (final Cookie cookie : cookies) {
        if (name.equals(cookie.getName()) == true) {
          return cookie;
        }
      }
    }
    return null;
  }

  public static void addStayLoggedInCookie(final HttpServletRequest request, final HttpServletResponse response,
      final Cookie stayLoggedInCookie)
  {
    stayLoggedInCookie.setMaxAge(COOKIE_MAX_AGE);
    stayLoggedInCookie.setPath("/");
    if (request.isSecure() == true) {
      log.debug("Set secure cookie");
      stayLoggedInCookie.setSecure(true);
    } else {
      log.debug("Set unsecure cookie");
    }
    response.addCookie(stayLoggedInCookie); // Refresh cookie.
  }

  public static void login(final HttpServletRequest request, final PFUserDO user)
  {
    request.getSession().setAttribute(SESSION_KEY_USER, user);
  }

  public static PFUserDO getUser(final HttpServletRequest request)
  {
    return (PFUserDO) request.getSession().getAttribute(SESSION_KEY_USER);
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
    HttpServletRequest request = (HttpServletRequest) req;
    if (log.isDebugEnabled() == true) {
      log.debug("doFilter " + request.getRequestURI() + ": " + request.getSession().getId());
      final Cookie[] cookies = request.getCookies();
      if (cookies != null) {
        for (final Cookie cookie : cookies) {
          log.debug("Cookie "
              + cookie.getName()
              + ", path="
              + cookie.getPath()
              + ", value="
              + cookie.getValue()
              + ", secure="
              + cookie.getVersion()
              + ", maxAge="
              + cookie.getMaxAge()
              + ", domain="
              + cookie.getDomain());
        }
      }
    }
    final HttpServletResponse response = (HttpServletResponse) resp;
    PFUserDO user = null;
    try {
      MDC.put("ip", request.getRemoteAddr());
      MDC.put("session", request.getSession().getId());
      if (ignoreFilterFor(request) == true) {
        // Ignore the filter for this request:
        if (log.isDebugEnabled() == true) {
          log.debug("Ignore: " + request.getRequestURI());
        }
        chain.doFilter(request, response);
      } else {
        // final boolean sessionTimeout = request.isRequestedSessionIdValid() == false;
        user = (PFUserDO) request.getSession().getAttribute(SESSION_KEY_USER);
        if (user != null) {
          if (log.isDebugEnabled() == true) {
            log.debug("User found in session: " + request.getRequestURI());
          }
        } else {
          user = checkStayLoggedIn(request, response);
          if (user != null) {
            if (log.isDebugEnabled() == true) {
              log.debug("User's stay logged-in cookie found: " + request.getRequestURI());
            }
            user.setAttribute(USER_ATTR_STAY_LOGGED_IN, true); // Used by MenuMobilePage.
            UserFilter.login(request, user);
          }
        }
        if (user != null) {
          PFUserContext.setUser(user);
          request = decorateWithLocale(request, user);
          chain.doFilter(request, response);
        } else if (redirectToLoginPage(request, response) == true) {
          // Redirect was done.
          return;
        } else {
          // No redirect, so process with the chain (loginPage is already the request).
          chain.doFilter(request, response);
        }
      }
    } finally {
      PFUserContext.setUser(null);
      MDC.remove("ip");
      MDC.remove("session");
      if (user != null) {
        MDC.remove("user");
      }
      if (log.isDebugEnabled() == true) {
        log.debug("doFilter finished for " + request.getRequestURI() + ": " + request.getSession().getId());
      }
    }
  }

  private boolean redirectToLoginPage(final HttpServletRequest request, final HttpServletResponse response) throws IOException
  {
    final String requestUri = request.getRequestURI();
    final String queryString = request.getQueryString();
    if (requestUri.contains(LOGIN_URL) == true
        || requestUri.contains(MOBILE_LOGIN_URL) == true
        || (requestUri.endsWith("/wa/") == true && queryString != null && (queryString.startsWith("wicket:interface=") == true
            && queryString.contains("form::IFormSubmitListener") == true && queryString.endsWith("&loginpage=true") == true))) {
      // For unactivated cookies: the login form posts (action link) to /wa;sessionid=.... with queryString
      // ...body:form::IFormSubmitListener...
      // This is no security problem because the MyAuthorizationStrategy throws an exception if the user tries to call a secure page without
      // login.
      // Don't redirect to login page after successful login!
      return false;
    } else {
      String targetUrlAfterLogin;
      if (queryString != null && queryString.contains("wicket:interface") == true) {
        targetUrlAfterLogin = null;
      } else if (queryString != null) {
        targetUrlAfterLogin = requestUri + "?" + request.getQueryString();
      } else {
        targetUrlAfterLogin = requestUri;
      }
      final StringBuffer buf = new StringBuffer();
      buf.append(response.encodeRedirectURL(request.getContextPath() + LOGIN_URL));
      if (targetUrlAfterLogin != null) {
        final String contextPath = request.getContextPath();
        if (StringUtils.isNotEmpty(contextPath) == true && targetUrlAfterLogin.startsWith(contextPath) == true) {
          targetUrlAfterLogin = targetUrlAfterLogin.substring(contextPath.length());
        }
        buf.append("?").append(SESSION_KEY_TARGET_URL).append("=").append(URLEncoder.encode(targetUrlAfterLogin, "UTF-8"));
      }
      response.sendRedirect(buf.toString());
      if (log.isDebugEnabled() == true) {
        log.debug("Redirect to login page " + buf.toString() + " with targetUrlAfterLogin: " + targetUrlAfterLogin);
      }
      return true;
    }
  }

  /**
   * User is not logged. Checks a stay-logged-in-cookie.
   * @return user if valid cookie found, otherwise null.
   */
  private PFUserDO checkStayLoggedIn(final HttpServletRequest request, final HttpServletResponse response)
  {
    final Cookie sessionIdCookie = getCookie(request, "JSESSIONID");
    if (sessionIdCookie != null && sessionIdCookie.getSecure() == false && request.isSecure() == true) {
      // Hack for developers: Safari (may-be also other browsers) don't update unsecure cookies for secure connections. This seems to be
      // occurring
      // if you use ProjectForge on localhost with http and https (e. g. for testing). You have to delete this cookie normally in your
      // browser.
      log.info("*** Unsecure JSESSIONID cookie found for a secure request. Please remove this cookie manually, if this message is displayed within your next request again.");
      final Cookie cookie = new Cookie("JSESSIONID", "hurzel");
      cookie.setMaxAge(0);
      cookie.setPath(sessionIdCookie.getPath()); // Doesn't work for Safari: getPath() returns always null!
      response.addCookie(cookie);
    }
    final Cookie stayLoggedInCookie = getStayLoggedInCookie(request);
    if (stayLoggedInCookie != null) {
      final String value = stayLoggedInCookie.getValue();
      if (StringUtils.isBlank(value) == true) {
        return null;
      }
      final String[] values = value.split(":");
      if (values == null || values.length != 3) {
        log.warn("Invalid cookie found: " + value);
        return null;
      }
      final Integer userId = NumberHelper.parseInteger(values[0]);
      final PFUserDO user = userDao.internalGetById(userId);
      if (user == null) {
        log.warn("Invalid cookie found (user not found): " + value);
        return null;
      }
      if (user.getUsername().equals(values[1]) == false) {
        log.warn("Invalid cookie found (user name wrong, maybe changed): " + value);
        return null;
      }
      if (values[2] == null || values[2].equals(user.getStayLoggedInKey()) == false) {
        log.warn("Invalid cookie found (stay-logged-in key, maybe renewed and/or user password changed): " + value);
        return null;
      }
      addStayLoggedInCookie(request, response, stayLoggedInCookie);
      log.info("User successfully logged in using stay-logged-in method: " + user.getDisplayUsername());
      return user;
    }
    return null;
  }

  /**
   * @param request
   * @param user
   * @return
   */
  protected HttpServletRequest decorateWithLocale(HttpServletRequest request, final PFUserDO user)
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
