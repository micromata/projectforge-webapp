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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;

import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.projectforge.calendar.TimePeriod;
import org.projectforge.common.DateHelper;
import org.projectforge.common.DateHolder;
import org.projectforge.common.NumberHelper;
import org.projectforge.core.UserException;
import org.projectforge.export.ExportJFreeChart;
import org.projectforge.export.ExportWorkbook;
import org.projectforge.export.JFreeChartImageType;
import org.projectforge.scripting.GroovyResult;
import org.projectforge.scripting.ScriptDO;
import org.projectforge.scripting.ScriptDao;
import org.projectforge.scripting.ScriptParameter;
import org.projectforge.task.TaskDO;
import org.projectforge.task.TaskDao;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserDao;
import org.projectforge.web.fibu.ISelectCallerPage;
import org.projectforge.web.wicket.AbstractEditPage;
import org.projectforge.web.wicket.AbstractListPage;
import org.projectforge.web.wicket.AbstractStandardFormPage;
import org.projectforge.web.wicket.DownloadUtils;
import org.projectforge.web.wicket.WicketUtils;
import org.projectforge.web.wicket.bootstrap.GridBuilder;
import org.projectforge.web.wicket.components.ContentMenuEntryPanel;
import org.projectforge.web.wicket.components.SourceCodePanel;
import org.projectforge.web.wicket.flowlayout.DivTextPanel;
import org.projectforge.web.wicket.flowlayout.FieldsetPanel;

public class ScriptExecutePage extends AbstractStandardFormPage implements ISelectCallerPage
{
  private static final long serialVersionUID = -183858142939207911L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ScriptExecutePage.class);

  @SpringBean(name = "scriptDao")
  private ScriptDao scriptDao;

  @SpringBean(name = "taskDao")
  private TaskDao taskDao;

  @SpringBean(name = "userDao")
  private UserDao userDao;

  private ScriptExecuteForm form;

  private Integer id;

  protected FieldsetPanel scriptResultFieldsetPanel;

  private SourceCodePanel sourceCodePanel;

  private GridBuilder resultGridBuilder;

  protected transient GroovyResult groovyResult;

  @SuppressWarnings("serial")
  public ScriptExecutePage(final PageParameters parameters)
  {
    super(parameters);
    id = WicketUtils.getAsInteger(parameters, AbstractEditPage.PARAMETER_KEY_ID);
    final ContentMenuEntryPanel editMenuEntryPanel = new ContentMenuEntryPanel(getNewContentMenuChildId(), new Link<Object>("link") {
      @Override
      public void onClick()
      {
        storeRecentScriptCalls();
        final PageParameters params = new PageParameters();
        params.add(AbstractEditPage.PARAMETER_KEY_ID, String.valueOf(id));
        final ScriptEditPage editPage = new ScriptEditPage(params);
        editPage.setReturnToPage(ScriptExecutePage.this);
        form.refresh = true; // Force reload of parameter settings.
        setResponsePage(editPage);
      };
    }, getString("edit"));
    addContentMenuEntry(editMenuEntryPanel);
    form = new ScriptExecuteForm(this, loadScript());
    body.add(form);
    form.init();
    resultGridBuilder = form.newGridBuilder(body, "results");
    resultGridBuilder.newGridPanel();
    {
      scriptResultFieldsetPanel = new FieldsetPanel(resultGridBuilder.getPanel(), getString("scripting.script.result")) {
        /**
         * @see org.apache.wicket.Component#isVisible()
         */
        @Override
        public boolean isVisible()
        {
          return groovyResult != null;
        }
      }.supressLabelForWarning();
      final DivTextPanel resultPanel = new DivTextPanel(scriptResultFieldsetPanel.newChildId(), new Model<String>() {
        @Override
        public String getObject()
        {
          return groovyResult != null ? groovyResult.getResultAsHtmlString() : "";
        }
      });
      resultPanel.getLabel().setEscapeModelStrings(false);
      scriptResultFieldsetPanel.add(resultPanel);
    }
    body.add(sourceCodePanel = new SourceCodePanel("sourceCode"));
  }

  protected void refreshSourceCode()
  {
    sourceCodePanel.setCode(getScript().getScript(), groovyResult);
  }

  protected ScriptDO loadScript()
  {
    final ScriptDO script = scriptDao.getById(id);
    if (script == null) {
      log.error("Script with id '" + id + "' not found");
      throw new UserException("scripting.script.error.notFound");
    }
    return script;
  }

  @Override
  protected String getTitle()
  {
    return getString("scripting.script.execute");
  }

  protected void cancel()
  {
    final PageParameters params = new PageParameters();
    params.add(AbstractListPage.PARAMETER_HIGHLIGHTED_ROW, id);
    setResponsePage(ScriptListPage.class, params);
  }

  protected void execute()
  {
    final StringBuffer buf = new StringBuffer();
    buf.append("Execute script '").append(getScript().getName()).append("': ");
    if (form.scriptParameters != null) {
      boolean first = true;
      for (final ScriptParameter parameter : form.scriptParameters) {
        if (first == true) {
          first = false;
        } else {
          buf.append(',');
        }
        buf.append(parameter.getAsString());
      }
    }
    log.info(buf.toString());
    storeRecentScriptCalls();
    groovyResult = scriptDao.execute(getScript(), form.scriptParameters);
    refreshSourceCode();
    if (groovyResult.hasException() == true) {
      form.error(getLocalizedMessage("exception.groovyError", String.valueOf(groovyResult.getException())));
      return;
    }
    if (groovyResult.hasResult() == true) {
      final Object obj = groovyResult.getResult();
      if (obj instanceof ExportWorkbook == true) {
        exportExcel((ExportWorkbook) obj);
      } else if (obj instanceof ExportJFreeChart == true) {
        exportJFreeChart((ExportJFreeChart) obj);
      }
    }
  }

  private void exportExcel(final ExportWorkbook workbook)
  {
    final StringBuffer buf = new StringBuffer();
    buf.append("pf_report_");
    buf.append(DateHelper.getTimestampAsFilenameSuffix(new Date())).append(".xls");
    final String filename = buf.toString();
    final byte[] xls = workbook.getAsByteArray();
    if (xls == null || xls.length == 0) {
      log.error("Oups, xls has zero size. Filename: " + filename);
      return;
    }
    DownloadUtils.setDownloadTarget(xls, filename);
  }

  private void exportJFreeChart(final ExportJFreeChart exportJFreeChart)
  {
    final JFreeChart chart = exportJFreeChart.getJFreeChart();
    final int width = exportJFreeChart.getWidth();
    final int height = exportJFreeChart.getHeight();
    final StringBuffer buf = new StringBuffer();
    buf.append("pf_chart_");
    buf.append(DateHelper.getTimestampAsFilenameSuffix(new Date()));
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    try {
      if (exportJFreeChart.getImageType() == JFreeChartImageType.PNG) {
        ChartUtilities.writeChartAsPNG(out, chart, width, height);
        buf.append(".png");
      } else {
        ChartUtilities.writeChartAsJPEG(out, chart, width, height);
        buf.append(".jpg");
      }
    } catch (final IOException ex) {
      log.fatal("Exception encountered " + ex, ex);
    }
    DownloadUtils.setDownloadTarget(out.toByteArray(), buf.toString());
  }

  protected void storeRecentScriptCalls()
  {
    final RecentScriptCalls recents = getRecentScriptCalls();
    final ScriptCallData scriptCallData = new ScriptCallData(getScript().getName(), form.scriptParameters);
    recents.append(scriptCallData);
  }

  protected RecentScriptCalls getRecentScriptCalls()
  {
    RecentScriptCalls recentScriptCalls = (RecentScriptCalls) getUserPrefEntry(ScriptExecutePage.class.getName());
    if (recentScriptCalls == null) {
      recentScriptCalls = new RecentScriptCalls();
      putUserPrefEntry(ScriptExecutePage.class.getName(), recentScriptCalls, true);
    }
    return recentScriptCalls;
  }

  protected ScriptDO getScript()
  {
    return form.data;
  }

  public void cancelSelection(final String property)
  {
    // Do nothing.
  }

  public void select(final String property, final Object selectedValue)
  {
    if (property == null) {
      log.error("Oups, null property not supported for selection.");
      return;
    }
    final int colonPos = property.indexOf(':'); // For date:idx e. g. date:3 for 3rd parameter.
    final int dotPos = property.lastIndexOf('.'); // For quick select e. g. quickSelect:3.month
    String indexString = null;
    if (dotPos > 0) {
      indexString = property.substring(colonPos + 1, dotPos);
    } else {
      indexString = colonPos > 0 ? property.substring(colonPos + 1) : null;
    }
    final Integer idx = NumberHelper.parseInteger(indexString);
    if (property.startsWith("quickSelect:") == true) {
      final Date date = (Date) selectedValue;
      TimePeriod timePeriod = form.scriptParameters.get(idx).getTimePeriodValue();
      if (timePeriod == null) {
        timePeriod = new TimePeriod();
      }
      timePeriod.setFromDate(date);
      final DateHolder dateHolder = new DateHolder(date);
      if (property.endsWith(".month") == true) {
        dateHolder.setEndOfMonth();
      } else if (property.endsWith(".week") == true) {
        dateHolder.setEndOfWeek();
      } else {
        log.error("Property '" + property + "' not supported for selection.");
      }
      timePeriod.setToDate(dateHolder.getDate());
      form.scriptParameters.get(idx).setTimePeriodValue(timePeriod);
      form.datePanel1[idx].markModelAsChanged();
      form.datePanel2[idx].markModelAsChanged();
    } else if (property.startsWith("taskId:") == true) {
      final TaskDO task = taskDao.getById((Integer) selectedValue);
      form.scriptParameters.get(idx).setTask(task);
    } else if (property.startsWith("userId:") == true) {
      final PFUserDO user = userDao.getById((Integer) selectedValue);
      form.scriptParameters.get(idx).setUser(user);
    } else {
      log.error("Property '" + property + "' not supported for selection.");
    }
  }

  public void unselect(final String property)
  {
    // Do nothing.
  }
}
