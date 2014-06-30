/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2014 Kai Reinhard (k.reinhard@micromata.de)
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

package org.projectforge.plugins.teamcal.event;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.plugins.todo.ToDoEditPage;
import org.projectforge.web.calendar.DateTimeFormatter;
import org.projectforge.web.user.UserFormatter;
import org.projectforge.web.user.UserPropertyColumn;
import org.projectforge.web.wicket.AbstractListPage;
import org.projectforge.web.wicket.CellItemListener;
import org.projectforge.web.wicket.CellItemListenerPropertyColumn;
import org.projectforge.web.wicket.IListPageColumnsCreator;
import org.projectforge.web.wicket.ListPage;
import org.projectforge.web.wicket.ListSelectActionPanel;

/**
 * @author Werner Feder (w.feder.extern@micromata.de)
 *
 */
@ListPage(editPage = ToDoEditPage.class)
public class LocalInvitationListPage extends AbstractListPage<LocalInvitationListForm, LocalInvitationDao, LocalInvitationDO> implements IListPageColumnsCreator<LocalInvitationDO>
{
  private static final long serialVersionUID = -1743737304199874436L;

  @SpringBean(name = "localInvitationDao")
  private LocalInvitationDao localInvitationDao;

  @SpringBean(name = "userFormatter")
  private UserFormatter userFormatter;

  public LocalInvitationListPage(final PageParameters parameters)
  {
    super(parameters, "plugins.teamcal");
  }

  @SuppressWarnings("serial")
  public List<IColumn<LocalInvitationDO, String>> createColumns(final WebPage returnToPage, final boolean sortable)
  {
    final List<IColumn<LocalInvitationDO, String>> columns = new ArrayList<IColumn<LocalInvitationDO, String>>();
    final CellItemListener<LocalInvitationDO> cellItemListener = new CellItemListener<LocalInvitationDO>() {
      public void populateItem(final Item<ICellPopulator<LocalInvitationDO>> item, final String componentId, final IModel<LocalInvitationDO> rowModel)
      {
        final LocalInvitationDO localInvitation = rowModel.getObject();
        appendCssClasses(item, localInvitation.getId(), localInvitation.isDeleted());
      }
    };

    columns.add(new CellItemListenerPropertyColumn<LocalInvitationDO>(getString("plugins.teamcal.event.subject"), getSortable("subject", sortable), "teamEvent.subject",
        cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<LocalInvitationDO>(getString("plugins.teamcal.event.startDate"), getSortable("startDate",
        sortable), "teamEvent.startDate", cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<LocalInvitationDO>(getString("plugins.teamcal.event.endDate"), getSortable("endDate", sortable),
        "teamEvent.endDate", cellItemListener));

    columns.add(new UserPropertyColumn<LocalInvitationDO>(getString("user.username"), getSortable("userId", sortable), "user",
        cellItemListener).withUserFormatter(userFormatter));

    columns.add(new CellItemListenerPropertyColumn<LocalInvitationDO>(LocalInvitationDO.class, getSortable("created", sortable),
        "created", cellItemListener) {
      @SuppressWarnings({ "unchecked", "rawtypes"})
      @Override
      public void populateItem(final Item item, final String componentId, final IModel rowModel)
      {
        final LocalInvitationDO localInvitation = (LocalInvitationDO) rowModel.getObject();
        item.add(new ListSelectActionPanel(componentId, rowModel, LocalInvitationEditPage.class, localInvitation.getId(), returnToPage, DateTimeFormatter
            .instance().getFormattedDateTime(localInvitation.getCreated())));
        addRowClick(item);
        cellItemListener.populateItem(item, componentId, rowModel);
      }
    });

    columns.add(new CellItemListenerPropertyColumn<LocalInvitationDO>(LocalInvitationDO.class, getSortable("lastUpdate", sortable), "lastUpdate",
        cellItemListener));

    return columns;
  }

  @Override
  protected void init()
  {
    dataTable = createDataTable(createColumns(this, true), "lastUpdate", SortOrder.DESCENDING);
    form.add(dataTable);
  }

  /**
   * @see org.projectforge.web.wicket.AbstractListPage#select(java.lang.String, java.lang.Object)
   */
  @Override
  public void select(final String property, final Object selectedValue)
  {
    // Do nothing.
  }

  /**
   * 
   * @see org.projectforge.web.fibu.ISelectCallerPage#unselect(java.lang.String)
   */
  @Override
  public void unselect(final String property)
  {
    // Do nothing.
  }

  @Override
  protected LocalInvitationListForm newListForm(final AbstractListPage< ? , ? , ? > parentPage)
  {
    return new LocalInvitationListForm(this);
  }

  @Override
  protected LocalInvitationDao getBaseDao()
  {
    return localInvitationDao;
  }

}
