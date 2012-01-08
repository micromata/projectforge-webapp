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

package org.projectforge.web.gantt;

import java.util.Date;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.log4j.Logger;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.common.FileHelper;
import org.projectforge.common.NumberHelper;
import org.projectforge.gantt.ExportMSProject;
import org.projectforge.gantt.GanttChart;
import org.projectforge.gantt.GanttChartDO;
import org.projectforge.gantt.GanttChartDao;
import org.projectforge.gantt.GanttChartData;
import org.projectforge.gantt.GanttChartSettings;
import org.projectforge.gantt.GanttChartStyle;
import org.projectforge.gantt.GanttTask;
import org.projectforge.renderer.BatikImageRenderer;
import org.projectforge.renderer.ImageFormat;
import org.projectforge.web.fibu.ISelectCallerPage;
import org.projectforge.web.wicket.AbstractBasePage;
import org.projectforge.web.wicket.AbstractEditPage;
import org.projectforge.web.wicket.BatikImage;
import org.projectforge.web.wicket.DownloadUtils;
import org.projectforge.web.wicket.EditPage;
import org.projectforge.web.wicket.WicketUtils;
import org.w3c.dom.Document;

@EditPage(defaultReturnPage = GanttChartListPage.class)
public class GanttChartEditPage extends AbstractEditPage<GanttChartDO, GanttChartEditForm, GanttChartDao> implements ISelectCallerPage
{
  private static final long serialVersionUID = 6994391085420314366L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(GanttChartEditPage.class);

  public static final String PARAM_KEY_TASK = "task";

  @SpringBean(name = "ganttChartDao")
  private GanttChartDao ganttChartDao;

  GanttChartEditTreeTablePanel ganttChartEditTreeTablePanel;

  GanttChartData ganttChartData;

  private WebComponent ganttImage;

  Fragment bottomPanelFragment;

  public GanttChartEditPage(final PageParameters parameters)
  {
    super(parameters, "gantt");
    init();
    if (isNew() == true) {
      final Integer taskId = parameters.getAsInteger(PARAM_KEY_TASK);
      if (taskId != null) {
        getBaseDao().setTask(getData(), taskId);
      }
    }
    ganttChartEditTreeTablePanel = new GanttChartEditTreeTablePanel("ganttTree", this.form, getData());
    form.add(ganttChartEditTreeTablePanel);
    ganttChartEditTreeTablePanel.init();
    refresh();
    ganttChartEditTreeTablePanel.setOpenNodes(getSettings().getOpenNodes());
  }

  void export(final String exportFormat)
  {
    final GanttChart ganttChart = createGanttChart();
    if (ganttChart == null) {
      return;
    }
    ImageFormat imageFormat = null;
    final String suffix;
    if (GanttChartEditForm.EXPORT_JPG.equals(exportFormat) == true) {
      suffix = ".jpg";
      imageFormat = ImageFormat.JPEG;
    } else if (GanttChartEditForm.EXPORT_MS_PROJECT_MPX.equals(exportFormat) == true) {
      suffix = ".mpx";
    } else if (GanttChartEditForm.EXPORT_MS_PROJECT_XML.equals(exportFormat) == true) {
      suffix = ".xml";
    } else if (GanttChartEditForm.EXPORT_PDF.equals(exportFormat) == true) {
      suffix = ".pdf";
      imageFormat = ImageFormat.PDF;
    } else if (GanttChartEditForm.EXPORT_PNG.equals(exportFormat) == true) {
      suffix = ".png";
      imageFormat = ImageFormat.PNG;
    } else if (GanttChartEditForm.EXPORT_PROJECTFORGE.equals(exportFormat) == true) {
      suffix = ".xml";
    } else if (GanttChartEditForm.EXPORT_SVG.equals(exportFormat) == true) {
      suffix = ".svg";
      imageFormat = ImageFormat.SVG;
    } else {
      log.error("Oups, exportFormat '" + exportFormat + "' not supported. Assuming png format.");
      suffix = ".png";
      imageFormat = ImageFormat.PNG;
    }
    final String filename = FileHelper.createSafeFilename(getData().getName(), suffix, 50, true);
    final byte[] content;
    if (imageFormat != null) {
      final Document document = ganttChart.create();
      content = BatikImageRenderer.getByteArray(document, ganttChart.getWidth(), imageFormat);
      DownloadUtils.setDownloadTarget(content, filename);
    } else {
      final String type;
      if (GanttChartEditForm.EXPORT_MS_PROJECT_MPX.equals(exportFormat) == true) {
        content = ExportMSProject.exportMpx(ganttChart);
        type = DownloadUtils.TYPE_MS_PROJECT;
      } else if (GanttChartEditForm.EXPORT_MS_PROJECT_XML.equals(exportFormat) == true) {
        content = ExportMSProject.exportXml(ganttChart);
        type = DownloadUtils.TYPE_MS_PROJECT;
      } else {
        content = ganttChartDao.exportAsXml(ganttChart, true).getBytes();
        type = DownloadUtils.TYPE_XML;
      }
      DownloadUtils.setDownloadTarget(content, filename, type);
    }
  }

  @Override
  public AbstractBasePage onSaveOrUpdate()
  {
    getSettings().setOpenNodes(ganttChartEditTreeTablePanel.getOpenNodes());
    getBaseDao().writeGanttObjects(getData(), ganttChartData.getRootObject());
    return null;
  }

  @Override
  protected void addBottomPanel()
  {
    bottomPanelFragment = new Fragment("bottomPanel", "bottomPanelFragment", this);
    bottomPanelFragment.setRenderBodyOnly(true);
    body.add(bottomPanelFragment);
  }

  private GanttChart createGanttChart()
  {
    if (ganttChartData == null) {
      return null;
    }
    ganttChartData.getRootObject().sortChildren();
    final GanttChart ganttChart = new GanttChart(ganttChartData.getRootObject(), getGanttChartStyle(), getSettings(), getData().getName());
    // chart.getRootObject().recalculate();
    return ganttChart;
  }

  protected void redraw()
  {
    if (ganttImage != null) {
      bottomPanelFragment.remove(ganttImage);
    }
    final GanttChart ganttChart = createGanttChart();
    if (ganttChart != null) {
      final Document document = ganttChart.create();
      if (document != null) {
        ganttImage = new BatikImage("ganttChart", document, getGanttChartStyle().getWidth());
      } else {
        ganttImage = WicketUtils.getInvisibleDummyImage("ganttChart", getResponse());
      }
    } else {
      ganttImage = WicketUtils.getInvisibleDummyImage("ganttChart", getResponse());
    }
    bottomPanelFragment.add(ganttImage);
  }

  private GanttChartStyle getGanttChartStyle()
  {
    return getData().getStyle();
  }

  private GanttChartSettings getSettings()
  {
    return getData().getSettings();
  }

  public void cancelSelection(final String property)
  {
    // Do nothing.
  }

  public void select(final String property, final Object selectedValue)
  {
    if ("taskId".equals(property) == true) {
      final Integer id;
      if (selectedValue instanceof String) {
        id = NumberHelper.parseInteger((String) selectedValue);
      } else {
        id = (Integer) selectedValue;
      }
      if (ganttChartData == null || ObjectUtils.equals(id, ganttChartData.getRootObject().getId()) == false) {
        ganttChartData = null; // Force refresh.
        ganttChartEditTreeTablePanel.refreshTreeTable();
      }
      getBaseDao().setTask(getData(), id);
      refresh();
    } else if ("ownerId".equals(property) == true) {
      final Integer id;
      if (selectedValue instanceof String) {
        id = NumberHelper.parseInteger((String) selectedValue);
      } else {
        id = (Integer) selectedValue;
      }
      getBaseDao().setOwner(getData(), id);
    } else if ("fromDate".equals(property) == true) {
      final Date date = (Date) selectedValue;
      getSettings().setFromDate(date);
      form.fromDatePanel.markModelAsChanged();
    } else if ("toDate".equals(property) == true) {
      final Date date = (Date) selectedValue;
      getSettings().setToDate(date);
      form.toDatePanel.markModelAsChanged();
    } else {
      log.error("Property '" + property + "' not supported for selection.");
    }
  }

  public void unselect(final String property)
  {
    throw new UnsupportedOperationException();
  };

  protected void refresh()
  {
    if (ganttChartData == null && getData().getTaskId() != null) {
      ganttChartData = getBaseDao().readGanttObjects(getData());
      final GanttTask rootObject = ganttChartData.getRootObject();
      if (rootObject != null && CollectionUtils.isNotEmpty(rootObject.getChildren()) == true && isNew() == true) {
        // For new charts set all children on level one as visible.
        for (final GanttTask child : rootObject.getChildren()) {
          child.setVisible(true);
        }
      }
    }
    ganttChartEditTreeTablePanel.setGanttChartData(ganttChartData).refresh();
    redraw();
  }

  @Override
  protected GanttChartDao getBaseDao()
  {
    return ganttChartDao;
  }

  @Override
  protected GanttChartEditForm newEditForm(AbstractEditPage< ? , ? , ? > parentPage, GanttChartDO data)
  {
    return new GanttChartEditForm(this, data);
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }
}
