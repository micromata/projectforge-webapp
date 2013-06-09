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

package org.projectforge.plugins.teamcal.event.importics;

import java.io.InputStream;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Calendar;

import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.access.AccessException;
import org.projectforge.common.ImportStorage;
import org.projectforge.plugins.teamcal.admin.TeamCalDao;
import org.projectforge.plugins.teamcal.event.TeamEventDO;
import org.projectforge.web.core.importstorage.AbstractImportPage;
import org.projectforge.web.wicket.WicketUtils;

public class TeamCalImportPage extends AbstractImportPage<TeamCalImportForm>
{
  private static final long serialVersionUID = 4717760936874814502L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TeamCalImportPage.class);

  public static String PARAM_KEY_TEAM_CAL_ID = "teamCalId";

  @SpringBean(name = "teamCalDao")
  private TeamCalDao teamCalDao;

  @SpringBean(name = "teamCalImportDao")
  private TeamCalImportDao teamCalImportDao;

  public TeamCalImportPage(final PageParameters parameters)
  {
    super(parameters);
    form = new TeamCalImportForm(this);
    body.add(form);
    final Integer calId = WicketUtils.getAsInteger(parameters, PARAM_KEY_TEAM_CAL_ID);
    if (calId != null) {
      form.calendar = teamCalDao.getById(calId);
    }
    form.init();
  }

  protected void importEvents()
  {
    checkAccess();
    final FileUpload fileUpload = form.fileUploadField.getFileUpload();
    if (fileUpload != null) {
      try {
        final InputStream is = fileUpload.getInputStream();
        actionLog.reset();
        final String clientFilename = fileUpload.getClientFileName();
        final CalendarBuilder builder = new CalendarBuilder();
        final Calendar calendar = builder.build(is);
        final ImportStorage<TeamEventDO> storage = teamCalImportDao.importEvents(calendar, clientFilename, actionLog);
        setStorage(storage);
      } catch (final Exception ex) {
        log.error(ex.getMessage(), ex);
        error("An error occurred (see log files for details): " + ex.getMessage());
        clear();
      } finally {
        fileUpload.closeStreams();
      }
    }
  }

  @Override
  protected void reconcile(final String sheetName)
  {
    checkAccess();
    super.reconcile(sheetName);
    teamCalImportDao.reconcile(getStorage(), sheetName, form.getCalendarId());
  }

  @Override
  protected void commit(final String sheetName)
  {
    checkAccess();
    super.commit(sheetName);
    teamCalImportDao.commit(getStorage(), sheetName, form.getCalendarId());
  }

  private void checkAccess()
  {
    accessChecker.checkRestrictedOrDemoUser();
    throw new AccessException("Todo");
    // final TeamCalRight right = new TeamCalRight();
    // if (isNew() == true || right.hasUpdateAccess(getUser(), data, data) == true) {
    // throw new AccessException("access.exception.userHasNotRight", rightId, StringHelper.listToString(", ", (Object[]) values));
    // }
    // if (form.isAccess() == true && getData().isExternalSubscription() == false) {
  }

  @Override
  protected String getTitle()
  {
    return getString("plugins.teamcal.import.ics.title");
  }
}
