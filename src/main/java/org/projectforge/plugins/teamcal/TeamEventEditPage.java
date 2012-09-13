/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.teamcal;

import java.sql.Date;

import org.apache.log4j.Logger;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.web.wicket.AbstractEditPage;
import org.projectforge.web.wicket.WicketUtils;

/**
 * @author Maximilian Lauterbach (m.lauterbach@micromata.de)
 *
 */
public class TeamEventEditPage extends AbstractEditPage<TeamEventDO, TeamEventEditForm, TeamEventDao>
{
  private static final long serialVersionUID = 1221484611148024273L;

  /**
   * Key for preset the start date.
   */
  public static final String PARAMETER_KEY_START_DATE_IN_MILLIS = "startMillis";

  /**
   * Key for preset the stop date.
   */
  public static final String PARAMETER_KEY_END_DATE_IN_MILLIS = "endMillis";

  /**
   * Key for moving start date.
   */
  public static final String PARAMETER_KEY_NEW_START_DATE = "newStartDate";

  /**
   * Key for moving start date.
   */
  public static final String PARAMETER_KEY_NEW_END_DATE = "newEndDate";

  /**
   * Key for owner id.
   */
  public static final String PARAMETER_KEY_OWNER = "ownerId";

  /**
   * Key for note.
   */
  public static final String PARAMETER_KEY_NOTE = "note";

  public static final String PARAMETER_KEY_TEAMCALID = "teamCalId";

  @SpringBean(name = "teamEventDao")
  private TeamEventDao teamEventDao;

  @SpringBean(name = "teamCalDao")
  private TeamCalDao teamCalDao;

  /**
   * @param parameters
   * @param i18nPrefix
   */
  public TeamEventEditPage(final PageParameters parameters)
  {
    super(parameters, "TeamEventEdit");
    super.init();
  }

  @SuppressWarnings("null")
  void preInit()
  {
    //    if (isNew() == true) {
    final PageParameters parameters = getPageParameters();
    //    final Integer teamCalId = WicketUtils.getAsInteger(parameters, PARAMETER_KEY_TASK_ID);
    //    if (teamCalId != null) {
    //      getBaseDao().setTask(getData(), teamCalId);
    //    }
    final Long startDateInMillis = WicketUtils.getAsLong(parameters, PARAMETER_KEY_START_DATE_IN_MILLIS);
    final Long stopTimeInMillis = WicketUtils.getAsLong(parameters, PARAMETER_KEY_END_DATE_IN_MILLIS);
    if (startDateInMillis != null) {
      getData().setStartDate(new Date(startDateInMillis));
      if (stopTimeInMillis == null) {
        getData().setEndDate(new Date(stopTimeInMillis)); // Default is time sheet with zero duration.
      }
    }
    if (stopTimeInMillis != null) {
      getData().setEndDate(new Date(stopTimeInMillis));
      if (startDateInMillis == null) {
        getData().setStartDate(new Date(startDateInMillis)); // Default is time sheet with zero duration.
      }
    }
    final String note = WicketUtils.getAsString(parameters, PARAMETER_KEY_NOTE);
    if (note != null) {
      getData().setNote(note);
    }
    final String teamCalId = WicketUtils.getAsString(parameters, PARAMETER_KEY_TEAMCALID);
    if (teamCalId != null) {
      getData().setCalendar(teamCalDao.getById(teamCalId));
    }
    final int userId = WicketUtils.getAsInt(parameters, PARAMETER_KEY_OWNER, -1);
    if (userId != -1) {
      if (getData().getCalendar() == null) {
        getData().setCalendar(new TeamCalDO());
      }
      teamEventDao.setOwner(getData().getCalendar(), userId);
    }
  }

  /**
   * @see org.projectforge.web.wicket.AbstractEditPage#create()
   */
  @Override
  protected void create()
  {
    //    final TeamEventDO tdo = getData();
    //    getBaseDao().save(tdo);
    //    final TeamCalEditForm tCForm = new TeamCalEditForm(new TeamCalEditPage(new PageParameters()), getData().getCalendar());
    final TeamCalEditPage page = new TeamCalEditPage(new PageParameters());
    page.newEditForm(this, getData().getCalendar());
    setResponsePage(page);
  }

  /**
   * @see org.projectforge.web.wicket.AbstractEditPage#cancel()
   */
  @Override
  protected void cancel()
  {
    setResponsePage(TeamCalListPage.class);
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
    // TODO Auto-generated method stub
    return null;
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
