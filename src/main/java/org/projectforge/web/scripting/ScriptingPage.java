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

package org.projectforge.web.scripting;

import java.io.File;
import java.io.InputStream;
import java.util.Collection;
import java.util.Date;
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
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.projectforge.common.DateHelper;
import org.projectforge.common.FileHelper;
import org.projectforge.core.ConfigXml;
import org.projectforge.export.ExportJFreeChart;
import org.projectforge.export.ExportWorkbook;
import org.projectforge.export.JFreeChartImageType;
import org.projectforge.fibu.kost.BusinessAssessment;
import org.projectforge.fibu.kost.reporting.Report;
import org.projectforge.fibu.kost.reporting.ReportGenerator;
import org.projectforge.fibu.kost.reporting.ReportGeneratorList;
import org.projectforge.fibu.kost.reporting.ReportStorage;
import org.projectforge.scripting.GroovyExecutor;
import org.projectforge.scripting.GroovyResult;
import org.projectforge.scripting.ScriptDao;
import org.projectforge.user.PFUserContext;
import org.projectforge.user.ProjectForgeGroup;
import org.projectforge.web.fibu.ReportObjectivesPage;
import org.projectforge.web.fibu.ReportScriptingStorage;
import org.projectforge.web.wicket.AbstractStandardFormPage;
import org.projectforge.web.wicket.DownloadUtils;
import org.projectforge.web.wicket.JFreeChartImage;
import org.projectforge.web.wicket.WicketUtils;
import org.projectforge.web.wicket.components.SourceCodePanel;

public class ScriptingPage extends AbstractStandardFormPage
{
  private static final long serialVersionUID = -1910145309628761662L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ScriptingPage.class);

  private transient ReportScriptingStorage reportScriptingStorage;

  @SpringBean(name = "scriptDao")
  private ScriptDao scriptDao;

  @SpringBean(name = "groovyExecutor")
  private GroovyExecutor groovyExecutor;

  protected GroovyResult groovyResult;

  private final ScriptingForm form;

  private SourceCodePanel sourceCodePanel;

  private Label availableScriptVariablesLabel;

  private WebMarkupContainer imageResultContainer;

  private transient Map<String, Object> scriptVariables;

  protected transient ReportStorage reportStorage;

  public ScriptingPage(final PageParameters parameters)
  {
    super(parameters);
    form = new ScriptingForm(this);
    body.add(form);
    form.init();
    initScriptVariables();
    body.add(imageResultContainer = (WebMarkupContainer) new WebMarkupContainer("imageResult").setVisible(false));
    body.add(sourceCodePanel = new SourceCodePanel("sourceCode"));
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
    final StringBuffer buf = new StringBuffer();
    buf.append("scriptResult"); // first available variable.
    for (final String key : set) {
      buf.append(", ").append(key);
    }
    if (availableScriptVariablesLabel == null) {
      body.add(availableScriptVariablesLabel = new Label("availableScriptVariables", buf.toString()));
    }
    scriptDao.addAliasForDeprecatedScriptVariables(scriptVariables);
    // buf = new StringBuffer();
    // boolean first = true;
    // for (final BusinessAssessmentRowConfig rowConfig : AccountingConfig.getInstance().getBusinessAssessmentConfig().getRows()) {
    // if (rowConfig.getId() == null) {
    // continue;
    // }
    // if (first == true) {
    // first = false;
    // } else {
    // buf.append(", ");
    // }
    // buf.append('r').append(rowConfig.getNo()).append(", ").append(rowConfig.getId());
    // }
    // if (businessAssessmentRowsVariablesLabel == null) {
    // body.add(businessAssessmentRowsVariablesLabel = new Label("businessAssessmentRowsVariables", buf.toString()));
    // }
  }

  @Override
  protected void onBeforeRender()
  {
    sourceCodePanel.setCode(getReportScriptingStorage().getGroovyScript(), groovyResult);
    super.onBeforeRender();
  }

  protected void execute()
  {
    accessChecker.checkIsLoggedInUserMemberOfGroup(ProjectForgeGroup.FINANCE_GROUP, ProjectForgeGroup.CONTROLLING_GROUP);
    accessChecker.checkRestrictedOrDemoUser();
    imageResultContainer.setVisible(false);
    ReportGeneratorList reportGeneratorList = new ReportGeneratorList();
    initScriptVariables();
    scriptVariables.put("reportStorage", getReportStorage());
    scriptVariables.put("reportScriptingStorage", getReportScriptingStorage());
    scriptVariables.put("reportList", reportGeneratorList);
    if (StringUtils.isNotBlank(getReportScriptingStorage().getGroovyScript()) == true) {
      groovyResult = groovyExecutor.execute(new GroovyResult(), getReportScriptingStorage().getGroovyScript(), scriptVariables);
      if (groovyResult.hasException() == true) {
        form.error(getLocalizedMessage("exception.groovyError", String.valueOf(groovyResult.getException())));
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
    accessChecker.checkRestrictedOrDemoUser();
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
          final StringBuffer buf = new StringBuffer();
          buf.append("report_").append(FileHelper.createSafeFilename(PFUserContext.getUser().getUsername(), 20)).append(".xls");
          final File file = new File(ConfigXml.getInstance().getWorkingDirectory(), buf.toString());
          fileUpload.writeTo(file);
          getReportScriptingStorage().setFilename(clientFileName, file.getAbsolutePath());
        } else {
          log.error("File extension not supported: " + clientFileName);
        }
      } catch (final Exception ex) {
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
   * Default report from reportStorage. Uses the current report and puts the business assessment values in parameter map.
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
    BusinessAssessment.putBusinessAssessmentRows(parameters, report.getBusinessAssessment());
    jasperReport(parameters, beanCollection);
  }

  private void jasperReport(final Map<String, Object> parameters, final Collection< ? > beanCollection)
  {
    try {
      final JasperReport jasperReport = getReportScriptingStorage().getJasperReport();
      final JasperPrint jp = JasperFillManager.fillReport(jasperReport, parameters, new JRBeanCollectionDataSource(beanCollection));
      final JasperPrint jasperPrint = jp;
      final StringBuffer buf = new StringBuffer();
      buf.append("pf_report_");
      buf.append(DateHelper.getTimestampAsFilenameSuffix(new Date())).append(".pdf");
      final String filename = buf.toString();
      final Response response = getResponse();
      ((WebResponse) response).setAttachmentHeader(filename);
      WicketUtils.getHttpServletResponse(response).setContentType(DownloadUtils.getContentType(filename));
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
      buf.append(DateHelper.getTimestampAsFilenameSuffix(new Date())).append(".xls");
      final String filename = buf.toString();
      final Response response = getResponse();
      ((WebResponse) response).setAttachmentHeader(filename);
      WicketUtils.getHttpServletResponse(response).setContentType(DownloadUtils.getContentType(filename));
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
      final StringBuffer buf = new StringBuffer();
      buf.append("pf_chart_");
      buf.append(DateHelper.getTimestampAsFilenameSuffix(new Date()));
      final Response response = getResponse();
      if (exportJFreeChart.getImageType() == JFreeChartImageType.PNG) {
        ChartUtilities.writeChartAsPNG(response.getOutputStream(), chart, width, height);
        buf.append(".png");
      } else {
        ChartUtilities.writeChartAsJPEG(response.getOutputStream(), chart, width, height);
        buf.append(".jpg");
      }
      final String filename = buf.toString();
      final JFreeChartImage image = new JFreeChartImage("image", chart, exportJFreeChart.getImageType(), width, height);
      image.add(AttributeModifier.replace("width", String.valueOf(width)));
      image.add(AttributeModifier.replace("height", String.valueOf(height)));
      imageResultContainer.removeAll();
      imageResultContainer.add(image).setVisible(true);
      ((WebResponse) response).setAttachmentHeader(filename);
      ((WebResponse) response).setContentType(DownloadUtils.getContentType(filename));
      response.getOutputStream().flush();
    } catch (final Exception ex) {
      error(getLocalizedMessage("error", ex.getMessage()));
      log.error(ex.getMessage(), ex);
    }
  }

  /**
   * @return Any existing user storage or null if not exist (wether in class nor in user's session).
   */
  protected ReportStorage getReportStorage()
  {
    if (reportStorage != null) {
      return reportStorage;
    }
    return (ReportStorage) getUserPrefEntry(ReportObjectivesPage.KEY_REPORT_STORAGE);
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
