<%@ page pageEncoding="utf-8" session="true" isELIgnored="false"%>
<%@ include file="../base-include.jsp"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<fmt:bundle basename="${actionBean.bundleName}">
  <head>

  <title><c:choose>
    <c:when test="${actionBean.selectMode == true and actionBean.period == true}">
      <fmt:message key="calendar.selectDateOrPeriod.title" />
    </c:when>
    <c:when test="${actionBean.selectMode == true}">
      <fmt:message key="calendar.selectDate.title" />
    </c:when>
    <c:otherwise>
      <fmt:message key="calendar.title" />
    </c:otherwise>
  </c:choose></title>
  <script type="text/javascript">
	$(document).ready(function(){
		// bei Klick auf die TD und TH bitte den ersten Link in der Zelle ausführen. Damit die Leute nicht so zielen müssen.
		$(".calendar th").add(".calendar td").click( function() { hurz = $(this).find("a:first").attr("href"); if (hurz!=undefined) {document.location.href = hurz};} );
		//Rollover
		$(".calendar th:has(a)").add(".calendar td:has(a)").hover( function() { $(this).addClass("hover");}, function () { $(this).removeClass("hover"); });
		//Hoehe der Tabelle minus die Hoehe der Headerzeilen
		height = ($("#content").height() - 50);
		//Hoehe der 4 5 oder 6 TableRows. -7 ist das Padding pro Zeile.
		rowHeight = (height / (<c:out value="${fn:length(actionBean.monthHolder.weeks)}" />)) - 7;
		//Setze die Zeilenhoehe
		$(".calendar tr.row td").height(Math.floor(rowHeight));
	});


</script>
  </head>
  <body>

  <stripes:form method="post" action="/secure/calendar/SelectDate.action">
    <stripes:hidden name="settings.current" />
    <stripes:hidden name="settings.periodStart" />
    <stripes:hidden name="settings.periodStop" />
    <stripes:hidden name="settings.showTimesheets" />
    <stripes:hidden name="settings.showBirthdays" />
    <stripes:hidden name="eventKey" />
    <stripes:hidden name="selectedValue" />
    <stripes:hidden name="flowKey" />

    <table class="calendar">
      <tr class="calendarhead">
        <th colspan="8">
        <ul class="calendartools">
          <li class="left"><a href="#" onclick="javascript:submitEvent('event.previous')"><pf:image src="/images/css_img/leftInactive.png"
            tooltip="calendar.tooltip.selectPrevious" /></a></li>
          <c:choose>
            <c:when test="${actionBean.selectMode == true and actionBean.period == true}">
              <li class="month"><a href="javascript:submitEvent('selectMonth')"><fmt:message
                key="calendar.month.${actionBean.monthHolder.monthKey}" /> &nbsp;${actionBean.monthHolder.year} </a></li>

            </c:when>

            <c:otherwise>
              <li class="month"><a href="#" class="deadlink"><fmt:message key="calendar.month.${actionBean.monthHolder.monthKey}" />&nbsp;
              ${actionBean.monthHolder.year}</a></li>
            </c:otherwise>
          </c:choose>
          <li><a href="#" onclick="javascript:submitEvent('today')" style="padding-right: 10px;"><fmt:message key="calendar.today" /></a></li>
          <c:if test="${actionBean.selectMode == true}">
            <li class="inner"><a href="javascript:submitEvent('cancel')"><pf:image src="${buttonCancelImage}"
              tooltip="tooltip.cancel" /></a></li>
          </c:if>
          <li class="right"><a href="#" onclick="javascript:submitEvent('event.next')"><pf:image
            src="/images/css_img/rightInactive.png" tooltip="calendar.tooltip.selectNext" /></a></li>

        </ul>
        <c:if test="${not empty actionBean.monthDuration}">
                        ${actionBean.monthDuration} (<pf:date date="${actionBean.monthHolder.begin}" type="date" /> - <pf:date
            date="${actionBean.monthHolder.end}" type="date" />)
                      </c:if> <!-- ul.calendatools END --></th>
      </tr>
      <tr class="calendarsubhead">
        <th class="week"><fmt:message key="calendar.weekOfYearShortLabel" /><c:if test="${actionBean.selectMode == true}">
          <pf:image src="${spacerImage}" width="16" />
        </c:if></th>
        <c:forEach items="${actionBean.monthHolder.firstWeek.days}" var="day">
          <th class="short"><fmt:message key="calendar.shortday.${day.dayKey}" /></th>
        </c:forEach>
      </tr>
      <c:forEach items="${actionBean.monthHolder.weeks}" var="week">
        <tr class="row">
          <th class="week"><c:choose>
            <c:when test="${actionBean.selectMode == true and actionBean.period == true}">
              <a href="javascript:submitSelectedEvent('selectWeek', '${week.days[0].timeInMillis}')">${week.weekOfYear} <pf:image
                src="/images/button_calendar_week.png" tooltip="calendar.tooltip.selectWeek" /></a>
            </c:when>
            <c:otherwise>${week.weekOfYear}</c:otherwise>
          </c:choose> <c:if test='${week.objects["duration"] != null}'>
            <br />${week.objects["duration"]}
                      </c:if></th>
          <c:forEach items="${week.days}" var="day">
            <c:choose>
              <c:when test="${day.today == true}">
                <c:set var="classStyle" value="today" />
              </c:when>
              <c:when test="${day.marker == true}">
                <c:set var="classStyle" value="other-month" />
              </c:when>
              <c:when test="${day.workingDay == false}">
                <c:set var="classStyle" value="non-working-day" />
              </c:when>
              <c:when test="${day.holiday == true}">
                <c:set var="classStyle" value="holiday-working-day" />
              </c:when>
              <c:otherwise>
                <c:set var="classStyle" value="normal" />
              </c:otherwise>
            </c:choose>
            <td class="${classStyle}"><c:choose>

              <c:when test="${actionBean.selectMode == true}">
                <c:if test='${day.objects["timesheets"] != null}'>
                  <span style="color: green; float: right;">${day.objects["duration"]}</span>
                </c:if>
                <a href="javascript:submitSelectedEvent('select', '${day.timeInMillis}')"> ${day.dayOfMonth} </a>
                <c:if test="${day.holiday == true}">
                  <small><fmt:message key="${day.holidayInfo}" /></small>
                </c:if>
              </c:when>

              <c:otherwise>
                <c:if test='${day.objects["timesheets"] != null}'>
                  <span style="color: green; float: right;">${day.objects["duration"]}</span>
                </c:if>
                <stripes:link href="/secure/timesheet/TimesheetEdit.action" event="preEdit">
                  <stripes:param name="startTime.dateString" value="${day.millisForCurrentTimeOfDay}" />

                  <span style="padding: 3px; font-size: 9px;">(+)</span>
                </stripes:link>
                ${day.dayOfMonth}
                <c:if test="${day.holiday == true}">
                  &nbsp;<small><fmt:message key="${day.holidayInfo}" /></small>
                </c:if>

              </c:otherwise>

            </c:choose>
            <p style="overflow-y: visible;"><c:if test='${day.objects["timesheets"] != null}'>
              <c:forEach items='${day.objects["timesheets"]}' var="timesheet">
                <%
                  /* Iterate over all timesheets of the current day. */
                %>
                <%
                  /***** No output from here ... *****/
                %>
                <pf:date date="${timesheet.startTime}" type="timeOfDay" var="fStartTime" />
                <pf:date date="${timesheet.stopTime}" type="timeOfDay" var="fStopTime" />
                <c:set var="fullStartTime">
                  <c:choose>
                    <c:when test="${timesheet.startTimeLinkEnabled == true}">
                      <c:choose>
                        <c:when test="${actionBean.selectMode == true}">
                          <%
                            /* Select start time of time sheet as stop time for caller. */
                          %>
                          <a href="javascript:submitSelectedEvent('selectStopTime', '${timesheet.startTime.time}')">${fStartTime}</a>
                        </c:when>
                        <c:otherwise>
                          <%
                            /* Create new time sheet with start time of this time sheet as start/stop time. */
                          %>
                          <stripes:link href="/secure/timesheet/TimesheetEdit.action" event="preEdit">
                            <stripes:param name="startTime.dateString" value="${timesheet.startTime.time}" />
                            <stripes:param name="stopTime.dateString" value="${timesheet.startTime.time}" />
                              ${fStartTime}
                            </stripes:link>
                        </c:otherwise>
                      </c:choose>
                    </c:when>
                    <c:otherwise>
                        ${fStartTime}
                    </c:otherwise>
                  </c:choose>
                </c:set>
                <c:set var="fullStopTime">
                  <c:choose>
                    <c:when test="${timesheet.stopTimeLinkEnabled == true}">
                      <c:choose>
                        <c:when test="${actionBean.selectMode == true}">
                          <%
                            /* Select stop time of time sheet as start time for caller. */
                          %>
                          <a href="javascript:submitSelectedEvent('selectStartTime', '${timesheet.stopTime.time}')">${fStopTime}</a>
                        </c:when>
                        <c:otherwise>
                          <%
                            /* Create new time sheet with stop time of this time sheet as start/stop time. */
                          %>
                          <stripes:link href="/secure/timesheet/TimesheetEdit.action" event="preEdit">
                            <stripes:param name="startTime.dateString" value="${timesheet.stopTime.time}" />
                            <stripes:param name="stopTime.dateString" value="${timesheet.stopTime.time}" />
                              ${fStopTime}
                            </stripes:link>
                        </c:otherwise>
                      </c:choose>
                    </c:when>
                    <c:otherwise>
                        ${fStopTime}
                      </c:otherwise>
                  </c:choose>
                </c:set>
                <%
                  /***** ... no output until here ... *****/
                %>
                <c:choose>
                  <c:when test="${timesheet.break == true }">
                    <%
                      /* Break between two time sheets of same day. */
                    %>
                    <span style="color: gray;"><strong> <c:choose>
                      <c:when test="${actionBean.selectMode == true}">
                        <%
                          /* Select time period of break for caller. */
                        %>
                        <a href="javascript:submitSelectedEvent('selectTimeperiod', '${timesheet.startTime};${timesheet.stopTime}')">
                        ${fStartTime}-${fStopTime}</a>
                      </c:when>
                      <c:otherwise>
                        <%
                          /* Create new time sheet with time period of break. */
                        %>
                        <stripes:link href="/secure/timesheet/TimesheetEdit.action" event="preEdit">
                          <stripes:param name="startTime.dateString" value="${timesheet.startTime.time}" />
                          <stripes:param name="stopTime.dateString" value="${timesheet.stopTime.time}" />
                          <span>${fStartTime}-${fStopTime}&nbsp;</span>
                        </stripes:link>
                      </c:otherwise>
                    </c:choose> <fmt:message key="timesheet.break" /> </strong></span>
                  </c:when>
                  <c:otherwise>
                    <%
                      /* Show time sheet. */
                    %>
                    <strong><c:out value="${fullStartTime}" escapeXml="false" />-<c:out value="${fullStopTime}" escapeXml="false" /></strong>
                    <c:choose>
                      <c:when test="${actionBean.selectMode == true}">
                        <%
                          /* In select mode, the task title is not linked to the task page. */
                        %>
                        <span title="<c:out value='${timesheet.toolTip}'/>">${timesheet.formatted4Calendar}
                        </span>
                      </c:when>
                      <c:otherwise>
                        <%
                          /* But in non select mode, the task title is linked to the task page. */
                        %>
                        <stripes:link href="/secure/timesheet/TimesheetEdit.action" event="preEdit">
                          <stripes:param name="id" value="${timesheet.id}" />
                          <span title="<c:out value='${timesheet.toolTip}'/>">${timesheet.formatted4Calendar}</span>
                        </stripes:link>
                      </c:otherwise>
                    </c:choose>
                  </c:otherwise>
                </c:choose>
                <br />
              </c:forEach>
            </c:if> <c:if test='${day.objects["birthdays"] != null}'>
              <c:forEach items='${day.objects["birthdays"]}' var="birthdayAddress">
                <c:set var="entry">
                  <c:if test="${birthdayAddress.age gt 0}">
                    <pf:date year="short" type="date" date="${birthdayAddress.address.birthday}" />&nbsp;</c:if><% /* Birthday is not visible for all users (age == 0). */ %>
                ${birthdayAddress.address.firstName}&nbsp;${birthdayAddress.address.name}<c:if test="${birthdayAddress.age gt 0}">&nbsp;(${birthdayAddress.age}&nbsp;<fmt:message
                      key="address.age.short" />)</c:if>
                </c:set>
                <c:choose>
                  <c:when test="${birthdayAddress.favorite == true}">
                    <span style="color: red; font-weight: bold;">${entry}</span>
                  </c:when>
                  <c:otherwise>
                    <span style="color: black; font-weight: normal;">${entry}</span>
                  </c:otherwise>
                </c:choose>

                <%
                  /* Iterate over all birthdays of the current day. */
                %>
                <br />
              </c:forEach>
            </c:if></p>
            <%
              /* Create new time sheet. */
            %>
            </td>
          </c:forEach>
        </tr>
      </c:forEach>
    </table>
  </stripes:form>

  </body>
</fmt:bundle>
</html>
