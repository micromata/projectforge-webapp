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



public class ReportObjectivesAction// extends BaseActionBean
{
//  public class ResultCell
//  {
//    private BwaZeile bwaZeile;
//
//    private Report report;
//
//    public Report getReport()
//    {
//      return this.report;
//    }
//
//    public BwaZeile getBwaZeile()
//    {
//      return this.bwaZeile;
//    }
//  }
//
//  private static final String KEY_REPORT_STORAGE = ReportObjectivesAction.class.getName() + ":ReportStorage";
//
//  private static final Logger log = Logger.getLogger(ReportObjectivesAction.class);
//
//  private static final String JSP_URL = "/WEB-INF/jsp/fibu/reportObjectives.jsp";
//
//  private ReportDao reportDao;
//
//  private BuchungssatzDao buchungssatzDao;
//
//  protected ReportFilter actionFilter;
//
//  private FileBean uploadFile;
//
//  private ReportStorage reportStorage;
//
//  private String eventKey;
//
//  private Priority priority = Priority.HIGH;
//
//  private List<LabelValueBean<String, Integer>> fromYearList;
//
//  private List<LabelValueBean<String, Integer>> toYearList;
//
//  private List<LabelValueBean<String, Integer>> monthList;
//
//  public List<LabelValueBean<String, Integer>> getFromYearList()
//  {
//    if (fromYearList == null) {
//      int[] years = buchungssatzDao.getYears();
//      fromYearList = new ArrayList<LabelValueBean<String, Integer>>();
//      for (int year : years) {
//        fromYearList.add(new LabelValueBean<String, Integer>(String.valueOf(year), year));
//      }
//    }
//    return fromYearList;
//  }
//
//  public List<LabelValueBean<String, Integer>> getToYearList()
//  {
//    if (toYearList == null) {
//      toYearList = getFromYearList();
//      toYearList.add(0, new LabelValueBean<String, Integer>("----", -1));
//    }
//    return toYearList;
//  }
//
//  public List<LabelValueBean<String, Integer>> getMonthList()
//  {
//    if (monthList == null) {
//      monthList = new ArrayList<LabelValueBean<String, Integer>>();
//      monthList.add(new LabelValueBean<String, Integer>("--", -1));
//      for (int month = 0; month < 12; month++) {
//        monthList.add(new LabelValueBean<String, Integer>(StringHelper.format2DigitNumber(month + 1), month));
//      }
//    }
//    return monthList;
//  }
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
//  /**
//   * Die Priorität steht für den Detailgrad, mit welchem die BWA angezeigt wird.
//   */
//  public Priority getPriority()
//  {
//    return this.priority;
//  }
//
//  @DefaultHandler
//  @DontValidate
//  public Resolution execute()
//  {
//    accessChecker.checkIsUserMemberOfGroup(ProjectForgeGroup.FINANCE_GROUP, ProjectForgeGroup.CONTROLLING_GROUP);
//    if (getReportStorage() != null) {
//      if (StringUtils.isNotEmpty(selectedValue) == true) {
//        if ("current".equals(eventKey) == true) {
//          reportStorage.setCurrentReport(selectedValue);
//        }
//      }
//    }
//    return getInputPage();
//  }
//
//  public Resolution loadReport()
//  {
//    accessChecker.checkIsUserMemberOfGroup(ProjectForgeGroup.FINANCE_GROUP, ProjectForgeGroup.CONTROLLING_GROUP);
//    accessChecker.checkDemoUser();
//    Report report = getReportStorage().getRoot();
//    report.setFrom(getActionFilter().getFromYear(), getActionFilter().getFromMonth());
//    report.setTo(getActionFilter().getToYear(), getActionFilter().getToMonth());
//    reportDao.loadReport(report);
//    return getInputPage();
//  }
//
//  public Resolution clear()
//  {
//    accessChecker.checkIsUserMemberOfGroup(ProjectForgeGroup.FINANCE_GROUP, ProjectForgeGroup.CONTROLLING_GROUP);
//    accessChecker.checkDemoUser();
//    getContext().removeEntry(KEY_REPORT_STORAGE);
//    this.reportStorage = null;
//    return getInputPage();
//  }
//
//  @DontValidate
//  public Resolution importReportObjectives()
//  {
//    accessChecker.checkIsUserMemberOfGroup(ProjectForgeGroup.FINANCE_GROUP, ProjectForgeGroup.CONTROLLING_GROUP);
//    accessChecker.checkDemoUser();
//    if (uploadFile == null) {
//      return getInputPage();
//    }
//    try {
//      InputStream is = uploadFile.getInputStream();
//      Report report = reportDao.createReport(is);
//      reportStorage = new ReportStorage(report);
//      reportStorage.setFileName(uploadFile.getFileName());
//    } catch (Exception ex) {
//      addGlobalError("error", ex.getMessage());
//      log.error(ex.getMessage(), ex);
//    } finally {
//      try {
//        uploadFile.delete();
//      } catch (IOException ex) {
//        log.error(ex.getMessage(), ex);
//      }
//    }
//    getContext().putEntry(KEY_REPORT_STORAGE, reportStorage, false);
//    return getInputPage();
//  }
//
//  @ValidationMethod
//  public void validateFilter(ValidationErrors errors)
//  {
//    if (buchungssatzDao.validateTimeperiod(getActionFilter()) == false) {
//      errors.addGlobalError(new LocalizableError("fibu.buchungssatz.error.invalidTimeperiod"));
//    }
//  }
//
//  /**
//   * @return report if exists (look up also in user's session), otherwise null.
//   */
//  public ReportStorage getReportStorage()
//  {
//    if (reportStorage == null) {
//      reportStorage = getReportStorage(getContext());
//    }
//    return reportStorage;
//  }
//  
//  public static ReportStorage getReportStorage(BaseActionBeanContext context) {
//    return (ReportStorage) context.getEntry(KEY_REPORT_STORAGE);
//  }
//
//  public BwaZeile[][] getChildBwaTable()
//  {
//    Report current = getReportStorage().getCurrentReport();
//    return current.getChildBwaArray(true);
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
//  private Resolution getInputPage()
//  {
//    this.eventKey = null;
//    this.selectedValue = null;
//    return new ForwardResolution(JSP_URL);
//  }
//
//  public ReportFilter getActionFilter()
//  {
//    if (actionFilter == null) {
//      actionFilter = (ReportFilter) getContext().getEntry(this.getClass().getName() + ":Filter");
//      if (actionFilter == null) {
//        actionFilter = new ReportFilter();
//        actionFilter.setFromYear(getFromYearList().get(0).getValue());
//        actionFilter.setFromMonth(-1);
//        actionFilter.setToYear(-1);
//        actionFilter.setToMonth(-1);
//        getContext().putEntry(this.getClass().getName() + ":Filter", actionFilter, true);
//      }
//    }
//    return actionFilter;
//  }
//
//  public void setReportDao(ReportDao reportDao)
//  {
//    this.reportDao = reportDao;
//  }
//
//  public void setBuchungssatzDao(BuchungssatzDao buchungssatzDao)
//  {
//    this.buchungssatzDao = buchungssatzDao;
//  }
}
