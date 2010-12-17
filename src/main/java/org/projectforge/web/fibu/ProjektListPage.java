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

package org.projectforge.web.fibu;

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
import org.projectforge.fibu.ProjektDO;
import org.projectforge.fibu.ProjektDao;
import org.projectforge.fibu.kost.KostCache;
import org.projectforge.reporting.Kost2Art;
import org.projectforge.reporting.impl.ProjektImpl;
import org.projectforge.user.GroupDO;
import org.projectforge.user.UserGroupCache;
import org.projectforge.web.task.TaskFormatter;
import org.projectforge.web.task.TaskPropertyColumn;
import org.projectforge.web.wicket.AbstractListPage;
import org.projectforge.web.wicket.CellItemListener;
import org.projectforge.web.wicket.CellItemListenerPropertyColumn;
import org.projectforge.web.wicket.DetachableDOModel;
import org.projectforge.web.wicket.IListPageColumnsCreator;
import org.projectforge.web.wicket.ListPage;
import org.projectforge.web.wicket.ListSelectActionPanel;

@ListPage(editPage = ProjektEditPage.class)
public class ProjektListPage extends AbstractListPage<ProjektListForm, ProjektDao, ProjektDO> implements IListPageColumnsCreator<ProjektDO>
{
  private static final long serialVersionUID = -8406452960003792763L;

  @SpringBean(name = "projektDao")
  private ProjektDao projektDao;

  @SpringBean(name = "kostCache")
  private KostCache kostCache;

  @SpringBean(name = "userGroupCache")
  private UserGroupCache userGroupCache;

  @SpringBean(name = "taskFormatter")
  private TaskFormatter taskFormatter;

  public ProjektListPage(final PageParameters parameters)
  {
    super(parameters, "fibu.projekt");
  }

  public ProjektListPage(final ISelectCallerPage caller, final String selectProperty)
  {
    super(caller, selectProperty, "fibu.projekt");
  }

  @SuppressWarnings("serial")
  @Override
  public List<IColumn<ProjektDO>> createColumns(final WebPage returnToPage, final boolean sortable)
  {
    final List<IColumn<ProjektDO>> columns = new ArrayList<IColumn<ProjektDO>>();
    CellItemListener<ProjektDO> cellItemListener = new CellItemListener<ProjektDO>() {
      public void populateItem(Item<ICellPopulator<ProjektDO>> item, String componentId, IModel<ProjektDO> rowModel)
      {
        final ProjektDO projekt = rowModel.getObject();
        if (projekt.getStatus() == null) {
          // Should not occur:
          return;
        }
        final StringBuffer cssStyle = getCssStyle(projekt.getId(), projekt.isDeleted());
        if (cssStyle.length() > 0) {
          item.add(new AttributeModifier("style", true, new Model<String>(cssStyle.toString())));
        }
      }
    };
    columns.add(new CellItemListenerPropertyColumn<ProjektDO>(new Model<String>(getString("fibu.projekt.nummer")), getSortable("kost",
        sortable), "kost", cellItemListener) {
      @SuppressWarnings("unchecked")
      @Override
      public void populateItem(final Item item, final String componentId, final IModel rowModel)
      {
        final ProjektDO projekt = (ProjektDO) rowModel.getObject();
        if (isSelectMode() == false) {
          item.add(new ListSelectActionPanel(componentId, rowModel, ProjektEditPage.class, projekt.getId(), returnToPage, String
              .valueOf(projekt.getKost())));
        } else {
          item.add(new ListSelectActionPanel(componentId, rowModel, caller, selectProperty, projekt.getId(), String.valueOf(projekt
              .getKost())));
        }
        cellItemListener.populateItem(item, componentId, rowModel);
        addRowClick(item);
      }
    });
    columns.add(new CellItemListenerPropertyColumn<ProjektDO>(new Model<String>(getString("fibu.projekt.identifier")), getSortable(
        "identifier", sortable), "identifier", cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<ProjektDO>(new Model<String>(getString("fibu.kunde.name")), getSortable("kunde.name",
        sortable), "kunde.name", cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<ProjektDO>(new Model<String>(getString("fibu.projekt.name")), getSortable("name",
        sortable), "name", cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<ProjektDO>(new Model<String>(getString("fibu.kunde.division")), getSortable(
        "kunde.division", sortable), "kunde.division", cellItemListener));
    columns.add(new TaskPropertyColumn<ProjektDO>(this, getString("task"), getSortable("task.title", sortable), "task", cellItemListener)
        .withTaskFormatter(taskFormatter));
    columns.add(new CellItemListenerPropertyColumn<ProjektDO>(new Model<String>(getString("status")), getSortable("status", sortable),
        "status", cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<ProjektDO>(new Model<String>(getString("fibu.projekt.projektManagerGroup")), null,
        "projektManagerGroup", cellItemListener) {
      @SuppressWarnings("unchecked")
      @Override
      public void populateItem(final Item item, final String componentId, final IModel rowModel)
      {
        final ProjektDO projektDO = (ProjektDO) rowModel.getObject();
        String groupName = "";
        if (projektDO.getProjektManagerGroupId() != null) {
          final GroupDO group = userGroupCache.getGroup(projektDO.getProjektManagerGroupId());
          if (group != null) {
            groupName = group.getName();
          }
        }
        Label label = new Label(componentId, groupName);
        item.add(label);
        cellItemListener.populateItem(item, componentId, rowModel);
      }
    });
    columns.add(new CellItemListenerPropertyColumn<ProjektDO>(new Model<String>(getString("fibu.kost2art.kost2arten")), null,
        "kost2ArtsAsHtml", cellItemListener) {
      @SuppressWarnings("unchecked")
      @Override
      public void populateItem(final Item item, final String componentId, final IModel rowModel)
      {
        final ProjektDO projektDO = (ProjektDO) rowModel.getObject();
        final ProjektImpl projekt = new ProjektImpl(projektDO);
        final List<Kost2Art> kost2Arts = kostCache.getAllKost2Arts(projektDO.getId());
        projekt.setKost2Arts(kost2Arts);
        final Label label = new Label(componentId, new Model<String>(projekt.getKost2ArtsAsHtml()));
        label.setEscapeModelStrings(false);
        item.add(label);
        cellItemListener.populateItem(item, componentId, rowModel);
      }
    });
    columns.add(new CellItemListenerPropertyColumn<ProjektDO>(new Model<String>(getString("description")), getSortable("description",
        sortable), "description", cellItemListener));
    return columns;
  }

  @Override
  protected void init()
  {
    dataTable = createDataTable(createColumns(this, true), "kost", true);
    form.add(dataTable);
  }

  @Override
  protected ProjektListForm newListForm(AbstractListPage< ? , ? , ? > parentPage)
  {
    return new ProjektListForm(this);
  }

  @Override
  protected ProjektDao getBaseDao()
  {
    return projektDao;
  }

  @Override
  protected IModel<ProjektDO> getModel(ProjektDO object)
  {
    return new DetachableDOModel<ProjektDO, ProjektDao>(object, getBaseDao());
  }

  protected ProjektDao getProjektDao()
  {
    return projektDao;
  }
}
