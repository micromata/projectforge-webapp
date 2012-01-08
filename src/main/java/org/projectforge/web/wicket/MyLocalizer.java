/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2012 Kai Reinhard (k.reinhard@micromata.com)
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

import java.util.Locale;
import java.util.MissingResourceException;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.Localizer;
import org.apache.wicket.model.IModel;
import org.projectforge.web.WebConfiguration;

import de.micromata.genome.gwiki.model.GWikiWeb;
import de.micromata.genome.gwiki.page.impl.GWikiI18nElement;

/**
 * Using GWiki internationalization.
 * 
 * @author Roger Rene Kommer (r.kommer@micromata.de)
 * 
 */
public class MyLocalizer extends Localizer
{
  protected String[] modules;

  public MyLocalizer(final String... modules)
  {
    this.modules = modules;
  }

  protected String getI18NFromGWiki(final String key, final Component component, final IModel< ? > model)
  {
    final GWikiWeb wikiWeb = GWikiWeb.getWiki();
    final Locale loc = component.getLocale();
    String lang = loc.getLanguage();
    if (StringUtils.isEmpty(lang) == true) {
      lang = "en";
    }

    for (final String mod : modules) {
      final GWikiI18nElement el = (GWikiI18nElement) wikiWeb.getElement(mod);
      final String v = el.getMessage(lang, key);
      if (v != null) {
        return v;
      }
    }
    return null;
  }

  @Override
  public String getString(final String key, final Component component, final IModel< ? > model, final String defaultValue) throws MissingResourceException
  {
    if (WebConfiguration.isGWikiAvailable() == true) {
      final String ret = getI18NFromGWiki(key, component, model);
      if (ret != null) {
        return ret;
      }
    }
    return super.getString(key, component, model, defaultValue);
  }
}
