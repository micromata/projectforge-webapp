/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2011 Kai Reinhard (k.reinhard@me.com)
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

package org.projectforge.web.admin;

import javax.sql.DataSource;

import org.apache.wicket.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.access.AccessChecker;
import org.projectforge.access.AccessException;
import org.projectforge.admin.SystemUpdater;
import org.projectforge.admin.UpdateEntry;
import org.projectforge.admin.UpdateEntryScript;
import org.projectforge.user.PFUserContext;
import org.projectforge.user.ProjectForgeGroup;
import org.projectforge.web.LoginPage;
import org.projectforge.web.wicket.AbstractSecuredPage;
import org.projectforge.web.wicket.DownloadUtils;
import org.projectforge.xml.stream.XmlHelper;
import org.projectforge.xml.stream.XmlObjectWriter;

public class SystemUpdatePage extends AbstractSecuredPage
{
  public static final String DOWNLOAD_BASE_URL = "http://www.projectforge.org/downloads/";

  public static final String UPDATE_URL = "https://www.projectforge.org/downloads/update-scripts.xml.gz";

  @SpringBean(name = "systemUpdater")
  protected SystemUpdater systemUpdater;

  @SpringBean(name = "dataSource")
  protected DataSource dataSource;

  private SystemUpdateForm form;

  public SystemUpdatePage(PageParameters parameters)
  {
    super(parameters);
    form = new SystemUpdateForm(this);
    body.add(form);
    form.init();
    refresh();
  }

  protected void downloadUpdateScript(final UpdateEntryScript updateScript)
  {
    final String filename = "update-script-" + updateScript.getVersion() + ".xml";
    final XmlObjectWriter writer = new XmlObjectWriter();
    final String script = writer.writeToXml(updateScript, true);
    final StringBuffer buf = new StringBuffer();
    buf.append(XmlHelper.XML_HEADER) //
        .append("\n<projectforge-self-update>") //
        .append(script) //
        .append("\n</projectforge-self-update>");
    DownloadUtils.setDownloadTarget(buf.toString().getBytes(), filename);
  }

  protected void update(final UpdateEntry updateEntry)
  {
    checkAdminUser();
    accessChecker.checkDemoUser();
    systemUpdater.update(updateEntry);
    refresh();
  }

  protected void refresh()
  {
    checkAdminUser();
    systemUpdater.runAllPreChecks();
    form.updateEntryRows();
  }
  
  private void checkAdminUser() {
    if (LoginPage.isAdminUser(PFUserContext.getUser(), dataSource) == false) {
      throw new AccessException(AccessChecker.I18N_KEY_VIOLATION_USER_NOT_MEMBER_OF, ProjectForgeGroup.ADMIN_GROUP.getKey());
    }
  }

  @Override
  protected String getTitle()
  {
    return getString("system.update.title");
  }
}
