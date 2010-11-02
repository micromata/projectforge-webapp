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
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.fibu.KundeDO;
import org.projectforge.fibu.KundeDao;
import org.projectforge.fibu.KundeStatus;
import org.projectforge.fibu.ProjektDO;
import org.projectforge.web.wicket.AbstractListPage;
import org.projectforge.web.wicket.CellItemListener;
import org.projectforge.web.wicket.CellItemListenerPropertyColumn;
import org.projectforge.web.wicket.DetachableDOModel;
import org.projectforge.web.wicket.ListPage;
import org.projectforge.web.wicket.ListSelectActionPanel;


@ListPage(editPage = AuftragEditPage.class)
public class KundeListPage extends AbstractListPage<KundeListForm, KundeDao, KundeDO>
{
  private static final long serialVersionUID = -8406452960003792763L;

  @SpringBean(name = "kundeDao")
  private KundeDao kundeDao;

  public KundeListPage(PageParameters parameters)
  {
    super(parameters, "fibu.kunde");
  }

  public KundeListPage(final ISelectCallerPage caller, final String selectProperty)
  {
    super(caller, selectProperty, "fibu.kunde");
  }

  @SuppressWarnings("serial")
  @Override
  protected void init()
  {
    List<IColumn<KundeDO>> columns = new ArrayList<IColumn<KundeDO>>();

    CellItemListener<KundeDO> cellItemListener = new CellItemListener<KundeDO>() {
      public void populateItem(Item<ICellPopulator<KundeDO>> item, String componentId, IModel<KundeDO> rowModel)
      {
        final KundeDO kunde = rowModel.getObject();
        if (kunde.getStatus() == null) {
          // Should not occur:
          return;
        }
        String cellStyle = "";
        if (kunde.isDeleted() == true || kunde.getStatus().isIn(KundeStatus.ENDED) == true) {
          cellStyle = "text-decoration: line-through;";
        }
        item.add(new AttributeModifier("style", true, new Model<String>(cellStyle)));
      }
    };
    columns.add(new CellItemListenerPropertyColumn<KundeDO>(new Model<String>(getString("fibu.kunde.nummer")), "kost", "kost",
        cellItemListener) {
      @SuppressWarnings("unchecked")
      @Override
      public void populateItem(final Item item, final String componentId, final IModel rowModel)
      {
        KundeDO kunde = (KundeDO) rowModel.getObject();
        if (isSelectMode() == false) {
          throw new UnsupportedOperationException("Edit page not yet implemented");
        } else {
          item
              .add(new ListSelectActionPanel(componentId, rowModel, caller, selectProperty, kunde.getId(), String.valueOf(kunde.getKost())));
        }
        cellItemListener.populateItem(item, componentId, rowModel);
        addRowClick(item);
      }
    });
    columns.add(new CellItemListenerPropertyColumn<KundeDO>(new Model<String>(getString("fibu.kunde.identifier")), "identifier",
        "identifier", cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<KundeDO>(new Model<String>(getString("fibu.kunde.name")), "name", "name",
        cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<KundeDO>(new Model<String>(getString("fibu.kunde.division")), "division",
        "division", cellItemListener));
    columns
        .add(new CellItemListenerPropertyColumn<KundeDO>(new Model<String>(getString("status")), "status", "status", cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<KundeDO>(new Model<String>(getString("description")), "description", "description",
        cellItemListener));
    dataTable = createDataTable(columns, "kost", true);
    form.add(dataTable);
  }

  @Override
  protected KundeListForm newListForm(AbstractListPage< ? , ? , ? > parentPage)
  {
    return new KundeListForm(this);
  }

  @Override
  protected KundeDao getBaseDao()
  {
    return kundeDao;
  }

  @Override
  protected IModel<KundeDO> getModel(KundeDO object)
  {
    return new DetachableDOModel<KundeDO, KundeDao>(object, getBaseDao());
  }

  protected KundeDao getKundeDao()
  {
    return kundeDao;
  }
}
