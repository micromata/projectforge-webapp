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

package org.projectforge.web.meb;

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
import org.projectforge.meb.MebDao;
import org.projectforge.meb.MebEntryDO;
import org.projectforge.meb.MebEntryStatus;
import org.projectforge.web.calendar.DateTimeFormatter;
import org.projectforge.web.user.UserFormatter;
import org.projectforge.web.user.UserPropertyColumn;
import org.projectforge.web.wicket.AbstractListPage;
import org.projectforge.web.wicket.CellItemListener;
import org.projectforge.web.wicket.CellItemListenerPropertyColumn;
import org.projectforge.web.wicket.DetachableDOModel;
import org.projectforge.web.wicket.ListPage;
import org.projectforge.web.wicket.ListSelectActionPanel;

@ListPage(editPage = MebEditPage.class)
public class MebListPage extends AbstractListPage<MebListForm, MebDao, MebEntryDO>
{
  private static final long serialVersionUID = -3852280776436565963L;

  @SpringBean(name = "mebDao")
  private MebDao mebDao;

  @SpringBean(name = "userFormatter")
  private UserFormatter userFormatter;

  public MebListPage(PageParameters parameters)
  {
    super(parameters, "meb");
  }

  @SuppressWarnings("serial")
  @Override
  protected void init()
  {
    List<IColumn<MebEntryDO>> columns = new ArrayList<IColumn<MebEntryDO>>();
    CellItemListener<MebEntryDO> cellItemListener = new CellItemListener<MebEntryDO>() {
      public void populateItem(Item<ICellPopulator<MebEntryDO>> item, String componentId, IModel<MebEntryDO> rowModel)
      {
        final MebEntryDO meb = rowModel.getObject();
        final StringBuffer cssStyle = getCssStyle(meb.getId(), meb.isDeleted());
        if (meb.isDeleted() == true) {
          // Should not occur
        } else if (meb.getStatus() == MebEntryStatus.RECENT) {
          cssStyle.append("font-weight: bold;");
        } else if (meb.getStatus() == MebEntryStatus.IMPORTANT) {
          cssStyle.append("font-weight: bold; color: red;");
        } else if (meb.getStatus() == MebEntryStatus.DONE) {
          cssStyle.append("color: green;");
        }
        if (cssStyle.length() > 0) {
          item.add(new AttributeModifier("style", true, new Model<String>(cssStyle.toString())));
        }
      }
    };
    columns.add(new CellItemListenerPropertyColumn<MebEntryDO>(new Model<String>(getString("date")), "date", "date", cellItemListener) {
      @SuppressWarnings("unchecked")
      @Override
      public void populateItem(final Item item, final String componentId, final IModel rowModel)
      {
        final MebEntryDO meb = (MebEntryDO) rowModel.getObject();
        item.add(new ListSelectActionPanel(componentId, rowModel, MebEditPage.class, meb.getId(), MebListPage.this, DateTimeFormatter
            .instance().getFormattedDateTime(meb.getDate())));
        cellItemListener.populateItem(item, componentId, rowModel);
        cellItemListener.populateItem(item, componentId, rowModel);
        addRowClick(item);
      }
    });
    columns.add(new UserPropertyColumn<MebEntryDO>(getString("meb.owner"), "owner", "owner", cellItemListener)
        .withUserFormatter(userFormatter));
    columns.add(new CellItemListenerPropertyColumn<MebEntryDO>(new Model<String>(getString("meb.sender")), "sender", "sender",
        cellItemListener));
    columns
        .add(new CellItemListenerPropertyColumn<MebEntryDO>(new Model<String>(getString("status")), "status", "status", cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<MebEntryDO>(new Model<String>(getString("meb.message")), "message", "message",
        cellItemListener));
    dataTable = createDataTable(columns, "date", false);
    form.add(dataTable);
  }

  @Override
  protected MebListForm newListForm(AbstractListPage< ? , ? , ? > parentPage)
  {
    return new MebListForm(this);
  }

  @Override
  protected MebDao getBaseDao()
  {
    return mebDao;
  }

  @Override
  protected IModel<MebEntryDO> getModel(MebEntryDO object)
  {
    return new DetachableDOModel<MebEntryDO, MebDao>(object, getBaseDao());
  }

  protected MebDao getMebDao()
  {
    return mebDao;
  }
}
