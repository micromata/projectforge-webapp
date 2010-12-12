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
import java.util.List;

import org.apache.wicket.PageParameters;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DefaultDataTable;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.projectforge.address.AddressDO;
import org.projectforge.address.AddressDao;
import org.projectforge.common.BeanHelper;
import org.projectforge.core.BaseDao;
import org.projectforge.core.BaseSearchFilter;
import org.projectforge.registry.DaoRegistry;
import org.projectforge.registry.Registry;
import org.projectforge.registry.RegistryEntry;
import org.projectforge.web.fibu.ISelectCallerPage;
import org.projectforge.web.wicket.AbstractSecuredPage;
import org.projectforge.web.wicket.IListPageColumnsCreator;
import org.projectforge.web.wicket.MySortableDataProvider;

public class SearchPage extends AbstractSecuredPage implements ISelectCallerPage
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SearchPage.class);

  private SearchForm form;

  private RepeatingView areaRepeater;

  public SearchPage(PageParameters parameters)
  {
    super(parameters);
    form = new SearchForm(this);
    body.add(form);
    form.init();
    refresh();
  }

  @SuppressWarnings("serial")
  void refresh()
  {
    if (areaRepeater != null) {
      body.remove(areaRepeater);
    }
    areaRepeater = new RepeatingView("areaRepeater");
    body.add(areaRepeater);
    final WebMarkupContainer areaContainer = new WebMarkupContainer(areaRepeater.newChildId());
    areaRepeater.add(areaContainer);
    final RegistryEntry registryEntry = Registry.instance().getEntry(DaoRegistry.ADDRESS);
    final BaseDao< ? > dao = registryEntry.getProxyDao();
    areaContainer.add(new Label("areaTitle", getString(registryEntry.getI18nTitleHeading())));
    final Class< ? extends IListPageColumnsCreator< ? >> clazz = registryEntry.getListPageColumnsCreatorClass();
    final IListPageColumnsCreator< ? > listPageColumnsCreator = clazz == null ? null : (IListPageColumnsCreator< ? >) BeanHelper.newInstance(clazz);
    if (listPageColumnsCreator == null) {
      log.warn("RegistryEntry '" + registryEntry.getId() + "' doesn't have an IListPageColumnsCreator (can't display search results).");
      final WebMarkupContainer dataTable = new WebMarkupContainer("dataTable");
      dataTable.setVisible(false);
      areaContainer.add(dataTable);
    } else {
      final List< ? > columns = listPageColumnsCreator.createColumns();
      @SuppressWarnings("unchecked")
      final DataTable<AddressDO> dataTable = new DefaultDataTable("dataTable", columns, new MySortableDataProvider("NOSORT", false) {
        @Override
        public List getList()
        {
          return ((AddressDao) dao).getNewest(new BaseSearchFilter());
        }

        @Override
        protected IModel getModel(Object object)
        {
          return new Model((Serializable)object);
        }
      }, form.data.getPageSize());
      areaContainer.add(dataTable);
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
