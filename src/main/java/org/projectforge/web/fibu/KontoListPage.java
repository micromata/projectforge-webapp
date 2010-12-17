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
import org.projectforge.fibu.KontoDO;
import org.projectforge.fibu.KontoDao;
import org.projectforge.web.wicket.AbstractListPage;
import org.projectforge.web.wicket.CellItemListener;
import org.projectforge.web.wicket.CellItemListenerPropertyColumn;
import org.projectforge.web.wicket.DetachableDOModel;
import org.projectforge.web.wicket.IListPageColumnsCreator;
import org.projectforge.web.wicket.ListPage;
import org.projectforge.web.wicket.ListSelectActionPanel;

@ListPage(editPage = KontoEditPage.class)
public class KontoListPage extends AbstractListPage<KontoListForm, KontoDao, KontoDO> implements IListPageColumnsCreator<KontoDO>
{
  private static final long serialVersionUID = -8406452960003792763L;

  @SpringBean(name = "kontoDao")
  private KontoDao kontoDao;

  public KontoListPage(final PageParameters parameters)
  {
    super(parameters, "fibu.konto");
  }

  public KontoListPage(final ISelectCallerPage caller, final String selectProperty)
  {
    super(caller, selectProperty, "fibu.konto");
  }

  @SuppressWarnings("serial")
  @Override
  public List<IColumn<KontoDO>> createColumns(final WebPage returnToPage)
  {
    final List<IColumn<KontoDO>> columns = new ArrayList<IColumn<KontoDO>>();
    CellItemListener<KontoDO> cellItemListener = new CellItemListener<KontoDO>() {
      public void populateItem(Item<ICellPopulator<KontoDO>> item, String componentId, IModel<KontoDO> rowModel)
      {
        final KontoDO konto = rowModel.getObject();
        String cellStyle = "";
        if (konto.isDeleted() == true) {
          cellStyle = "text-decoration: line-through;";
        }
        item.add(new AttributeModifier("style", true, new Model<String>(cellStyle)));
      }
    };
    columns.add(new CellItemListenerPropertyColumn<KontoDO>(new Model<String>(getString("fibu.konto.nummer")), "nummer", "nummer",
        cellItemListener) {
      @SuppressWarnings("unchecked")
      @Override
      public void populateItem(final Item item, final String componentId, final IModel rowModel)
      {
        KontoDO konto = (KontoDO) rowModel.getObject();
        if (isSelectMode() == false) {
          item.add(new ListSelectActionPanel(componentId, rowModel, KontoEditPage.class, konto.getId(), returnToPage, String.valueOf(konto
              .getNummer())));
        } else {
          item.add(new ListSelectActionPanel(componentId, rowModel, caller, selectProperty, konto.getId(), String
              .valueOf(konto.getNummer())));
        }
        cellItemListener.populateItem(item, componentId, rowModel);
        addRowClick(item);
      }
    });
    columns.add(new CellItemListenerPropertyColumn<KontoDO>(new Model<String>(getString("fibu.konto.bezeichnung")), "bezeichnung",
        "bezeichnung", cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<KontoDO>(new Model<String>(getString("description")), "description", "description",
        cellItemListener));
    return columns;
  }

  @Override
  protected void init()
  {
    dataTable = createDataTable(createColumns(this), "nummer", true);
    form.add(dataTable);
  }

  @Override
  protected KontoListForm newListForm(AbstractListPage< ? , ? , ? > parentPage)
  {
    return new KontoListForm(this);
  }

  @Override
  protected KontoDao getBaseDao()
  {
    return kontoDao;
  }

  @Override
  protected IModel<KontoDO> getModel(KontoDO object)
  {
    return new DetachableDOModel<KontoDO, KontoDao>(object, getBaseDao());
  }

  protected KontoDao getKontoDao()
  {
    return kontoDao;
  }
}
