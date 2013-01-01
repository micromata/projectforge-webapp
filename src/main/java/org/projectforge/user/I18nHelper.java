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

package org.projectforge.user;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import org.projectforge.plugins.core.AbstractPlugin;
import org.projectforge.plugins.core.PluginsRegistry;

/**
 * ThreadLocal context.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class I18nHelper
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(I18nHelper.class);

  public static final String BUNDLE_NAME = "I18nResources";

  /**
   * Use-ful for using the locale of another user (e. g. the receiver of an e-mail).
   * @param locale If null, then the context user's locale is assumed.
   * @return
   */
  private static ResourceBundle getResourceBundle(final String bundleName, final Locale locale)
  {
    final ResourceBundle resourceBundle = locale != null ? ResourceBundle.getBundle(bundleName, locale) : ResourceBundle
        .getBundle(bundleName);
    return resourceBundle;
  }

  public static String getLocalizedMessage(final Locale locale, final String messageKey, final Object... params)
  {
    if (params == null) {
      return getLocalizedString(locale, messageKey);
    }
    return MessageFormat.format(getLocalizedString(locale, messageKey), params);
  }

  private static String getString(final String bundleName, final Locale locale, final String key)
  {
    try {
      final ResourceBundle bundle = getResourceBundle(bundleName, locale);
      if (bundle.containsKey(key) == true) {
        return bundle.getString(key);
      }
    } catch (final Exception ex) {
      log.warn("Resource key '" + key + "' not found for locale '" + locale + "'");
    }
    return null;
  }

  public static String getLocalizedString(final Locale locale, final String key)
  {
    try {
      String translation = getString(BUNDLE_NAME, locale, key);
      if (translation != null) {
        return translation;
      }
      for (final AbstractPlugin plugin : PluginsRegistry.instance().getPlugins()) {
        if (plugin.getResourceBundleName() == null) {
          continue;
        }
        translation = getString(plugin.getResourceBundleName(), locale, key);
        if (translation != null) {
          return translation;
        }
      }
    } catch (Exception ex) { // MissingResourceException or NullpointerException
      log.warn("Resource key '" + key + "' not found for locale '" + locale + "'");
    }
    return "???" + key + "???";
  }
}
