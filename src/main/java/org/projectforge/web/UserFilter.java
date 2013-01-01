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
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.MDC;
import org.projectforge.common.NumberHelper;
import org.projectforge.common.StringHelper;
import org.projectforge.user.Login;
import org.projectforge.user.PFUserContext;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserDao;
import org.projectforge.web.core.LogoServlet;
import org.projectforge.web.meb.SMSReceiverServlet;
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
  public static final String USER_ATTR_STAY_LOGGED_IN = "stayLoggedIn";

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(UserFilter.class);

  private final static String SESSION_KEY_USER = "UserFilter.user";

  private static final String COOKIE_NAME_FOR_STAY_LOGGED_IN = "stayLoggedIn";

  private static final int COOKIE_MAX_AGE = 30 * 24 * 3600; // 30 days.

  private static String IGNORE_PREFIX_WICKET;

  private static String IGNORE_PREFIX_DOC;

  private static String IGNORE_PREFIX_SITE_DOC;

  private static String IGNORE_PREFIX_LOGO;

  private static String IGNORE_PREFIX_SMS_REVEIVE_SERVLET;

  private static String WICKET_PAGES_PREFIX;

  public static String CONTEXT_PATH;

  private static UserDao userDao;

  private static boolean updateRequiredFirst = false;

  public static void initialize(final UserDao userDao, final String contextPath)
  {
    UserFilter.userDao = userDao;
    CONTEXT_PATH = contextPath;
    WICKET_PAGES_PREFIX = CONTEXT_PATH + "/" + WicketUtils.WICKET_APPLICATION_PATH;
    IGNORE_PREFIX_WICKET = WICKET_PAGES_PREFIX + "resources";
    IGNORE_PREFIX_DOC = contextPath + "/secure/doc";
    IGNORE_PREFIX_SITE_DOC = contextPath + "/secure/site";
    IGNORE_PREFIX_LOGO = contextPath + "/" + LogoServlet.BASE_URL;
    IGNORE_PREFIX_SMS_REVEIVE_SERVLET = contextPath + "/" + SMSReceiverServlet.URL;
  }

  public static void setUpdateRequiredFirst(final boolean value)
  {
    updateRequiredFirst = value;
  }

  public static boolean isUpdateRequiredFirst()
  {
    return updateRequiredFirst;
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

  /**
   * Adds or refresh the given cookie.
   * @param request
   * @param response
   * @param stayLoggedInCookie
   */
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
    final HttpSession session = request.getSession();
    final PFUserDO storedUser = new PFUserDO();
    copyUser(user, storedUser);
    session.setAttribute(SESSION_KEY_USER, storedUser);
  }

  public static void updateUser(final HttpServletRequest request, final PFUserDO user)
  {
    final PFUserDO origUser = getUser(request);
    if (origUser.getId().equals(user.getId()) == false) {
      log.error("**** Intruser? User id of the session user is different to the id of the given user!");
      return;
    }
    copyUser(user, origUser);
  }

  private static void copyUser(final PFUserDO srcUser, final PFUserDO destUser)
  {
    destUser.copyValuesFrom(srcUser, "password", "stayLoggedInKey");
  }

  public static PFUserDO getUser(final HttpServletRequest request)
  {
    final HttpSession session = request.getSession();
    if (session == null) {
      return null;
    }
    return (PFUserDO) session.getAttribute(SESSION_KEY_USER);
  }

  public void destroy()
  {
    // do nothing
  }

  public void init(final FilterConfig filterConfig) throws ServletException
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
        } else if (updateRequiredFirst == false) {
          // Ignore stay-logged-in if redirect to update page is required.
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
          MDC.put("user", user.getUsername());
          PFUserContext.setUser(user);
          request = decorateWithLocale(request, user);
          chain.doFilter(request, response);
        } else {
          if (((HttpServletRequest) req).getRequestURI().startsWith(WICKET_PAGES_PREFIX) == true) {
            // Access-checking is done by Wicket, not by this filter:
            request = decorateWithLocale(request, user);
            chain.doFilter(request, response);
          } else {
            response.getWriter().append("No access.");
          }
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
      final Cookie cookie = new Cookie("JSESSIONID", "to be deleted");
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
      if (Login.getInstance().checkStayLoggedIn(user) == false) {
        log.warn("Stay-logged-in wasn't accepted by the login handler: " + user.getUserDisplayname());
        return null;
      }
      addStayLoggedInCookie(request, response, stayLoggedInCookie);
      log.info("User successfully logged in using stay-logged-in method: " + user.getUserDisplayname());
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
    // Paranoia setting. May-be there could be a vulnerability with request parameters:
    if (uri.contains("?") == false) {
      // if (uri.startsWith(IGNORE_PREFIX_WICKET) && StringHelper.endsWith(uri, ".js", ".css", ".gif", ".png") == true) {
      // No access checking for Wicket resources.
      // return true;
      // } else if (StringHelper.startsWith(uri, IGNORE_PREFIX_DOC, IGNORE_PREFIX_SITE_DOC) == true
      // && StringHelper.endsWith(uri, ".html", ".pdf", ".js", ".css", ".gif", ".png") == true) {
      // No access checking for documentation (including site doc).
      // return true;
      // } else
      if (StringHelper.startsWith(uri, IGNORE_PREFIX_LOGO, IGNORE_PREFIX_SMS_REVEIVE_SERVLET) == true) {
        // No access checking for logo and sms receiver servlet.
        // The sms receiver servlet has its own authentification (key).
        return true;
      }
    }
    return false;
  }
}
