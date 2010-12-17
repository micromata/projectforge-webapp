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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.wicket.PageParameters;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DefaultDataTable;
import org.apache.wicket.markup.html.JavascriptPackageResource;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.calendar.TimePeriod;
import org.projectforge.common.BeanHelper;
import org.projectforge.core.BaseSearchFilter;
import org.projectforge.core.SearchDao;
import org.projectforge.core.SearchResultData;
import org.projectforge.database.StatisticsCache;
import org.projectforge.registry.Registry;
import org.projectforge.registry.RegistryEntry;
import org.projectforge.task.TaskDO;
import org.projectforge.task.TaskDependentFilter;
import org.projectforge.task.TaskTree;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserGroupCache;
import org.projectforge.web.fibu.ISelectCallerPage;
import org.projectforge.web.wicket.AbstractSecuredPage;
import org.projectforge.web.wicket.IListPageColumnsCreator;
import org.projectforge.web.wicket.MySortableDataProvider;
import org.springframework.util.CollectionUtils;

public class SearchPage extends AbstractSecuredPage implements ISelectCallerPage
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SearchPage.class);

  private static final int MAXIMUM_ENTRIES_WITHOUT_FILTER_SETTINGS = 10000;

  private SearchForm form;

  private RepeatingView areaRepeater;

  @SpringBean(name = "searchDao")
  private SearchDao searchDao;

  @SpringBean(name = "statisticsCache")
  private StatisticsCache statisticsCache;

  @SpringBean(name = "taskTree")
  private TaskTree taskTree;

  @SpringBean(name = "userGroupCache")
  private UserGroupCache userGroupCache;

  private boolean refreshed = false;

  public SearchPage(PageParameters parameters)
  {
    super(parameters);
    add(JavascriptPackageResource.getHeaderContribution("scripts/zoom.js"));
    form = new SearchForm(this);
    body.add(form);
    form.init();
  }

  @Override
  protected void onBeforeRender()
  {
    super.onBeforeRender();
    refresh();
  }

  @Override
  protected void onAfterRender()
  {
    super.onAfterRender();
    refreshed = false;
  }

  @SuppressWarnings( { "serial", "unchecked"})
  void refresh()
  {
    if (refreshed == true) {
      // Do nothing (called twice).
      return;
    }
    refreshed = true;
    if (areaRepeater != null) {
      body.remove(areaRepeater);
    }
    areaRepeater = new RepeatingView("areaRepeater");
    body.add(areaRepeater);
    if (form.filter.isEmpty() == true) {
      return;
    }
    for (final RegistryEntry registryEntry : Registry.instance().getOrderedList()) {
      final Class< ? extends IListPageColumnsCreator< ? >> clazz = registryEntry.getListPageColumnsCreatorClass();
      final IListPageColumnsCreator< ? > listPageColumnsCreator = clazz == null ? null : (IListPageColumnsCreator< ? >) BeanHelper
          .newInstance(clazz, PageParameters.class, new PageParameters());
      if (listPageColumnsCreator == null) {
        continue;
      }
      final Integer number = statisticsCache.getNumberOfEntities(registryEntry.getDOClass());
      final Class< ? extends BaseSearchFilter> registeredFilterClass = registryEntry.getSearchFilterClass();
      final boolean isTaskDependentFilter = registeredFilterClass != null
          && TaskDependentFilter.class.isAssignableFrom(registeredFilterClass);
      if (number > MAXIMUM_ENTRIES_WITHOUT_FILTER_SETTINGS
          && (form.filter.getSearchString() == null || form.filter.getSearchString().length() < 3)
          && (isTaskDependentFilter == false || form.filter.getTask() == null)
          && form.filter.getStartTimeOfLastModification() == null
          && form.filter.getStopTimeOfLastModification() == null) {
        // Don't search to large tables if to less filter settings are given.
        continue;
      }
      final BaseSearchFilter filter;
      if (isTaskDependentFilter == true) {
        filter = (BaseSearchFilter) BeanHelper.newInstance(registeredFilterClass, new Class< ? >[] { BaseSearchFilter.class}, form.filter);
        ((TaskDependentFilter)filter).setTaskId(form.filter.getTaskId());
      } else {
        filter = form.filter;
      }
      final List<SearchResultData> searchResult = searchDao.getEntries(filter, registryEntry.getDOClass(), registryEntry.getDao());
      if (CollectionUtils.isEmpty(searchResult) == true) {
        continue;
      }
      final List list = new ArrayList();
      boolean hasMore = false;
      for (final SearchResultData data : searchResult) {
        if (data.getDataObject() != null) {
          list.add(data.getDataObject());
        } else {
          // Empty entry means: more entries found.
          hasMore = true;
          break;
        }
      }
      final WebMarkupContainer areaContainer = new WebMarkupContainer(areaRepeater.newChildId());
      areaRepeater.add(areaContainer);
      areaContainer.add(new Label("areaTitle", getString(registryEntry.getI18nTitleHeading())));
      final List< ? > columns = listPageColumnsCreator.createColumns(this);
      final DataTable dataTable = new DefaultDataTable("dataTable", columns, new MySortableDataProvider("NOSORT", false) {
        @Override
        public List getList()
        {
          return list;
        }

        @Override
        protected IModel getModel(Object object)
        {
          return new Model((Serializable) object);
        }
      }, form.filter.getMaxRows());
      areaContainer.add(dataTable);
      if (hasMore == true) {
        areaContainer.add(new WebMarkupContainer("hasMoreEntries"));
      } else {
        areaContainer.add(new Label("hasMoreEntries", "[invisible]").setVisible(false));
      }
    }
  }

  @Override
  public void cancelSelection(final String property)
  {
  }

  public void select(final String property, final Object selectedValue)
  {
    if ("taskId".equals(property) == true) {
      final TaskDO task = taskTree.getTaskById((Integer) selectedValue);
      form.filter.setTask(task);
    } else if ("userId".equals(property) == true) {
      final PFUserDO user = userGroupCache.getUser((Integer) selectedValue);
      form.filter.setModifiedByUserId(user != null ? user.getId() : null);
    } else if ("startDate".equals(property) == true) {
      if (selectedValue instanceof Date) {
        // Date selected.
        final Date date = (Date) selectedValue;
        form.filter.setStartTimeOfLastModification(date);
      } else if (selectedValue instanceof TimePeriod) {
        // Period selected.
        final TimePeriod timePeriod = (TimePeriod) selectedValue;
        form.filter.setStartTimeOfLastModification(timePeriod.getFromDate());
        form.filter.setStopTimeOfLastModification(timePeriod.getToDate());
        form.modifiedStopDatePanel.markModelAsChanged();
      }
      form.modifiedStartDatePanel.markModelAsChanged();
    } else if ("stopDate".equals(property) == true) {
      if (selectedValue instanceof Date) {
        // Date selected.
        final Date date = (Date) selectedValue;
        form.filter.setStopTimeOfLastModification(date);
      } else if (selectedValue instanceof TimePeriod) {
        // Period selected.
        final TimePeriod timePeriod = (TimePeriod) selectedValue;
        form.filter.setStartTimeOfLastModification(timePeriod.getFromDate());
        form.filter.setStopTimeOfLastModification(timePeriod.getToDate());
        form.modifiedStartDatePanel.markModelAsChanged();
      }
      form.modifiedStopDatePanel.markModelAsChanged();
    } else {
      log.error("Property '" + property + "' not supported for selection.");
    }
  }

  public void unselect(final String property)
  {
    if ("taskId".equals(property) == true) {
      form.filter.setTask(null);
    } else {
      log.error("Property '" + property + "' not supported for unselection.");
    }
  }

  @Override
  protected String getTitle()
  {
    return getString("search.title");
  }
}
