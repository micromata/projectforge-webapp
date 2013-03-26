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

import java.sql.Timestamp;
import java.util.Date;

import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.common.DateHelper;
import org.projectforge.core.ModificationStatus;
import org.projectforge.plugins.teamcal.integration.TeamCalCalendarPage;
import org.projectforge.user.PFUserContext;
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

  /**
   * Only given if called by recurrence dialog.
   */
  private TeamEvent eventOfCaller;

  /**
   * Used for recurrence events in {@link #onSaveOrUpdate()} and {@link #afterSaveOrUpdate()}
   */
  private TeamEventDO newEvent;

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
  public TeamEventEditPage(final PageParameters parameters, final TeamEvent event, final Timestamp newStartDate,
      final Timestamp newEndDate, final RecurrencyChangeType recurrencyChangeType)
  {
    super(parameters, "plugins.teamcal.event");
    Validate.notNull(event);
    Validate.notNull(recurrencyChangeType);
    // event contains the new start and/or stop date if modified.
    if (log.isDebugEnabled() == true) {
      log.debug("TeamEvent is: newStartDate="
          + newStartDate
          + ", newEndDate="
          + newEndDate
          + ", event=["
          + event
          + "], recurrencyChangeType=["
          + recurrencyChangeType
          + "]");
    }
    this.eventOfCaller = event;
    this.recurrencyChangeType = recurrencyChangeType;
    Integer id;
    if (event instanceof TeamEventDO) {
      id = ((TeamEventDO) event).getId();
    } else {
      id = ((TeamRecurrenceEvent) event).getMaster().getId();
    }
    final TeamEventDO teamEventDO = teamEventDao.getById(id);
    if (recurrencyChangeType == RecurrencyChangeType.ALL) {
      // The user wants to edit all events, so check if the user changes start and/or end date. If so, move the date of the original event.
      if (newStartDate != null) {
        final long startDateMove = newStartDate.getTime() - event.getStartDate().getTime();
        teamEventDO.setStartDate(new Timestamp(teamEventDO.getStartDate().getTime() + startDateMove));
      }
      if (newEndDate != null) {
        final long endDateMove = newEndDate.getTime() - event.getEndDate().getTime();
        teamEventDO.setEndDate(new Timestamp(teamEventDO.getEndDate().getTime() + endDateMove));
      }
    } else {
      if (newStartDate != null) {
        teamEventDO.setStartDate(newStartDate);
      } else {
        teamEventDO.setStartDate(new Timestamp(event.getStartDate().getTime()));
      }
      if (newEndDate != null) {
        teamEventDO.setEndDate(newEndDate);
      } else {
        teamEventDO.setEndDate(new Timestamp(event.getEndDate().getTime()));
      }
    }
    if (recurrencyChangeType == RecurrencyChangeType.ONLY_CURRENT) {
      // The user wants to change only the current event, so remove all recurrency fields.
      teamEventDO.clearAllRecurrenceFields();
    }
    super.init(teamEventDO);
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
    if (returnToPage == null) {
      returnToPage = new TeamCalCalendarPage(new PageParameters());
    }
    super.setResponsePage();
    if (returnToPage instanceof CalendarPage) {
      // Display the date of this time sheet in the CalendarPage (useful if the time sheet was moved).
      if (newEvent != null) {
        ((CalendarPage) returnToPage).setStartDate(newEvent.getStartDate());
      } else if (eventOfCaller != null) {
        ((CalendarPage) returnToPage).setStartDate(eventOfCaller.getStartDate());
      } else {
        ((CalendarPage) returnToPage).setStartDate(getData().getStartDate());
      }
    }
  }

  /**
   * @see org.projectforge.web.wicket.AbstractEditPage#onDelete()
   */
  @Override
  public AbstractSecuredBasePage onDelete()
  {
    super.onDelete();
    if (recurrencyChangeType == null || recurrencyChangeType == RecurrencyChangeType.ALL) {
      return null;
    }
    final Integer masterId = getData().getId(); // Store the id of the master entry.
    final TeamEventDO masterEvent = teamEventDao.getById(masterId);
    if (recurrencyChangeType == RecurrencyChangeType.ALL_FUTURE) {
      final Date recurrenceUntil = new Date(eventOfCaller.getStartDate().getTime() - 24 * 3600 * 1000);
      form.recurrenceData.setUntil(recurrenceUntil); // Minus 24 hour.
      masterEvent.setRecurrence(form.recurrenceData);
      getBaseDao().update(masterEvent);
    } else if (recurrencyChangeType == RecurrencyChangeType.ONLY_CURRENT) { // only current date
      masterEvent.addRecurrenceExDate(eventOfCaller.getStartDate(), PFUserContext.getTimeZone());
      getBaseDao().update(masterEvent);
    }
    return (AbstractSecuredBasePage) getReturnToPage();
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
    final TeamEventDO oldDataObject = getData();
    final TeamEventDO masterEvent = teamEventDao.getById(masterId);
    form.setData(masterEvent);
    if (recurrencyChangeType == RecurrencyChangeType.ALL_FUTURE) {
      // Set the end date of the master date one day before current date and save this event.
      final Date recurrenceUntil = new Date(eventOfCaller.getStartDate().getTime() - 24 * 3600 * 1000);
      newEvent = oldDataObject;
      if (log.isDebugEnabled() == true) {
        log.debug("Recurrency until date of master entry will be set to: " + DateHelper.formatAsUTC(recurrenceUntil));
        log.debug("The new event is: " + newEvent);
      }
      form.recurrenceData.setUntil(recurrenceUntil); // Minus 24 hour.
      getData().setRecurrence(form.recurrenceData);
      return null;
    } else if (recurrencyChangeType == RecurrencyChangeType.ONLY_CURRENT) { // only current date
      // Add current date to the master date as exclusion date and save this event (without recurrency settings).
      masterEvent.addRecurrenceExDate(eventOfCaller.getStartDate(), PFUserContext.getTimeZone());
      newEvent = oldDataObject;
      newEvent.setRecurrenceDate(eventOfCaller.getStartDate());
      newEvent.setRecurrenceReferenceId(masterEvent.getId());
      if (log.isDebugEnabled() == true) {
        log.debug("Recurrency ex date of master entry is now added: "
            + DateHelper.formatAsUTC(eventOfCaller.getStartDate())
            + ". The new string is: "
            + masterEvent.getRecurrenceExDate());
        log.debug("The new event is: " + newEvent);
      }
      return null;
    }
    return null;
  }

  /**
   * @see org.projectforge.web.wicket.AbstractEditPage#afterUpdate(org.projectforge.core.ModificationStatus)
   */
  @Override
  public AbstractSecuredBasePage afterUpdate(final ModificationStatus modificationStatus)
  {
    if (newEvent != null) {
      newEvent.setExternalUid(null); // Avoid multiple usage of external uids.
      teamEventDao.save(newEvent);
    }
    return null;
  }

  /**
   * @see org.projectforge.web.wicket.AbstractEditPage#cloneData()
   */
  @Override
  protected void cloneData()
  {
    super.cloneData();
    getData().setExternalUid(null); // Avoid multiple usage of external uid.
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
   * @see org.projectforge.web.wicket.AbstractEditPage#newEditForm(org.projectforge.web.wicket.AbstractEditPage,
   *      org.projectforge.core.AbstractBaseDO)
   */
  @Override
  protected TeamEventEditForm newEditForm(final AbstractEditPage< ? , ? , ? > parentPage, final TeamEventDO data)
  {
    return new TeamEventEditForm(this, data);
  }

}
