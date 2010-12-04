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

import java.util.Date;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.common.StringHelper;
import org.projectforge.task.TaskTree;
import org.projectforge.web.calendar.DateTimeFormatter;
import org.projectforge.web.task.TaskSelectPanel;
import org.projectforge.web.wicket.AbstractSecuredForm;
import org.projectforge.web.wicket.WicketUtils;
import org.projectforge.web.wicket.components.DatePanel;
import org.projectforge.web.wicket.components.DatePanelSettings;
import org.projectforge.web.wicket.components.LabelValueChoiceRenderer;

public class SearchForm extends AbstractSecuredForm<SearchData, SearchPage>
{
  private static final long serialVersionUID = 2638309407446431727L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SearchForm.class);

  @SpringBean(name = "taskTree")
  private TaskTree taskTree;

  @SpringBean(name = "dateTimeFormatter")
  private DateTimeFormatter dateTimeFormatter;

  protected DatePanel startDatePanel;

  protected DatePanel stopDatePanel;

  private TaskSelectPanel taskSelectPanel;

  SearchData data;

  public SearchForm(final SearchPage parentPage)
  {
    super(parentPage);
    data = new SearchData();
  }

  @Override
  @SuppressWarnings("serial")
  protected void init()
  {
    super.init();
    startDatePanel = new DatePanel("startDate", new PropertyModel<Date>(data, "modifiedStartDate"), DatePanelSettings.get().withCallerPage(
        parentPage).withSelectPeriodMode(true));
    add(startDatePanel);
    stopDatePanel = new DatePanel("stopDate", new PropertyModel<Date>(data, "modifiedStopDate"), DatePanelSettings.get().withCallerPage(
        parentPage).withSelectPeriodMode(true));
    add(stopDatePanel);
    add(new Label("datesAsUTC", new Model<String>() {
      @Override
      public String getObject()
      {
        return WicketUtils.getUTCDates(data.getModifiedStartDate(), data.getModifiedStopDate());
      }
    }));

    // DropDownChoice time period
    final LabelValueChoiceRenderer<Integer> lastDaysChoiceRenderer = new LabelValueChoiceRenderer<Integer>();
    lastDaysChoiceRenderer.addValue(0, getString("search.today"));
    lastDaysChoiceRenderer.addValue(1, getString("search.lastDay"));
    for (final int days : new int[] { 3, 7, 14, 30, 60, 90}) {
      lastDaysChoiceRenderer.addValue(days, getLocalizedMessage("search.lastDays", days));
    }
    @SuppressWarnings("unchecked")
    final DropDownChoice lastDaysChoice = new DropDownChoice("lastDays", new PropertyModel(this, "lastDays"), lastDaysChoiceRenderer
        .getValues(), lastDaysChoiceRenderer) {
      @Override
      protected void onSelectionChanged(Object newSelection)
      {
      }

      @Override
      protected boolean wantOnSelectionChangedNotifications()
      {
        return true;
      }
    };
    lastDaysChoice.setNullValid(true);
    lastDaysChoice.setRequired(false);
    add(lastDaysChoice);
  }
}
