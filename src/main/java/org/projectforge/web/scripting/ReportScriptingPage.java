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

package org.projectforge.web.scripting;

import java.io.File;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.apache.wicket.Response;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.protocol.http.WebResponse;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.jfree.chart.JFreeChart;
import org.projectforge.common.DateHelper;
import org.projectforge.common.FileHelper;
import org.projectforge.core.ConfigXml;
import org.projectforge.export.ExportJFreeChart;
import org.projectforge.export.ExportWorkbook;
import org.projectforge.fibu.kost.Bwa;
import org.projectforge.fibu.kost.BwaZeileId;
import org.projectforge.fibu.kost.reporting.Report;
import org.projectforge.fibu.kost.reporting.ReportGenerator;
import org.projectforge.fibu.kost.reporting.ReportGeneratorList;
import org.projectforge.fibu.kost.reporting.ReportStorage;
import org.projectforge.scripting.GroovyExecutor;
import org.projectforge.scripting.GroovyResult;
import org.projectforge.scripting.ScriptDao;
import org.projectforge.user.PFUserContext;
import org.projectforge.user.ProjectForgeGroup;
import org.projectforge.web.fibu.ReportScriptingStorage;
import org.projectforge.web.wicket.AbstractSecuredPage;
import org.projectforge.web.wicket.DownloadUtils;
import org.projectforge.web.wicket.JFreeChartImage;

public class ReportScriptingPage extends AbstractSecuredPage
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ReportScriptingPage.class);

  private transient ReportScriptingStorage reportScriptingStorage;

  @SpringBean(name = "scriptDao")
  private ScriptDao scriptDao;

  @SpringBean(name = "groovyExecutor")
  private GroovyExecutor groovyExecutor;

  protected GroovyResult groovyResult;

  private ReportScriptingForm form;

  private Component exceptionContainer;

  private Label availableScriptVariablesLabel, bwaZeilenVariablesLabel;

  private WebMarkupContainer imageResultContainer;

  private transient Map<String, Object> scriptVariables;

  public ReportScriptingPage(PageParameters parameters)
  {
    super(parameters);
    form = new ReportScriptingForm(this);
    body.add(form);
    form.init();
    initScriptVariables();
    body.add(imageResultContainer = (WebMarkupContainer) new WebMarkupContainer("imageResult").setVisible(false));
  }

  private void initScriptVariables()
  {
    if (scriptVariables != null) {
      // Already initialized.
      return;
    }
    scriptVariables = new HashMap<String, Object>();
    scriptVariables.put("reportStorage", null);
    scriptVariables.put("reportScriptingStorage", null);
    scriptDao.addScriptVariables(scriptVariables);
    final SortedSet<String> set = new TreeSet<String>();
    set.addAll(scriptVariables.keySet());
    StringBuffer buf = new StringBuffer();
    buf.append("scriptResult"); // first available variable.
    for (String key : set) {
      buf.append(", ").append(key);
    }
    if (availableScriptVariablesLabel == null) {
      body.add(availableScriptVariablesLabel = new Label("availableScriptVariables", buf.toString()));
    }
    scriptDao.addAliasForDeprecatedScriptVariables(scriptVariables);
    buf = new StringBuffer();
    boolean first = true;
    for (final BwaZeileId bwaZeileId : BwaZeileId.values()) {
      if (first == true) {
        first = false;
      } else {
        buf.append(", ");
      }
      buf.append('z').append(bwaZeileId.getId()).append(", ").append(bwaZeileId.getKey());
    }
    for (final String bwaValue : Bwa.getAdditionalValues()) {
      buf.append(", ").append(bwaValue);
    }
    if (bwaZeilenVariablesLabel == null) {
      body.add(bwaZeilenVariablesLabel = new Label("bwaZeilenVariables", buf.toString()));
    }
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
    accessChecker.checkIsLoggedInUserMemberOfGroup(ProjectForgeGroup.FINANCE_GROUP, ProjectForgeGroup.CONTROLLING_GROUP);
    accessChecker.checkDemoUser();
    imageResultContainer.setVisible(false);
    ReportGeneratorList reportGeneratorList = new ReportGeneratorList();
    initScriptVariables();
    scriptVariables.put("reportStorage", getReportStorage());
    scriptVariables.put("reportScriptingStorage", getReportScriptingStorage());
    scriptVariables.put("reportList", reportGeneratorList);
    if (StringUtils.isNotBlank(getReportScriptingStorage().getGroovyScript()) == true) {
      groovyResult = groovyExecutor.execute(getReportScriptingStorage().getGroovyScript(), scriptVariables);
      if (groovyResult.hasException() == true) {
        error(getLocalizedMessage("exception.groovyError", String.valueOf(groovyResult.getException())));
        return;
      }
      if (groovyResult.hasResult() == true) {
        final Object result = groovyResult.getResult();
        if (result instanceof ExportWorkbook == true) {
          excelExport();
        } else if (groovyResult.getResult() instanceof ReportGeneratorList == true) {
          reportGeneratorList = (ReportGeneratorList) groovyResult.getResult();
          jasperReport(reportGeneratorList);
        } else if (result instanceof ExportJFreeChart) {
          jFreeChartExport();
        }
      }
    } else if (getReportScriptingStorage().getJasperReport() != null) {
      jasperReport();
    }
  }

  protected void upload()
  {
    accessChecker.checkIsLoggedInUserMemberOfGroup(ProjectForgeGroup.FINANCE_GROUP, ProjectForgeGroup.CONTROLLING_GROUP);
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
          File file = new File(ConfigXml.getInstance().getWorkingDirectory(), buf.toString());
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

  /**
   * Creates the reports for the entries.
   * @param reportGeneratorList
   */
  private void jasperReport(final ReportGeneratorList reportGeneratorList)
  {
    if (CollectionUtils.isEmpty(reportGeneratorList.getReports()) == true) {
      error(getString("fibu.reporting.jasper.error.reportListIsEmpty"));
      return;
    }
    final ReportGenerator report = reportGeneratorList.getReports().get(0);
    final Collection< ? > beanCollection = report.getBeanCollection();
    final Map<String, Object> parameters = report.getParameters();
    jasperReport(parameters, beanCollection);
  }

  /**
   * Default report from reportStorage. Uses the current report and puts the bwa values in parameter map.
   */
  private void jasperReport()
  {
    if (getReportStorage() == null || getReportStorage().getRoot() == null || getReportStorage().getRoot().isLoad() == false) {
      error(getString("fibu.reporting.jasper.error.reportDataDoesNotExist"));
      return;
    }
    final Map<String, Object> parameters = new HashMap<String, Object>();
    final Report report = getReportStorage().getCurrentReport();
    final Collection< ? > beanCollection = report.getBuchungssaetze();
    Bwa.putBwaWerte(parameters, report.getBwa());
    jasperReport(parameters, beanCollection);
  }

  private void jasperReport(Map<String, Object> parameters, Collection< ? > beanCollection)
  {
    try {
      final JasperReport jasperReport = getReportScriptingStorage().getJasperReport();
      final JasperPrint jp = JasperFillManager.fillReport(jasperReport, parameters, new JRBeanCollectionDataSource(beanCollection));
      final JasperPrint jasperPrint = jp;
      final StringBuffer buf = new StringBuffer();
      buf.append("pf_report_");
      buf.append(DateHelper.getTimestampAsFilenameSuffix(now())).append(".pdf");
      final String filename = buf.toString();
      final Response response = getResponse();
      ((WebResponse) response).setAttachmentHeader(filename);
      response.setContentType(DownloadUtils.getContentType(filename));
      JasperExportManager.exportReportToPdfStream(jasperPrint, response.getOutputStream());
      response.getOutputStream().flush();
    } catch (final Exception ex) {
      error(getLocalizedMessage("error", ex.getMessage()));
      log.error(ex.getMessage(), ex);
    }
  }

  private void excelExport()
  {
    try {
      final ExportWorkbook workbook = (ExportWorkbook) groovyResult.getResult();
      final StringBuffer buf = new StringBuffer();
      buf.append("pf_report_");
      buf.append(DateHelper.getTimestampAsFilenameSuffix(now())).append(".xls");
      final String filename = buf.toString();
      final Response response = getResponse();
      ((WebResponse) response).setAttachmentHeader(filename);
      response.setContentType(DownloadUtils.getContentType(filename));
      workbook.write(response.getOutputStream());
      response.getOutputStream().flush();
    } catch (final Exception ex) {
      error(getLocalizedMessage("error", ex.getMessage()));
      log.error(ex.getMessage(), ex);
    }
  }

  private void jFreeChartExport()
  {
    try {
      final ExportJFreeChart exportJFreeChart = (ExportJFreeChart) groovyResult.getResult();
      final JFreeChart chart = exportJFreeChart.getJFreeChart();
      final int width = exportJFreeChart.getWidth();
      final int height = exportJFreeChart.getHeight();
      // final StringBuffer buf = new StringBuffer();
      // buf.append("pf_chart_");
      // buf.append(DateHelper.getTimestampAsFilenameSuffix(now()));
      // final Response response = getResponse();
      // if (exportJFreeChart.getImageType() == JFreeChartImageType.PNG) {
      // ChartUtilities.writeChartAsPNG(response.getOutputStream(), chart, width, height);
      // buf.append(".png");
      // } else {
      // ChartUtilities.writeChartAsJPEG(response.getOutputStream(), chart, width, height);
      // buf.append(".jpg");
      // }
      // final String filename = buf.toString();
      final JFreeChartImage image = new JFreeChartImage("image", chart, exportJFreeChart.getImageType(), width, height);
      image.add(new SimpleAttributeModifier("width", String.valueOf(width)));
      image.add(new SimpleAttributeModifier("height", String.valueOf(height)));
      imageResultContainer.removeAll();
      imageResultContainer.add(image).setVisible(true);
      // ((WebResponse) response).setAttachmentHeader(filename);
      // response.setContentType(DownloadUtils.getContentType(filename));
      // response.getOutputStream().flush();
      // setRedirect(false);
    } catch (final Exception ex) {
      error(getLocalizedMessage("error", ex.getMessage()));
      log.error(ex.getMessage(), ex);
    }
  }

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
