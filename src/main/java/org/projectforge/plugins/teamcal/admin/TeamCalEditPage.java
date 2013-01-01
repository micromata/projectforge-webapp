/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2013 Kai Reinhard (k.reinhard@micromata.de)
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

package org.projectforge.plugins.teamcal.admin;

import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.plugins.teamcal.event.TeamEventListPage;
import org.projectforge.web.wicket.AbstractEditPage;
import org.projectforge.web.wicket.AbstractSecuredBasePage;
import org.projectforge.web.wicket.EditPage;
import org.projectforge.web.wicket.components.ContentMenuEntryPanel;

/**
 * @author M. Lauterbach (m.lauterbach@micromata.de)
 * 
 */
@EditPage(defaultReturnPage = TeamCalListPage.class)
public class TeamCalEditPage extends AbstractEditPage<TeamCalDO, TeamCalEditForm, TeamCalDao>
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TeamCalEditPage.class);

  private static final long serialVersionUID = -3352981782657771662L;

  @SpringBean(name = "teamCalDao")
  private TeamCalDao teamCalDao;

  /**
   * @param parameters
   * @param i18nPrefix
   */
  public TeamCalEditPage(final PageParameters parameters)
  {
    super(parameters, "plugins.teamcal");
    init();
    addTopMenuPanel();
  }

  @SuppressWarnings("serial")
  private void addTopMenuPanel()
  {
    if (isNew() == false) {
      final Integer id = form.getData().getId();
      final ContentMenuEntryPanel menu = new ContentMenuEntryPanel(getNewContentMenuChildId(), new Link<Void>(ContentMenuEntryPanel.LINK_ID) {
        @Override
        public void onClick()
        {
          final TeamEventListPage teamEventListPage = new TeamEventListPage(new PageParameters().add(TeamEventListPage.PARAM_CALENDARS, String.valueOf(id)));
          setResponsePage(teamEventListPage);
        };
      }, getString("plugins.teamcal.events"));
      addContentMenuEntry(menu);
    }
  }

  @Override
  public AbstractSecuredBasePage onSaveOrUpdate()
  {
    teamCalDao.setFullAccessUsers(getData(), form.fullAccessUsersListHelper.getAssignedItems());
    teamCalDao.setReadonlyAccessUsers(getData(), form.readonlyAccessUsersListHelper.getAssignedItems());
    teamCalDao.setMinimalAccessUsers(getData(), form.minimalAccessUsersListHelper.getAssignedItems());
    teamCalDao.setFullAccessGroups(getData(), form.fullAccessGroupsListHelper.getAssignedItems());
    teamCalDao.setReadonlyAccessGroups(getData(), form.readonlyAccessGroupsListHelper.getAssignedItems());
    teamCalDao.setMinimalAccessGroups(getData(), form.minimalAccessGroupsListHelper.getAssignedItems());
    return super.onSaveOrUpdate();
  }

  /**
   * @see org.projectforge.web.wicket.AbstractEditPage#getBaseDao()
   */
  @Override
  protected TeamCalDao getBaseDao()
  {
    return teamCalDao;
  }

  /**
   * @see org.projectforge.web.wicket.AbstractEditPage#getLogger()
   */
  @Override
  protected Logger getLogger()
  {
    return log;
  }

  /**
   * @see org.projectforge.web.wicket.AbstractEditPage#newEditForm(org.projectforge.web.wicket.AbstractEditPage,
   *      org.projectforge.core.AbstractBaseDO)
   */
  @Override
  protected TeamCalEditForm newEditForm(final AbstractEditPage< ? , ? , ? > parentPage, final TeamCalDO data)
  {
    return new TeamCalEditForm(this, data);
  }
}
