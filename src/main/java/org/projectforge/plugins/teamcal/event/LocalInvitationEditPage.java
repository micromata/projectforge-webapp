/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2014 Kai Reinhard (k.reinhard@micromata.de)
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

import org.apache.log4j.Logger;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.plugins.teamcal.admin.TeamCalDO;
import org.projectforge.web.fibu.ISelectCallerPage;
import org.projectforge.web.wicket.AbstractEditPage;
import org.projectforge.web.wicket.AbstractSecuredBasePage;
import org.projectforge.web.wicket.EditPage;

/**
 * @author Werner Feder (w.feder.extern@micromata.de)
 *
 */
@EditPage(defaultReturnPage = LocalInvitationListPage.class)
public class LocalInvitationEditPage extends AbstractEditPage<LocalInvitationDO, LocalInvitationEditForm, LocalInvitationDao> implements ISelectCallerPage
{
  private static final long serialVersionUID = 6671410971521504L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LocalInvitationEditPage.class);

  @SpringBean(name = "localInvitationDao")
  private LocalInvitationDao localInvitationDao;

  @SpringBean(name = "teamEventDao")
  private TeamEventDao teamEventDao;

  public LocalInvitationEditPage(final PageParameters parameters)
  {
    super(parameters, "plugins.teamcal");
    init();
  }

  /**
   * @see org.projectforge.web.wicket.AbstractEditPage#onSaveOrUpdate()
   */
  @Override
  public AbstractSecuredBasePage onSaveOrUpdate()
  {
    final TeamCalDO teamCalDO = form.calDropDownChoice.getConvertedInput();
    final Integer teamEventId = getData().getTeamEvent().getId();
    final TeamEventDO teamEventDO = teamEventDao.getById(teamEventId);
    teamEventDO.setCalendar(teamCalDO);
    teamEventDao.saveOrUpdate(teamEventDO);
    localInvitationDao.markAsDeleted(getData());
    return super.onSaveOrUpdate();
  }

  /**
   * @see org.projectforge.web.fibu.ISelectCallerPage#select(java.lang.String, java.lang.Integer)
   */
  public void select(final String property, final Object selectedValue)
  {
    // Do nothing.
  }

  /**
   * @see org.projectforge.web.fibu.ISelectCallerPage#unselect(java.lang.String)
   */
  public void unselect(final String property)
  {
    // Do nothing.
  }

  /**
   * @see org.projectforge.web.fibu.ISelectCallerPage#cancelSelection(java.lang.String)
   */
  public void cancelSelection(final String property)
  {
    // Do nothing.
  }

  protected void updateAndClose()
  {
    update();
  }

  @Override
  protected LocalInvitationDao getBaseDao()
  {
    return localInvitationDao;
  }

  @Override
  protected LocalInvitationEditForm newEditForm(final AbstractEditPage< ? , ? , ? > parentPage, final LocalInvitationDO data)
  {
    return new LocalInvitationEditForm(this, data);
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }
}
