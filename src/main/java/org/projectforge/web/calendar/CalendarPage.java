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

package org.projectforge.web.calendar;

import java.util.Date;

import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.joda.time.DateMidnight;
import org.projectforge.user.PFUserContext;
import org.projectforge.web.fibu.ISelectCallerPage;
import org.projectforge.web.wicket.AbstractSecuredPage;

public class CalendarPage extends AbstractSecuredPage implements ISelectCallerPage
{
  private static final long serialVersionUID = 8710165041912824126L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CalendarPage.class);

  private static final String USERPREF_KEY = "CalendarPage.userPrefs";

  protected CalendarForm form;

  protected CalendarPanel calendarPanel;

  protected final PageParameters pageParameters;

  public CalendarPage(final PageParameters parameters)
  {
    super(parameters);
    this.pageParameters = parameters;
    init();
  }

  public void init()
  {
    form = new CalendarForm(this);
    body.add(form);
    CalendarFilter filter = (CalendarFilter) getUserPrefEntry(USERPREF_KEY);
    if (filter == null) {
      filter = new CalendarFilter();
      putUserPrefEntry(USERPREF_KEY, filter, true);
    }
    form.setFilter(filter);
    form.init();
    calendarPanel = new CalendarPanel("cal", form.currentDatePanel);
    calendarPanel.setOutputMarkupId(true);
    form.add(calendarPanel);
    calendarPanel.init(getFilter());
    if (pageParameters != null) {
      if (pageParameters.get("showTimesheets").isNull() == false) {
        form.getFilter().setUserId(getUserId());
      }
      if (pageParameters.get("showBirthdays").isNull() == false) {
        form.getFilter().setShowBirthdays(true);
      }
    }
  }

  /**
   * @see org.projectforge.web.wicket.AbstractSecuredPage#onBeforeRender()
   */
  @Override
  protected void onBeforeRender()
  {
    super.onBeforeRender();
    calendarPanel.forceReload(); // Needed if e. g. user or anything was changed.
  }

  @Override
  protected String getTitle()
  {
    return getString("calendar.title");
  }

  CalendarFilter getFilter()
  {
    return form.getFilter();
  }

  public void cancelSelection(final String property)
  {
  }

  public void select(final String property, final Object selectedValue)
  {
    if ("userId".equals(property) == true) {
      getFilter().setUserId((Integer) selectedValue);
    } else {
      log.error("Property '" + property + "' not supported for selection.");
    }
  }

  public void unselect(final String property)
  {
    if ("userId".equals(property) == true) {
      getFilter().setUserId(null);
    } else {
      log.error("Property '" + property + "' not supported for selection.");
    }
  }

  /**
   * @param startDate
   * @return this for chaining.
   */
  public CalendarPage setStartDate(final Date startDate)
  {
    form.getFilter().setStartDate(new DateMidnight(startDate, PFUserContext.getDateTimeZone()));
    return this;
  }
}
