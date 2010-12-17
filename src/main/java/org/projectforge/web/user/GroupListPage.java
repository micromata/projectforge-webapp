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

package org.projectforge.web.user;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.PageParameters;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.access.OperationType;
import org.projectforge.user.GroupDO;
import org.projectforge.user.GroupDao;
import org.projectforge.web.fibu.ISelectCallerPage;
import org.projectforge.web.wicket.AbstractListPage;
import org.projectforge.web.wicket.CellItemListener;
import org.projectforge.web.wicket.CellItemListenerPropertyColumn;
import org.projectforge.web.wicket.DetachableDOModel;
import org.projectforge.web.wicket.IListPageColumnsCreator;
import org.projectforge.web.wicket.ListPage;
import org.projectforge.web.wicket.ListSelectActionPanel;

@ListPage(editPage = GroupEditPage.class)
public class GroupListPage extends AbstractListPage<GroupListForm, GroupDao, GroupDO> implements IListPageColumnsCreator<GroupDO>
{
  private static final long serialVersionUID = 3124148202828889250L;

  @SpringBean(name = "groupDao")
  private GroupDao groupDao;

  public GroupListPage(final PageParameters parameters)
  {
    super(parameters, "group");
  }

  public GroupListPage(final ISelectCallerPage caller, final String selectProperty)
  {
    super(caller, selectProperty, "group");
  }

  @SuppressWarnings("serial")
  @Override
  public List<IColumn<GroupDO>> createColumns(final WebPage returnToPage)
  {
    final List<IColumn<GroupDO>> columns = new ArrayList<IColumn<GroupDO>>();
    final CellItemListener<GroupDO> cellItemListener = new CellItemListener<GroupDO>() {
      public void populateItem(Item<ICellPopulator<GroupDO>> item, String componentId, IModel<GroupDO> rowModel)
      {
        final GroupDO group = rowModel.getObject();
        final StringBuffer cssStyle = getCssStyle(group.getId(), group.isDeleted());
        if (cssStyle.length() > 0) {
          item.add(new AttributeModifier("style", true, new Model<String>(cssStyle.toString())));
        }
      }
    };
    columns.add(new CellItemListenerPropertyColumn<GroupDO>(new Model<String>(getString("name")), "name", "name", cellItemListener) {
      @SuppressWarnings("unchecked")
      @Override
      public void populateItem(final Item item, final String componentId, final IModel rowModel)
      {
        final boolean updateAccess = groupDao.hasAccess(null, null, OperationType.UPDATE, false);
        final GroupDO group = (GroupDO) rowModel.getObject();
        if (isSelectMode() == true) {
          item.add(new ListSelectActionPanel(componentId, rowModel, caller, selectProperty, group.getId(), group.getName()));
          addRowClick(item);
        } else if (updateAccess == true) {
          item.add(new ListSelectActionPanel(componentId, rowModel, GroupEditPage.class, group.getId(), returnToPage, group.getName()));
          addRowClick(item);
        } else {
          item.add(new Label(componentId, group.getName()));
        }
        cellItemListener.populateItem(item, componentId, rowModel);
      }
    });
    columns.add(new CellItemListenerPropertyColumn<GroupDO>(new Model<String>(getString("organization")), "organization", "organization",
        cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<GroupDO>(new Model<String>(getString("description")), "description", "description",
        cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<GroupDO>(new Model<String>(getString("group.assignedUsers")), "usernames", "usernames",
        cellItemListener));
    return columns;
  }

  @Override
  protected void init()
  {
    dataTable = createDataTable(createColumns(this), "name", true);
    form.add(dataTable);
  }

  @Override
  protected GroupListForm newListForm(AbstractListPage< ? , ? , ? > parentPage)
  {
    return new GroupListForm(this);
  }

  @Override
  protected GroupDao getBaseDao()
  {
    return groupDao;
  }

  @Override
  protected IModel<GroupDO> getModel(GroupDO object)
  {
    return new DetachableDOModel<GroupDO, GroupDao>(object, getBaseDao());
  }

  protected GroupDao getGroupDao()
  {
    return groupDao;
  }
}
