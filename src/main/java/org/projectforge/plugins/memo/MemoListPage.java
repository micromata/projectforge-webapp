/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2012 Kai Reinhard (k.reinhard@micromata.com)
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

package org.projectforge.plugins.memo;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
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
import org.projectforge.web.calendar.DateTimeFormatter;
import org.projectforge.web.wicket.AbstractListPage;
import org.projectforge.web.wicket.CellItemListener;
import org.projectforge.web.wicket.CellItemListenerPropertyColumn;
import org.projectforge.web.wicket.DetachableDOModel;
import org.projectforge.web.wicket.IListPageColumnsCreator;
import org.projectforge.web.wicket.ListPage;
import org.projectforge.web.wicket.ListSelectActionPanel;

/**
 * The controller of the list page. Most functionality such as search etc. is done by the super class.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
@ListPage(editPage = MemoEditPage.class)
public class MemoListPage extends AbstractListPage<MemoListForm, MemoDao, MemoDO> implements IListPageColumnsCreator<MemoDO>
{
  private static final long serialVersionUID = 4228569581764015696L;

  @SpringBean(name = "memoDao")
  private MemoDao memoDao;

  public MemoListPage(final PageParameters parameters)
  {
    super(parameters, "plugins.memo");
  }

  @SuppressWarnings("serial")
  public List<IColumn<MemoDO>> createColumns(final WebPage returnToPage, final boolean sortable)
  {
    final List<IColumn<MemoDO>> columns = new ArrayList<IColumn<MemoDO>>();
    final CellItemListener<MemoDO> cellItemListener = new CellItemListener<MemoDO>() {
      public void populateItem(Item<ICellPopulator<MemoDO>> item, String componentId, IModel<MemoDO> rowModel)
      {
        final MemoDO memo = rowModel.getObject();
        final StringBuffer cssStyle = getCssStyle(memo.getId(), memo.isDeleted());
        if (cssStyle.length() > 0) {
          item.add(new AttributeModifier("style", true, new Model<String>(cssStyle.toString())));
        }
      }
    };

    columns.add(new CellItemListenerPropertyColumn<MemoDO>(new Model<String>(getString("created")), getSortable("created", sortable),
        "created", cellItemListener) {
      @SuppressWarnings("unchecked")
      @Override
      public void populateItem(final Item item, final String componentId, final IModel rowModel)
      {
        final MemoDO memo = (MemoDO) rowModel.getObject();
        item.add(new ListSelectActionPanel(componentId, rowModel, MemoEditPage.class, memo.getId(), returnToPage, DateTimeFormatter
            .instance().getFormattedDateTime(memo.getCreated())));
        addRowClick(item);
        cellItemListener.populateItem(item, componentId, rowModel);
      }
    });
    columns.add(new CellItemListenerPropertyColumn<MemoDO>(getString("modified"), getSortable("lastUpdate", sortable), "lastUpdate",
        cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<MemoDO>(new Model<String>(getString("plugins.memo.subject")), getSortable("subject",
        sortable), "subject", cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<MemoDO>(new Model<String>(getString("plugins.memo.memo")),
        getSortable("memo", sortable), "memo", cellItemListener) {
          @Override
          public void populateItem(Item<ICellPopulator<MemoDO>> item, String componentId, IModel<MemoDO> rowModel)
          {
            final MemoDO memo = rowModel.getObject();
            final Label label = new Label(componentId, new Model<String>(StringUtils.abbreviate(memo.getMemo(), 100)));
            cellItemListener.populateItem(item, componentId, rowModel);
            item.add(label);
          }
        });
    return columns;
  }

  @Override
  protected void init()
  {
    dataTable = createDataTable(createColumns(this, true), "lastUpdate", false);
    form.add(dataTable);
  }

  @Override
  protected MemoListForm newListForm(AbstractListPage< ? , ? , ? > parentPage)
  {
    return new MemoListForm(this);
  }

  @Override
  protected MemoDao getBaseDao()
  {
    return memoDao;
  }

  @Override
  protected IModel<MemoDO> getModel(MemoDO object)
  {
    return new DetachableDOModel<MemoDO, MemoDao>(object, getBaseDao());
  }

  protected MemoDao getMemoDao()
  {
    return memoDao;
  }
}
