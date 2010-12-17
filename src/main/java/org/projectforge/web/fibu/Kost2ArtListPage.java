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
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.common.StringHelper;
import org.projectforge.fibu.kost.Kost2ArtDO;
import org.projectforge.fibu.kost.Kost2ArtDao;
import org.projectforge.web.wicket.AbstractListPage;
import org.projectforge.web.wicket.CellItemListener;
import org.projectforge.web.wicket.CellItemListenerPropertyColumn;
import org.projectforge.web.wicket.DetachableDOModel;
import org.projectforge.web.wicket.IListPageColumnsCreator;
import org.projectforge.web.wicket.ListPage;
import org.projectforge.web.wicket.ListSelectActionPanel;
import org.projectforge.web.wicket.WebConstants;
import org.projectforge.web.wicket.components.SingleImagePanel;

@ListPage(editPage = Kost2ArtEditPage.class)
public class Kost2ArtListPage extends AbstractListPage<Kost2ArtListForm, Kost2ArtDao, Kost2ArtDO> implements
    IListPageColumnsCreator<Kost2ArtDO>
{
  private static final long serialVersionUID = -202443770217040251L;

  @SpringBean(name = "kost2ArtDao")
  private Kost2ArtDao kost2ArtDao;

  public Kost2ArtListPage(final PageParameters parameters)
  {
    super(parameters, "fibu.kost2art");
  }

  @SuppressWarnings("serial")
  @Override
  public List<IColumn<Kost2ArtDO>> createColumns(final WebPage returnToPage)
  {
    final List<IColumn<Kost2ArtDO>> columns = new ArrayList<IColumn<Kost2ArtDO>>();
    CellItemListener<Kost2ArtDO> cellItemListener = new CellItemListener<Kost2ArtDO>() {
      public void populateItem(Item<ICellPopulator<Kost2ArtDO>> item, String componentId, IModel<Kost2ArtDO> rowModel)
      {
        final Kost2ArtDO kost2Art = rowModel.getObject();
        final StringBuffer cssStyle = getCssStyle(kost2Art.getId(), kost2Art.isDeleted());
        if (cssStyle.length() > 0) {
          item.add(new AttributeModifier("style", true, new Model<String>(cssStyle.toString())));
        }
      }
    };
    columns.add(new CellItemListenerPropertyColumn<Kost2ArtDO>(new Model<String>(getString("fibu.kost2art.nummer")), "id", "id",
        cellItemListener) {
      @SuppressWarnings("unchecked")
      @Override
      public void populateItem(final Item item, final String componentId, final IModel rowModel)
      {
        final Kost2ArtDO kost2Art = (Kost2ArtDO) rowModel.getObject();
        item.add(new ListSelectActionPanel(componentId, rowModel, Kost2ArtEditPage.class, kost2Art.getId(), returnToPage, StringHelper
            .format2DigitNumber(kost2Art.getId())));
        cellItemListener.populateItem(item, componentId, rowModel);
        addRowClick(item);
      }
    });
    columns.add(new CellItemListenerPropertyColumn<Kost2ArtDO>(new Model<String>(getString("name")), "name", "name", cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<Kost2ArtDO>(new Model<String>(getString("fibu.fakturiert")), "fakturiert", "fakturiert",
        cellItemListener) {
      @Override
      public void populateItem(Item<ICellPopulator<Kost2ArtDO>> item, String componentId, IModel<Kost2ArtDO> rowModel)
      {
        final Kost2ArtDO kost2Art = (Kost2ArtDO) rowModel.getObject();
        if (kost2Art.isFakturiert() == true) {
          item.add(SingleImagePanel.createPresizedImage(componentId, WebConstants.IMAGE_ACCEPT));
        } else {
          item.add(createInvisibleDummyComponent(componentId));
        }
        cellItemListener.populateItem(item, componentId, rowModel);
      }
    });
    columns.add(new CellItemListenerPropertyColumn<Kost2ArtDO>(new Model<String>(getString("fibu.kost2art.workFraction")), "workFraction",
        "workFraction", cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<Kost2ArtDO>(new Model<String>(getString("fibu.kost2art.projektStandard")),
        "projektStandard", "projektStandard", cellItemListener) {
      @Override
      public void populateItem(Item<ICellPopulator<Kost2ArtDO>> item, String componentId, IModel<Kost2ArtDO> rowModel)
      {
        final Kost2ArtDO kost2Art = (Kost2ArtDO) rowModel.getObject();
        if (kost2Art.isProjektStandard() == true) {
          item.add(SingleImagePanel.createPresizedImage(componentId, WebConstants.IMAGE_ACCEPT));
        } else {
          item.add(createInvisibleDummyComponent(componentId));
        }
        cellItemListener.populateItem(item, componentId, rowModel);
      }
    });
    columns.add(new CellItemListenerPropertyColumn<Kost2ArtDO>(new Model<String>(getString("description")), "description", "description",
        cellItemListener));
    return columns;
  }

  @Override
  protected void init()
  {
    dataTable = createDataTable(createColumns(this), "id", true);
    form.add(dataTable);
  }

  @Override
  protected Kost2ArtListForm newListForm(AbstractListPage< ? , ? , ? > parentPage)
  {
    return new Kost2ArtListForm(this);
  }

  @Override
  protected Kost2ArtDao getBaseDao()
  {
    return kost2ArtDao;
  }

  @Override
  protected IModel<Kost2ArtDO> getModel(Kost2ArtDO object)
  {
    return new DetachableDOModel<Kost2ArtDO, Kost2ArtDao>(object, getBaseDao());
  }

  protected Kost2ArtDao getKost2ArtDao()
  {
    return kost2ArtDao;
  }
}
