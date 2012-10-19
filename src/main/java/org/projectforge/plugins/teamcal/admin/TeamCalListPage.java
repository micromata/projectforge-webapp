/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.teamcal.admin;

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
import org.projectforge.user.UserGroupCache;
import org.projectforge.web.wicket.AbstractEditPage;
import org.projectforge.web.wicket.AbstractListPage;
import org.projectforge.web.wicket.CellItemListener;
import org.projectforge.web.wicket.CellItemListenerPropertyColumn;
import org.projectforge.web.wicket.DetachableDOModel;
import org.projectforge.web.wicket.IListPageColumnsCreator;
import org.projectforge.web.wicket.ListPage;
import org.projectforge.web.wicket.ListSelectActionPanel;

/**
 * @author M. Lauterbach (m.lauterbach@micromata.de)
 * 
 */
@ListPage(editPage = TeamCalEditPage.class)
public class TeamCalListPage extends AbstractListPage<TeamCalListForm, TeamCalDao, TeamCalDO> implements IListPageColumnsCreator<TeamCalDO>
{
  private static final long serialVersionUID = 1749480610890950450L;

  @SpringBean(name = "teamCalDao")
  private TeamCalDao teamCalDao;

  @SpringBean(name = "userGroupCache")
  private UserGroupCache userGroupCache;

  private final TeamCalRight right;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TeamCalListPage.class);

  /**
   * 
   */
  public TeamCalListPage(final PageParameters parameters)
  {
    super(parameters, "plugins.teamcal");
    right = new TeamCalRight();
  }

  /**
   * @see org.projectforge.web.wicket.IListPageColumnsCreator#createColumns(org.apache.wicket.markup.html.WebPage, boolean)
   */
  @SuppressWarnings("serial")
  @Override
  public List<IColumn<TeamCalDO>> createColumns(final WebPage returnToPage, final boolean sortable)
  {
    final List<IColumn<TeamCalDO>> columns = new ArrayList<IColumn<TeamCalDO>>();

    final CellItemListener<TeamCalDO> cellItemListener = new CellItemListener<TeamCalDO>() {
      public void populateItem(final Item<ICellPopulator<TeamCalDO>> item, final String componentId, final IModel<TeamCalDO> rowModel)
      {
        final TeamCalDO teamCal = rowModel.getObject();
        final StringBuffer cssStyle = getCssStyle(teamCal.getId(), teamCal.isDeleted());
        if (cssStyle.length() > 0) {
          item.add(AttributeModifier.append("style", new Model<String>(cssStyle.toString())));
        }
      }
    };

    columns.add(new CellItemListenerPropertyColumn<TeamCalDO>(getString("plugins.teamcal.title"), getSortable("title", sortable), "title", cellItemListener) {
      /**
       * @see org.projectforge.web.wicket.CellItemListenerPropertyColumn#populateItem(org.apache.wicket.markup.repeater.Item, java.lang.String, org.apache.wicket.model.IModel)
       */
      @Override
      public void populateItem(final Item<ICellPopulator<TeamCalDO>> item, final String componentId, final IModel<TeamCalDO> rowModel)
      {
        final TeamCalDO teamCal = rowModel.getObject();
        final StringBuffer cssStyle = getCssStyle(teamCal.getId(), teamCal.isDeleted());
        if (cssStyle.length() > 0) {
          item.add(AttributeModifier.append("style", new Model<String>(cssStyle.toString())));
        }
        item.add(new ListSelectActionPanel(componentId, rowModel, TeamCalEditPage.class, teamCal.getId(), returnToPage, teamCal.getTitle()));
        addRowClick(item);
      }
    });

    columns.add(new CellItemListenerPropertyColumn<TeamCalDO>(getString("plugins.teamcal.description"), getSortable("description", sortable),
        "description", cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<TeamCalDO>(getString("plugins.teamcal.owner"), getSortable("owner", sortable), "owner.username",
        cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<TeamCalDO>(getString("lastUpdate"), getSortable("lastUpdate", sortable), "lastUpdate",
        cellItemListener));
    return columns;
  }

  protected TeamCalFilter getFilter()
  {
    return form.getFilter();
  }

  @Override
  public void select(final String property, final Object selectedValue)
  {
    if ("ownerId".equals(property) == true) {
      final Integer id = (Integer) selectedValue;
      getFilter().setOwnerId(id);
      refresh();
    } else {
      super.select(property, selectedValue);
      log.error("Property '" + property + "' not supported for selection.");
    }
  }

  @Override
  public void unselect(final String property)
  {
    if ("ownerId".equals(property) == true) {
      getFilter().setOwnerId(null);
      refresh();
    } else {
      log.error("Property '" + property + "' not supported for selection.");
    }
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
    final DetachableDOModel<TeamCalDO, TeamCalDao> det = new DetachableDOModel<TeamCalDO, TeamCalDao>(object, getBaseDao());
    TeamCalDO teamcal = det.getObject();
    if (right.isOwner(getUser(), object)
        ||right.hasAccessGroup(teamcal.getFullAccessGroup(), userGroupCache, getUser()) == true
        || right.hasAccessGroup(teamcal.getReadOnlyAccessGroup(), userGroupCache, getUser()) == true)
      return det;
    if (right.hasAccessGroup(teamcal.getMinimalAccessGroup(), userGroupCache, getUser()) == true) {
      teamcal = new TeamCalDO();
      teamcal.setId(object.getId());
      teamcal.setMinimalAccessGroup(object.getMinimalAccessGroup());
      teamcal.setOwner(object.getOwner());
      det.setObject(teamcal);
    }
    return det;
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
