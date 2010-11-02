<%@ page pageEncoding="utf-8" session="true" isELIgnored="false"%>
<%@ include file="base-include.jsp"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<fmt:bundle basename="${actionBean.bundleName}">
  <head>
  <title><fmt:message key="search.title" /></title>
  </head>
  <body>

  <stripes:form method="post" action="/secure/Search.action" focus="actionFilter.startTime">
    <stripes:hidden name="actionFilter.modifiedByUserId" />
    <stripes:hidden name="actionFilter.taskId" />
    <stripes:hidden name="selectedValue" />
    <stripes:hidden name="eventKey" />

    <fieldset><legend><fmt:message key="label.filterSettings" /></legend>
    <table class="form">
      <tr>
        <th><fmt:message key="timePeriod" /></th>
        <td><stripes:text class="date" name="actionFilter.startTime" /><pf:selectDate select="startTime" period="true" /> - <stripes:text
          class="date" name="actionFilter.stopTime" /><pf:selectDate select="stopTime" period="true" />&nbsp; <stripes:select
          name="lastDays" onchange="javascript:submit();">
          <stripes:options-collection collection="${actionBean.timePeriodList}" label="label" value="value" />
        </stripes:select><span style="font-size: x-small; color: white;"><pf:date date="${actionBean.actionFilter.startTime}" type="utc" /> - <pf:date
          date="${actionBean.actionFilter.stopTime}" type="utc" /></span></td>
        <th><fmt:message key="search.area" /></th>
        <td><stripes:select name="actionFilter.area" onchange="javascript:submit();">
          <stripes:options-collection collection="${actionBean.areas}" label="label" value="value" />
        </stripes:select></td>
      </tr>
      <tr>
        <!-- th><fmt:message key="search.history" /></th>
        <td><fmt:message key="message.notYetImplemented" /></td-->
        <th><fmt:message key="modifiedBy" /></th>
        <td><pf:user userId="${actionBean.actionFilter.modifiedByUserId}" select="modifiedByUserId" nullable="true" /></td>
        <th><fmt:message key="label.pageSize" /></th>
        <td><stripes:select name="actionFilter.maxRows" onchange="javascript:submit();">
          <stripes:options-collection collection="${actionBean.maxRows}" label="label" value="value" />
        </stripes:select></td>
      </tr>
      <tr>
        <th><fmt:message key="task" /></th>
        <td colspan="3"><pf:task taskId="${actionBean.actionFilter.taskId}" showPath="true" select="taskId" nullable="true" /></td>
      </tr>
      <tr>
        <td colspan="4" class="buttons"><stripes:submit name="search" /> <stripes:submit name="reset" /></td>
      </tr>
    </table>
    </fieldset>

  </stripes:form>

  <fieldset><legend><fmt:message key="label.resultset" /></legend> <c:forEach items="${actionBean.resultLists}" var="list">
    <c:set var="more" value="false" />
    <c:choose>
      <c:when test="${fn:contains(list[0].dataObject.class.name, 'org.projectforge.timesheet.TimesheetDO')}">
        <c:set var="icon" value="clock" />
        <c:set var="titleKey" value="timesheet.timesheets" />
        <c:set var="editLink" value="/wa/timesheetList?wicket:bookmarkablePage=:org.projectforge.web.timesheet.TimesheetEditPage" />
        <c:set var="type" value="TIMESHEET" />
        <c:set var="colspan" value="8" />
      </c:when>
      <c:when test="${fn:contains(list[0].dataObject.class.name, 'org.projectforge.address.AddressDO')}">
        <c:set var="icon" value="vcard" />
        <c:set var="titleKey" value="address.addresses" />
        <c:set var="editLink" value="/secure/address/AddressEdit.action" />
        <c:set var="type" value="ADDRESS" />
        <c:set var="colspan" value="5" />
      </c:when>
      <c:when test="${fn:contains(list[0].dataObject.class.name, 'org.projectforge.book.BookDO')}">
        <c:set var="icon" value="book" />
        <c:set var="titleKey" value="book.books" />
        <c:set var="editLink" value="/wa/bookList?wicket:bookmarkablePage=:org.projectforge.web.book.BookEditPage" />
        <c:set var="type" value="BOOK" />
        <c:set var="colspan" value="7" />
      </c:when>
      <c:when test="${fn:contains(list[0].dataObject.class.name, 'org.projectforge.task.TaskDO')}">
        <c:set var="icon" value="task" />
        <c:set var="titleKey" value="task.tasks" />
        <c:set var="editLink" value="/wa/taskTree?wicket:bookmarkablePage=:org.projectforge.web.task.TaskEditPage" />
        <c:set var="type" value="TASK" />
        <c:set var="colspan" value="6" />
      </c:when>
      <c:when test="${fn:contains(list[0].dataObject.class.name, 'org.projectforge.user.PFUserDO')}">
        <c:set var="icon" value="user" />
        <c:set var="titleKey" value="user.users" />
        <c:set var="editLink" value="/secure/user/UserEdit.action" />
        <c:set var="type" value="USER" />
        <c:set var="colspan" value="6" />
      </c:when>
      <c:when test="${fn:contains(list[0].dataObject.class.name, 'org.projectforge.user.GroupDO')}">
        <c:set var="icon" value="group" />
        <c:set var="titleKey" value="group.groups" />
        <c:set var="editLink" value="/secure/user/GroupEdit.action" />
        <c:set var="type" value="GROUP" />
        <c:set var="colspan" value="5" />
      </c:when>
      <c:when test="${fn:contains(list[0].dataObject.class.name, 'org.projectforge.access.GroupTaskAccessDO')}">
        <c:set var="icon" value="lock" />
        <c:set var="titleKey" value="access.list.title" />
        <c:set var="editLink" value="/secure/access/GroupTaskAccessEdit.action" />
        <c:set var="type" value="ACCESS" />
        <c:set var="colspan" value="6" />
      </c:when>
      <c:when test="${fn:contains(list[0].dataObject.class.name, 'org.projectforge.fibu.RechnungDO')}">
        <c:set var="icon" value="calculator" />
        <c:set var="titleKey" value="fibu.rechnung.rechnungen" />
        <c:set var="editLink" value="/wa/rechnungList?wicket:bookmarkablePage=:org.projectforge.web.fibu.RechnungEditPage" />
        <c:set var="type" value="RECHNUNG" />
        <c:set var="colspan" value="8" />
      </c:when>
      <c:when test="${fn:contains(list[0].dataObject.class.name, 'org.projectforge.fibu.KundeDO')}">
        <c:set var="icon" value="building" />
        <c:set var="titleKey" value="fibu.kunde.kunden" />
        <c:set var="editLink" value="/secure/fibu/KundeEdit.action" />
        <c:set var="type" value="KUNDE" />
        <c:set var="colspan" value="6" />
      </c:when>
      <c:when test="${fn:contains(list[0].dataObject.class.name, 'org.projectforge.fibu.ProjektDO')}">
        <c:set var="icon" value="plugin" />
        <c:set var="titleKey" value="fibu.projekt.projekte" />
        <c:set var="editLink" value="/wa/projektList?wicket:bookmarkablePage=:org.projectforge.web.fibu.ProjektEditPage" />
        <c:set var="type" value="PROJEKT" />
        <c:set var="colspan" value="7" />
      </c:when>
      <c:when test="${fn:contains(list[0].dataObject.class.name, 'org.projectforge.fibu.kost.BuchungssatzDO')}">
        <c:set var="icon" value="coins" />
        <c:set var="titleKey" value="fibu.buchungssatz.buchungssaetze" />
        <c:set var="editLink" value="/secure/fibu/BuchungssatzEdit.action" />
        <c:set var="type" value="BUCHUNGSSATZ" />
        <c:set var="colspan" value="12" />
      </c:when>
      <c:when test="${fn:contains(list[0].dataObject.class.name, 'org.projectforge.fibu.kost.Kost1DO')}">
        <c:set var="icon" value="coins" />
        <c:set var="titleKey" value="fibu.kost1.kost1s" />
        <c:set var="editLink" value="/secure/fibu/Kost1Edit.action" />
        <c:set var="type" value="KOST1" />
        <c:set var="colspan" value="5" />
      </c:when>
      <c:when test="${fn:contains(list[0].dataObject.class.name, 'org.projectforge.fibu.kost.Kost2DO')}">
        <c:set var="icon" value="coins" />
        <c:set var="titleKey" value="fibu.kost2.kost2s" />
        <c:set var="editLink" value="/secure/fibu/Kost2Edit.action" />
        <c:set var="type" value="KOST2" />
        <c:set var="colspan" value="5" />
      </c:when>
      <c:when test="${fn:contains(list[0].dataObject.class.name, 'org.projectforge.fibu.kost.Kost2ArtDO')}">
        <c:set var="icon" value="tag_blue" />
        <c:set var="titleKey" value="fibu.kost2art.kost2arten" />
        <c:set var="editLink" value="/wa/kost2ArtList?wicket:bookmarkablePage=:org.projectforge.web.fibu.Kost2ArtEditPage" />
        <c:set var="type" value="KOST2ART" />
        <c:set var="colspan" value="6" />
      </c:when>
      <c:when test="${fn:contains(list[0].dataObject.class.name, 'org.projectforge.fibu.KontoDO')}">
        <%
          /* Please regard datevImport.jsp for changes and vice versa. */
        %>
        <c:set var="icon" value="money" />
        <c:set var="titleKey" value="fibu.konto.konten" />
        <c:set var="editLink" value="/secure/fibu/KontoEdit.action" />
        <c:set var="type" value="KONTO" />
        <c:set var="colspan" value="6" />
      </c:when>
      <c:otherwise>
        <c:set var="type" value="UNKNOWN" />
      ERROR: UNSUPPORTED TYPE: ${list[0].dataObject.class.name} (Please contact developer).
      </c:otherwise>
    </c:choose>
    <b><fmt:message key="${titleKey}" /></b>
    <table class="dataTable">
      <tbody>
        <c:set var="style" value="even" />
        <c:forEach items="${list}" var="entry">
          <c:choose>
            <c:when test="${style eq 'even'}">
              <c:set var="style" value="odd" />
            </c:when>
            <c:otherwise>
              <c:set var="style" value="even" />
            </c:otherwise>
          </c:choose>
          <c:set var="tdStyle">
            <c:choose>
              <c:when test="${entry.dataObject.deleted == true}">text-decoration: line-through;</c:when>
              <c:otherwise></c:otherwise>
            </c:choose>
          </c:set>
          <tr class="${style}">
            <c:choose>
              <c:when test="${entry.dataObject eq null}">
                <td colspan="${colspan}" style="color: red;" class="notrlink">
                  <a onclick="javascript:submitSelectedEvent('expand', '${type}')" href="#">
                    <fmt:message key="moreEntriesAvailable" />
                  </a>
                </td>
              </c:when>
              <c:otherwise>
                <td><stripes:link href="${editLink}" event="preEdit">
                  <stripes:param name="id" value="${entry.dataObject.id}" />
                  <c:choose>
                    <c:when test="${entry.historyEntry.type eq 'INSERT'}">
                      <pf:image src="/images/${icon}_add.png" />
                    </c:when>
                    <c:when test="${entry.historyEntry.type eq 'UPDATE'}">
                      <pf:image src="/images/${icon}_edit.png" />
                    </c:when>
                    <c:when test="${entry.historyEntry.type eq 'DELETE'}">
                      <pf:image src="/images/${icon}_delete.png" />
                    </c:when>
                  </c:choose>
                </stripes:link></td>
                <c:choose>
                  <c:when test="${type eq 'TIMESHEET'}">
                    <!-- Timesheets -->
                    <td style="${style}"><pf:date date="${entry.dataObject.startTime}" type="timestamp" precision="minute" year="short" /></td>
                    <td style="${style}"><pf:formatDuration millis="${entry.dataObject.duration}" /></td>
                    <td style="${style}"><pf:task taskId="${entry.dataObject.taskId}" /></td>
                    <td style="${style}">${entry.dataObject.location}: ${entry.dataObject.shortDescription}</td>
                  </c:when>
                  <c:when test="${type eq 'ADDRESS'}">
                    <!-- Addresses -->
                    <td style="${style}">${entry.dataObject.name}, ${entry.dataObject.firstName}, ${entry.dataObject.organization}</td>
                  </c:when>
                  <c:when test="${type eq 'TASK'}">
                    <!-- Tasks -->
                    <td style="${style}"><pf:task taskId="${entry.dataObject.id}" showPathAsTooltip="true" /></td>
                    <td style="${style}">${entry.dataObject.shortDescription}</td>
                  </c:when>
                  <c:when test="${type eq 'BOOK'}">
                    <!-- Books -->
                    <td style="${style}">${entry.dataObject.yearOfPublishing}</td>
                    <td style="${style}">${entry.dataObject.authors}</td>
                    <td style="${style}">${entry.dataObject.title}</td>
                  </c:when>
                  <c:when test="${type eq 'RECHNUNG'}">
                    <!-- Rechnungen -->
                    <td style="${style}">${entry.dataObject.nummer}</td>
                    <td style="${style}"><c:choose>
                      <c:when test="${entry.dataObject.kunde.id > 0}">${entry.dataObject.kunde.name}</c:when>
                      <c:when test='${not empty entry.dataObject.kundeText and entry.dataObject.kundeText != ""}'>${entry.dataObject.kundeText}</c:when>
                      <c:otherwise>
                        <c:set var="kunde" value="false" />
                      </c:otherwise>
                    </c:choose> <c:if test="${entry.dataObject.projekt.id > 0}">
                      <c:if test='${kunde == "false"}'>${entry.dataObject.projekt.kunde.name}</c:if>
                  - ${entry.dataObject.projekt.name}
                  </c:if></td>
                    <td style="${style}">${entry.dataObject.betreff}</td>
                    <td style="white-space: nowrap; text-align: right;${style}"><pf:currency value="${entry.dataObject.netSum}" /></td>
                  </c:when>
                  <c:when test="${type eq 'AUFTRAG'}">
                    <!-- Aufträge -->
                    <td style="${style}"><c:choose>
                      <c:when test="${entry.dataObject.kunde.id > 0}">${entry.dataObject.kunde.name}</c:when>
                      <c:when test='${not empty entry.dataObject.kundeText and entry.dataObject.kundeText != ""}'>${entry.dataObject.kundeText}</c:when>
                      <c:otherwise>
                        <c:set var="kunde" value="false" />
                      </c:otherwise>
                    </c:choose> <c:if test="${entry.dataObject.projekt.id > 0}">
                      <c:if test='${kunde == "false"}'>${entry.dataObject.projekt.kunde.name}</c:if>
                  - ${entry.dataObject.projekt.name}
                  </c:if></td>
                    <td style="${style}">${entry.dataObject.titel}</td>
                    <td style="white-space: nowrap; text-align: right;${style}"><pf:currency value="${entry.dataObject.nettoSumme}" /></td>
                  </c:when>
                  <c:when test="${type eq 'USER'}">
                    <!-- Users -->
                    <td style="${style}">${entry.dataObject.username}</td>
                    <td style="${style}"><pf:user userId="${entry.dataObject.id}" /></td>
                  </c:when>
                  <c:when test="${type eq 'GROUP'}">
                    <!-- Users -->
                    <td style="${style}">${entry.dataObject.name}</td>
                  </c:when>
                  <c:when test="${type eq 'ACCESS'}">
                    <!-- Accesses -->
                    <td style="${style}"><pf:task taskId="${entry.dataObject.task.id}" showPathAsTooltip="true" /></td>
                    <td style="${style}">${entry.dataObject.group.name}</td>
                  </c:when>
                  <c:when test="${type eq 'KOST2ART' or type eq 'KUNDE'}">
                    <!-- Kost2Arten, Kunden -->
                    <td style="white-space: nowrap; text-align: right;${style}">${entry.dataObject.id}</td>
                    <td style="${style}">${entry.dataObject.name}</td>
                  </c:when>
                  <c:when test="${type eq 'KOST1' or type eq 'KOST2'}">
                    <!-- Kost2 -->
                    <td style="white-space: nowrap; text-align: right;${style}">${entry.dataObject.shortDisplayName}</td>
                  </c:when>
                  <c:when test="${type eq 'PROJEKT'}">
                    <!-- Projekt -->
                    <td style="white-space: nowrap; text-align: right;${style}">${entry.dataObject.kost}</td>
                    <td style="${style}">${entry.dataObject.kunde.name}</td>
                    <td style="${style}">${entry.dataObject.name}</td>
                  </c:when>
                  <c:when test="${type eq 'BUCHUNGSSATZ'}">
                    <%
                      /* Please regard datevImport.jsp for changes and vice versa. */
                    %>
                    <!-- Buchungssatz -->
                    <c:set var="obj" value="${entry.dataObject}"/>
                      <td style="white-space: nowrap; text-align: right;${style}">${obj.satznr}</td>
                      <td style="${style}"><pf:date date="${obj.datum}" type="date" /></td>
                      <td style="white-space: nowrap; text-align: right;${style}"><pf:currency value="${obj.betrag}" /></td>
                      <td style="${style}">${obj.text}</td>
                      <td style="${style}"><c:if test="${obj.konto != null}">${obj.konto.nummer}</c:if></td>
                      <td style="${style}"><c:if test="${obj.gegenKonto != null}">${obj.gegenKonto.nummer}</c:if></td>
                      <td style="${style}"><c:if test="${obj.kost1 != null}">${obj.kost1.shortDisplayName}</c:if></td>
                      <td style="${style}"><c:if test="${obj.kost2 != null}">${obj.kost2.shortDisplayName}</c:if></td>
                  </c:when>
                  <c:when test="${type eq 'KONTO'}">
                    <%
                      /* Please regard datevImport.jsp for changes and vice versa. */
                    %>
                    <!-- Konto -->
                    <td style="white-space: nowrap; text-align: right;${style}">${entry.dataObject.nummer}</td>
                    <td style="${style}">${entry.dataObject.bezeichnung}</td>
                  </c:when>
                  <c:otherwise>
                    <td style="${style}">Not yet implemented: ${type}</td>
                  </c:otherwise>
                </c:choose>
                <td style="${style}"><c:if test="${entry.historyEntry.type eq 'UPDATE'}">
                  <c:forEach items="${entry.propertyChanges}" var="delta">
                   ${delta.propertyName}=<a title="${delta.oldValue}">${delta.newValue}</a>;
                  </c:forEach>
                </c:if></td>
                <td style="${style}"><pf:date date="${entry.historyEntry.timestamp}" /></td>
                <td style="${style}"><pf:user userId="${entry.historyEntry.userName}" /></td>
              </c:otherwise>
            </c:choose>
          </tr>
        </c:forEach>
      </tbody>
    </table>
  </c:forEach></fieldset>

  </body>
</fmt:bundle>
</html>
