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

package org.projectforge.plugins.marketing;

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
@ListPage(editPage = CampaignEditPage.class)
public class CampaignListPage extends AbstractListPage<CampaignListForm, CampaignDao, CampaignDO> implements IListPageColumnsCreator<CampaignDO>
{
  private static final long serialVersionUID = -4070838758263185222L;
  @SpringBean(name = "campaignDao")
  private CampaignDao campaignDao;

  public CampaignListPage(final PageParameters parameters)
  {
    super(parameters, "plugins.marketing.campaign");
  }

  @SuppressWarnings("serial")
  public List<IColumn<CampaignDO>> createColumns(final WebPage returnToPage, final boolean sortable)
  {
    final List<IColumn<CampaignDO>> columns = new ArrayList<IColumn<CampaignDO>>();
    final CellItemListener<CampaignDO> cellItemListener = new CellItemListener<CampaignDO>() {
      public void populateItem(final Item<ICellPopulator<CampaignDO>> item, final String componentId, final IModel<CampaignDO> rowModel)
      {
        final CampaignDO campaign = rowModel.getObject();
        final StringBuffer cssStyle = getCssStyle(campaign.getId(), campaign.isDeleted());
        if (cssStyle.length() > 0) {
          item.add(new AttributeModifier("style", true, new Model<String>(cssStyle.toString())));
        }
      }
    };

    columns.add(new CellItemListenerPropertyColumn<CampaignDO>(new Model<String>(getString("created")), getSortable("created", sortable),
        "created", cellItemListener) {
      @SuppressWarnings("unchecked")
      @Override
      public void populateItem(final Item item, final String componentId, final IModel rowModel)
      {
        final CampaignDO campaign = (CampaignDO) rowModel.getObject();
        item.add(new ListSelectActionPanel(componentId, rowModel, CampaignEditPage.class, campaign.getId(), returnToPage, DateTimeFormatter
            .instance().getFormattedDateTime(campaign.getCreated())));
        addRowClick(item);
        cellItemListener.populateItem(item, componentId, rowModel);
      }
    });
    columns.add(new CellItemListenerPropertyColumn<CampaignDO>(getString("modified"), getSortable("lastUpdate", sortable), "lastUpdate",
        cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<CampaignDO>(new Model<String>(getString("title")), getSortable("title",
        sortable), "title", cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<CampaignDO>(new Model<String>(getString("values")), getSortable("values",
        sortable), "values", cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<CampaignDO>(new Model<String>(getString("comment")), null, "comment", cellItemListener));
    return columns;
  }

  @Override
  protected void init()
  {
    dataTable = createDataTable(createColumns(this, true), "title", true);
    form.add(dataTable);
  }

  @Override
  protected CampaignListForm newListForm(final AbstractListPage< ? , ? , ? > parentPage)
  {
    return new CampaignListForm(this);
  }

  @Override
  protected CampaignDao getBaseDao()
  {
    return campaignDao;
  }

  @Override
  protected IModel<CampaignDO> getModel(final CampaignDO object)
  {
    return new DetachableDOModel<CampaignDO, CampaignDao>(object, getBaseDao());
  }

  protected CampaignDao getCampaignDao()
  {
    return campaignDao;
  }
}
