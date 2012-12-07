/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2012 Kai Reinhard (k.reinhard@micromata.de)
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

package org.projectforge.plugins.teamcal.event;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.user.PFUserContext;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserDao;
import org.projectforge.web.WebConfiguration;
import org.projectforge.web.wicket.AbstractEditPage;
import org.projectforge.web.wicket.AbstractListPage;
import org.projectforge.web.wicket.CellItemListener;
import org.projectforge.web.wicket.CellItemListenerPropertyColumn;
import org.projectforge.web.wicket.DetachableDOModel;
import org.projectforge.web.wicket.IListPageColumnsCreator;
import org.projectforge.web.wicket.ListPage;
import org.projectforge.web.wicket.ListSelectActionPanel;
import org.projectforge.web.wicket.flowlayout.IconLinkPanel;
import org.projectforge.web.wicket.flowlayout.IconType;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
@ListPage(editPage = TeamEventEditPage.class)
public class TeamEventListPage extends AbstractListPage<TeamEventListForm, TeamEventDao, TeamEventDO> implements IListPageColumnsCreator<TeamEventDO>
{
  private static final long serialVersionUID = 1749480610890950450L;

  @SpringBean(name = "teamEventDao")
  private TeamEventDao teamEventDao;

  @SpringBean(name = "userDao")
  private UserDao userDao;

  private String iCalTarget;

  /**
   * 
   */
  public TeamEventListPage(final PageParameters parameters)
  {
    super(parameters, "plugins.teamcal");
  }

  /**
   * @see org.projectforge.web.wicket.IListPageColumnsCreator#createColumns(org.apache.wicket.markup.html.WebPage, boolean)
   */
  @SuppressWarnings("serial")
  @Override
  public List<IColumn<TeamEventDO>> createColumns(final WebPage returnToPage, final boolean sortable)
  {
    final List<IColumn<TeamEventDO>> columns = new ArrayList<IColumn<TeamEventDO>>();

    final CellItemListener<TeamEventDO> cellItemListener = new CellItemListener<TeamEventDO>() {
      public void populateItem(final Item<ICellPopulator<TeamEventDO>> item, final String componentId, final IModel<TeamEventDO> rowModel)
      {
        final TeamEventDO teamEvent = rowModel.getObject();
        final StringBuffer cssStyle = getCssStyle(teamEvent.getId(), teamEvent.isDeleted());
        if (cssStyle.length() > 0) {
          item.add(AttributeModifier.append("style", new Model<String>(cssStyle.toString())));
        }
      }
    };

    columns.add(new CellItemListenerPropertyColumn<TeamEventDO>(getString("plugins.teamcal.title"), getSortable("title", sortable), "title", cellItemListener) {
      /**
       * @see org.projectforge.web.wicket.CellItemListenerPropertyColumn#populateItem(org.apache.wicket.markup.repeater.Item, java.lang.String, org.apache.wicket.model.IModel)
       */
      @Override
      public void populateItem(final Item<ICellPopulator<TeamEventDO>> item, final String componentId, final IModel<TeamEventDO> rowModel)
      {
        final TeamEventDO teamEvent = rowModel.getObject();
        final StringBuffer cssStyle = getCssStyle(teamEvent.getId(), teamEvent.isDeleted());
        if (cssStyle.length() > 0) {
          item.add(AttributeModifier.append("style", new Model<String>(cssStyle.toString())));
        }
        item.add(new ListSelectActionPanel(componentId, rowModel, TeamEventEditPage.class, teamEvent.getId(), returnToPage, teamEvent.getSubject()));
        addRowClick(item);
      }
    });

    columns.add(new CellItemListenerPropertyColumn<TeamEventDO>(getString("plugins.teamcal.description"), getSortable("description", sortable),
        "description", cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<TeamEventDO>(getString("plugins.teamcal.owner"), getSortable("owner", sortable), "owner.username",
        cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<TeamEventDO>(getString("lastUpdate"), getSortable("lastUpdate", sortable), "lastUpdate",
        cellItemListener));
    // ics export buttons
    columns.add(new CellItemListenerPropertyColumn<TeamEventDO>(getString("plugins.teamcal.subscribe.column"), getSortable("", sortable), "pk",
        cellItemListener) {
      /**
       * @see org.projectforge.web.wicket.CellItemListenerPropertyColumn#populateItem(org.apache.wicket.markup.repeater.Item, java.lang.String, org.apache.wicket.model.IModel)
       */
      @Override
      public void populateItem(final Item<ICellPopulator<TeamEventDO>> item, final String componentId, final IModel<TeamEventDO> rowModel)
      {
        if (accessChecker.isRestrictedUser() == false && WebConfiguration.isDevelopmentMode() == true) {
          final TeamEventDO teamEvent = rowModel.getObject();
          createICalTarget(teamEvent.getId());
          final ExternalLink iCalExportLink = new ExternalLink(IconLinkPanel.LINK_ID, iCalTarget);
          final IconLinkPanel exportICalButtonPanel = new IconLinkPanel(componentId, IconType.SUBSCRIPTION,
              getString("plugins.teamcal.subscribe"), iCalExportLink).setLight();
          item.add(exportICalButtonPanel);
          final StringBuffer cssStyle = getCssStyle(teamEvent.getId(), teamEvent.isDeleted());
          if (cssStyle.length() > 0) {
            item.add(AttributeModifier.append("style", new Model<String>(cssStyle.toString())));
          }
        }
      }
    });
    return columns;
  }

  /**
   * creates an URL for ics export.
   * 
   * @param id
   */
  public void createICalTarget(final Integer id)
  {
    final PFUserDO user = PFUserContext.getUser();
    final String authenticationKey = userDao.getAuthenticationToken(user.getId());
    final String contextPath = WebApplication.get().getServletContext().getContextPath();
    iCalTarget = contextPath
        + "/export/ProjectForge.ics?timesheetUser="
        + user.getUsername()
        + "&token="
        + authenticationKey
        + "&teamEvents="
        + id
        + "&timesheetRequired="
        + false;
  }

  protected TeamEventFilter getFilter()
  {
    return form.getFilter();
  }

  /**
   * @see org.projectforge.web.wicket.AbstractListPage#getBaseDao()
   */
  @Override
  protected TeamEventDao getBaseDao()
  {
    return teamEventDao;
  }

  /**
   * @see org.projectforge.web.wicket.AbstractListPage#newListForm(org.projectforge.web.wicket.AbstractListPage)
   */
  @Override
  protected TeamEventListForm newListForm(final AbstractListPage< ? , ? , ? > parentPage)
  {
    return new TeamEventListForm(this);
  }

  /**
   * @see org.projectforge.web.wicket.AbstractListPage#getModel(java.lang.Object)
   */
  @Override
  protected IModel<TeamEventDO> getModel(final TeamEventDO object)
  {
    final DetachableDOModel<TeamEventDO, TeamEventDao> model = new DetachableDOModel<TeamEventDO, TeamEventDao>(object, getBaseDao());
    return model;
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
    setResponsePage(new TeamEventEditPage(getPageParameters()));
    return null;
  }
}
