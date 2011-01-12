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
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.projectforge.user.UserRightId;
import org.projectforge.user.UserRightValue;
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

  // public Resolution execute()
  // {
  // checkAccess();
  // onSubmit();
  // if (getStorage() != null && StringUtils.isNotEmpty(selectedValue) == true) {
  // if ("open".equals(eventKey) == true) {
  // storage.setSheetOpen(selectedValue, true);
  // } else if ("reconcile".equals(eventKey) == true) {
  // return reconcile(selectedValue);
  // } else if ("selectAll".equals(eventKey) == true) {
  // ImportedSheet< ? > sheet = (ImportedSheet< ? >) storage.getNamedSheet(selectedValue);
  // Validate.notNull(sheet);
  // sheet.selectAll(true, "modified".equals(listType));
  // updateSelectedItems();
  // } else if ("deselectAll".equals(eventKey) == true) {
  // ImportedSheet< ? > sheet = (ImportedSheet< ? >) storage.getNamedSheet(selectedValue);
  // Validate.notNull(sheet);
  // sheet.selectAll(false, false);
  // updateSelectedItems();
  // } else if ("deselectAll".equals(eventKey) == true) {
  // return commit(selectedValue);
  // } else if ("commit".equals(eventKey) == true) {
  // return commit(selectedValue);
  // } else if ("showErrorSummary".equals(eventKey) == true) {
  // ImportedSheet< ? > sheet = (ImportedSheet< ? >) storage.getNamedSheet(selectedValue);
  // Validate.notNull(sheet);
  // errorProperties = sheet.getErrorProperties();
  // } else if ("showBWA".equals(eventKey) == true) {
  // ImportedSheet< ? > sheet = (ImportedSheet< ? >) storage.getNamedSheet(selectedValue);
  // Validate.notNull(sheet);
  // List<BuchungssatzDO> list = new ArrayList<BuchungssatzDO>();
  // for (ImportedElement< ? > element : sheet.getElements()) {
  // BuchungssatzDO satz = (BuchungssatzDO) element.getValue();
  // list.add(satz);
  // }
  // this.bwa = new Bwa((Integer) sheet.getProperty("year"), (Integer) sheet.getProperty("month"));
  // this.bwa.setBuchungssaetze(list);
  // } else {
  // storage.setSheetOpen(selectedValue, false);
  // }
  // }
  // }
  //
  /**
   * Clears imported storages if exists.
   * @return
   */
  protected void clear()
  {
    checkAccess();
    log.info("clear called");
    // onSubmit();
    // getContext().removeEntry(KEY_IMPORT_STORAGE);
    // storage = null;
  }

  protected void importAccountList()
  {
    // final FileUpload fileUpload = form.fileUploadField.getFileUpload();
    // if (fileUpload != null) {
    // boolean delete = false;
    // try {
    // final InputStream is = fileUpload.getInputStream();
    // final String clientFileName = fileUpload.getClientFileName();
    // if (clientFileName.endsWith(".jrxml") == true) {
    // delete = true;
    // final JasperReport report = JasperCompileManager.compileReport(is);
    // if (report != null) {
    // getReportScriptingStorage().setJasperReport(report, clientFileName);
    // }
    // } else if (clientFileName.endsWith(".xls") == true) {
    // StringBuffer buf = new StringBuffer();
    // buf.append("report_").append(FileHelper.createSafeFilename(PFUserContext.getUser().getUsername(), 20)).append(".xls");
    // File file = new File(configuration.getWorkingDirectory(), buf.toString());
    // fileUpload.writeTo(file);
    // getReportScriptingStorage().setFilename(clientFileName, file.getAbsolutePath());
    // } else {
    // log.error("File extension not supported: " + clientFileName);
    // }
    // } catch (Exception ex) {
    // log.error(ex.getMessage(), ex);
    // error("An error occurred (see log files for details): " + ex.getMessage());
    // } finally {
    // if (delete == true) {
    // fileUpload.delete();
    // }
    // }
    // }

    // if (uploadFile == null) {
    // return getInputPage();
    // }
    // checkAccess();
    // onSubmit();
    // try {
    // final InputStream is = uploadFile.getInputStream();
    // actionLog.reset();
    // storage = datevImportDao.importKontenplan(is, uploadFile.getFileName(), actionLog);
    // } catch (Exception ex) {
    // addGlobalError("error", ex.getMessage());
    // log.error(ex.getMessage(), ex);
    // return clear();
    // } finally {
    // try {
    // uploadFile.delete();
    // } catch (IOException ex) {
    // log.error(ex.getMessage(), ex);
    // }
    // }
    // getContext().putEntry(KEY_IMPORT_STORAGE, storage, false);
  }

  protected void importAccountRecords()
  {
    // if (uploadFile == null) {
    // return getInputPage();
    // }
    // checkAccess();
    // onSubmit();
    // try {
    // final InputStream is = uploadFile.getInputStream();
    // actionLog.reset();
    // storage = datevImportDao.importBuchungsdaten(is, uploadFile.getFileName(), actionLog);
    // } catch (ExcelImportException ex) {
    // throw new UserException("common.import.excel.error", ex.getMessage(), ex.getRow(), ex.getColumnname());
    // } catch (Exception ex) {
    // addGlobalError("error", ex.getMessage());
    // log.error(ex.getMessage(), ex);
    // return clear();
    // } finally {
    // try {
    // uploadFile.delete();
    // } catch (IOException ex) {
    // log.error(ex.getMessage(), ex);
    // }
    // }
    // getContext().putEntry(KEY_IMPORT_STORAGE, storage, false);
  }

  private void reconcile(String sheetName)
  {
    checkAccess();
    // if (getStorage() == null) {
    // log.error("Reconcile called without storage.");
    // return getInputPage();
    // }
    // datevImportDao.reconcile(storage, sheetName);
  }

  private void commit(String sheetName)
  {
    checkAccess();
    // if (getStorage() == null) {
    // log.error("Commit called without storage.");
    // return getInputPage();
    // }
    // datevImportDao.commit(storage, sheetName);
  }

  private void checkAccess()
  {
    accessChecker.checkRight(UserRightId.FIBU_DATEV_IMPORT, UserRightValue.TRUE);
    accessChecker.checkDemoUser();
  }

  @Override
  protected String getTitle()
  {
    return getString("fibu.datev.import");
  }
}
