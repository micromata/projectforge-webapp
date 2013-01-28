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

package org.projectforge.web.wicket.components;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.convert.IConverter;
import org.projectforge.user.PFUserContext;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserGroupCache;
import org.projectforge.web.wicket.WebConstants;
import org.projectforge.web.wicket.WicketUtils;
import org.projectforge.web.wicket.autocompletion.PFAutoCompleteTextField;
import org.projectforge.web.wicket.converter.TimeZoneConverter;

/**
 * Panel contains a ajax autocompletion text field for choosing and displaying a time zone. The time zones of all users will be shown as
 * favorite list.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
@SuppressWarnings("serial")
public class TimeZonePanel extends Panel
{
  @SpringBean(name = "userGroupCache")
  private UserGroupCache userGroupCache;

  private static final IConverter converter = new TimeZoneConverter();

  public TimeZonePanel(final String id, final IModel<TimeZone> model)
  {
    super(id);
    final String[] availableTimeZones = TimeZone.getAvailableIDs();
    Arrays.sort(availableTimeZones);
    final List<TimeZone> list = getAsTimeZoneObjects(availableTimeZones);
    final List<String> favoritesIds = new ArrayList<String>();
    for (final PFUserDO user : userGroupCache.getAllUsers()) {
      final String timeZone = user.getTimeZone();
      if (timeZone == null) {
        continue;
      }
      if (favoritesIds.contains(timeZone) == false) {
        favoritesIds.add(timeZone);
      }
    }
    final String[] favoriteIds = favoritesIds.toArray(new String[favoritesIds.size()]);
    final List<TimeZone> favoriteTimeZones = getAsTimeZoneObjects(favoriteIds);
    final PFAutoCompleteTextField<TimeZone> textField = new PFAutoCompleteTextField<TimeZone>("input", model) {
      @Override
      protected List<TimeZone> getChoices(final String input)
      {
        final List<TimeZone> result = new ArrayList<TimeZone>();
        for (final TimeZone timeZone : list) {
          final String str = converter.convertToString(timeZone, getLocale()).toLowerCase();
          if (str.contains(input.toLowerCase()) == true) {
            result.add(timeZone);
          }
        }
        return result;
      }

      /**
       * @see org.apache.wicket.Component#getConverter(java.lang.Class)
       */
      @Override
      public <C> IConverter<C> getConverter(final Class<C> type)
      {
        return converter;
      }

      @Override
      protected List<TimeZone> getFavorites()
      {
        return favoriteTimeZones;
      }

      @Override
      protected String formatValue(final TimeZone value)
      {
        return converter.convertToString(value, getLocale());
      }
    };
    textField.withMatchContains(true).withMinChars(2);
    // Cant't use getString(i18nKey) because we're in the constructor and this would result in a Wicket warning.
    final String tooltip = PFUserContext.getLocalizedString("tooltip.autocomplete.timeZone");
    WicketUtils.addTooltip(textField, tooltip);
    add(textField);
    add(new TooltipImage("autocompleteDblClickHelpImage", WebConstants.IMAGE_HELP_KEYBOARD, tooltip));
    setRenderBodyOnly(true);
  }

  private List<TimeZone> getAsTimeZoneObjects(final String[] timeZoneIds)
  {
    final List<TimeZone> list = new ArrayList<TimeZone>();
    for (final String timeZoneId : timeZoneIds) {
      list.add(TimeZone.getTimeZone(timeZoneId));
    }
    return list;
  }
}
