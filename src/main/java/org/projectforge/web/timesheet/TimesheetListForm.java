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

package org.projectforge.web.timesheet;

import java.util.Date;

import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.task.TaskDO;
import org.projectforge.task.TaskTree;
import org.projectforge.timesheet.TimesheetDO;
import org.projectforge.timesheet.TimesheetFilter;
import org.projectforge.user.PFUserDO;
import org.projectforge.web.calendar.DateTimeFormatter;
import org.projectforge.web.calendar.QuickSelectPanel;
import org.projectforge.web.task.TaskSelectPanel;
import org.projectforge.web.user.UserSelectPanel;
import org.projectforge.web.wicket.AbstractListForm;
import org.projectforge.web.wicket.WebConstants;
import org.projectforge.web.wicket.WicketUtils;
import org.projectforge.web.wicket.components.DatePanel;
import org.projectforge.web.wicket.components.DatePanelSettings;
import org.projectforge.web.wicket.components.LabelValueChoiceRenderer;
import org.projectforge.web.wicket.components.TooltipImage;


public class TimesheetListForm extends AbstractListForm<TimesheetListFilter, TimesheetListPage>
{
  private static final long serialVersionUID = 3167681159669386691L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TimesheetListForm.class);

  @SpringBean(name = "taskTree")
  private TaskTree taskTree;

  @SpringBean(name = "dateTimeFormatter")
  private DateTimeFormatter dateTimeFormatter;

  protected DatePanel startDatePanel;

  protected DatePanel stopDatePanel;

  private TaskSelectPanel taskSelectPanel;

  private String exportFormat;

  @SuppressWarnings("serial")
  @Override
  protected void init()
  {
    super.init();
    final TimesheetFilter filter = getSearchFilter();
    filterContainer.add(new CheckBox("longFormatCheckBox", new PropertyModel<Boolean>(getSearchFilter(), "longFormat")));
    filterContainer.add(new CheckBox("recursiveCheckBox", new PropertyModel<Boolean>(getSearchFilter(), "recursive")));
    filterContainer.add(new CheckBox("deletedCheckBox", new PropertyModel<Boolean>(getSearchFilter(), "deleted")));
    filterContainer.add(new CheckBox("markedCheckBox", new PropertyModel<Boolean>(getSearchFilter(), "marked")));
    startDatePanel = new DatePanel("startDate", new PropertyModel<Date>(getSearchFilter(), "startTime"), DatePanelSettings.get()
        .withCallerPage(parentPage).withSelectPeriodMode(true));
    filterContainer.add(startDatePanel);
    stopDatePanel = new DatePanel("stopDate", new PropertyModel<Date>(getSearchFilter(), "stopTime"), DatePanelSettings.get()
        .withCallerPage(parentPage).withSelectPeriodMode(true));
    filterContainer.add(stopDatePanel);
    {
      final SubmitLink clearPeriodButton = new SubmitLink("clearPeriod") {
        public void onSubmit()
        {
          getSearchFilter().setStartTime(null);
          getSearchFilter().setStopTime(null);
          clearInput();
          parentPage.refresh();
        };
      };
      filterContainer.add(clearPeriodButton);
      clearPeriodButton.add(new TooltipImage("clearPeriodImage", getResponse(), WebConstants.IMAGE_DATE_UNSELECT,
          getString("calendar.tooltip.unselectPeriod")));
    }
    final QuickSelectPanel quickSelectPanel = new QuickSelectPanel("quickSelect", parentPage, "quickSelect", startDatePanel);
    filterContainer.add(quickSelectPanel);
    quickSelectPanel.init();

    filterContainer.add(new Label("calendarWeeks", new Model<String>() {
      @Override
      public String getObject()
      {
        return WicketUtils.getCalendarWeeks(TimesheetListForm.this, filter.getStartTime(), filter.getStopTime());
      }
    }).setRenderBodyOnly(true));
    filterContainer.add(new Label("datesAsUTC", new Model<String>() {
      @Override
      public String getObject()
      {
        return WicketUtils.getUTCDates(filter.getStartTime(), filter.getStopTime());
      }
    }));

    final Label totalDurationLabel = new Label("totalDuration", new Model<String>() {
      @Override
      public String getObject()
      {
        long duration = 0;
        if (parentPage.getList() != null) {
          for (TimesheetDO sheet : parentPage.getList()) {
            duration += sheet.getDuration();
          }
        }
        return dateTimeFormatter.getPrettyFormattedDuration(duration);
      }
    });
    totalDurationLabel.setRenderBodyOnly(true);
    filterContainer.add(totalDurationLabel);

    taskSelectPanel = new TaskSelectPanel("task", new Model<TaskDO>() {
      @Override
      public TaskDO getObject()
      {
        return taskTree.getTaskById(filter.getTaskId());
      }
    }, parentPage, "taskId") {
      @Override
      protected void selectTask(final TaskDO task)
      {
        super.selectTask(task);
        if (task != null) {
          getSearchFilter().setTaskId(task.getId());
        }
        parentPage.refresh();
      }
    };
    filterContainer.add(taskSelectPanel);
    taskSelectPanel.setEnableLinks(true);
    taskSelectPanel.init();
    taskSelectPanel.setRequired(false);

    UserSelectPanel userSelectPanel = new UserSelectPanel("user", new Model<PFUserDO>() {
      @Override
      public PFUserDO getObject()
      {
        return userGroupCache.getUser(filter.getUserId());
      }
    }, parentPage, "userId");
    filterContainer.add(userSelectPanel);
    userSelectPanel.setDefaultFormProcessing(false);
    userSelectPanel.init();

    {
      final SubmitLink exportPDFButton = new SubmitLink("exportPDF") {
        public void onSubmit()
        {
          parentPage.exportPDF();
        };
      };
      filterContainer.add(exportPDFButton);
      exportPDFButton
          .add(new TooltipImage("exportPDFImage", getResponse(), WebConstants.IMAGE_EXPORT_PDF, getString("tooltip.export.pdf")));
    }
    {
      final SubmitLink exportExcelButton = new SubmitLink("exportExcel") {
        public void onSubmit()
        {
          parentPage.exportExcel();
        };
      };
      filterContainer.add(exportExcelButton);
      exportExcelButton.add(new TooltipImage("exportExcelImage", getResponse(), WebConstants.IMAGE_EXPORT_EXCEL,
          getString("tooltip.export.excel")));
    }
    final LabelValueChoiceRenderer<String> exportFormatChoiceRenderer = new LabelValueChoiceRenderer<String>();
    exportFormatChoiceRenderer.addValue("Micromata", "Micromata");
    @SuppressWarnings("unchecked")
    final DropDownChoice exportFormatChoice = new DropDownChoice("exportFormat", new PropertyModel(this, "exportFormat"),
        exportFormatChoiceRenderer.getValues(), exportFormatChoiceRenderer);
    exportFormatChoice.setNullValid(false);
    filterContainer.add(exportFormatChoice);
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
      exportFormat = "Micromata";
    }

    return exportFormat;
  }

  /**
   * @param exportFormat the exportFormat to set
   */
  public void setExportFormat(String exportFormat)
  {
    this.exportFormat = exportFormat;
    parentPage.putUserPrefEntry(this.getClass().getName() + ":exportFormat", this.exportFormat, true);
  }

  @Override
  protected void validation()
  {
    if (parentPage.isMassUpdateMode() == true) {

    } else {
      final TimesheetFilter filter = getSearchFilter();
      startDatePanel.validate();
      stopDatePanel.validate();
      final Date from = startDatePanel.getConvertedInput();
      final Date to = stopDatePanel.getConvertedInput();
      if (from == null && to == null && filter.getTaskId() == null) {
        addComponentError(startDatePanel, "timesheet.error.filter.needMore");
      } else if (from != null && to != null && from.after(to) == true) {
        addComponentError(stopDatePanel, "timesheet.error.startTimeAfterStopTime");
      }
    }
  }

  public TimesheetListForm(TimesheetListPage parentPage)
  {
    super(parentPage);
  }

  @Override
  protected TimesheetListFilter newSearchFilterInstance()
  {
    return new TimesheetListFilter();
  }

  @Override
  protected boolean isFilterVisible()
  {
    return parentPage.isMassUpdateMode() == false;
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }
}
