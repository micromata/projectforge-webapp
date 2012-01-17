/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2012 Kai Reinhard (k.reinhard@micromata.com)
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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.common.ImportedElement;
import org.projectforge.common.ImportedSheet;
import org.projectforge.core.ActionLog;
import org.projectforge.fibu.datev.DatevImportDao;
import org.projectforge.fibu.kost.AccountingConfig;
import org.projectforge.fibu.kost.BuchungssatzDO;
import org.projectforge.fibu.kost.BusinessAssessment;
import org.projectforge.user.UserRightId;
import org.projectforge.user.UserRightValue;
import org.projectforge.web.wicket.AbstractSecuredPage;

public class DatevImportPage extends AbstractSecuredPage
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DatevImportPage.class);

  @SpringBean(name = "datevImportDao")
  private DatevImportDao datevImportDao;

  static final String KEY_IMPORT_STORAGE = "ImportStorage";

  private final DatevImportForm form;

  private final ActionLog actionLog = new ActionLog();

  public DatevImportPage(final PageParameters parameters)
  {
    super(parameters);
    body.add(new FeedbackPanel("feedback").setOutputMarkupId(true));
    form = new DatevImportForm(this);
    body.add(form);
    form.init();
  }

  /**
   * Clears imported storages if exists.
   * @return
   */
  protected void clear()
  {
    checkAccess();
    log.info("clear called");
    form.storage = null;
    removeUserPrefEntry(KEY_IMPORT_STORAGE);
  }

  protected void importAccountList()
  {
    checkAccess();
    final FileUpload fileUpload = form.fileUploadField.getFileUpload();
    if (fileUpload != null) {
      try {
        final InputStream is = fileUpload.getInputStream();
        actionLog.reset();
        final String clientFileName = fileUpload.getClientFileName();
        form.storage = datevImportDao.importKontenplan(is, clientFileName, actionLog);
        putUserPrefEntry(KEY_IMPORT_STORAGE, form.storage, false);
      } catch (final Exception ex) {
        log.error(ex.getMessage(), ex);
        error("An error occurred (see log files for details): " + ex.getMessage());
        clear();
      }
    }
  }

  protected void importAccountRecords()
  {
    checkAccess();
    final FileUpload fileUpload = form.fileUploadField.getFileUpload();
    if (fileUpload != null) {
      try {
        final InputStream is = fileUpload.getInputStream();
        actionLog.reset();
        final String clientFileName = fileUpload.getClientFileName();
        form.storage = datevImportDao.importBuchungsdaten(is, clientFileName, actionLog);
        putUserPrefEntry(KEY_IMPORT_STORAGE, form.storage, false);
      } catch (final Exception ex) {
        log.error(ex.getMessage(), ex);
        error("An error occurred (see log files for details): " + ex.getMessage());
        clear();
      }
    }
  }

  protected void reconcile(final String sheetName)
  {
    checkAccess();
    if (form.storage == null) {
      log.error("Reconcile called without storage.");
      return;
    }
    datevImportDao.reconcile(form.storage, sheetName);
  }

  protected void commit(final String sheetName)
  {
    checkAccess();
    if (form.storage == null) {
      log.error("Commit called without storage.");
      return;
    }
    datevImportDao.commit(form.storage, sheetName);
  }

  protected void selectAll(final String sheetName)
  {
    checkAccess();
    final ImportedSheet< ? > sheet = form.storage.getNamedSheet(sheetName);
    Validate.notNull(sheet);
    sheet.selectAll(true, "modified".equals(form.filter.getListType()));
    // updateSelectedItems();
  }

  protected void select(final String sheetName, final int number)
  {
    checkAccess();
    final ImportedSheet< ? > sheet = form.storage.getNamedSheet(sheetName);
    Validate.notNull(sheet);
    sheet.select(true, "modified".equals(form.filter.getListType()), number);
    // updateSelectedItems();
  }

  protected void deselectAll(final String sheetName)
  {
    checkAccess();
    final ImportedSheet< ? > sheet = form.storage.getNamedSheet(sheetName);
    Validate.notNull(sheet);
    sheet.selectAll(false, false);
    // updateSelectedItems();
  }

  protected void showErrorSummary(final String sheetName)
  {
    final ImportedSheet< ? > sheet = form.storage.getNamedSheet(sheetName);
    Validate.notNull(sheet);
    form.errorProperties = sheet.getErrorProperties();
  }

  protected void showBusinessAssessment(final String sheetName)
  {
    final ImportedSheet< ? > sheet = form.storage.getNamedSheet(sheetName);
    Validate.notNull(sheet);
    final List<BuchungssatzDO> list = new ArrayList<BuchungssatzDO>();
    for (final ImportedElement< ? > element : sheet.getElements()) {
      final BuchungssatzDO satz = (BuchungssatzDO) element.getValue();
      list.add(satz);
    }
    form.businessAssessment = new BusinessAssessment(AccountingConfig.getInstance().getBusinessAssessmentConfig(), (Integer) sheet.getProperty("year"), (Integer) sheet.getProperty("month"));
    form.businessAssessment.setAccountRecords(list);
  }

  private void checkAccess()
  {
    accessChecker.checkLoggedInUserRight(UserRightId.FIBU_DATEV_IMPORT, UserRightValue.TRUE);
    accessChecker.checkDemoUser();
  }

  @Override
  protected String getTitle()
  {
    return getString("fibu.datev.import");
  }
}
