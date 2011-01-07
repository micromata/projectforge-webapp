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

package org.projectforge.web.scripting;

import java.io.File;
import java.io.InputStream;

import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.common.FileHelper;
import org.projectforge.core.Configuration;
import org.projectforge.fibu.kost.reporting.ReportStorage;
import org.projectforge.scripting.GroovyResult;
import org.projectforge.user.PFUserContext;
import org.projectforge.user.ProjectForgeGroup;
import org.projectforge.web.fibu.ReportScriptingStorage;
import org.projectforge.web.wicket.AbstractSecuredPage;

public class ReportScriptingPage extends AbstractSecuredPage
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ReportScriptingPage.class);

  private transient ReportScriptingStorage reportScriptingStorage;

  @SpringBean(name = "configuration")
  private Configuration configuration;
  
//  @SpringBean(name = "scriptDao")
//  private ScriptDao scriptDao;

  protected GroovyResult groovyResult;

  private ReportScriptingForm form;

  public ReportScriptingPage(PageParameters parameters)
  {
    super(parameters);
    form = new ReportScriptingForm(this);
    body.add(form);
    form.init();
  }

  protected void execute()
  {
//    accessChecker.checkIsUserMemberOfGroup(ProjectForgeGroup.FINANCE_GROUP, ProjectForgeGroup.CONTROLLING_GROUP);
//    accessChecker.checkDemoUser();
//    final ReportGeneratorList reportGeneratorList = new ReportGeneratorList();
//    getScriptVariables();
//    scriptVariables.put("reportStorage", getReportStorage());
//    scriptVariables.put("reportScriptingStorage", getStorage());
//    scriptVariables.put("reportList", reportGeneratorList);
//    if (StringUtils.isNotBlank(getStorage().getGroovyScript()) == true) {
//      groovyResult = groovyExecutor.execute(getStorage().getGroovyScript(), scriptVariables);
//      if (groovyResult.hasException() == true) {
//        addError("groovyScript", "exception.groovyError", String.valueOf(groovyResult.getException()));
//        return getInputPage();
//      }
//      if (groovyResult.hasResult() == true) {
//        final Object result = groovyResult.getResult();
//        if (result instanceof ExportWorkbook == true) {
//          return excelExport();
//        } else if (groovyResult.getResult() instanceof ReportGeneratorList == true) {
//          reportGeneratorList = (ReportGeneratorList) groovyResult.getResult();
//          return jasperReport(reportGeneratorList);
//        } else if (result instanceof ExportJFreeChart) {
//          return jFreeChartExport();
//        }
//      }
//    } else if (getStorage().getJasperReport() != null) {
//      return jasperReport();
//    }
  }
  
//public List<LabelValueBean<String, Class<?>>> getScriptVariables()
//{
//  if (MapUtils.isEmpty(scriptVariables) == true) {
//    scriptVariables = new HashMap<String, Object>();
//    scriptVariables.put("reportStorage", null);
//    scriptVariables.put("reportScriptingStorage", null);
//    scriptVariables.put("appId", Version.APP_ID);
//    scriptVariables.put("appVersion", getAppVersion());
//    scriptVariables.put("appRelease", getAppReleaseDate());
//
//    scriptVariables.put("addressDao", new ScriptingDao<AddressDO>(addressDao));
//    scriptVariables.put("auftragDao", new ScriptingDao<AuftragDO>(auftragDao));
//    scriptVariables.put("buchungssatzDao", new ScriptingDao<BuchungssatzDO>(buchungssatzDao));
//    scriptVariables.put("eingangsrechnungDao", new ScriptingDao<EingangsrechnungDO>(eingangsrechnungDao));
//    scriptVariables.put("employeeDao", new EmployeeScriptingDao(employeeDao));
//    scriptVariables.put("employeeSalaryDao", new ScriptingDao<EmployeeSalaryDO>(employeeSalaryDao));
//    scriptVariables.put("kost1Dao", new Kost1ScriptingDao(kost1Dao));
//    scriptVariables.put("kost2Dao", new ScriptingDao<Kost2DO>(kost2Dao));
//    scriptVariables.put("kost2ArtDao", new ScriptingDao<Kost2ArtDO>(kost2ArtDao));
//    scriptVariables.put("kostZuweisungDao", new ScriptingDao<KostZuweisungDO>(kostZuweisungDao));
//    scriptVariables.put("kundeDao", new ScriptingDao<KundeDO>(kundeDao));
//    scriptVariables.put("projektDao", new ScriptingDao<ProjektDO>(projektDao));
//    scriptVariables.put("rechnungDao", new ScriptingDao<RechnungDO>(rechnungDao));
//    scriptVariables.put("reportList", null);
//    scriptVariables.put("taskDao", new ScriptingDao<TaskDO>(taskDao));
//    scriptVariables.put("taskTree", new ScriptingTaskTree(taskTree));
//    scriptVariables.put("timesheetDao", new ScriptingDao<TimesheetDO>(timesheetDao));
//    scriptVariables.put("userDao", new ScriptingDao<PFUserDO>(userDao));
//  }
//  List<LabelValueBean<String, Class<?>>> result = new ArrayList<LabelValueBean<String, Class<?>>>();
//  SortedSet<String> set = new TreeSet<String>();
//  set.addAll(scriptVariables.keySet());
//  for (String key : set) {
//    Object obj = scriptVariables.get(key);
//    Class<?> clazz = null;
//    if (obj != null) {
//      clazz = obj.getClass();
//    }
//    LabelValueBean<String, Class<?>> lv = new LabelValueBean<String, Class<?>>(key, clazz);
//    result.add(lv);
//  }
//  return result;
//}


  protected void upload()
  {
    accessChecker.checkIsUserMemberOfGroup(ProjectForgeGroup.FINANCE_GROUP, ProjectForgeGroup.CONTROLLING_GROUP);
    accessChecker.checkDemoUser();
    log.info("upload");
    final FileUpload fileUpload = form.fileUploadField.getFileUpload();
    if (fileUpload != null) {
      boolean delete = false;
      try {
        final InputStream is = fileUpload.getInputStream();
        final String clientFileName = fileUpload.getClientFileName();
        if (clientFileName.endsWith(".jrxml") == true) {
          delete = true;
          final JasperReport report = JasperCompileManager.compileReport(is);
          if (report != null) {
            getReportScriptingStorage().setJasperReport(report, clientFileName);
          }
        } else if (clientFileName.endsWith(".xls") == true) {
          StringBuffer buf = new StringBuffer();
          buf.append("report_").append(FileHelper.createSafeFilename(PFUserContext.getUser().getUsername(), 20)).append(".xls");
          File file = new File(configuration.getWorkingDirectory(), buf.toString());
          fileUpload.writeTo(file);
          getReportScriptingStorage().setFilename(clientFileName, file.getAbsolutePath());
        } else {
          log.error("File extension not supported: " + clientFileName);
        }
      } catch (Exception ex) {
        log.error(ex.getMessage(), ex);
        error("An error occurred (see log files for details): " + ex.getMessage());
      } finally {
        if (delete == true) {
          fileUpload.delete();
        }
      }
    }
  }

///**
//* Gets the lines of the groovy script for displaying line number etc.
//* @return
//*/
//public List<String> getLines()
//{
// if (lines != null) {
//   return lines;
// }
// lines = new ArrayList<String>();
// String groovyScript = getGroovyScript();
// StringBuffer line = new StringBuffer();
// for (int i = 0; i < groovyScript.length(); i++) {
//   char c = groovyScript.charAt(i);
//   if (c == '\n') {
//     lines.add(HtmlHelper.escapeXml(line.toString()));
//     line = new StringBuffer();
//   } else {
//     line.append(c);
//   }
// }
// lines.add(HtmlHelper.escapeXml(line.toString()));
// return lines;
//}
//
//public boolean isException()
//{
// return this.groovyResult != null && this.groovyResult.hasException();
//}
//
///**
//* Creates the reports for the entries.
//* @param reportGeneratorList
//* @return
//*/
//private Resolution jasperReport(ReportGeneratorList reportGeneratorList)
//{
// if (CollectionUtils.isEmpty(reportGeneratorList.getReports()) == true) {
//   addError("groovyScript", "fibu.reporting.jasper.error.reportListIsEmpty");
//   return getInputPage();
// }
// Map<String, Object> parameters = new HashMap<String, Object>();
// ReportGenerator report = reportGeneratorList.getReports().get(0);
// Collection< ? > beanCollection = report.getBeanCollection();
// parameters = report.getParameters();
// return jasperReport(parameters, beanCollection);
//}
//
///**
//* Default report from reportStorage. Uses the current report and puts the bwa values in parameter map.
//*/
//private Resolution jasperReport()
//{
// if (getReportStorage() == null || getReportStorage().getRoot() == null || getReportStorage().getRoot().isLoad() == false) {
//   addGlobalError("fibu.reporting.jasper.error.reportDataDoesNotExist");
//   return getInputPage();
// }
// Map<String, Object> parameters = new HashMap<String, Object>();
// Report report = getReportStorage().getCurrentReport();
// Collection< ? > beanCollection = report.getBuchungssaetze();
// Bwa.putBwaWerte(parameters, report.getBwa());
// return jasperReport(parameters, beanCollection);
//}
//
//private Resolution jasperReport(Map<String, Object> parameters, Collection< ? > beanCollection)
//{
// JasperReport jasperReport = getStorage().getJasperReport();
// JasperPrint jp = null;
// try {
//   jp = JasperFillManager.fillReport(jasperReport, parameters, new JRBeanCollectionDataSource(beanCollection));
// } catch (JRException ex) {
//   addGlobalError("error", ex.getMessage());
//   log.error(ex.getMessage(), ex);
//   return getInputPage();
// }
// final JasperPrint jasperPrint = jp;
// return new Resolution() {
//   public void execute(HttpServletRequest request, HttpServletResponse response) throws Exception
//   {
//     StringBuffer buf = new StringBuffer();
//     buf.append("pf_report_");
//     buf.append(DateHelper.getTimestampAsFilenameSuffix(getContext().now())).append(".pdf");
//     ResponseUtils.prepareDownload(buf.toString(), response, getContext().getServletContext(), true);
//     JasperExportManager.exportReportToPdfStream(jasperPrint, response.getOutputStream());
//     response.getOutputStream().flush();
//   }
// };
//}
//
//private Resolution excelExport()
//{
// final ExportWorkbook workbook = (ExportWorkbook) groovyResult.getResult();
// return new Resolution() {
//   public void execute(HttpServletRequest request, HttpServletResponse response) throws Exception
//   {
//     StringBuffer buf = new StringBuffer();
//     buf.append("pf_report_");
//     buf.append(DateHelper.getTimestampAsFilenameSuffix(getContext().now())).append(".xls");
//     ResponseUtils.prepareDownload(buf.toString(), response, getContext().getServletContext(), true);
//     workbook.write(response.getOutputStream());
//     response.getOutputStream().flush();
//   }
// };
//}
//
//private Resolution jFreeChartExport()
//{
// final ExportJFreeChart exportJFreeChart = (ExportJFreeChart)groovyResult.getResult();
// final JFreeChart chart = exportJFreeChart.getJFreeChart();
// final int width = exportJFreeChart.getWidth();
// final int height = exportJFreeChart.getHeight();
// return new Resolution() {
//   public void execute(HttpServletRequest request, HttpServletResponse response) throws Exception
//   {
//     StringBuffer buf = new StringBuffer();
//     buf.append("pf_chart_");
//     buf.append(DateHelper.getTimestampAsFilenameSuffix(getContext().now()));
//     ResponseUtils.prepareDownload(buf.toString(), response, getContext().getServletContext(), true);
//     if (exportJFreeChart.getImageType() == JFreeChartImageType.PNG) {
//       ChartUtilities.writeChartAsPNG(response.getOutputStream(), chart, width, height);
//       buf.append(".png");
//     } else {
//       ChartUtilities.writeChartAsJPEG(response.getOutputStream(), chart, width, height);
//       buf.append(".jpg");
//     }
//     response.getOutputStream().flush();
//   }
// };
//}
  /**
   * TODO
   * @return
   */
  public ReportStorage getReportStorage()
  {
    return null;// ReportObjectivesAction.getReportStorage(getContext());
  }

  protected ReportScriptingStorage getReportScriptingStorage()
  {
    if (reportScriptingStorage != null) {
      return reportScriptingStorage;
    }
    reportScriptingStorage = (ReportScriptingStorage) getUserPrefEntry(ReportScriptingStorage.class.getName());
    if (reportScriptingStorage == null) {
      reportScriptingStorage = new ReportScriptingStorage();
      putUserPrefEntry(ReportScriptingStorage.class.getName(), reportScriptingStorage, false);
    }
    return reportScriptingStorage;
  }

  @Override
  protected String getTitle()
  {
    return getString("fibu.reporting.scripting");
  }
}
