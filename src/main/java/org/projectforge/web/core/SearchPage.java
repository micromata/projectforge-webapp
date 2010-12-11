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

import java.util.List;

import org.apache.wicket.PageParameters;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.address.AddressDO;
import org.projectforge.address.AddressDao;
import org.projectforge.core.BaseSearchFilter;
import org.projectforge.web.address.AddressListPage;
import org.projectforge.web.fibu.ISelectCallerPage;
import org.projectforge.web.wicket.AbstractSecuredPage;
import org.projectforge.web.wicket.DetachableDOModel;
import org.projectforge.web.wicket.MySortableDataProvider;

public class SearchPage extends AbstractSecuredPage implements ISelectCallerPage
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SearchPage.class);

  private SearchForm form;

  private RepeatingView areaRepeater;

  @SpringBean(name = "addressDao")
  private AddressDao addressDao;

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
    areaContainer.add(new Label("areaTitle", "Hurzel"));
    final AddressListPage listPage = new AddressListPage(new PageParameters());
    final List<IColumn<AddressDO>> columns = listPage.createColumns();
    final DataTable<AddressDO> dataTable = new DefaultDataTable<AddressDO>("dataTable", columns, new MySortableDataProvider<AddressDO>("NOSORT", false) {
      @Override
      public List<AddressDO> getList()
      {
        return addressDao.getNewest(new BaseSearchFilter());
      }
      @Override
      protected IModel<AddressDO> getModel(AddressDO object)
      {
        return new DetachableDOModel<AddressDO, AddressDao>(object, addressDao);
      }
    }, 25);
    areaContainer.add(dataTable);
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
