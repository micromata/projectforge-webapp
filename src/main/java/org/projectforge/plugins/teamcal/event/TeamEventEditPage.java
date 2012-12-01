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

import org.apache.log4j.Logger;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.web.calendar.CalendarPage;
import org.projectforge.web.wicket.AbstractEditPage;

/**
 * @author M. Lauterbach (m.lauterbach@micromata.de)
 *
 */
public class TeamEventEditPage extends AbstractEditPage<TeamEventDO, TeamEventEditForm, TeamEventDao>
{
  private static final long serialVersionUID = 1221484611148024273L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TeamEventEditPage.class);

  @SpringBean(name = "teamEventDao")
  private TeamEventDao teamEventDao;

  /**
   * @param parameters
   */
  public TeamEventEditPage(final PageParameters parameters)
  {
    super(parameters, "plugins.teamevent");
    super.init();
  }

  /**
   * @param parameters
   */
  public TeamEventEditPage(final PageParameters parameters, final TeamEventDO event)
  {
    super(parameters, "plugins.teamevent");
    super.init(event);
  }

  /**
   * @see org.projectforge.web.wicket.AbstractEditPage#update()
   */
  @Override
  protected void update()
  {
    if (getData().getStartDate().after(getData().getEndDate()) || isZeroDuration() == false) {
      error(getString("plugins.teamevent.duration.error"));
    } else {
      super.update();
    }
  }

  /**
   * false, if there is no duration and all day is not selected.
   * 
   * @return
   */
  private boolean isZeroDuration() {
    final long startDate = getData().getStartDate().getTime();
    final long endDate = getData().getEndDate().getTime();
    if (startDate == endDate && getData().isAllDay() == false)
      return false;
    else
      return true;
  }

  @Override
  public void setResponsePage()
  {
    super.setResponsePage();
    if (returnToPage instanceof CalendarPage) {
      // Display the date of this time sheet in the CalendarPage (usefull if the time sheet was moved).
      ((CalendarPage) returnToPage).setStartDate(getData().getStartDate());
    }
  }

  /**
   * @see org.projectforge.web.wicket.AbstractEditPage#create()
   */
  @Override
  protected void create()
  {
    if (getData().getStartDate().after(getData().getEndDate()) || isZeroDuration() == false) {
      error(getString("plugins.teamevent.duration.error"));
    } else {
      super.create();
    }
  }


  /**
   * @see org.projectforge.web.wicket.AbstractEditPage#getBaseDao()
   */
  @Override
  protected TeamEventDao getBaseDao()
  {
    return teamEventDao;
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
   * @see org.projectforge.web.wicket.AbstractEditPage#newEditForm(org.projectforge.web.wicket.AbstractEditPage, org.projectforge.core.AbstractBaseDO)
   */
  @Override
  protected TeamEventEditForm newEditForm(final AbstractEditPage< ? , ? , ? > parentPage, final TeamEventDO data)
  {
    return new TeamEventEditForm(this, data);
  }

}
