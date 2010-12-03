/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2010 Kai Reinhard (k.reinhard@me.com)
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



public class BuchungssatzListAction// extends BaseListActionBean<BuchungssatzListFilter, BuchungssatzDao, BuchungssatzDO>
{
//  private static final Logger log = Logger.getLogger(BuchungssatzListAction.class);
//
//  private Bwa bwa;
//
//  private String reportId;
//
//  private Integer bwaZeileId;
//
//  private Report report;
//
//  public List<LabelValueBean<String, Integer>> getFromYearList()
//  {
//    int[] years = baseDao.getYears();
//    List<LabelValueBean<String, Integer>> list = new ArrayList<LabelValueBean<String, Integer>>();
//    for (int year : years) {
//      list.add(new LabelValueBean<String, Integer>(String.valueOf(year), year));
//    }
//    return list;
//  }
//
//  public List<LabelValueBean<String, Integer>> getToYearList()
//  {
//    List<LabelValueBean<String, Integer>> list = getFromYearList();
//    list.add(0, new LabelValueBean<String, Integer>("----", -1));
//    return list;
//  }
//
//  public List<LabelValueBean<String, Integer>> getMonthList()
//  {
//    List<LabelValueBean<String, Integer>> list = new ArrayList<LabelValueBean<String, Integer>>();
//    list.add(new LabelValueBean<String, Integer>("--", -1));
//    for (int month = 0; month < 12; month++) {
//      list.add(new LabelValueBean<String, Integer>(StringHelper.format2DigitNumber(month + 1), month));
//    }
//    return list;
//  }
//
//  @Override
//  protected List<BuchungssatzDO> buildList()
//  {
//    List<BuchungssatzDO> list = null;
//    if (StringUtils.isNotEmpty(reportId) == true) {
//      ReportStorage reportStorage = getReportStorage();
//      if (reportStorage != null) {
//        report = reportStorage.findById(this.reportId);
//        if (report != null) {
//          if (this.bwaZeileId != null) {
//            BwaZeile zeile = report.getBwa().getZeile(bwaZeileId);
//            if (zeile != null) {
//              list = zeile.getBuchungssaetze();
//            } else {
//              log.info("BwaZeile " + bwaZeileId + " not found for report with id '" + reportId + "' in existing ReportStorage.");
//            }
//          } else {
//            list = report.getBuchungssaetze();
//          }
//        } else {
//          log.info("Report with id '" + reportId + "' not found in existing ReportStorage.");
//        }
//      } else {
//        log.info("Report with id '" + reportId + "' not found. ReportStorage does not exist.");
//      }
//    } else {
//      list = super.buildList();
//      if (CollectionUtils.isEmpty(list) == true) {
//        return list;
//      }
//    }
//    this.bwa = new Bwa();
//    this.bwa.setBuchungssaetze(list);
//    return list;
//  }
//
//  @ValidationMethod
//  public void validateFilter(ValidationErrors errors)
//  {
//    if (baseDao.validateTimeperiod(getActionFilter()) == false) {
//      errors.addGlobalError(new LocalizableError("fibu.buchungssatz.error.invalidTimeperiod"));
//    }
//  }
//
//  /**
//   * @return the BWA of the entries.
//   */
//  public Bwa getBwa()
//  {
//    return bwa;
//  }
//
//  /**
//   * Wenn die Report-Id gesetzt ist, dann werden die Buchungssätze aus diesem Report (aus dem ReportStorage) angezeigt. Die Suchfilter
//   * werden dann ignoriert.
//   */
//  public String getReportId()
//  {
//    return reportId;
//  }
//
//  public void setReportId(String reportId)
//  {
//    this.reportId = reportId;
//  }
//
//  public Report getReport()
//  {
//    return report;
//  }
//
//  public ReportStorage getReportStorage()
//  {
//    return ReportObjectivesAction.getReportStorage(getContext());
//  }
//
//  /**
//   * Wenn die Report-Id und die BwaZeileId gesetzt sind, dann werden die Buchungssätze aus diesem Report aus der entsprechendne BwaZeile
//   * angezeigt. Die Suchfilter werden dann ignoriert.
//   */
//  public Integer getBwaZeileId()
//  {
//    return bwaZeileId;
//  }
//
//  public void setBwaZeileId(Integer bwaZeileId)
//  {
//    this.bwaZeileId = bwaZeileId;
//  }
//
//  public void setBuchungssatzDao(BuchungssatzDao buchungssatzDao)
//  {
//    this.baseDao = buchungssatzDao;
//  }
//
//  @Override
//  protected boolean isShowResultInstantly()
//  {
//    return StringUtils.isNotEmpty(this.reportId);
//  }
//
//  @Override
//  protected BuchungssatzListFilter createFilterInstance()
//  {
//    return new BuchungssatzListFilter();
//  }
//
//  @Override
//  protected Logger getLogger()
//  {
//    return log;
//  }
}
