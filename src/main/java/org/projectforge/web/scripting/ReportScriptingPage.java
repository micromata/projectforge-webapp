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
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.common.FileHelper;
import org.projectforge.common.LabelValueBean;
import org.projectforge.core.Configuration;
import org.projectforge.fibu.kost.reporting.ReportStorage;
import org.projectforge.scripting.GroovyResult;
import org.projectforge.scripting.ScriptDao;
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

  @SpringBean(name = "scriptDao")
  private ScriptDao scriptDao;

  protected GroovyResult groovyResult;

  private ReportScriptingForm form;

  private Component exceptionContainer;

  private Map<String, Object> scriptVariables;

  public ReportScriptingPage(PageParameters parameters)
  {
    super(parameters);
    form = new ReportScriptingForm(this);
    body.add(form);
    form.init();
    initScriptVariables();
  }

  private void initScriptVariables()
  {
    scriptVariables = new HashMap<String, Object>();
    scriptVariables.put("reportStorage", null);
    scriptVariables.put("reportScriptingStorage", null);
    scriptDao.addScriptVariables(scriptVariables);
    final SortedSet<String> set = new TreeSet<String>();
    set.addAll(scriptVariables.keySet());
    final StringBuffer buf = new StringBuffer();
    buf.append("scriptResult"); // first available variable.
    for (String key : set) {
      buf.append(", ").append(key);
    }
    body.add(new Label("availableScriptVariables", buf.toString()));
    scriptDao.addAliasForDeprecatedScriptVariables(scriptVariables);
    // scriptResult<c:forEach var="variable" items="${actionBean.scriptVariables}">
    // , ${variable.label}</c:forEach>
  }

  @Override
  protected void onBeforeRender()
  {
    addGroovyResult();
    super.onBeforeRender();
  }

  private void addGroovyResult()
  {
    if (exceptionContainer != null) {
      body.remove(exceptionContainer);
    }
    if (this.groovyResult != null && this.groovyResult.hasException() == true) {
      body.add(exceptionContainer = new WebMarkupContainer("exceptionContainer"));
      final RepeatingView linesRepeater = new RepeatingView("linesRepeater");
      ((WebMarkupContainer) exceptionContainer).add(linesRepeater);
      final String groovyScript = getReportScriptingStorage().getGroovyScript();
      StringBuffer buf = new StringBuffer();
      int lineNo = 1;
      for (int i = 0; i < groovyScript.length(); i++) {
        char c = groovyScript.charAt(i);
        if (c == '\n') {
          addLine(linesRepeater, lineNo++, buf.toString());
          buf = new StringBuffer();
        } else {
          buf.append(c);
        }
      }
      final String line = buf.toString();
      if (StringUtils.isNotEmpty(line) == true) {
        addLine(linesRepeater, lineNo++, buf.toString());
      }
    } else {
      body.add(exceptionContainer = new Label("exceptionContainer", "[invisible]").setVisible(false));
    }
  }

  private void addLine(final RepeatingView linesRepeater, final int lineNo, final String line)
  {
    final WebMarkupContainer row = new WebMarkupContainer(linesRepeater.newChildId());
    linesRepeater.add(row);
    row.add(new Label("lineNo", String.valueOf(lineNo)));
    row.add(new Label("line", line));
  }

  protected void execute()
  {
    // accessChecker.checkIsUserMemberOfGroup(ProjectForgeGroup.FINANCE_GROUP, ProjectForgeGroup.CONTROLLING_GROUP);
    // accessChecker.checkDemoUser();
    // final ReportGeneratorList reportGeneratorList = new ReportGeneratorList();
    // getScriptVariables();
    // scriptVariables.put("reportStorage", getReportStorage());
    // scriptVariables.put("reportScriptingStorage", getStorage());
    // scriptVariables.put("reportList", reportGeneratorList);
    // if (StringUtils.isNotBlank(getStorage().getGroovyScript()) == true) {
    // groovyResult = groovyExecutor.execute(getStorage().getGroovyScript(), scriptVariables);
    // if (groovyResult.hasException() == true) {
    // addError("groovyScript", "exception.groovyError", String.valueOf(groovyResult.getException()));
    // return getInputPage();
    // }
    // if (groovyResult.hasResult() == true) {
    // final Object result = groovyResult.getResult();
    // if (result instanceof ExportWorkbook == true) {
    // return excelExport();
    // } else if (groovyResult.getResult() instanceof ReportGeneratorList == true) {
    // reportGeneratorList = (ReportGeneratorList) groovyResult.getResult();
    // return jasperReport(reportGeneratorList);
    // } else if (result instanceof ExportJFreeChart) {
    // return jFreeChartExport();
    // }
    // }
    // } else if (getStorage().getJasperReport() != null) {
    // return jasperReport();
    // }
  }

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

  //
  // public boolean isException()
  // {
  // return this.groovyResult != null && this.groovyResult.hasException();
  // }
  //
  // /**
  // * Creates the reports for the entries.
  // * @param reportGeneratorList
  // * @return
  // */
  // private Resolution jasperReport(ReportGeneratorList reportGeneratorList)
  // {
  // if (CollectionUtils.isEmpty(reportGeneratorList.getReports()) == true) {
  // addError("groovyScript", "fibu.reporting.jasper.error.reportListIsEmpty");
  // return getInputPage();
  // }
  // Map<String, Object> parameters = new HashMap<String, Object>();
  // ReportGenerator report = reportGeneratorList.getReports().get(0);
  // Collection< ? > beanCollection = report.getBeanCollection();
  // parameters = report.getParameters();
  // return jasperReport(parameters, beanCollection);
  // }
  //
  // /**
  // * Default report from reportStorage. Uses the current report and puts the bwa values in parameter map.
  // */
  // private Resolution jasperReport()
  // {
  // if (getReportStorage() == null || getReportStorage().getRoot() == null || getReportStorage().getRoot().isLoad() == false) {
  // addGlobalError("fibu.reporting.jasper.error.reportDataDoesNotExist");
  // return getInputPage();
  // }
  // Map<String, Object> parameters = new HashMap<String, Object>();
  // Report report = getReportStorage().getCurrentReport();
  // Collection< ? > beanCollection = report.getBuchungssaetze();
  // Bwa.putBwaWerte(parameters, report.getBwa());
  // return jasperReport(parameters, beanCollection);
  // }
  //
  // private Resolution jasperReport(Map<String, Object> parameters, Collection< ? > beanCollection)
  // {
  // JasperReport jasperReport = getStorage().getJasperReport();
  // JasperPrint jp = null;
  // try {
  // jp = JasperFillManager.fillReport(jasperReport, parameters, new JRBeanCollectionDataSource(beanCollection));
  // } catch (JRException ex) {
  // addGlobalError("error", ex.getMessage());
  // log.error(ex.getMessage(), ex);
  // return getInputPage();
  // }
  // final JasperPrint jasperPrint = jp;
  // return new Resolution() {
  // public void execute(HttpServletRequest request, HttpServletResponse response) throws Exception
  // {
  // StringBuffer buf = new StringBuffer();
  // buf.append("pf_report_");
  // buf.append(DateHelper.getTimestampAsFilenameSuffix(getContext().now())).append(".pdf");
  // ResponseUtils.prepareDownload(buf.toString(), response, getContext().getServletContext(), true);
  // JasperExportManager.exportReportToPdfStream(jasperPrint, response.getOutputStream());
  // response.getOutputStream().flush();
  // }
  // };
  // }
  //
  // private Resolution excelExport()
  // {
  // final ExportWorkbook workbook = (ExportWorkbook) groovyResult.getResult();
  // return new Resolution() {
  // public void execute(HttpServletRequest request, HttpServletResponse response) throws Exception
  // {
  // StringBuffer buf = new StringBuffer();
  // buf.append("pf_report_");
  // buf.append(DateHelper.getTimestampAsFilenameSuffix(getContext().now())).append(".xls");
  // ResponseUtils.prepareDownload(buf.toString(), response, getContext().getServletContext(), true);
  // workbook.write(response.getOutputStream());
  // response.getOutputStream().flush();
  // }
  // };
  // }
  //
  // private Resolution jFreeChartExport()
  // {
  // final ExportJFreeChart exportJFreeChart = (ExportJFreeChart)groovyResult.getResult();
  // final JFreeChart chart = exportJFreeChart.getJFreeChart();
  // final int width = exportJFreeChart.getWidth();
  // final int height = exportJFreeChart.getHeight();
  // return new Resolution() {
  // public void execute(HttpServletRequest request, HttpServletResponse response) throws Exception
  // {
  // StringBuffer buf = new StringBuffer();
  // buf.append("pf_chart_");
  // buf.append(DateHelper.getTimestampAsFilenameSuffix(getContext().now()));
  // ResponseUtils.prepareDownload(buf.toString(), response, getContext().getServletContext(), true);
  // if (exportJFreeChart.getImageType() == JFreeChartImageType.PNG) {
  // ChartUtilities.writeChartAsPNG(response.getOutputStream(), chart, width, height);
  // buf.append(".png");
  // } else {
  // ChartUtilities.writeChartAsJPEG(response.getOutputStream(), chart, width, height);
  // buf.append(".jpg");
  // }
  // response.getOutputStream().flush();
  // }
  // };
  // }
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
