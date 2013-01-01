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

package org.projectforge.web.fibu;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.request.mapper.parameter.PageParameters;
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
import org.projectforge.web.wicket.AbstractStandardFormPage;

public class DatevImportPage extends AbstractStandardFormPage
{
  private static final long serialVersionUID = 3158445617725488919L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DatevImportPage.class);

  @SpringBean(name = "datevImportDao")
  private DatevImportDao datevImportDao;

  static final String KEY_IMPORT_STORAGE = "ImportStorage";

  private final DatevImportForm form;

  private final ActionLog actionLog = new ActionLog();

  public DatevImportPage(final PageParameters parameters)
  {
    super(parameters);
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
    form.setStorage(null);
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
        form.setStorage(datevImportDao.importKontenplan(is, clientFileName, actionLog));
        putUserPrefEntry(KEY_IMPORT_STORAGE, form.getStorage(), false);
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
        form.setStorage(datevImportDao.importBuchungsdaten(is, clientFileName, actionLog));
        putUserPrefEntry(KEY_IMPORT_STORAGE, form.getStorage(), false);
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
    if (form.getStorage() == null) {
      log.error("Reconcile called without storage.");
      return;
    }
    datevImportDao.reconcile(form.getStorage(), sheetName);
  }

  protected void commit(final String sheetName)
  {
    checkAccess();
    if (form.getStorage() == null) {
      log.error("Commit called without storage.");
      return;
    }
    datevImportDao.commit(form.getStorage(), sheetName);
  }

  protected void selectAll(final String sheetName)
  {
    checkAccess();
    final ImportedSheet< ? > sheet = form.getStorage().getNamedSheet(sheetName);
    Validate.notNull(sheet);
    sheet.selectAll(true, "modified".equals(form.filter.getListType()));
    // updateSelectedItems();
  }

  protected void select(final String sheetName, final int number)
  {
    checkAccess();
    final ImportedSheet< ? > sheet = form.getStorage().getNamedSheet(sheetName);
    Validate.notNull(sheet);
    sheet.select(true, "modified".equals(form.filter.getListType()), number);
    // updateSelectedItems();
  }

  protected void deselectAll(final String sheetName)
  {
    checkAccess();
    final ImportedSheet< ? > sheet = form.getStorage().getNamedSheet(sheetName);
    Validate.notNull(sheet);
    sheet.selectAll(false, false);
    // updateSelectedItems();
  }

  protected void showErrorSummary(final String sheetName)
  {
    final ImportedSheet< ? > sheet = form.getStorage().getNamedSheet(sheetName);
    Validate.notNull(sheet);
    form.setErrorProperties(sheet.getErrorProperties());
  }

  protected void showBusinessAssessment(final String sheetName)
  {
    final ImportedSheet< ? > sheet = form.getStorage().getNamedSheet(sheetName);
    Validate.notNull(sheet);
    final List<BuchungssatzDO> list = new ArrayList<BuchungssatzDO>();
    for (final ImportedElement< ? > element : sheet.getElements()) {
      final BuchungssatzDO satz = (BuchungssatzDO) element.getValue();
      list.add(satz);
    }
    final BusinessAssessment businessAssessment = new BusinessAssessment(AccountingConfig.getInstance().getBusinessAssessmentConfig(),
        (Integer) sheet.getProperty("year"), (Integer) sheet.getProperty("month"));
    form.setBusinessAssessment(businessAssessment);
    businessAssessment.setAccountRecords(list);
  }

  private void checkAccess()
  {
    accessChecker.checkLoggedInUserRight(UserRightId.FIBU_DATEV_IMPORT, UserRightValue.TRUE);
    accessChecker.checkRestrictedOrDemoUser();
  }

  @Override
  protected String getTitle()
  {
    return getString("fibu.datev.import");
  }
}
