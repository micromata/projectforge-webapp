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

package org.projectforge.web.core;

import java.util.Calendar;
import java.util.Date;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.projectforge.common.DateHolder;
import org.projectforge.common.DatePrecision;
import org.projectforge.registry.Registry;
import org.projectforge.registry.RegistryEntry;
import org.projectforge.task.TaskDO;
import org.projectforge.user.PFUserDO;
import org.projectforge.web.task.TaskSelectPanel;
import org.projectforge.web.user.UserSelectPanel;
import org.projectforge.web.wicket.AbstractListForm;
import org.projectforge.web.wicket.AbstractSecuredForm;
import org.projectforge.web.wicket.FocusOnLoadBehavior;
import org.projectforge.web.wicket.WebConstants;
import org.projectforge.web.wicket.WicketUtils;
import org.projectforge.web.wicket.components.DatePanel;
import org.projectforge.web.wicket.components.DatePanelSettings;
import org.projectforge.web.wicket.components.LabelValueChoiceRenderer;
import org.projectforge.web.wicket.components.SingleButtonPanel;

public class SearchForm extends AbstractSecuredForm<SearchPageFilter, SearchPage>
{
  private static final String USER_PREF_KEY_FILTER = "Search:Filter";
  
  private static final int MAGIC_NUMBER_LAST_DAYS_FOR_WITHOUT_TIME_SETTINGS = 42;

  private static final long serialVersionUID = 2638309407446431727L;

  protected DatePanel modifiedStartDatePanel;

  protected DatePanel modifiedStopDatePanel;

  private TaskSelectPanel taskSelectPanel;

  SearchPageFilter filter;

  public SearchForm(final SearchPage parentPage)
  {
    super(parentPage);
    filter = (SearchPageFilter) getParentPage().getUserPrefEntry(SearchPageFilter.class, USER_PREF_KEY_FILTER);
    if (filter == null) {
      filter = new SearchPageFilter();
      getParentPage().putUserPrefEntry(USER_PREF_KEY_FILTER, filter, true);
    }
  }

  @Override
  @SuppressWarnings("serial")
  protected void init()
  {
    super.init();
    add(new FeedbackPanel("feedback").setOutputMarkupId(true));
    final TextField<String> searchField = new TextField<String>("searchString", new PropertyModel<String>(filter, "searchString"));
    searchField.add(new FocusOnLoadBehavior());
    add(searchField);
    modifiedStartDatePanel = new DatePanel("startDate", new PropertyModel<Date>(filter, "startTimeOfLastModification"), DatePanelSettings
        .get().withCallerPage(parentPage).withSelectPeriodMode(true));
    add(modifiedStartDatePanel);
    modifiedStopDatePanel = new DatePanel("stopDate", new PropertyModel<Date>(filter, "stopTimeOfLastModification"), DatePanelSettings
        .get().withCallerPage(parentPage).withSelectPeriodMode(true));
    add(modifiedStopDatePanel);
    add(new Label("datesAsUTC", new Model<String>() {
      @Override
      public String getObject()
      {
        return WicketUtils.getUTCDates(filter.getStartTimeOfLastModification(), filter.getStopTimeOfLastModification());
      }
    }));

    final UserSelectPanel userSelectPanel = new UserSelectPanel("modifiedByUser", new PropertyModel<PFUserDO>(filter, "modifiedByUser"),
        parentPage, "userId");
    add(userSelectPanel);
    userSelectPanel.init().withAutoSubmit(true);

    taskSelectPanel = new TaskSelectPanel("task", new PropertyModel<TaskDO>(filter, "task"), parentPage, "taskId");
    add(taskSelectPanel);
    taskSelectPanel.setEnableLinks(true);
    taskSelectPanel.init();
    taskSelectPanel.setRequired(false);
    {
      // DropDownChoice: time period
      final LabelValueChoiceRenderer<Integer> lastDaysChoiceRenderer = new LabelValueChoiceRenderer<Integer>();
      lastDaysChoiceRenderer.addValue(MAGIC_NUMBER_LAST_DAYS_FOR_WITHOUT_TIME_SETTINGS, getString("search.withoutTimePeriod"));
      lastDaysChoiceRenderer.addValue(0, getString("search.today"));
      lastDaysChoiceRenderer.addValue(1, getString("search.lastDay"));
      for (final int days : new int[] { 3, 7, 14, 30, 60, 90}) {
        lastDaysChoiceRenderer.addValue(days, getLocalizedMessage("search.lastDays", days));
      }
      final DropDownChoice<Integer> lastDaysChoice = new DropDownChoice<Integer>("lastDays",
          new PropertyModel<Integer>(filter, "lastDays"), lastDaysChoiceRenderer.getValues(), lastDaysChoiceRenderer) {
        @Override
        protected void onSelectionChanged(final Integer newSelection)
        {
          if (newSelection == null) {
            return;
          }
          if (newSelection == MAGIC_NUMBER_LAST_DAYS_FOR_WITHOUT_TIME_SETTINGS) { // Magic number for
            filter.setStartTimeOfLastModification(null);
            filter.setStopTimeOfLastModification(null);
            modifiedStartDatePanel.markModelAsChanged();
            modifiedStopDatePanel.markModelAsChanged();
            filter.setLastDays(null);
            return;
          }
          final DateHolder dh = new DateHolder(new Date(), DatePrecision.MILLISECOND);
          dh.setEndOfDay();
          filter.setStopTimeOfLastModification(dh.getDate());
          dh.setBeginOfDay();
          dh.add(Calendar.DAY_OF_YEAR, -newSelection);
          filter.setStartTimeOfLastModification(dh.getDate());
          filter.setLastDays(null);
          modifiedStartDatePanel.markModelAsChanged();
          modifiedStopDatePanel.markModelAsChanged();
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
    {
      // DropDownChoice: area
      final LabelValueChoiceRenderer<String> areaChoiceRenderer = new LabelValueChoiceRenderer<String>();
      areaChoiceRenderer.addValue("ALL", getString("filter.all"));
      for (final RegistryEntry entry : Registry.instance().getOrderedList()) {
        if (entry.getDao().hasHistoryAccess(false) == true) {
          areaChoiceRenderer.addValue(entry.getId(), getString(entry.getI18nTitleHeading()));
        }
      }
      final DropDownChoice<String> areaChoice = new DropDownChoice<String>("area", new PropertyModel<String>(filter, "area"),
          areaChoiceRenderer.getValues(), areaChoiceRenderer) {
        @Override
        protected boolean wantOnSelectionChangedNotifications()
        {
          return true;
        }
      };
      areaChoice.setNullValid(true);
      areaChoice.setRequired(false);
      add(areaChoice);
    }
    {
      // DropDownChoice pageSize
      final DropDownChoice<Integer> pageSizeChoice = AbstractListForm.getPageSizeDropDownChoice("maxRows", getLocale(),
          new PropertyModel<Integer>(filter, "maxRows"), 3, 100);
      add(pageSizeChoice);
    }
    final Button searchButton = new Button("button", new Model<String>(getString("search")));
    searchButton.add(WebConstants.BUTTON_CLASS_DEFAULT);
    add(new SingleButtonPanel("search", searchButton));
    setDefaultButton(searchButton);

    final Button resetButton = new Button("button", new Model<String>(getString("reset"))) {
      @Override
      public void onSubmit()
      {
        super.onSubmit();
        filter.reset();
      }
    };
    resetButton.add(WebConstants.BUTTON_CLASS_RESET);
    add(new SingleButtonPanel("reset", resetButton));
  }
}
