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
import org.projectforge.orga.PostausgangDO;
import org.projectforge.orga.PostausgangDao;
import org.projectforge.web.calendar.DateTimeFormatter;
import org.projectforge.web.wicket.AbstractListPage;
import org.projectforge.web.wicket.CellItemListener;
import org.projectforge.web.wicket.CellItemListenerPropertyColumn;
import org.projectforge.web.wicket.DetachableDOModel;
import org.projectforge.web.wicket.ListPage;
import org.projectforge.web.wicket.ListSelectActionPanel;


@ListPage(editPage = PostausgangEditPage.class)
public class PostausgangListPage extends AbstractListPage<PostausgangListForm, PostausgangDao, PostausgangDO>
{
  private static final long serialVersionUID = 6121734373079865758L;

  @SpringBean(name = "postausgangDao")
  private PostausgangDao postausgangDao;

  public PostausgangListPage(PageParameters parameters)
  {
    super(parameters, "orga.postausgang");
  }

  @SuppressWarnings("serial")
  @Override
  protected void init()
  {
    List<IColumn<PostausgangDO>> columns = new ArrayList<IColumn<PostausgangDO>>();

    CellItemListener<PostausgangDO> cellItemListener = new CellItemListener<PostausgangDO>() {
      public void populateItem(Item<ICellPopulator<PostausgangDO>> item, String componentId, IModel<PostausgangDO> rowModel)
      {
        final PostausgangDO postausgang = rowModel.getObject();
        String cellStyle = "";
        if (postausgang.isDeleted() == true) {
          cellStyle = "text-decoration: line-through;";
        }
        item.add(new AttributeModifier("style", true, new Model<String>(cellStyle)));
      }
    };
    columns
        .add(new CellItemListenerPropertyColumn<PostausgangDO>(new Model<String>(getString("date")), "datum", "datum", cellItemListener) {
          @SuppressWarnings("unchecked")
          @Override
          public void populateItem(final Item item, final String componentId, final IModel rowModel)
          {
            PostausgangDO postausgang = (PostausgangDO) rowModel.getObject();
            item.add(new ListSelectActionPanel(componentId, rowModel, PostausgangEditPage.class, postausgang.getId(),
                PostausgangListPage.this, DateTimeFormatter.instance().getFormattedDate(postausgang.getDatum())));
            cellItemListener.populateItem(item, componentId, rowModel);
            addRowClick(item);
          }
        });
    columns.add(new CellItemListenerPropertyColumn<PostausgangDO>(new Model<String>(getString("orga.postausgang.empfaenger")),
        "empfaenger", "empfaenger", cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<PostausgangDO>(new Model<String>(getString("orga.postausgang.person")), "person",
        "person", cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<PostausgangDO>(new Model<String>(getString("orga.post.inhalt")), "inhalt", "inhalt",
        cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<PostausgangDO>(new Model<String>(getString("comment")), "bemerkung", "bemerkung",
        cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<PostausgangDO>(new Model<String>(getString("orga.post.type")), "type", "type",
        cellItemListener));
    dataTable = createDataTable(columns, "datum", false);
    form.add(dataTable);
  }

  @Override
  protected PostausgangListForm newListForm(AbstractListPage< ? , ? , ? > parentPage)
  {
    return new PostausgangListForm(this);
  }

  @Override
  protected PostausgangDao getBaseDao()
  {
    return postausgangDao;
  }

  @Override
  protected IModel<PostausgangDO> getModel(PostausgangDO object)
  {
    return new DetachableDOModel<PostausgangDO, PostausgangDao>(object, getBaseDao());
  }
}
