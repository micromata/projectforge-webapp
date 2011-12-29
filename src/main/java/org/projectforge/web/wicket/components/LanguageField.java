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

package org.projectforge.web.wicket.components;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;

import org.apache.wicket.model.IModel;
import org.apache.wicket.util.convert.IConverter;
import org.projectforge.user.PFUserContext;
import org.projectforge.web.wicket.WicketUtils;
import org.projectforge.web.wicket.autocompletion.PFAutoCompleteTextField;
import org.projectforge.web.wicket.converter.LanguageConverter;

/**
 * Text field contains a ajax autocompletion text field for choosing and displaying a language (stored as locale). The favorite list is
 * configurable.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class LanguageField extends PFAutoCompleteTextField<Locale>
{
  private static final long serialVersionUID = -8354902369312317045L;

  @SuppressWarnings("serial")
  private final IConverter converter = new LanguageConverter() {
    @Override
    protected void error() {
      LanguageField.this.error(getString("error.language.unsupported"));
    };
  };

  private  List<Locale> favoriteLanguages;

  private final List<Locale> languages;

  public LanguageField(final String id, final IModel<Locale> model)
  {
    super(id, model);
    final TreeSet<String> locales = new TreeSet<String>();
    for (final Locale locale : Locale.getAvailableLocales()) {
      locales.add(locale.getDisplayName(getLocale()));
    }
    final String[] availableLanguages = locales.toArray(new String[0]);
    languages = getAsLocaleObjects(availableLanguages);
    withMatchContains(true).withMinChars(2);
    // Cant't use getString(i18nKey) because we're in the constructor and this would result in a Wicket warning.
    final String tooltip = PFUserContext.getLocalizedString("tooltip.autocomplete.locale");
    WicketUtils.addTooltip(this, tooltip);
  }

  public void setFavoriteLanguages(final List<Locale> favoriteLanguages)
  {
    this.favoriteLanguages = favoriteLanguages;
  }

  @Override
  protected List<Locale> getChoices(final String input)
  {
    final List<Locale> result = new ArrayList<Locale>();
    for (final Locale locale : languages) {
      final String str = converter.convertToString(locale, getLocale()).toLowerCase();
      if (str.contains(input.toLowerCase()) == true) {
        result.add(locale);
      }
    }
    return result;
  }

  @Override
  public IConverter getConverter(final Class< ? > type)
  {
    return converter;
  }

  @Override
  protected List<Locale> getFavorites()
  {
    return favoriteLanguages;
  }

  @Override
  protected String formatValue(final Locale value)
  {
    return converter.convertToString(value, getLocale());
  }

  private List<Locale> getAsLocaleObjects(final String[] localeNames)
  {
    final List<Locale> list = new ArrayList<Locale>();
    final Locale usersLocale = getLocale();
    for (final String name : localeNames) {
      final Locale locale = LanguageConverter.getLanguage(name, usersLocale);
      if (locale != null) {
        list.add(locale);
      }
    }
    return list;
  }
}
