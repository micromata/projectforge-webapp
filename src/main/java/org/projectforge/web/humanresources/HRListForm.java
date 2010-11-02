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

package org.projectforge.web.humanresources;

import java.util.Date;

import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.projectforge.humanresources.HRFilter;
import org.projectforge.web.calendar.QuickSelectPanel;
import org.projectforge.web.wicket.AbstractListForm;
import org.projectforge.web.wicket.WicketUtils;
import org.projectforge.web.wicket.components.DatePanel;
import org.projectforge.web.wicket.components.DatePanelSettings;

/**
 * 
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class HRListForm extends AbstractListForm<HRFilter, HRListPage>
{
  private static final long serialVersionUID = -5511800187080680095L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(HRListForm.class);

  protected DatePanel startDate;

  protected DatePanel stopDate;

  @SuppressWarnings("serial")
  @Override
  protected void init()
  {
    final HRFilter filter = getSearchFilter();
    super.init();
    startDate = new DatePanel("startDate", new PropertyModel<Date>(filter, "startTime"), DatePanelSettings.get().withCallerPage(parentPage)
        .withSelectPeriodMode(true).withRequired(true));
    filterContainer.add(startDate);
    stopDate = new DatePanel("stopDate", new PropertyModel<Date>(filter, "stopTime"), DatePanelSettings.get().withCallerPage(parentPage)
        .withSelectPeriodMode(true).withRequired(true));
    filterContainer.add(stopDate);
    final QuickSelectPanel quickSelectPanel = new QuickSelectPanel("quickSelect", parentPage, "quickSelect", startDate);
    filterContainer.add(quickSelectPanel);
    quickSelectPanel.init();
    filterContainer.add(new Label("calendarWeeks", new Model<String>() {
      @Override
      public String getObject()
      {
        return WicketUtils.getCalendarWeeks(HRListForm.this, filter.getStartTime(), filter.getStopTime());
      }
    }).setRenderBodyOnly(true));
    filterContainer.add(new Label("datesAsUTC", new Model<String>() {
      @Override
      public String getObject()
      {
        return WicketUtils.getUTCDates(filter.getStartTime(), filter.getStopTime());
      }
    }));
    filterContainer.add(new CheckBox("showPlanningCheckBox", new PropertyModel<Boolean>(filter, "showPlanning")));
    filterContainer.add(new CheckBox("showBookedTimesheetsCheckBox", new PropertyModel<Boolean>(filter, "showBookedTimesheets")));
    filterContainer.add(new CheckBox("onlyMyProjectsCheckBox", new PropertyModel<Boolean>(filter, "onlyMyProjects")));
    filterContainer.add(new CheckBox("allProjectsGroupedByCustomerCheckBox", new PropertyModel<Boolean>(filter,
        "allProjectsGroupedByCustomer")));
    filterContainer.add(new CheckBox("otherProjectsGroupedByCustomerCheckBox", new PropertyModel<Boolean>(filter,
        "otherProjectsGroupedByCustomer")));
  }

  public HRListForm(HRListPage parentPage)
  {
    super(parentPage);
  }

  @Override
  protected boolean isSearchFilterVisible()
  {
    return false;
  }

  @Override
  protected HRFilter newSearchFilterInstance()
  {
    return new HRFilter();
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }
}
