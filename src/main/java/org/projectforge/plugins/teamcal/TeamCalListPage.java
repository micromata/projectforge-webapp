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

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.web.fibu.ISelectCallerPage;
import org.projectforge.web.wicket.AbstractEditPage;
import org.projectforge.web.wicket.AbstractListPage;
import org.projectforge.web.wicket.CellItemListener;
import org.projectforge.web.wicket.CellItemListenerPropertyColumn;
import org.projectforge.web.wicket.DetachableDOModel;
import org.projectforge.web.wicket.IListPageColumnsCreator;
import org.projectforge.web.wicket.ListPage;
import org.projectforge.web.wicket.ListSelectActionPanel;

/**
 * @author Maximilian Lauterbach (m.lauterbach@micromata.de)
 * 
 */
@ListPage(editPage = TeamCalEditPage.class)
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
  }

  /**
   * @see org.projectforge.web.wicket.IListPageColumnsCreator#createColumns(org.apache.wicket.markup.html.WebPage, boolean)
   */
  // TODO replace names with i18n
  @SuppressWarnings("serial")
  @Override
  public List<IColumn<TeamCalDO>> createColumns(final WebPage returnToPage, final boolean sortable)
  {
    final List<IColumn<TeamCalDO>> columns = new ArrayList<IColumn<TeamCalDO>>();
    final CellItemListener<TeamCalDO> cellItemListener = new CellItemListener<TeamCalDO>() {
      public void populateItem(final Item<ICellPopulator<TeamCalDO>> item, final String componentId, final IModel<TeamCalDO> rowModel)
      {
        final TeamCalDO teamCalDO = rowModel.getObject();
        final StringBuffer cssStyle = getCssStyle(teamCalDO.getId(), teamCalDO.isDeleted());
        if (cssStyle.length() > 0) {
          item.add(AttributeModifier.append("style", new Model<String>(cssStyle.toString())));
        }
      }
    };

    columns.add(new CellItemListenerPropertyColumn<TeamCalDO>(getString("title"), getSortable("title", sortable), "title",
        cellItemListener)
        {
      @Override
      public void populateItem(final Item<ICellPopulator<TeamCalDO>> item, final String componentId, final IModel<TeamCalDO> rowModel)
      {
        final TeamCalDO teamcal = rowModel.getObject();
        final StringBuffer cssStyle = getCssStyle(teamcal.getId(), teamcal.isDeleted());
        if (cssStyle.length() > 0)
          item.add(AttributeModifier.append("style", new Model<String>(cssStyle.toString())));
        item.add(new ListSelectActionPanel(componentId, rowModel, TeamCalEditPage.class, teamcal.getId(), returnToPage, teamcal.getTitle()));
        addRowClick(item);
      }
        }
        );

    columns.add(new CellItemListenerPropertyColumn<TeamCalDO>(getString("description"), getSortable("description", sortable), "description",
        cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<TeamCalDO>(getString("user"), getSortable("owner", sortable),
        "owner.username", cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<TeamCalDO>(getString("lastUpdate"), getSortable("lastUpdate", sortable), "lastUpdate",
        cellItemListener));
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

    // Add additional menu buttons here!
    //    final BookmarkablePageLink<Void> addTemplatesLink = UserPrefListPage.createLink("link", TeamCalPlugin.USER_PREF_AREA);
    // TODO add options
    //    final ContentMenuEntryPanel menuEntry = new ContentMenuEntryPanel(getNewContentMenuChildId(), addTemplatesLink, "Abonnement");
    //    addContentMenuEntry(menuEntry);
  }

  /**
   * @see org.projectforge.web.wicket.AbstractListPage#redirectToEditPage(org.apache.wicket.request.mapper.parameter.PageParameters)
   */
  @Override
  protected AbstractEditPage< ? , ? , ? > redirectToEditPage(final PageParameters params)
  {
    setResponsePage(new TeamCalEditPage(getPageParameters()));
    return null;
  }
}
