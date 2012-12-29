/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2012 Kai Reinhard (k.reinhard@micromata.de)
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
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DefaultDataTable;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.common.BeanHelper;
import org.projectforge.core.BaseSearchFilter;
import org.projectforge.core.ExtendedBaseDO;
import org.projectforge.core.NumberFormatter;
import org.projectforge.core.SearchDao;
import org.projectforge.core.SearchResultData;
import org.projectforge.database.StatisticsCache;
import org.projectforge.task.TaskDependentFilter;
import org.projectforge.web.registry.WebRegistryEntry;
import org.projectforge.web.wicket.IListPageColumnsCreator;
import org.projectforge.web.wicket.MySortableDataProvider;
import org.springframework.util.CollectionUtils;

public class SearchAreaPanel extends Panel
{
  private static final long serialVersionUID = -4258095807245346743L;

  private static final int MAXIMUM_ENTRIES_WITHOUT_FILTER_SETTINGS = 10000;

  @SpringBean(name = "searchDao")
  private SearchDao searchDao;

  @SpringBean(name = "statisticsCache")
  private StatisticsCache statisticsCache;

  /**
   * @param page Needed, because in constructor this panel is not yet added to a page.
   * @param id
   * @param filter
   * @param registryEntry the area to show.
   */
  @SuppressWarnings("serial")
  public SearchAreaPanel(final WebPage page, final String id, final SearchPageFilter filter, final WebRegistryEntry webRegistryEntry)
  {
    super(id);
    final long millis = System.currentTimeMillis();
    final Class< ? extends IListPageColumnsCreator< ? >> listPageColumnsCreatorClass = webRegistryEntry.getListPageColumnsCreatorClass();
    final IListPageColumnsCreator< ? > listPageColumnsCreator = listPageColumnsCreatorClass == null ? null
        : (IListPageColumnsCreator< ? >) BeanHelper.newInstance(listPageColumnsCreatorClass, PageParameters.class, new PageParameters());
    if (listPageColumnsCreator == null) {
      return;
    }
    final Integer number = statisticsCache.getNumberOfEntities(webRegistryEntry.getDOClass());
    final Class< ? extends BaseSearchFilter> registeredFilterClass = webRegistryEntry.getSearchFilterClass();
    final boolean isTaskDependentFilter = registeredFilterClass != null
        && TaskDependentFilter.class.isAssignableFrom(registeredFilterClass);
    if (number > MAXIMUM_ENTRIES_WITHOUT_FILTER_SETTINGS
        && (filter.getSearchString() == null || filter.getSearchString().length() < 3)
        && (isTaskDependentFilter == false || filter.getTask() == null)
        && filter.getStartTimeOfLastModification() == null
        && filter.getStopTimeOfLastModification() == null) {
      // Don't search to large tables if to less filter settings are given.
      return;
    }
    final BaseSearchFilter baseSearchFilter;
    if (isTaskDependentFilter == true) {
      baseSearchFilter = (BaseSearchFilter) BeanHelper.newInstance(registeredFilterClass, new Class< ? >[] { BaseSearchFilter.class},
          filter);
      ((TaskDependentFilter) baseSearchFilter).setTaskId(filter.getTaskId());
    } else {
      baseSearchFilter = filter;
    }
    final List<SearchResultData> searchResult = searchDao.getEntries(baseSearchFilter, webRegistryEntry.getDOClass(), webRegistryEntry.getDao());
    if (CollectionUtils.isEmpty(searchResult) == true) {
      return;
    }
    final List<ExtendedBaseDO<Integer>> list = new ArrayList<ExtendedBaseDO<Integer>>();
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
    final List< ? > columns = listPageColumnsCreator.createColumns(page, false);
    @SuppressWarnings({ "rawtypes", "unchecked"})
    final DataTable< ?, String > dataTable = new DefaultDataTable("dataTable", columns, new MySortableDataProvider("NOSORT", SortOrder.DESCENDING) {
      @Override
      public List< ? > getList()
      {
        return list;
      }

      @Override
      protected IModel< ? > getModel(final Object object)
      {
        return new Model((Serializable) object);
      }
    }, filter.getMaxRows());
    add(dataTable);
    if (hasMore == true) {
      add(new WebMarkupContainer("hasMoreEntries"));
    } else {
      add(new Label("hasMoreEntries", "[invisible]").setVisible(false));
    }
    final long duration = System.currentTimeMillis() - millis;
    add(new Label("areaTitle", page.getString(webRegistryEntry.getI18nTitleHeading()) + " (" + NumberFormatter.format(duration) + " ms)"));
  }
}
