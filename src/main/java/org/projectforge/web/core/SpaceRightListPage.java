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

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.PageParameters;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.core.SpaceDO;
import org.projectforge.core.SpaceDao;
import org.projectforge.web.wicket.AbstractListPage;
import org.projectforge.web.wicket.CellItemListener;
import org.projectforge.web.wicket.CellItemListenerPropertyColumn;
import org.projectforge.web.wicket.DetachableDOModel;
import org.projectforge.web.wicket.ListPage;
import org.projectforge.web.wicket.ListSelectActionPanel;

@ListPage(editPage = SpaceEditPage.class)
public class SpaceRightListPage extends AbstractListPage<SpaceRightListForm, SpaceDao, SpaceDO>
{
  private static final long serialVersionUID = -5568398941502247868L;

  @SpringBean(name = "spaceDao")
  private SpaceDao spaceDao;

  public SpaceRightListPage(final PageParameters parameters)
  {
    super(parameters, "space");
  }

  @SuppressWarnings("serial")
  @Override
  protected void init()
  {
    final List<IColumn<SpaceDO>> columns = new ArrayList<IColumn<SpaceDO>>();
    final CellItemListener<SpaceDO> cellItemListener = new CellItemListener<SpaceDO>() {
      public void populateItem(final Item<ICellPopulator<SpaceDO>> item, final String componentId, final IModel<SpaceDO> rowModel)
      {
        final SpaceDO space = rowModel.getObject();
        final StringBuffer cssStyle = getCssStyle(space.getId(), space.isDeleted());
        if (cssStyle.length() > 0) {
          item.add(new AttributeModifier("style", true, new Model<String>(cssStyle.toString())));
        }
      }
    };
    columns.add(new CellItemListenerPropertyColumn<SpaceDO>(new Model<String>(getString("space.identifier")), "identifier", "identifier",
        cellItemListener) {
      @Override
      public void populateItem(final Item<ICellPopulator<SpaceDO>> item, final String componentId, final IModel<SpaceDO> rowModel)
      {
        final SpaceDO space = rowModel.getObject();
        item.add(new ListSelectActionPanel(componentId, rowModel, SpaceEditPage.class, space.getId(), SpaceRightListPage.this, space
            .getIdentifier()));
        addRowClick(item);
        cellItemListener.populateItem(item, componentId, rowModel);
      }
    });
    columns
    .add(new CellItemListenerPropertyColumn<SpaceDO>(new Model<String>(getString("space.title")), "title", "title", cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<SpaceDO>(new Model<String>(getString("status")), "status", "status", cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<SpaceDO>(new Model<String>(getString("description")), "description", "description",
        cellItemListener));
    dataTable = createDataTable(columns, null, true);
    form.add(dataTable);
  }

  @Override
  protected SpaceRightListForm newListForm(final AbstractListPage< ? , ? , ? > parentPage)
  {
    return new SpaceRightListForm(this);
  }

  @Override
  protected SpaceDao getBaseDao()
  {
    return spaceDao;
  }

  @Override
  protected IModel<SpaceDO> getModel(final SpaceDO object)
  {
    return new DetachableDOModel<SpaceDO, SpaceDao>(object, getBaseDao());
  }
}
