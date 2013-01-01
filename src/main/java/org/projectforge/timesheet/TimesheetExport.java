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

package org.projectforge.timesheet;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.apache.poi.hssf.util.HSSFColor;
import org.projectforge.common.DateFormatType;
import org.projectforge.common.DateFormats;
import org.projectforge.common.DateHolder;
import org.projectforge.export.CellFormat;
import org.projectforge.export.ContentProvider;
import org.projectforge.export.ExportCell;
import org.projectforge.export.ExportColumn;
import org.projectforge.export.ExportRow;
import org.projectforge.export.ExportSheet;
import org.projectforge.export.ExportWorkbook;
import org.projectforge.export.I18nExportColumn;
import org.projectforge.export.PropertyMapping;
import org.projectforge.export.XlsContentProvider;
import org.projectforge.fibu.KundeDO;
import org.projectforge.fibu.ProjektDO;
import org.projectforge.fibu.kost.Kost2DO;
import org.projectforge.task.TaskNode;
import org.projectforge.task.TaskTree;
import org.projectforge.user.PFUserContext;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserGroupCache;
import org.projectforge.web.calendar.DateTimeFormatter;
import org.projectforge.web.common.OutputType;
import org.projectforge.web.task.TaskFormatter;

/**
 * For excel export.
 * 
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class TimesheetExport
{
  private class MyContentProvider extends XlsContentProvider
  {
    public MyContentProvider(ExportWorkbook workbook)
    {
      super(workbook);
    }

    @Override
    public void updateRowStyle(ExportRow row)
    {
      for (ExportCell cell : row.getCells()) {
        final CellFormat format = cell.ensureAndGetCellFormat();
        format.setFillForegroundColor(HSSFColor.WHITE.index);
        switch (row.getRowNum()) {
          case 0:
            format.setFont(FONT_HEADER);
            break;
          case 1:
            format.setFont(FONT_NORMAL_BOLD);
            // alignment = CellStyle.ALIGN_CENTER;
            break;
          default:
            format.setFont(FONT_NORMAL);
            if (row.getRowNum() % 2 == 0) {
              format.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
            }
            break;
        }
      }
    }

    @Override
    public ContentProvider newInstance()
    {
      return new MyContentProvider(this.workbook);
    }
  };

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TimesheetExport.class);

  private TaskTree taskTree;

  private TaskFormatter taskFormatter;

  private DateTimeFormatter dateTimeFormatter;

  private UserGroupCache userGroupCache;

  private enum Col
  {
    USER, KUNDE, PROJEKT, KOST2, WEEK_OF_YEAR, DAY_OF_WEEK, START_TIME, STOP_TIME, DURATION, HOURS, LOCATION, TASK_TITLE, REFERENCE, SHORT_DESCRIPTION, DESCRIPTION, TASK_PATH, ID;
  }

  /**
   * Exports the filtered list as table with almost all fields.
   */
  public byte[] export(List<TimesheetDO> list)
  {
    log.info("Exporting timesheet list.");
    final ExportWorkbook xls = new ExportWorkbook();
    final ContentProvider contentProvider = new MyContentProvider(xls);
    // create a default Date format and currency column
    xls.setContentProvider(contentProvider);

    final String sheetTitle = PFUserContext.getLocalizedString("timesheet.timesheets");
    ExportSheet sheet = xls.addSheet(sheetTitle);
    sheet.createFreezePane(8, 1);

    ExportColumn[] cols = new ExportColumn[] { //
    new I18nExportColumn(Col.USER, "timesheet.user", XlsContentProvider.LENGTH_USER),
        new I18nExportColumn(Col.KUNDE, "fibu.kunde", XlsContentProvider.LENGTH_STD),
        new I18nExportColumn(Col.PROJEKT, "fibu.projekt", XlsContentProvider.LENGTH_STD),
        new I18nExportColumn(Col.KOST2, "fibu.kost2", XlsContentProvider.LENGTH_KOSTENTRAEGER),
        new I18nExportColumn(Col.WEEK_OF_YEAR, "calendar.weekOfYearShortLabel", 4),
        new I18nExportColumn(Col.DAY_OF_WEEK, "calendar.dayOfWeekShortLabel", 4),
        new I18nExportColumn(Col.START_TIME, "timesheet.startTime", XlsContentProvider.LENGTH_DATETIME),
        new I18nExportColumn(Col.STOP_TIME, "timesheet.stopTime", XlsContentProvider.LENGTH_TIMESTAMP),
        new I18nExportColumn(Col.DURATION, "timesheet.duration", XlsContentProvider.LENGTH_DURATION),
        new I18nExportColumn(Col.HOURS, "hours", XlsContentProvider.LENGTH_DURATION),
        new I18nExportColumn(Col.LOCATION, "timesheet.location", XlsContentProvider.LENGTH_STD),
        new I18nExportColumn(Col.TASK_TITLE, "task.title", XlsContentProvider.LENGTH_STD),
        new I18nExportColumn(Col.REFERENCE, "task.reference", XlsContentProvider.LENGTH_STD),
        new I18nExportColumn(Col.SHORT_DESCRIPTION, "shortDescription", XlsContentProvider.LENGTH_STD),
        new I18nExportColumn(Col.DESCRIPTION, "timesheet.description", XlsContentProvider.LENGTH_EXTRA_LONG),
        new I18nExportColumn(Col.TASK_PATH, "task.path", XlsContentProvider.LENGTH_EXTRA_LONG),
        new I18nExportColumn(Col.ID, "id", XlsContentProvider.LENGTH_ID)};

    // column property names
    sheet.setColumns(cols);

    final ContentProvider sheetProvider = sheet.getContentProvider();
    sheetProvider.putFormat(Col.START_TIME, "yyyy-MM-dd HH:mm");
    sheetProvider.putFormat(Col.STOP_TIME, "HH:mm");
    sheetProvider.putFormat(Col.DURATION, "[h]:mm");
    sheetProvider.putFormat(Col.HOURS, "#,##0.00");
    sheetProvider.putFormat(Col.ID, "0");

    PropertyMapping mapping = new PropertyMapping();
    for (TimesheetDO timesheet : list) {
      final TaskNode node = taskTree.getTaskNodeById(timesheet.getTaskId());
      final PFUserDO user = userGroupCache.getUser(timesheet.getUserId());
      mapping.add(Col.USER, user.getFullname());
      final Kost2DO kost2 = timesheet.getKost2();
      String kost2Name = null;
      String projektName = null;
      String kundeName = null;
      if (kost2 != null) {
        kost2Name = kost2.getShortDisplayName();
        ProjektDO projekt = kost2.getProjekt();
        if (projekt != null) {
          projektName = projekt.getName();
          KundeDO kunde = projekt.getKunde();
          if (kunde != null) {
            kundeName = kunde.getName();
          } else {
          }
        }
      }
      mapping.add(Col.KOST2, kost2Name);
      mapping.add(Col.PROJEKT, projektName);
      mapping.add(Col.KUNDE, kundeName);
      mapping.add(Col.TASK_TITLE, node.getTask().getTitle());
      mapping.add(Col.TASK_PATH, taskFormatter.getTaskPath(timesheet.getTaskId(), null, true, OutputType.PLAIN));
      mapping.add(Col.WEEK_OF_YEAR, timesheet.getFormattedWeekOfYear());
      mapping.add(Col.DAY_OF_WEEK, dateTimeFormatter.getFormattedDate(timesheet.getStartTime(), DateFormats
          .getFormatString(DateFormatType.DAY_OF_WEEK_SHORT)));
      final DateHolder startTime = new DateHolder(timesheet.getStartTime());
      final DateHolder stopTime = new DateHolder(timesheet.getStopTime());
      mapping.add(Col.START_TIME, startTime);
      mapping.add(Col.STOP_TIME, stopTime);
      final BigDecimal seconds = new BigDecimal(timesheet.getDuration() / 1000); // Seconds
      final BigDecimal duration = seconds.divide(new BigDecimal(60 * 60 * 24), 8, RoundingMode.HALF_UP); // Fraction of day (24 hours)
      mapping.add(Col.DURATION, duration.doubleValue());
      final BigDecimal hours = seconds.divide(new BigDecimal(60 * 60), 2, RoundingMode.HALF_UP);
      mapping.add(Col.HOURS, hours.doubleValue());
      mapping.add(Col.LOCATION, timesheet.getLocation());
      mapping.add(Col.REFERENCE, node.getReference());
      mapping.add(Col.SHORT_DESCRIPTION, timesheet.getShortDescription());
      mapping.add(Col.DESCRIPTION, timesheet.getDescription());
      mapping.add(Col.ID, timesheet.getId());
      sheet.addRow(mapping.getMapping(), 0);
    }
    sheet.setZoom(3, 4); // 75%

    return xls.getAsByteArray();
  }

  public void setTaskTree(TaskTree taskTree)
  {
    this.taskTree = taskTree;
  }

  public void setTaskFormatter(TaskFormatter taskFormatter)
  {
    this.taskFormatter = taskFormatter;
  }

  public void setDateTimeFormatter(DateTimeFormatter dateTimeFormatter)
  {
    this.dateTimeFormatter = dateTimeFormatter;
  }

  public void setUserGroupCache(UserGroupCache userGroupCache)
  {
    this.userGroupCache = userGroupCache;
  }
}
