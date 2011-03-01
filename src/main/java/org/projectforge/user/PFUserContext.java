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

package org.projectforge.user;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.TimeZone;

import org.apache.commons.lang.ObjectUtils;
import org.projectforge.core.Configuration;
import org.projectforge.plugins.core.AbstractPlugin;
import org.projectforge.plugins.core.PluginsRegistry;

/**
 * ThreadLocal context.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class PFUserContext
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PFUserContext.class);

  public static final String BUNDLE_NAME = "I18nResources";

  private static ThreadLocal<PFUserDO> context = new ThreadLocal<PFUserDO>();

  /**
   * @return The user of ThreadLocal if exists.
   */
  public final static PFUserDO getUser()
  {
    return context.get();
  }

  public final static void setUser(final PFUserDO user)
  {
    if (log.isDebugEnabled() == true) {
      log.debug("setUserInfo: " + user != null ? user.getDisplayUsername() : "null" + ", was: " + context.get() != null ? context.get()
          .getDisplayUsername() : "null");
    }
    context.set(user);
    if (log.isDebugEnabled() == true) {
      log.debug("user is now: " + context.get() != null ? context.get().getDisplayUsername() : "null");
    }
  }

  /**
   * @return The user id of the ThreadLocal user if exists.
   * @see #getUser()
   */
  public final static Integer getUserId()
  {
    PFUserDO user = getUser();
    return user != null ? user.getId() : null;
  }

  /**
   * @return The locale of the user if exists, otherwise default locale.
   * @see #getUser()
   * @see PFUserDO#getLocale()
   */
  public final static Locale getLocale()
  {
    return getLocale(null);
  }

  /**
   * If context user's locale is null and the given defaultLocale is not null, then the context user's client locale will be set to given
   * defaultLocale.
   * @param defaultLocale will be used, if the context user or his user locale does not exist.
   * @return The locale of the user if exists, otherwise the given default locale or if null the system's default locale.
   * @see #getUser()
   * @see PFUserDO#getLocale()
   */
  public final static Locale getLocale(Locale defaultLocale)
  {
    PFUserDO user = getUser();
    Locale userLocale = user != null ? user.getLocale() : null;
    if (userLocale != null) {
      return userLocale;
    }
    Locale clientLocale = user != null ? user.getClientLocale() : null;
    if (defaultLocale != null && user != null && ObjectUtils.equals(clientLocale, defaultLocale) == false) {
      user.setClientLocale(defaultLocale);
      clientLocale = defaultLocale;
    }
    if (clientLocale != null) {
      return clientLocale;
    }
    return defaultLocale != null ? defaultLocale : Locale.getDefault();
  }

  /**
   * @return The locale of the user if exists, otherwise default timezone of PFUserDO
   * @see #getUser()
   * @see PFUserDO#getTimeZoneObject()
   * @see PFUserDO#DEFAULT_TIME_ZONE
   */
  public final static TimeZone getTimeZone()
  {
    return getUser() != null ? getUser().getTimeZoneObject() : Configuration.getInstance().getDefaultTimeZone();
  }

  /**
   * Gets the logged in user's resource bundle (getting the locale from the context user).
   */
  private static ResourceBundle getResourceBundle(final String bundleName)
  {
    return getResourceBundle(bundleName, getLocale());
  }

  /**
   * Use-ful for using the locale of another user (e. g. the receiver of an e-mail).
   * @param locale If null, then the context user's locale is assumed.
   * @return
   */
  private static ResourceBundle getResourceBundle(final String bundleName, Locale locale)
  {
    if (locale == null) {
      locale = getLocale();
    }
    final ResourceBundle resourceBundle = ResourceBundle.getBundle(bundleName, locale);
    return resourceBundle;
  }

  public static String getLocalizedMessage(String messageKey, Object... params)
  {
    if (params == null) {
      return getLocalizedString(messageKey);
    }
    return MessageFormat.format(getLocalizedString(messageKey), params);
  }

  private static String getString(final String bundleName, final String key)
  {
    try {
      final ResourceBundle bundle = getResourceBundle(bundleName);
      if (bundle.containsKey(key) == true) {
        return bundle.getString(key);
      }
    } catch (final Exception ex) {
      log.warn("Resource key '" + key + "' not found for locale '" + PFUserContext.getLocale() + "'");
    }
    return null;
  }

  public static String getLocalizedString(final String key)
  {
    try {
      String translation = getString(BUNDLE_NAME, key);
      if (translation != null) {
        return translation;
      }
      for (final AbstractPlugin plugin : PluginsRegistry.instance().getPlugins()) {
        if (plugin.getResourceBundleName() == null) {
          continue;
        }
        translation = getString(plugin.getResourceBundleName(), key);
        if (translation != null) {
          return translation;
        }
      }
    } catch (Exception ex) { // MissingResourceException or NullpointerException
      log.warn("Resource key '" + key + "' not found for locale '" + PFUserContext.getLocale() + "'");
    }
    return "???" + key + "???";
  }
}
