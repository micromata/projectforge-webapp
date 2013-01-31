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

package org.projectforge.plugins.teamcal.event;

import java.util.Date;

import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.common.DateHelper;
import org.projectforge.web.calendar.CalendarPage;
import org.projectforge.web.wicket.AbstractEditPage;
import org.projectforge.web.wicket.AbstractSecuredBasePage;

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

  private RecurrencyChangeType recurrencyChangeType;

  private TeamEvent originalEvent;

  /**
   * @param parameters
   */
  public TeamEventEditPage(final PageParameters parameters)
  {
    super(parameters, "plugins.teamcal.event");
    super.init();
  }

  /**
   * @param parameters
   */
  public TeamEventEditPage(final PageParameters parameters, final TeamEventDO event)
  {
    super(parameters, "plugins.teamcal.event");
    super.init(event);
  }

  /**
   * @param parameters
   */
  public TeamEventEditPage(final PageParameters parameters, final TeamEventDO event, final TeamEvent originalEvent, final RecurrencyChangeType recurrencyChangeType)
  {
    super(parameters, "plugins.teamcal.event");
    Validate.notNull(originalEvent);
    Validate.notNull(recurrencyChangeType);
    if (log.isDebugEnabled() == true) {
      log.debug("TeamEvent is: " + originalEvent);
    }
    this.originalEvent = originalEvent;
    this.recurrencyChangeType = recurrencyChangeType;
    super.init(event);
  }

  /**
   * @return the recurrencyChangeType
   */
  public RecurrencyChangeType getRecurrencyChangeType()
  {
    return recurrencyChangeType;
  }

  @Override
  public void setResponsePage()
  {
    super.setResponsePage();
    if (returnToPage instanceof CalendarPage) {
      // Display the date of this time sheet in the CalendarPage (useful if the time sheet was moved).
      ((CalendarPage) returnToPage).setStartDate(getData().getStartDate());
    }
  }

  /**
   * @see org.projectforge.web.wicket.AbstractEditPage#onSaveOrUpdate()
   */
  @Override
  public AbstractSecuredBasePage onSaveOrUpdate()
  {
    super.onSaveOrUpdate();
    getData().setRecurrence(form.recurrenceData);
    if (recurrencyChangeType == null || recurrencyChangeType == RecurrencyChangeType.ALL) {
      return null;
    }
    final Integer masterId = getData().getId(); // Store the id of the master entry.
    getData().setId(null); // Clone object.
    final TeamEventDO newEvent = getData();
    final TeamEventDO masterEvent = teamEventDao.getById(masterId);
    form.setData(masterEvent);
    if (recurrencyChangeType == RecurrencyChangeType.ALL_FUTURE) {
      // Set the end date of the master date one day before current date and save this event.
      final Date recurrenceUntil = new Date(originalEvent.getStartDate().getTime() - 3600 * 1000);
      if (log.isDebugEnabled() == true) {
        log.debug("Recurrency until date of master entry will be set to: " + DateHelper.formatAsUTC(recurrenceUntil));
        log.debug("The new event is: " + newEvent);
      }
      masterEvent.setRecurrenceUntil(recurrenceUntil); // Minus 1 hour.
      throw new UnsupportedOperationException("Not yet implemented");
    } else { // only current date
      // Add current date to the master date as exlusion date and save this event (without recurrency settings).
      masterEvent.addRecurrenceExDate(originalEvent.getStartDate());
      if (log.isDebugEnabled() == true) {
        log.debug("Recurrency ex date of master entry is now added: " + DateHelper.formatAsUTC(originalEvent.getStartDate()) + ". The new string is: " + masterEvent.getRecurrenceExDate());
        log.debug("The new event is: " + newEvent);
      }
      throw new UnsupportedOperationException("Not yet implemented");
    }
    //return null;
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
