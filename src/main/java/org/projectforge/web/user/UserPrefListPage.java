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

package org.projectforge.web.user;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.PageParameters;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.user.UserPrefDO;
import org.projectforge.user.UserPrefDao;
import org.projectforge.web.wicket.AbstractListPage;
import org.projectforge.web.wicket.CellItemListener;
import org.projectforge.web.wicket.CellItemListenerPropertyColumn;
import org.projectforge.web.wicket.DetachableDOModel;
import org.projectforge.web.wicket.ListPage;
import org.projectforge.web.wicket.ListSelectActionPanel;


@ListPage(editPage = UserPrefEditPage.class)
public class UserPrefListPage extends AbstractListPage<UserPrefListForm, UserPrefDao, UserPrefDO>
{
  private static final long serialVersionUID = 6121734373079865758L;

  @SpringBean(name = "userPrefDao")
  private UserPrefDao userPrefDao;

  @SpringBean(name = "userFormatter")
  private UserFormatter userFormatter;

  public UserPrefListPage(PageParameters parameters)
  {
    super(parameters, "user.pref");
  }

  @SuppressWarnings("serial")
  @Override
  protected void init()
  {
    List<IColumn<UserPrefDO>> columns = new ArrayList<IColumn<UserPrefDO>>();

    CellItemListener<UserPrefDO> cellItemListener = new CellItemListener<UserPrefDO>() {
      public void populateItem(Item<ICellPopulator<UserPrefDO>> item, String componentId, IModel<UserPrefDO> rowModel)
      {
      }
    };
    columns.add(new CellItemListenerPropertyColumn<UserPrefDO>(new Model<String>(getString("user.pref.area")), "area", "area",
        cellItemListener) {
      @SuppressWarnings("unchecked")
      @Override
      public void populateItem(final Item item, final String componentId, final IModel rowModel)
      {
        final UserPrefDO userPref = (UserPrefDO) rowModel.getObject();
        final String label;
        if (userPref.getArea() != null) {
          label = getString(userPref.getArea().getI18nKey());
        } else {
          label = "";
        }
        item.add(new ListSelectActionPanel(componentId, rowModel, UserPrefEditPage.class, userPref.getId(), UserPrefListPage.this, label));
        cellItemListener.populateItem(item, componentId, rowModel);
        addRowClick(item);
      }
    });
    columns.add(new CellItemListenerPropertyColumn<UserPrefDO>(new Model<String>(getString("user.pref.name")), "name", "name",
        cellItemListener));
    columns.add(new UserPropertyColumn<UserPrefDO>(getString("user"), "user.fullname", "user", cellItemListener)
        .withUserFormatter(userFormatter));
    columns.add(new CellItemListenerPropertyColumn<UserPrefDO>(new Model<String>(getString("filter.lastModified")), "lastUpdate",
        "lastUpdate", cellItemListener));
    dataTable = createDataTable(columns, null, false);
    form.add(dataTable);
  }

  @Override
  protected UserPrefListForm newListForm(AbstractListPage< ? , ? , ? > parentPage)
  {
    return new UserPrefListForm(this);
  }

  @Override
  protected UserPrefDao getBaseDao()
  {
    return userPrefDao;
  }

  @Override
  protected IModel<UserPrefDO> getModel(UserPrefDO object)
  {
    return new DetachableDOModel<UserPrefDO, UserPrefDao>(object, getBaseDao());
  }
}
