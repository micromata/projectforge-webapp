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

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.model.PropertyModel;
import org.projectforge.gantt.GanttAccess;
import org.projectforge.gantt.GanttChartDO;
import org.projectforge.gantt.GanttChartSettings;
import org.projectforge.gantt.GanttTaskImpl;
import org.projectforge.task.TaskDO;
import org.projectforge.user.PFUserContext;
import org.projectforge.user.PFUserDO;
import org.projectforge.web.task.TaskSelectPanel;
import org.projectforge.web.user.UserSelectPanel;
import org.projectforge.web.wicket.AbstractEditForm;
import org.projectforge.web.wicket.WicketUtils;
import org.projectforge.web.wicket.components.DatePanel;
import org.projectforge.web.wicket.components.DatePanelSettings;
import org.projectforge.web.wicket.components.LabelValueChoiceRenderer;
import org.projectforge.web.wicket.components.MaxLengthTextField;
import org.projectforge.web.wicket.components.MinMaxNumberField;
import org.projectforge.web.wicket.components.RequiredMaxLengthTextField;

public class GanttChartEditForm extends AbstractEditForm<GanttChartDO, GanttChartEditPage>
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(GanttChartEditForm.class);

  private static final long serialVersionUID = 3199820655287750358L;

  static final String EXPORT_JPG = "JPG";

  static final String EXPORT_MS_PROJECT_XML = "MSP-XML";

  static final String EXPORT_MS_PROJECT_MPX = "MPX";

  static final String EXPORT_PDF = "PDF";

  static final String EXPORT_PNG = "PNG";

  static final String EXPORT_PROJECTFORGE = "PROJECTFORGE";

  static final String EXPORT_SVG = "SVG";

  private Button redrawButton;

  DatePanel fromDatePanel;

  DatePanel toDatePanel;

  private String exportFormat;

  public GanttChartEditForm(final GanttChartEditPage parentPage, final GanttChartDO data)
  {
    super(parentPage, data);
    if (isNew() == true) {
      if (data.getOwner() == null) {
        data.setOwner(PFUserContext.getUser());
      }
      if (StringUtils.isEmpty(data.getName()) == true) {
        data.setName("MyChart");
      }
    }
    if (data.getReadAccess() == null) {
      data.setReadAccess(GanttAccess.OWNER);
    }
    if (data.getWriteAccess() == null) {
      data.setWriteAccess(GanttAccess.OWNER);
    }
  }

  @SuppressWarnings("serial")
  @Override
  protected void init()
  {
    super.init();
    add(new RequiredMaxLengthTextField("name", new PropertyModel<String>(data, "name")));
    final TaskSelectPanel taskSelectPanel = new TaskSelectPanel("task", new PropertyModel<TaskDO>(data, "task"), parentPage, "taskId") {
      @Override
      protected void selectTask(final TaskDO task)
      {
        super.selectTask(task);
        parentPage.refresh(); // Task was changed. Therefore update the kost2 list.
      }
    };
    add(taskSelectPanel);
    taskSelectPanel.init();
    taskSelectPanel.setRequired(true);
    final UserSelectPanel userSelectPanel = new UserSelectPanel("owner", new PropertyModel<PFUserDO>(data, "owner"), parentPage, "ownerId");
    add(userSelectPanel);
    userSelectPanel.init();
    taskSelectPanel.setRequired(true);
    {
      // read-access:
      final LabelValueChoiceRenderer<GanttAccess> readAccessChoiceRenderer = new LabelValueChoiceRenderer<GanttAccess>(this,
          GanttAccess.values());
      @SuppressWarnings("unchecked")
      final DropDownChoice readAccessChoice = new DropDownChoice("readAccess", new PropertyModel(getData(), "readAccess"),
          readAccessChoiceRenderer.getValues(), readAccessChoiceRenderer);
      readAccessChoice.setNullValid(false);
      add(readAccessChoice);
      // write-access:
      final LabelValueChoiceRenderer<GanttAccess> writeAccessChoiceRenderer = new LabelValueChoiceRenderer<GanttAccess>(this,
          GanttAccess.values());
      @SuppressWarnings("unchecked")
      final DropDownChoice writeAccessChoice = new DropDownChoice("writeAccess", new PropertyModel(getData(), "writeAccess"),
          writeAccessChoiceRenderer.getValues(), writeAccessChoiceRenderer);
      writeAccessChoice.setNullValid(false);
      add(writeAccessChoice);
    }
    add(new MaxLengthTextField("title", new PropertyModel<String>(getSettings(), "title"), 100));
    add(new MinMaxNumberField<Double>("totalLabelWidth", new PropertyModel<Double>(data.getStyle(), "totalLabelWidth"), 10.0, 10000.0));
    add(new MinMaxNumberField<Integer>("width", new PropertyModel<Integer>(data.getStyle(), "width"), 100, 10000));
    add(new CheckBox("relativeTimeValues", new PropertyModel<Boolean>(data.getStyle(), "relativeTimeValues")));
    add(new CheckBox("showToday", new PropertyModel<Boolean>(data.getStyle(), "showToday")));
    add(new CheckBox("showCompletion", new PropertyModel<Boolean>(data.getStyle(), "showCompletion")));
    fromDatePanel = new DatePanel("fromDate", new PropertyModel<Date>(getSettings(), "fromDate"), DatePanelSettings.get()
        .withSelectProperty("fromDate"));
    add(fromDatePanel);
    toDatePanel = new DatePanel("toDate", new PropertyModel<Date>(getSettings(), "toDate"), DatePanelSettings.get().withSelectProperty(
        "toDate"));
    add(toDatePanel);
    add(new CheckBox("showOnlyVisibles", new PropertyModel<Boolean>(getSettings(), "showOnlyVisibles")) {
      @Override
      protected boolean wantOnSelectionChangedNotifications()
      {
        // Submit form after toggling the check box.
        return true;
      }
    });
    add(new CheckBox("showNonWorkingDays", new PropertyModel<Boolean>(data.getStyle(), "showNonWorkingDays")));
    {
      final LabelValueChoiceRenderer<String> exportFormatChoiceRenderer = new LabelValueChoiceRenderer<String>();
      exportFormatChoiceRenderer.addValue(EXPORT_JPG, getString("gantt.export.jpg"));
      exportFormatChoiceRenderer.addValue(EXPORT_MS_PROJECT_MPX, getString("gantt.export.msproject.mpx"));
      exportFormatChoiceRenderer.addValue(EXPORT_MS_PROJECT_XML, getString("gantt.export.msproject.xml"));
      exportFormatChoiceRenderer.addValue(EXPORT_PDF, getString("gantt.export.pdf"));
      exportFormatChoiceRenderer.addValue(EXPORT_PNG, getString("gantt.export.png"));
      exportFormatChoiceRenderer.addValue(EXPORT_PROJECTFORGE, getString("gantt.export.projectforge"));
      exportFormatChoiceRenderer.addValue(EXPORT_SVG, getString("gantt.export.svg"));
      @SuppressWarnings("unchecked")
      final DropDownChoice exportFormatChoice = new DropDownChoice("exportFormat", new PropertyModel(this, "exportFormat"),
          exportFormatChoiceRenderer.getValues(), exportFormatChoiceRenderer);
      exportFormatChoice.setNullValid(false);
      add(exportFormatChoice);

      // final SingleButtonPanel exportButtonPanel = new SingleButtonPanel("export", new Button("button", new Model<String>(
      // getString("export"))) {
      // @Override
      // public final void onSubmit()
      // {
      // parentPage.export(exportFormat);
      // }
      // });
      // add(exportButtonPanel);
    }
    final SubmitLink addPositionButton = new SubmitLink("addActivity") {
      @Override
      public void onSubmit()
      {
        final GanttTaskImpl root = (GanttTaskImpl) parentPage.ganttChartData.getRootObject();
        final Integer nextId = root.getNextId();
        root.addChild(new GanttTaskImpl(nextId).setVisible(true).setTitle(getString("untitled")));
        final GanttChartEditTreeTablePanel tablePanel = parentPage.ganttChartEditTreeTablePanel;
        final Set<Serializable> openNodes = tablePanel.getOpenNodes();
        tablePanel.refreshTreeTable();
        tablePanel.setOpenNodes(openNodes);
        parentPage.refresh();
      };
    };
    add(addPositionButton);
    addPositionButton.add(WicketUtils.getAddRowImage("addImage", getResponse(), getString("gantt.action.newActivity")));
  }

  // @Override
  // @SuppressWarnings("serial")
  // protected void addButtonPanel()
  // {
  // final Fragment buttonFragment = new Fragment("buttonPanel", "buttonFragment", this);
  // buttonFragment.setRenderBodyOnly(true);
  // buttonCell.add(buttonFragment);
  // redrawButton = new Button("button", new Model<String>(getString("redraw"))) {
  // @Override
  // public final void onSubmit()
  // {
  // parentPage.refresh();
  // }
  // };
  // final SingleButtonPanel redrawButtonPanel = new SingleButtonPanel("redraw", redrawButton);
  // buttonFragment.add(redrawButtonPanel);
  // final TooltipImage redrawInfoImage = new TooltipImage("redrawInfo", getResponse(), WebConstants.IMAGE_INFO,
  // getString("gantt.tooltip.returnKeyCallsRedraw"));
  // buttonFragment.add(redrawInfoImage);
  // final SingleButtonPanel cloneButtonPanel = new SingleButtonPanel("clone", new Button("button", new Model<String>(getString("clone"))) {
  // @Override
  // public final void onSubmit()
  // {
  // getData().setId(null);
  // this.setVisible(false);
  // updateButtonVisibility();
  // }
  // });
  // if (isNew() == true || getData().isDeleted() == true) {
  // // Show clone button only for existing gantt diagrams.
  // cloneButtonPanel.setVisible(false);
  // }
  // buttonFragment.add(cloneButtonPanel);
  // }

  @Override
  public void updateButtonVisibility()
  {
    super.updateButtonVisibility();
    setDefaultButton(redrawButton);
  }

  @Override
  protected void markDefaultButtons()
  {
    // redrawButton.add(WebConstants.BUTTON_CLASS_DEFAULT);
  }

  /**
   * @return the exportFormat
   */
  public String getExportFormat()
  {

    if (exportFormat == null) {
      exportFormat = (String) parentPage.getUserPrefEntry(this.getClass().getName() + ":exportFormat");
    }
    if (exportFormat == null) {
      exportFormat = EXPORT_PDF;
    }

    return exportFormat;
  }

  /**
   * @param exportFormat the exportFormat to set
   */
  public void setExportFormat(final String exportFormat)
  {
    this.exportFormat = exportFormat;
    parentPage.putUserPrefEntry(this.getClass().getName() + ":exportFormat", this.exportFormat, true);
  }

  GanttChartSettings getSettings()
  {
    return getData().getSettings();
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }
}
