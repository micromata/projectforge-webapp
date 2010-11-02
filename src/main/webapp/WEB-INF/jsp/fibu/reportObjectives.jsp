¸®<%@ page pageEncoding="utf-8" session="true" isELIgnored="false"%>
<%@ include file="../base-include.jsp"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<fmt:bundle basename="${actionBean.bundleName}">
  <head>
  <title><fmt:message key="fibu.kost.reporting" /></title>
  </head>
  <body>

  <stripes:errors />

  <stripes:form method="post" action="/secure/fibu/ReportObjectives.action">
    <stripes:hidden name="selectedValue" />
    <stripes:hidden name="eventKey" />

    <c:choose>
      <c:when test="${actionBean.reportStorage == null}">
        <!--  No report available. Show upload functionality. -->
        <fieldset><legend style="display: inline;"><fmt:message key="upload" /></legend>
        <table class="form">
          <tbody>
            <tr>
              <th><fmt:message key="fibu.kost.reporting.upload" /></th>
              <td><stripes:file name="uploadFile" /></td>
            </tr>
            <tr>
              <td colspan="2" class="buttons" style="direction: ltr;"><stripes:submit name="importReportObjectives" /></td>
            </tr>
          </tbody>
        </table>
        </fieldset>
      </c:when>
      <c:when test="${actionBean.reportStorage.root.load == false}">
        <!--  Report available, but not yet load (selected). -->
        <fieldset><legend><fmt:message key="label.filterSettings" /></legend>
        <table class="form">
          <tbody>
            <tr>
              <th><fmt:message key="label.options" /></th>
              <td><stripes:select name="actionFilter.fromYear">
                <stripes:options-collection collection="${actionBean.fromYearList}" label="label" value="value" />
              </stripes:select>&nbsp;<stripes:select name="actionFilter.fromMonth">
                <stripes:options-collection collection="${actionBean.monthList}" label="label" value="value" />
              </stripes:select>&nbsp;<fmt:message key="until" />&nbsp; <stripes:select name="actionFilter.toYear">
                <stripes:options-collection collection="${actionBean.toYearList}" label="label" value="value" />
              </stripes:select>&nbsp;<stripes:select name="actionFilter.toMonth">
                <stripes:options-collection collection="${actionBean.monthList}" label="label" value="value" />
              </stripes:select></td>
            </tr>
            <tr>
              <td colspan="2" class="buttons"><stripes:submit name="loadReport" /><stripes:submit name="clear" /></td>
            </tr>
          </tbody>
        </table>
        </fieldset>
      </c:when>
      <c:otherwise>
        <!--  Report is available and load. -->
        <!--  If update h1 or h2, please update also buchungssatzList.jsp and jasperReport.jsp. -->
        <c:set var="report" value="${actionBean.reportStorage.currentReport}" />
        <c:set var="rootReport" value="${actionBean.reportStorage.root}" />
        <h1>${report.id} - ${report.title}: ${report.zeitraum}</h1>
        <c:if test="${rootReport.id != report.id}">
          <h2>
            <c:forEach var="ancestor" items="${report.path}">
              <a onclick="javascript:submitSelectedEvent('current', '${ancestor.id}')" href="#">${ancestor.id}</a> -&gt;
            </c:forEach>
            ${report.id}
          </h2>
        </c:if>
        <table class="dataTable">
          <thead>
            <tr>
              <th><a href="#">&nbsp;</a></th>
              <th></th>
              <th>${report.reportObjective.id}</th>
              <c:forEach var="child" items="${report.childs}">
                <th>
                  <c:choose>
                    <c:when test="${child.hasChilds eq true}">
                      <a onclick="javascript:submitSelectedEvent('current', '${child.id}')" href="#">${child.id}</a>
                    </c:when>
                    <c:otherwise>${child.id}</c:otherwise>
                  </c:choose>
                  <stripes:link href="/secure/fibu/BuchungssatzList.action">
                    <stripes:param name="reportId" value="${child.id}" />
                    <pf:image src="/images/attach.png" />
                  </stripes:link>
                </th>
              </c:forEach>
            </tr>
          </thead>
          <tbody>
            <c:set var="style" value="even" />
            <c:forEach var="zeile" items="${actionBean.childBwaTable}">
              <c:if test="${zeile[0].priority.ordinal ge actionBean.priority.ordinal}">
                <c:choose>
                  <c:when test="${style eq 'even'}">
                    <c:set var="style" value="odd" />
                  </c:when>
                  <c:otherwise>
                    <c:set var="style" value="even" />
                  </c:otherwise>
                </c:choose>
                <tr class="${style}">
                  <td><a href="#">${zeile[0].zeile}</a></td>
                  <td><c:forEach begin="0" end="${zeile[0].indent}">&nbsp;</c:forEach>${zeile[0].bezeichnung}</td>
                  <c:set var="col" value="0" />
                  <c:forEach var="entry" items="${zeile}">
                    <c:choose>
                      <c:when test="${col == 0}">
                        <c:set var="tdStyle" value="font-weight: bold;" />
                      </c:when>
                      <c:otherwise>
                        <c:set var="tdStyle" value="" />
                      </c:otherwise>
                    </c:choose>
                    <c:if test="${entry.bwaWert lt 0}">
                        <c:set var="tdStyle" value="${tdStyle}color: red;" />
                    </c:if>
                    <c:set var="col" value="${col + 1}" />
                    <td style="text-align: right; white-space: nowrap; ${tdStyle}">
                      <pf:currency value="${entry.bwaWert}" suppressOutputOfZeroAmount="true" />
                      <stripes:link href="/secure/fibu/BuchungssatzList.action">
                        <stripes:param name="reportId" value="${entry.bwa.reference.id}" />
                        <stripes:param name="bwaZeileId" value="${entry.zeile}" />
                        <pf:image src="/images/attach.png" />
                      </stripes:link>
                    </td>
                  </c:forEach>
                </tr>
              </c:if>
            </c:forEach>
          </tbody>
        </table>
        <table class="form">
          <tbody>
            <tr>
              <td colspan="2" class="buttons" style="direction: ltr;"><stripes:submit name="clear" /></td>
            </tr>
          </tbody>
        </table>
      </c:otherwise>
    </c:choose>
  </stripes:form>
  </body>
</fmt:bundle>
</html>
