<%@ page pageEncoding="utf-8" session="true" isELIgnored="false"%>
<%@ include file="../base-include.jsp"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<fmt:bundle basename="${actionBean.bundleName}">
  <head>
  <title><fmt:message key="menu.monthlyEmployeeReport" /></title>
  </head>
  <body>

  <stripes:form method="post" action="/secure/fibu/MonthlyEmployeeReport.action" focus="actionFilter.month">
    <stripes:hidden name="userId" />
    <stripes:hidden name="eventKey" />

    <fieldset><legend><fmt:message key="label.filterSettings" /></legend>
    <table class="form">
      <tr>
        <th><fmt:message key="timesheet.user" /></th>
        <td><pf:user userId="${actionBean.userId}" select="userId" nullable="false" /></td>
      </tr>
      <tr>
        <th><fmt:message key="label.options" /></th>
        <td><stripes:select name="actionFilter.year" onchange="javascript:submit();">
          <stripes:options-collection collection="${actionBean.yearList}" label="label" value="value" />
        </stripes:select>&nbsp;<stripes:select name="actionFilter.month" onchange="javascript:submit();">
          <stripes:options-collection collection="${actionBean.monthList}" label="label" value="value" />
        </stripes:select></td>
      </tr>
      <tr>
        <td>&nbsp;</td><td class="buttons"><stripes:submit name="show" /> <stripes:submit name="exportAsPdf" /> <stripes:submit name="reset" /></td>
      </tr>
    </table>
    </fieldset>
  </stripes:form>
  <c:if test="${actionBean.report != null}">
    <fieldset>
    <table>
      <tr>
        <th><fmt:message key="timesheet.user" /></th>
        <td><pf:user userId="${actionBean.actionFilter.userId}" /></td>
        <th><fmt:message key="calendar.month" /></th>
        <td>${actionBean.actionFilter.year}-${actionBean.actionFilter.formattedMonth}</td>
      </tr>
      <c:if test="${actionBean.unbookedWorkingDays != null}">
        <tr>
          <th style="color: red;"><fmt:message key="fibu.monthlyEmployeeReport.unbookedWorkingDays" /></th>
          <td colspan="3" style="color: red;">${actionBean.unbookedWorkingDays}</td>
        </tr>
      </c:if>
      <tr>
        <th><fmt:message key="fibu.common.workingDays" /></th>
        <td>${actionBean.report.numberOfWorkingDays}</td>
        <th><fmt:message key="fibu.kost1" /></th>
        <td><pf:kost1 kost1Id="${actionBean.report.kost1Id}" /></td>
      </tr>
    </table>
    <c:set var="count" value="0" /> <c:set var="style" />
    <table class="dataTable">
      <thead>
        <tr>
          <th><fmt:message key="fibu.kost2" /></th>
          <th><fmt:message key="fibu.kunde" /></th>
          <th><fmt:message key="fibu.projekt" /></th>
          <th><fmt:message key="fibu.kost2.art" /></th>
          <c:forEach var="week" items="${actionBean.report.weeks}">
            <th style="white-space: nowrap;">${week.formattedFromDayOfMonth}.-${week.formattedToDayOfMonth}.</th>
          </c:forEach>
          <th><fmt:message key="sum" /></th>
        </tr>
      </thead>
      <tbody>
        <!-- Kost2 entries. -->
        <c:forEach var="entry" items="${actionBean.report.kost2Rows}">
          <c:set var="count" value="${count + 1}" />
          <c:choose>
            <c:when test="${count % 2 == 0}">
              <c:set var="style" value="even" />
            </c:when>
            <c:otherwise>
              <c:set var="style" value="odd" />
            </c:otherwise>
          </c:choose>
          <tr class="${style}">
            <td><c:url var="linkUrl" value="/wa/timesheetList">
              <c:param name="searchString" value="kost2.nummer:${entry.value.kost2.formattedNumber}" />
              <c:param name="taskId" value="" />
              <c:param name="userId" value="${actionBean.userId}" />
              <c:param name="startTime" value="${actionBean.report.fromDate.time}" />
              <c:param name="stopTime" value="${actionBean.report.toDate.time}" />
              <c:param name="storeFilter" value="false" />
            </c:url> <a href="${linkUrl}"> <pf:kost2 kost2Id="${entry.value.kost2.id}" ignoreAccess="true" /> </a></td>
            <c:choose>
              <c:when test="${entry.value.kost2.projekt != null}">
                <td><c:if test="${entry.value.kost2.projekt.kunde != null}">${entry.value.kost2.projekt.kunde.name}</c:if></td>
                <td>${entry.value.kost2.projekt.name}</td>
                <td>${entry.value.kost2.kost2Art.name}</td>
              </c:when>
              <c:otherwise>
                <td colspan="3">${entry.value.kost2.description}</td>
              </c:otherwise>
            </c:choose>
            <c:forEach var="week" items="${actionBean.report.weeks}">
              <td style="text-align: right;">${week.kost2Entries[entry.value.kost2.id].formattedDuration}</td>
            </c:forEach>
            <td style="font-weight: bold; text-align: right;">${actionBean.report.kost2Durations[entry.value.kost2.id].formattedDuration}</td>
          </tr>
        </c:forEach>
        <!-- Task entries (without kost2 information). -->
        <c:forEach var="entry" items="${actionBean.report.taskEntries}">
          <c:set var="count" value="${count + 1}" />
          <c:choose>
            <c:when test="${count % 2 == 0}">
              <c:set var="style" value="even" />
            </c:when>
            <c:otherwise>
              <c:set var="style" value="odd" />
            </c:otherwise>
          </c:choose>
          <tr class="${style}">
            <td colspan="4"><c:url var="linkUrl" value="/wa/timesheetList">
              <c:param name="searchString" value="" />
              <c:param name="taskId" value="${entry.value.id}" />
              <c:param name="userId" value="${actionBean.userId}" />
              <c:param name="startTime" value="${actionBean.report.fromDate.time}" />
              <c:param name="stopTime" value="${actionBean.report.toDate.time}" />
              <c:param name="storeFilter" value="false" />
            </c:url> <a href="${linkUrl}"> <pf:task taskId="${entry.value.id}" showPath="true" /> </a></td>
            <c:forEach var="week" items="${actionBean.report.weeks}">
              <td style="text-align: right;">${week.taskEntries[entry.value.id].formattedDuration}</td>
            </c:forEach>
            <td style="font-weight: bold; text-align: right;">${actionBean.report.taskDurations[entry.value.id].formattedDuration}</td>
          </tr>
        </c:forEach>
        <tr class="even">
          <td colspan="4" style="text-align: right;"><c:url var="linkUrl" value="/wa/timesheetList">
            <c:param name="searchString" value="" />
            <c:param name="taskId" value="" />
            <c:param name="userId" value="${actionBean.userId}" />
            <c:param name="startTime" value="${actionBean.report.fromDate.time}" />
            <c:param name="stopTime" value="${actionBean.report.toDate.time}" />
            <c:param name="storeFilter" value="false" />
          </c:url> <a href="${linkUrl}"> <fmt:message key="totalSum" /></a></td>
          <c:forEach var="week" items="${actionBean.report.weeks}">
            <td style="font-weight: bold; text-align: right;">${week.formattedTotalDuration}</td>
          </c:forEach>
          <td style="font-weight: bold; color: red; text-align: right;">${actionBean.report.formattedTotalDuration}</td>
        </tr>
      </tbody>
    </table>
    </fieldset>
  </c:if>

  </body>
</fmt:bundle>
</html>
