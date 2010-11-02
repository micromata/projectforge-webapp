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

package org.projectforge.web.orga;

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
import org.projectforge.orga.PosteingangDO;
import org.projectforge.orga.PosteingangDao;
import org.projectforge.web.calendar.DateTimeFormatter;
import org.projectforge.web.wicket.AbstractListPage;
import org.projectforge.web.wicket.CellItemListener;
import org.projectforge.web.wicket.CellItemListenerPropertyColumn;
import org.projectforge.web.wicket.DetachableDOModel;
import org.projectforge.web.wicket.ListPage;
import org.projectforge.web.wicket.ListSelectActionPanel;


@ListPage(editPage = PosteingangEditPage.class)
public class PosteingangListPage extends AbstractListPage<PosteingangListForm, PosteingangDao, PosteingangDO>
{
  private static final long serialVersionUID = 6121734373079865758L;

  @SpringBean(name = "posteingangDao")
  private PosteingangDao posteingangDao;

  public PosteingangListPage(PageParameters parameters)
  {
    super(parameters, "orga.posteingang");
  }

  @SuppressWarnings("serial")
  @Override
  protected void init()
  {
    List<IColumn<PosteingangDO>> columns = new ArrayList<IColumn<PosteingangDO>>();

    CellItemListener<PosteingangDO> cellItemListener = new CellItemListener<PosteingangDO>() {
      public void populateItem(Item<ICellPopulator<PosteingangDO>> item, String componentId, IModel<PosteingangDO> rowModel)
      {
        final PosteingangDO posteingang = rowModel.getObject();
        String cellStyle = "";
        if (posteingang.isDeleted() == true) {
          cellStyle = "text-decoration: line-through;";
        }
        item.add(new AttributeModifier("style", true, new Model<String>(cellStyle)));
      }
    };
    columns
        .add(new CellItemListenerPropertyColumn<PosteingangDO>(new Model<String>(getString("date")), "datum", "datum", cellItemListener) {
          @SuppressWarnings("unchecked")
          @Override
          public void populateItem(final Item item, final String componentId, final IModel rowModel)
          {
            PosteingangDO posteingang = (PosteingangDO) rowModel.getObject();
            item.add(new ListSelectActionPanel(componentId, rowModel, PosteingangEditPage.class, posteingang.getId(),
                PosteingangListPage.this, DateTimeFormatter.instance().getFormattedDate(posteingang.getDatum())));
            cellItemListener.populateItem(item, componentId, rowModel);
            addRowClick(item);
          }
        });
    columns.add(new CellItemListenerPropertyColumn<PosteingangDO>(new Model<String>(getString("orga.posteingang.absender")),
        "absender", "absender", cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<PosteingangDO>(new Model<String>(getString("orga.posteingang.person")),
        "person", "person", cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<PosteingangDO>(new Model<String>(getString("orga.post.inhalt")), "inhalt", "inhalt",
        cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<PosteingangDO>(new Model<String>(getString("comment")), "bemerkung", "bemerkung",
        cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<PosteingangDO>(new Model<String>(getString("orga.post.type")), "type", "type",
        cellItemListener));
    dataTable = createDataTable(columns, "datum", false);
    form.add(dataTable);
  }

  @Override
  protected PosteingangListForm newListForm(AbstractListPage< ? , ? , ? > parentPage)
  {
    return new PosteingangListForm(this);
  }

  @Override
  protected PosteingangDao getBaseDao()
  {
    return posteingangDao;
  }

  @Override
  protected IModel<PosteingangDO> getModel(PosteingangDO object)
  {
    return new DetachableDOModel<PosteingangDO, PosteingangDao>(object, getBaseDao());
  }
}
