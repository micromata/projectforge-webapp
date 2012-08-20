/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.teamcal;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.web.fibu.ISelectCallerPage;
import org.projectforge.web.wicket.AbstractListPage;
import org.projectforge.web.wicket.CellItemListenerPropertyColumn;
import org.projectforge.web.wicket.DetachableDOModel;
import org.projectforge.web.wicket.IListPageColumnsCreator;

/**
 * @author Maximilian Lauterbach (m.lauterbach@micromata.de)
 * 
 */
public class TeamCalListPage extends AbstractListPage<TeamCalListForm, TeamCalDao, TeamCalDO> implements IListPageColumnsCreator<TeamCalDO>
{
  private static final long serialVersionUID = 1749480610890950450L;

  @SpringBean(name = "teamCalDao")
  private TeamCalDao teamCalDao;

  /**
   * 
   */
  public TeamCalListPage(final PageParameters parameters)
  {
    super(parameters, "plugins.teamcal");
  }

  TeamCalListPage(final String id, final ISelectCallerPage caller, final String selectProperty, final TeamCalListPage parentPage)
  {
    super(caller, id, selectProperty);
    // this.parentPage = parentPage;
  }

  /**
   * @see org.projectforge.web.wicket.IListPageColumnsCreator#createColumns(org.apache.wicket.markup.html.WebPage, boolean)
   */
  @Override
  public List<IColumn<TeamCalDO>> createColumns(final WebPage returnToPage, final boolean sortable)
  {
    final List<IColumn<TeamCalDO>> columns = new ArrayList<IColumn<TeamCalDO>>();
    columns.add(new CellItemListenerPropertyColumn<TeamCalDO>(new Model<String>(getString("created")), getSortable("created", sortable),
        "created") {
      private static final long serialVersionUID = 6752182079455440972L;

      @SuppressWarnings({ "unchecked", "rawtypes"})
      @Override
      public void populateItem(final Item item, final String componentId, final IModel rowModel)
      {
        final TeamCalDO calendar = (TeamCalDO) rowModel.getObject();
        item.add(new Label(componentId, calendar.getOwner().getFirstname()));
        addRowClick(item);
        cellItemListener.populateItem(item, componentId, rowModel);
      }
    });
    return columns;
  }

  /**
   * @see org.projectforge.web.wicket.AbstractListPage#getBaseDao()
   */
  @Override
  protected TeamCalDao getBaseDao()
  {
    return teamCalDao;
  }

  /**
   * @see org.projectforge.web.wicket.AbstractListPage#newListForm(org.projectforge.web.wicket.AbstractListPage)
   */
  @Override
  protected TeamCalListForm newListForm(final AbstractListPage< ? , ? , ? > parentPage)
  {
    return new TeamCalListForm(this);
  }

  /**
   * @see org.projectforge.web.wicket.AbstractListPage#getModel(java.lang.Object)
   */
  @Override
  protected IModel<TeamCalDO> getModel(final TeamCalDO object)
  {
    return new DetachableDOModel<TeamCalDO, TeamCalDao>(object, getBaseDao());
  }

  /**
   * @see org.projectforge.web.wicket.AbstractListPage#init()
   */
  @Override
  protected void init()
  {
    dataTable = createDataTable(createColumns(this, true), "lastUpdate", SortOrder.DESCENDING);
    form.add(dataTable);
  }

}
