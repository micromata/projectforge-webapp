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
import java.util.List;

import org.apache.wicket.PageParameters;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DefaultDataTable;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.common.BeanHelper;
import org.projectforge.core.SearchDao;
import org.projectforge.core.SearchFilter;
import org.projectforge.core.SearchResultData;
import org.projectforge.registry.Registry;
import org.projectforge.registry.RegistryEntry;
import org.projectforge.web.fibu.ISelectCallerPage;
import org.projectforge.web.wicket.AbstractSecuredPage;
import org.projectforge.web.wicket.IListPageColumnsCreator;
import org.projectforge.web.wicket.MySortableDataProvider;
import org.springframework.util.CollectionUtils;

public class SearchPage extends AbstractSecuredPage implements ISelectCallerPage
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SearchPage.class);

  private SearchForm form;

  private RepeatingView areaRepeater;

  @SpringBean(name = "searchDao")
  private SearchDao searchDao;

  public SearchPage(PageParameters parameters)
  {
    super(parameters);
    form = new SearchForm(this);
    body.add(form);
    form.init();
    refresh();
  }

  @SuppressWarnings({ "serial", "unchecked"})
  void refresh()
  {
    if (areaRepeater != null) {
      body.remove(areaRepeater);
    }
    areaRepeater = new RepeatingView("areaRepeater");
    body.add(areaRepeater);
    for (final RegistryEntry registryEntry : Registry.instance().getOrderedList()) {
      final SearchFilter filter = new SearchFilter();
      final List<SearchResultData> searchResult = searchDao.getHistoryEntries(filter, registryEntry.getDOClass(), registryEntry.getDao());
      if (CollectionUtils.isEmpty(searchResult) == true) {
        continue;
      }
      final List list = new ArrayList();
      int counter = form.data.getPageSize();
      for (final SearchResultData data : searchResult) {
        list.add(data.getDataObject());
        if (--counter <= 0) {
          break;
        }
      }
      final WebMarkupContainer areaContainer = new WebMarkupContainer(areaRepeater.newChildId());
      areaRepeater.add(areaContainer);
      areaContainer.add(new Label("areaTitle", getString(registryEntry.getI18nTitleHeading())));
      final Class< ? extends IListPageColumnsCreator< ? >> clazz = registryEntry.getListPageColumnsCreatorClass();
      final IListPageColumnsCreator< ? > listPageColumnsCreator = clazz == null ? null : (IListPageColumnsCreator< ? >) BeanHelper
          .newInstance(clazz);
      if (listPageColumnsCreator == null) {
        log.warn("RegistryEntry '" + registryEntry.getId() + "' doesn't have an IListPageColumnsCreator (can't display search results).");
        final WebMarkupContainer dataTable = new WebMarkupContainer("dataTable");
        dataTable.setVisible(false);
        areaContainer.add(dataTable);
      } else {
        final List< ? > columns = listPageColumnsCreator.createColumns();
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
        }, form.data.getPageSize());
        areaContainer.add(dataTable);
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
      // form.setTask((Integer) selectedValue);
    } else {
      log.error("Property '" + property + "' not supported for selection.");
    }
  }

  public void unselect(final String property)
  {
    if ("taskId".equals(property) == true) {
      // getData().setTaskId(null);
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
