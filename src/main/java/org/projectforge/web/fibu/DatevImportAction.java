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



public class DatevImportAction// extends BaseActionBean
{
//  private static final String KEY_IMPORT_STORAGE = "ImportStorage";
//
//  private static final Logger log = Logger.getLogger(DatevImportAction.class);
//
//  private static final String JSP_URL = "/WEB-INF/jsp/fibu/datevImport.jsp";
//
//  private DatevImportDao datevImportDao;
//
//  private FileBean uploadFile;
//
//  private ImportStorage< ? > storage;
//
//  private String eventKey;
//
//  private String listType = "modified";
//
//  private boolean selectedItems[];
//
//  private Map<String, Set<Object>> errorProperties;
//
//  private Bwa bwa;
//  
//  private ActionLog actionLog = new ActionLog();
//
//  public FileBean getUploadFile()
//  {
//    return uploadFile;
//  }
//
//  public void setUploadFile(FileBean uploadFile)
//  {
//    this.uploadFile = uploadFile;
//  }
//
//  private void onSubmit()
//  {
//    populateSelectedItems();
//  }
//
//  @DefaultHandler
//  @DontValidate
//  public Resolution execute()
//  {
//    checkAccess();
//    onSubmit();
//    if (getStorage() != null && StringUtils.isNotEmpty(selectedValue) == true) {
//      if ("open".equals(eventKey) == true) {
//        storage.setSheetOpen(selectedValue, true);
//      } else if ("reconcile".equals(eventKey) == true) {
//        return reconcile(selectedValue);
//      } else if ("selectAll".equals(eventKey) == true) {
//        ImportedSheet< ? > sheet = (ImportedSheet< ? >) storage.getNamedSheet(selectedValue);
//        Validate.notNull(sheet);
//        sheet.selectAll(true, "modified".equals(listType));
//        updateSelectedItems();
//      } else if ("deselectAll".equals(eventKey) == true) {
//        ImportedSheet< ? > sheet = (ImportedSheet< ? >) storage.getNamedSheet(selectedValue);
//        Validate.notNull(sheet);
//        sheet.selectAll(false, false);
//        updateSelectedItems();
//      } else if ("deselectAll".equals(eventKey) == true) {
//        return commit(selectedValue);
//      } else if ("commit".equals(eventKey) == true) {
//        return commit(selectedValue);
//      } else if ("showErrorSummary".equals(eventKey) == true) {
//        ImportedSheet< ? > sheet = (ImportedSheet< ? >) storage.getNamedSheet(selectedValue);
//        Validate.notNull(sheet);
//        errorProperties = sheet.getErrorProperties();
//      } else if ("showBWA".equals(eventKey) == true) {
//        ImportedSheet< ? > sheet = (ImportedSheet< ? >) storage.getNamedSheet(selectedValue);
//        Validate.notNull(sheet);
//        List<BuchungssatzDO> list = new ArrayList<BuchungssatzDO>();
//        for (ImportedElement< ? > element : sheet.getElements()) {
//          BuchungssatzDO satz = (BuchungssatzDO) element.getValue();
//          list.add(satz);
//        }
//        this.bwa = new Bwa((Integer) sheet.getProperty("year"), (Integer) sheet.getProperty("month"));
//        this.bwa.setBuchungssaetze(list);
//      } else {
//        storage.setSheetOpen(selectedValue, false);
//      }
//    }
//    return getInputPage();
//  }
//
//  /**
//   * Clears imported storages if exists.
//   * @return
//   */
//  @DontValidate
//  public Resolution clear()
//  {
//    checkAccess();
//    log.info("clear called");
//    onSubmit();
//    getContext().removeEntry(KEY_IMPORT_STORAGE);
//    storage = null;
//    return getInputPage();
//  }
//
//  @DontValidate
//  public Resolution importKontenplan()
//  {
//    if (uploadFile == null) {
//      return getInputPage();
//    }
//    checkAccess();
//    onSubmit();
//    try {
//      final InputStream is = uploadFile.getInputStream();
//      actionLog.reset();
//      storage = datevImportDao.importKontenplan(is, uploadFile.getFileName(), actionLog);
//    } catch (Exception ex) {
//      addGlobalError("error", ex.getMessage());
//      log.error(ex.getMessage(), ex);
//      return clear();
//    } finally {
//      try {
//        uploadFile.delete();
//      } catch (IOException ex) {
//        log.error(ex.getMessage(), ex);
//      }
//    }
//    getContext().putEntry(KEY_IMPORT_STORAGE, storage, false);
//    return getInputPage();
//  }
//
//  @DontValidate
//  public Resolution importBuchungsdaten()
//  {
//    if (uploadFile == null) {
//      return getInputPage();
//    }
//    checkAccess();
//    onSubmit();
//    try {
//      final InputStream is = uploadFile.getInputStream();
//      actionLog.reset();
//      storage = datevImportDao.importBuchungsdaten(is, uploadFile.getFileName(), actionLog);
//    } catch (ExcelImportException ex) {
//      throw new UserException("common.import.excel.error", ex.getMessage(), ex.getRow(), ex.getColumnname());
//    } catch (Exception ex) {
//      addGlobalError("error", ex.getMessage());
//      log.error(ex.getMessage(), ex);
//      return clear();
//    } finally {
//      try {
//        uploadFile.delete();
//      } catch (IOException ex) {
//        log.error(ex.getMessage(), ex);
//      }
//    }
//    getContext().putEntry(KEY_IMPORT_STORAGE, storage, false);
//    return getInputPage();
//  }
//
//  private Resolution reconcile(String sheetName)
//  {
//    checkAccess();
//    if (getStorage() == null) {
//      log.error("Reconcile called without storage.");
//      return getInputPage();
//    }
//    datevImportDao.reconcile(storage, sheetName);
//    return getInputPage();
//  }
//
//  private Resolution commit(String sheetName)
//  {
//    checkAccess();
//    if (getStorage() == null) {
//      log.error("Commit called without storage.");
//      return getInputPage();
//    }
//    datevImportDao.commit(storage, sheetName);
//    return getInputPage();
//  }
//
//  /**
//   * Writes the form check box values to the storage ((un)selects the imported elements of the sheets).
//   */
//  private void populateSelectedItems()
//  {
//    if (getStorage() == null || storage.getSheets() == null) {
//      return;
//    }
//    getSelectedItems();
//    for (ImportedSheet< ? > sheet : storage.getSheets()) {
//      if (sheet.getElements() == null) {
//        continue;
//      }
//      for (ImportedElement< ? > el : sheet.getElements()) {
//        el.setSelected(selectedItems[el.getIndex()]);
//      }
//    }
//  }
//
//  /**
//   * Updates the selected items (for check boxes) from the values of the imported elements of the sheets.
//   */
//  private void updateSelectedItems()
//  {
//    if (this.selectedItems == null) {
//      this.selectedItems = new boolean[storage.getLastVal()];
//    }
//    if (storage.getSheets() == null) {
//      return;
//    }
//    for (ImportedSheet< ? > sheet : storage.getSheets()) {
//      if (sheet.getElements() == null) {
//        continue;
//      }
//      for (ImportedElement< ? > el : sheet.getElements()) {
//        selectedItems[el.getIndex()] = el.isSelected();
//      }
//    }
//  }
//
//  /**
//   * @return imported data if exists, otherwise null.
//   */
//  @SuppressWarnings("unchecked")
//  public ImportStorage< ? > getStorage()
//  {
//    if (storage == null) {
//      storage = (ImportStorage<KontoDO>) getContext().getEntry(KEY_IMPORT_STORAGE);
//    }
//    return storage;
//  }
//
//  public boolean isKontenplanStorage()
//  {
//    return getStorage() != null && storage.getId() == DatevImportDao.Type.KONTENPLAN;
//  }
//
//  /**
//   * open or close for sheets.
//   */
//  public String getEventKey()
//  {
//    return eventKey;
//  }
//
//  public void setEventKey(String eventKey)
//  {
//    this.eventKey = eventKey;
//  }
//
//  /**
//   * Filter: Show all or only modified.
//   */
//  public String getListType()
//  {
//    return listType;
//  }
//
//  /**
//   * @return KONTENPLAN, BUCHUNGSSAETZE OR null (for empty storage).
//   */
//  public DatevImportDao.Type getStorageType()
//  {
//    if (getStorage() == null) {
//      return null;
//    } else {
//      return (DatevImportDao.Type) storage.getId();
//    }
//  }
//
//  public void setListType(String listType)
//  {
//    this.listType = listType;
//  }
//
//  public void setDatevImportDao(DatevImportDao datevImportDao)
//  {
//    this.datevImportDao = datevImportDao;
//  }
//
//  private Resolution getInputPage()
//  {
//    this.eventKey = null;
//    this.selectedValue = null;
//    return new ForwardResolution(JSP_URL);
//  }
//
//  /**
//   * The array of selected items. The index of the array is equal to the index of the item ids managed by the storage.
//   */
//  public boolean[] getSelectedItems()
//  {
//    if (selectedItems == null && getStorage() != null) {
//      updateSelectedItems();
//    }
//    return selectedItems;
//  }
//
//  public void setSelectedItems(boolean[] selectedItems)
//  {
//    this.selectedItems = selectedItems;
//  }
//
//  /**
//   * @return the error properties of one imported sheet, if requested by the user, otherwise null.
//   */
//  public Map<String, Set<Object>> getErrorProperties()
//  {
//    return errorProperties;
//  }
//
//  /**
//   * @return the BWA of one imported sheet, if requested by the user, otherwise null.
//   */
//  public Bwa getBwa()
//  {
//    return bwa;
//  }
//  
//  private void checkAccess() {
//    accessChecker.checkRight(UserRightId.FIBU_DATEV_IMPORT, UserRightValue.TRUE);
//    accessChecker.checkDemoUser();
//  }
}
