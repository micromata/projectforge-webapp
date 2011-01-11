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

package org.projectforge.web.fibu;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.projectforge.user.ProjectForgeGroup;
import org.projectforge.web.wicket.AbstractSecuredPage;

public class DatevImportPage extends AbstractSecuredPage
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DatevImportPage.class);

  private DatevImportForm form;

  public DatevImportPage(final PageParameters parameters)
  {
    super(parameters);
    body.add(new FeedbackPanel("feedback").setOutputMarkupId(true));
    form = new DatevImportForm(this);
    body.add(form);
    form.init();
  }

  protected void upload()
  {
    accessChecker.checkIsUserMemberOfGroup(ProjectForgeGroup.FINANCE_GROUP, ProjectForgeGroup.CONTROLLING_GROUP);
    accessChecker.checkDemoUser();
    log.info("upload");
    final FileUpload fileUpload = form.fileUploadField.getFileUpload();
    if (fileUpload != null) {
//      boolean delete = false;
//      try {
//        final InputStream is = fileUpload.getInputStream();
//        final String clientFileName = fileUpload.getClientFileName();
//        if (clientFileName.endsWith(".jrxml") == true) {
//          delete = true;
//          final JasperReport report = JasperCompileManager.compileReport(is);
//          if (report != null) {
//            getReportScriptingStorage().setJasperReport(report, clientFileName);
//          }
//        } else if (clientFileName.endsWith(".xls") == true) {
//          StringBuffer buf = new StringBuffer();
//          buf.append("report_").append(FileHelper.createSafeFilename(PFUserContext.getUser().getUsername(), 20)).append(".xls");
//          File file = new File(configuration.getWorkingDirectory(), buf.toString());
//          fileUpload.writeTo(file);
//          getReportScriptingStorage().setFilename(clientFileName, file.getAbsolutePath());
//        } else {
//          log.error("File extension not supported: " + clientFileName);
//        }
//      } catch (Exception ex) {
//        log.error(ex.getMessage(), ex);
//        error("An error occurred (see log files for details): " + ex.getMessage());
//      } finally {
//        if (delete == true) {
//          fileUpload.delete();
//        }
//      }
    }
  }

  @Override
  protected String getTitle()
  {
    return getString("fibu.datev.import");
  }
}
