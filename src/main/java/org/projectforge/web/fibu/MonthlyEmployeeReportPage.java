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

package org.projectforge.web.fibu;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.wicket.PageParameters;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.common.NumberHelper;
import org.projectforge.fibu.KostFormatter;
import org.projectforge.fibu.KundeDO;
import org.projectforge.fibu.MonthlyEmployeeReport;
import org.projectforge.fibu.MonthlyEmployeeReportDao;
import org.projectforge.fibu.MonthlyEmployeeReportEntry;
import org.projectforge.fibu.MonthlyEmployeeReportWeek;
import org.projectforge.fibu.ProjektDO;
import org.projectforge.fibu.MonthlyEmployeeReport.Kost2Row;
import org.projectforge.fibu.kost.Kost1DO;
import org.projectforge.fibu.kost.Kost1Dao;
import org.projectforge.fibu.kost.Kost2ArtDO;
import org.projectforge.fibu.kost.Kost2DO;
import org.projectforge.renderer.PdfRenderer;
import org.projectforge.task.TaskDO;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserDao;
import org.projectforge.user.UserGroupCache;
import org.projectforge.web.calendar.DateTimeFormatter;
import org.projectforge.web.common.OutputType;
import org.projectforge.web.task.TaskFormatter;
import org.projectforge.web.timesheet.TimesheetListPage;
import org.projectforge.web.wicket.AbstractSecuredPage;
import org.projectforge.web.wicket.DownloadUtils;
import org.projectforge.web.wicket.WicketUtils;

public class MonthlyEmployeeReportPage extends AbstractSecuredPage implements ISelectCallerPage
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MonthlyEmployeeReportPage.class);

  private static final String USER_PREF_KEY_FILTER = "monthlyEmployeeReportFilter";

  @SpringBean(name = "userDao")
  private UserDao userDao;

  private MonthlyEmployeeReportForm form;

  private MonthlyEmployeeReport report;

  private WebMarkupContainer reportContainer;

  @SpringBean(name = "monthlyEmployeeReportDao")
  private MonthlyEmployeeReportDao monthlyEmployeeReportDao;

  @SpringBean(name = "pdfRenderer")
  private PdfRenderer pdfRenderer;

  @SpringBean(name = "kost1Dao")
  private Kost1Dao kost1Dao;

  @SpringBean(name = "dateTimeFormatter")
  private DateTimeFormatter dateTimeFormatter;

  @SpringBean(name = "taskFormatter")
  private TaskFormatter taskFormatter;

  @SpringBean(name = "userGroupCache")
  private UserGroupCache userGroupCache;

  public MonthlyEmployeeReportPage(final PageParameters parameters)
  {
    super(parameters);
    body.add(new FeedbackPanel("feedback").setOutputMarkupId(true));
    form = new MonthlyEmployeeReportForm(this);
    if (form.filter == null) {
      form.filter = (MonthlyEmployeeReportFilter) getUserPrefEntry(MonthlyEmployeeReportFilter.class, USER_PREF_KEY_FILTER);
    }
    if (form.filter == null) {
      form.filter = new MonthlyEmployeeReportFilter();
      putUserPrefEntry(USER_PREF_KEY_FILTER, form.filter, true);
    }
    if (form.filter.getUser() == null) {
      form.filter.setUser(getUser());
    }
    body.add(form);
    form.init();
  }

  @Override
  public void onBeforeRender()
  {
    if (reportContainer != null) {
      body.remove(reportContainer);
    }
    reportContainer = new WebMarkupContainer("report");
    body.add(reportContainer.setRenderBodyOnly(true));
    report = monthlyEmployeeReportDao.getReport(form.filter.getYear(), form.filter.getMonth(), form.filter.getUser());
    if (report == null) {
      reportContainer.setVisible(false);
    } else {
      addReport();
    }
    super.onBeforeRender();
  }

  private void addReport()
  {
    reportContainer.add(new Label("user", form.filter.getUser().getFullname()));
    reportContainer.add(new Label("month", form.filter.getYear() + "-" + form.filter.getFormattedMonth()));
    final String unbookedDays = report.getFormattedUnbookedDays();
    if (StringUtils.isBlank(unbookedDays) == true) {
      reportContainer.add(new WebMarkupContainer("unbookedWorkingDaysRow").setVisible(false));
    } else {
      final WebMarkupContainer unbookedWorkingDaysRow = new WebMarkupContainer("unbookedWorkingDaysRow");
      reportContainer.add(unbookedWorkingDaysRow);
      unbookedWorkingDaysRow.add(new Label("unbookedWorkingDays", unbookedDays));
    }
    reportContainer.add(new Label("numberOfWorkingDays", String.valueOf(report.getNumberOfWorkingDays())));
    final Kost1DO kost1 = kost1Dao.internalGetById(report.getKost1Id());
    if (kost1 != null) {
      reportContainer.add(new Label("kost1", KostFormatter.format(kost1)));
    } else {
      reportContainer.add(new Label("kost1", "[invisible]").setVisible(false));
    }
    final RepeatingView headcolRepeater = new RepeatingView("headcolRepeater");
    reportContainer.add(headcolRepeater);
    if (MapUtils.isEmpty(report.getKost2Rows()) == false) {
      headcolRepeater.add(new Label(headcolRepeater.newChildId(), getString("fibu.kost2")));
      headcolRepeater.add(new Label(headcolRepeater.newChildId(), getString("fibu.kunde")));
      headcolRepeater.add(new Label(headcolRepeater.newChildId(), getString("fibu.projekt")));
      headcolRepeater.add(new Label(headcolRepeater.newChildId(), getString("fibu.kost2.art")));
    } else {
      // No kost 2 entries, so only task as head is useful.
      headcolRepeater.add(new Label(headcolRepeater.newChildId(), getString("task")).add(new SimpleAttributeModifier("colspan", "4")));
    }
    final RepeatingView headcolWeekRepeater = new RepeatingView("headcolWeekRepeater");
    reportContainer.add(headcolWeekRepeater);
    for (final MonthlyEmployeeReportWeek week : report.getWeeks()) {
      headcolWeekRepeater.add(new Label(headcolWeekRepeater.newChildId(), week.getFormattedFromDayOfMonth()
          + ".-"
          + week.getFormattedToDayOfMonth()
          + "."));
    }
    final RepeatingView rowRepeater = new RepeatingView("rowRepeater");
    reportContainer.add(rowRepeater);
    int rowCounter = 0;
    for (final Map.Entry<String, Kost2Row> rowEntry : report.getKost2Rows().entrySet()) {
      final WebMarkupContainer row = new WebMarkupContainer(rowRepeater.newChildId());
      rowRepeater.add(row);
      if (rowCounter++ % 2 == 0) {
        row.add(new SimpleAttributeModifier("class", "even"));
      } else {
        row.add(new SimpleAttributeModifier("class", "odd"));
      }
      final Kost2Row kost2Row = rowEntry.getValue();
      final Kost2DO cost2 = kost2Row.getKost2();
      addLabelCols(row, cost2, null, "kost2.nummer:" + cost2.getFormattedNumber(), report.getUser(), report.getFromDate().getTime(), report
          .getToDate().getTime());
      final RepeatingView colWeekRepeater = new RepeatingView("colWeekRepeater");
      row.add(colWeekRepeater);
      for (final MonthlyEmployeeReportWeek week : report.getWeeks()) {
        final MonthlyEmployeeReportEntry entry = week.getKost2Entries().get(kost2Row.getKost2().getId());
        colWeekRepeater.add(new Label(colWeekRepeater.newChildId(), entry != null ? entry.getFormattedDuration() : ""));
      }
      row.add(new Label("sum", report.getKost2Durations().get(cost2.getId()).getFormattedDuration()));
    }

    for (final Map.Entry<String, TaskDO> rowEntry : report.getTaskEntries().entrySet()) {
      final WebMarkupContainer row = new WebMarkupContainer(rowRepeater.newChildId());
      rowRepeater.add(row);
      if (rowCounter++ % 2 == 0) {
        row.add(new SimpleAttributeModifier("class", "even"));
      } else {
        row.add(new SimpleAttributeModifier("class", "odd"));
      }
      final TaskDO task = rowEntry.getValue();
      addLabelCols(row, null, task, null, report.getUser(), report.getFromDate().getTime(), report.getToDate().getTime());
      final RepeatingView colWeekRepeater = new RepeatingView("colWeekRepeater");
      row.add(colWeekRepeater);
      for (final MonthlyEmployeeReportWeek week : report.getWeeks()) {
        final MonthlyEmployeeReportEntry entry = week.getTaskEntries().get(task.getId());
        colWeekRepeater.add(new Label(colWeekRepeater.newChildId(), entry != null ? entry.getFormattedDuration() : ""));
      }
      row.add(new Label("sum", report.getTaskDurations().get(task.getId()).getFormattedDuration()));
    }
    {
      // Sum row.
      final WebMarkupContainer row = new WebMarkupContainer(rowRepeater.newChildId());
      rowRepeater.add(row);
      if (rowCounter++ % 2 == 0) {
        row.add(new SimpleAttributeModifier("class", "even"));
      } else {
        row.add(new SimpleAttributeModifier("class", "odd"));
      }
      addLabelCols(row, null, null, null, report.getUser(), report.getFromDate().getTime(), report.getToDate().getTime()).add(
          new SimpleAttributeModifier("style", "text-align: right;"));
      final RepeatingView colWeekRepeater = new RepeatingView("colWeekRepeater");
      row.add(colWeekRepeater);
      for (final MonthlyEmployeeReportWeek week : report.getWeeks()) {
        colWeekRepeater.add(new Label(colWeekRepeater.newChildId(), week.getFormattedTotalDuration()));
      }
      row.add(new Label("sum", report.getFormattedTotalDuration()).add(new SimpleAttributeModifier("style",
          "font-weight: bold; color:red; text-align: right;")));
    }
  }

  @SuppressWarnings("serial")
  private WebMarkupContainer addLabelCols(final WebMarkupContainer row, final Kost2DO cost2, final TaskDO task, final String searchString,
      final PFUserDO user, final long startTime, final long stopTime)
  {
    final WebMarkupContainer result = new WebMarkupContainer("cost2");
    row.add(result);
    final Link<String> link = new Link<String>("link") {
      @Override
      public void onClick()
      {
        final PageParameters params = new PageParameters();
        params.put("userId", user.getId());
        if (task != null) {
          params.put("taskId", task.getId());
        }
        params.put("startTime", startTime);
        params.put("stopTime", stopTime);
        params.put("storeFilter", false);
        if (searchString != null) {
          params.put("searchString", searchString);
        }
        setResponsePage(new TimesheetListPage(params));
      }
    };
    result.add(link);
    WicketUtils.addRowClick(row);
    if (cost2 != null) {
      final ProjektDO project = cost2.getProjekt();
      final KundeDO customer = project != null ? project.getKunde() : null;
      final Kost2ArtDO costType = cost2.getKost2Art();
      link.add(new Label("label", KostFormatter.format(cost2)));
      if (project != null) {
        row.add(new Label("customer", customer != null ? customer.getName() : ""));
        row.add(new Label("project", project.getName()));
      } else {
        row.add(new Label("customer", cost2.getDescription()).add(new SimpleAttributeModifier("colspan", "2")));
        row.add(new Label("project", "").setVisible(false));
      }
      row.add(new Label("costType", costType.getName()));
    } else {
      if (task != null) {
        // Entries for one task (not cost2).
        link.add(new Label("label", taskFormatter.getTaskPath(task.getId(), true, OutputType.PLAIN)));
      } else {
        link.add(new Label("label", getString("totalSum")));
      }
      result.add(new SimpleAttributeModifier("colspan", "4"));
      row.add(new Label("customer", "").setVisible(false));
      row.add(new Label("project", "").setVisible(false));
      row.add(new Label("costType", "").setVisible(false));
    }
    return result;
  }

  protected void exportAsPdf()
  {
    log.info("Monthly employee report for " + form.filter.getUser().getFullname() + ": " + form.filter.getFormattedMonth());
    final StringBuffer buf = new StringBuffer();
    buf.append(getString("menu.monthlyEmployeeReport.fileprefix")).append("_");
    final PFUserDO employee = userDao.getById(form.filter.getUserId());
    buf.append(employee.getLastname()).append("_").append(form.filter.getYear()).append("-").append(form.filter.getFormattedMonth())
        .append(".pdf");
    final String filename = buf.toString();

    // get the sheets of the given format
    final String styleSheet = "/fo-styles/monthlyEmployeeReport-template-fo.xsl";
    final String xmlData = "/fo-styles/monthlyEmployeeReport2pdf.xml";

    report = monthlyEmployeeReportDao.getReport(form.filter.getYear(), form.filter.getMonth(), employee);
    final Map<String, Object> data = new HashMap<String, Object>();
    data.put("systemDate", dateTimeFormatter.getFormattedDateTime(new Date()));
    data.put("title", getString("menu.monthlyEmployeeReport"));
    data.put("employeeLabel", getString("timesheet.user"));
    data.put("employee", employee.getFullname());
    data.put("monthLabel", getString("calendar.month"));
    data.put("year", form.filter.getYear());
    data.put("month", form.filter.getFormattedMonth());
    data.put("workingDaysLabel", getString("fibu.common.workingDays"));
    data.put("workingDays", report.getNumberOfWorkingDays());
    data.put("kost1Label", getString("fibu.kost1"));
    final Kost1DO kost1 = kost1Dao.internalGetById(report.getKost1Id());
    data.put("kost1", kost1 != null ? kost1.getFormattedNumber() : "--");
    data.put("kost2Label", getString("fibu.kost2"));
    data.put("kundeLabel", getString("fibu.kunde"));
    data.put("projektLabel", getString("fibu.projekt"));
    data.put("kost2ArtLabel", getString("fibu.kost2.art"));
    data.put("sumLabel", getString("sum"));
    data.put("totalSumLabel", getString("totalSum"));
    data.put("report", report);
    data.put("signatureEmployeeLabel", getString("timesheet.signatureEmployee") + ": " + employee.getFullname());
    data.put("signatureProjectLeaderLabel", getString("timesheet.signatureProjectLeader"));
    data.put("unbookedWorkingDaysLabel", getString("fibu.monthlyEmployeeReport.unbookedWorkingDays"));
    // render the PDF with fop
    final byte[] ba = pdfRenderer.render(styleSheet, xmlData, data);
    DownloadUtils.setDownloadTarget(ba, filename);
  }

  @Override
  protected String getTitle()
  {
    return getString("menu.monthlyEmployeeReport");
  }

  @Override
  public void cancelSelection(String property)
  {
    log.error("cancelSelection not supported. Property was '" + property + "'.");
  }

  @Override
  public void select(String property, Object selectedValue)
  {
    if ("user".equals(property) == true) {
      final Integer id;
      if (selectedValue instanceof String) {
        id = NumberHelper.parseInteger((String) selectedValue);
      } else {
        id = (Integer) selectedValue;
      }
      form.filter.setUser(userGroupCache.getUser(id));
    } else {
      log.error("Property '" + property + "' not supported for selection.");
    }
  }

  @Override
  public void unselect(String property)
  {
    log.error("unselect not supported. Property was '" + property + "'.");
  }
}
