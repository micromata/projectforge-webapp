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

package org.projectforge.web.fibu;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.DontValidate;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StrictBinding;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.validation.Validate;
import net.sourceforge.stripes.validation.ValidateNestedProperties;

import org.apache.log4j.Logger;
import org.projectforge.common.DateHelper;
import org.projectforge.common.LabelValueBean;
import org.projectforge.fibu.MonthlyEmployeeReport;
import org.projectforge.fibu.MonthlyEmployeeReportDao;
import org.projectforge.fibu.kost.Kost1DO;
import org.projectforge.fibu.kost.Kost1Dao;
import org.projectforge.renderer.PdfRenderer;
import org.projectforge.timesheet.TimesheetDao;
import org.projectforge.user.PFUserContext;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserDao;
import org.projectforge.web.calendar.DateTimeFormatter;
import org.projectforge.web.core.BaseAction;
import org.projectforge.web.core.ExtendedActionBean;
import org.projectforge.web.core.FlowScope;
import org.projectforge.web.core.ResponseUtils;
import org.projectforge.web.core.Select;
import org.projectforge.web.user.UserListAction;


@StrictBinding
@UrlBinding("/secure/fibu/MonthlyEmployeeReport.action")
@BaseAction(jspUrl = "/WEB-INF/jsp/fibu/monthlyEmployeeReport.jsp")
public class MonthlyEmployeeReportAction extends ExtendedActionBean
{
  private static final Logger log = Logger.getLogger(MonthlyEmployeeReportAction.class);

  protected final String FLOW_BEAN_KEY = this.getClass().getName() + ":bean";

  private TimesheetDao timesheetDao;

  private UserDao userDao;

  private MonthlyEmployeeReportDao monthlyEmployeeReportDao;

  private MonthlyEmployeeReportFilter actionFilter;

  private PdfRenderer pdfRenderer;

  private MonthlyEmployeeReport report;

  private Kost1Dao kost1Dao;

  private DateTimeFormatter dateTimeFormatter;

  public void setTimesheetDao(TimesheetDao timesheetDao)
  {
    this.timesheetDao = timesheetDao;
  }

  public void setUserDao(UserDao userDao)
  {
    this.userDao = userDao;
  }

  public void setMonthlyEmployeeReportDao(MonthlyEmployeeReportDao monthlyEmployeeReportDao)
  {
    this.monthlyEmployeeReportDao = monthlyEmployeeReportDao;
  }

  public void setPdfRenderer(PdfRenderer pdfRenderer)
  {
    this.pdfRenderer = pdfRenderer;
  }

  public void setKost1Dao(Kost1Dao kost1Dao)
  {
    this.kost1Dao = kost1Dao;
  }

  public void setDateTimeFormatter(DateTimeFormatter dateTimeFormatter)
  {
    this.dateTimeFormatter = dateTimeFormatter;
  }

  public List<LabelValueBean<String, Integer>> getYearList()
  {
    if (getActionFilter().getUserId() == null) {
      getActionFilter().setUserId(PFUserContext.getUser().getId());
    }
    int[] years = timesheetDao.getYears(getActionFilter().getUserId());
    List<LabelValueBean<String, Integer>> list = new ArrayList<LabelValueBean<String, Integer>>();
    for (int year : years) {
      list.add(new LabelValueBean<String, Integer>(String.valueOf(year), year));
    }
    return list;
  }

  public List<LabelValueBean<String, Integer>> getMonthList()
  {
    return DateHelper.getMonthList();
  }

  @Validate(required = true)
  @Select(selectAction = UserListAction.class)
  public Integer getUserId()
  {
    return getActionFilter().getUserId();
  }

  public void setUserId(Integer userId)
  {
    getActionFilter().setUserId(userId);
  }

  public MonthlyEmployeeReport getReport()
  {
    return report;
  }
  
  public String getUnbookedWorkingDays()
  {
    return report.getFormattedUnbookedDays();
  }

  public Resolution show()
  {
    init();
    PFUserDO user = userDao.getById(getActionFilter().getUserId());
    report = monthlyEmployeeReportDao.getReport(getActionFilter().getYear(), this.actionFilter.getMonth(), user);
    return getInputPage();
  }

  @DefaultHandler
  @DontValidate
  public Resolution execute()
  {
    init();
    Resolution inputPage = getInputPage();
    Resolution resolution = handleSelectEvents(this, inputPage);
    if (resolution != null) {
      if (resolution.equals(inputPage) == true) {
        return show();
      }
      return resolution;
    }
    FlowScope scope = getFlowScope(false);
    if (scope != null) {
      MonthlyEmployeeReportFilter filter = (MonthlyEmployeeReportFilter) scope.get(FLOW_BEAN_KEY);
      if (filter != null) {
        getLogger().debug("filter restored in execute: " + filter);
      }
      scope.closeFlowScope(getContext().getRequest());
      processSelection(this);
    } else {
      getLogger().debug("action");
    }
    return show();
  }

  public Resolution exportAsPdf()
  {
    return new Resolution() {
      public void execute(HttpServletRequest request, HttpServletResponse response) throws Exception
      {
        StringBuffer buf = new StringBuffer();
        buf.append(getLocalizedString("menu.monthlyEmployeeReport.fileprefix")).append("_");
        PFUserDO employee = userDao.getById(getActionFilter().getUserId());
        buf.append(employee.getLastname()).append("_").append(actionFilter.getYear()).append("-").append(actionFilter.getFormattedMonth()).append(
            ".pdf");
        ResponseUtils.prepareDownload(buf.toString(), response, getContext().getServletContext(), true);

        // get the sheets from the given Format
        String styleSheet = "/fo-styles/monthlyEmployeeReport-template-fo.xsl";
        String xmlData = "/fo-styles/monthlyEmployeeReport2pdf.xml";

        report = monthlyEmployeeReportDao.getReport(getActionFilter().getYear(), actionFilter.getMonth(), employee);
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("systemDate", dateTimeFormatter.getFormattedDateTime(new Date()));
        data.put("title", getLocalizedString("menu.monthlyEmployeeReport"));
        data.put("employeeLabel", getLocalizedString("timesheet.user"));
        data.put("employee", employee.getFullname());
        data.put("monthLabel", getLocalizedString("calendar.month"));
        data.put("year", actionFilter.getYear());
        data.put("month", actionFilter.getFormattedMonth());
        data.put("workingDaysLabel", getLocalizedString("fibu.common.workingDays"));
        data.put("workingDays", report.getNumberOfWorkingDays());
        data.put("kost1Label", getLocalizedString("fibu.kost1"));
        Kost1DO kost1 = kost1Dao.internalGetById(report.getKost1Id());
        data.put("kost1", kost1 != null ? kost1.getFormattedNumber() : "--");
        data.put("kost2Label", getLocalizedString("fibu.kost2"));
        data.put("kundeLabel", getLocalizedString("fibu.kunde"));
        data.put("projektLabel", getLocalizedString("fibu.projekt"));
        data.put("kost2ArtLabel", getLocalizedString("fibu.kost2.art"));
        data.put("sumLabel", getLocalizedString("sum"));
        data.put("totalSumLabel", getLocalizedString("totalSum"));
        data.put("report", report);
        data.put("signatureEmployeeLabel", getLocalizedString("timesheet.signatureEmployee") + ": " + employee.getFullname());
        data.put("signatureProjectLeaderLabel", getLocalizedString("timesheet.signatureProjectLeader"));
        data.put("unbookedWorkingDaysLabel", getLocalizedString("fibu.monthlyEmployeeReport.unbookedWorkingDays"));
        // render the PDF with fop
        byte[] ba = pdfRenderer.render(styleSheet, xmlData, data);
        response.getOutputStream().write(ba);
        response.getOutputStream().flush();
      }
    };
  }

  protected void init()
  {
    getActionFilter().init();
  }

  @ValidateNestedProperties( { @Validate(field = "year"), @Validate(field = "month"), @Validate(field = "userId")})
  public MonthlyEmployeeReportFilter getActionFilter()
  {
    if (actionFilter == null) {
      actionFilter = (MonthlyEmployeeReportFilter) getContext().getEntry(this.getClass().getName() + ":Filter");
    }
    if (actionFilter == null) {
      actionFilter = new MonthlyEmployeeReportFilter();
      getContext().putEntry(this.getClass().getName() + ":Filter", actionFilter, true);
    }
    return actionFilter;
  }

  protected Resolution getInputPage()
  {
    return new ForwardResolution(getJspUrl());
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }

  @Override
  protected void storeToFlowScope()
  {
    storeFlowScopeObject(FLOW_BEAN_KEY, getActionFilter());
  }
}
