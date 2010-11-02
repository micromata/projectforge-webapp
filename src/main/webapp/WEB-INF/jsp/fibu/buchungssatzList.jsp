<%@ page pageEncoding="utf-8" session="true" isELIgnored="false"%>
<%@ include file="../base-include.jsp"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<fmt:bundle basename="${actionBean.bundleName}">
  <head>
  <title><fmt:message key="fibu.buchungssatz.list.title" /></title>
  <script type="text/javascript">
   <!--
    function toggleBwa() {
      $("#bwa").toggle("normal");
    }
   // -->
  </script>
  </head>
  <body>

  <div id="contentMenu"><c:if test="${actionBean.report != null}">
    <!--  Report is available and load. -->
    <!--  If update h1 or h2, please update also report.jsp. -->
    <c:set var="report" value="${actionBean.report}" />
    <h3><c:forEach var="ancestor" items="${report.path}">${ancestor.id} -&gt;</c:forEach> ${report.id}</h3>
  </c:if></div>

  <stripes:errors />

  <stripes:form method="post" action="/secure/fibu/BuchungssatzList.action" focus="actionFilter.searchString">
    <stripes:hidden name="flowKey" />
    <stripes:hidden name="eventKey" />
    <stripes:hidden name="selectedValue" />
    <stripes:hidden name="reportId" />
    <stripes:hidden name="bwaZeileId" />

    <fieldset><legend><fmt:message key="label.filterSettings" /></legend>
    <table class="form">
      <c:if test="${actionBean.report == null}">
        <tr>
          <th><fmt:message key="searchString" /></th>
          <td><stripes:text class="stdtext" name="actionFilter.searchString" title="${actionBean.searchToolTip}" />&nbsp;<pf:help
            key="tooltip.lucene.link" /><jsp:include page="../include/showLuceneSearchInfo.jsp" /></td>
        </tr>
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
          </stripes:select>,&nbsp;<jsp:include page="../include/pageSizeOption.jsp" /></td>
        </tr>
        <tr>
          <td colspan="2" class="buttons"><stripes:submit name="search" /> <stripes:submit name="reset" /></td>
        </tr>
      </c:if>
      <c:if test="${actionBean.bwa != null}">
        <tr>
          <th>BWA<a onclick="javascript:toggleBwa()" href="#"><pf:image src="/images/zoom.png" /></a></th>
          <td>Gesamtleistung:&nbsp;<pf:currency value="${actionBean.bwa.gesamtleistung.bwaWert}" />,&nbsp;Mat./Wareneinkauf:&nbsp;<pf:currency
            value="${actionBean.bwa.matWareneinkauf.bwaWert}" />,&nbsp;Vorläufiges Ergebnis:&nbsp;<pf:currency
            value="${actionBean.bwa.vorlaeufigesErgebnis.bwaWert}" /></td>
        </tr>
      </c:if>
      <tr>
        <td colspan="2">
        <div id="bwa" style="display: none;"><pre>${actionBean.bwa}</pre></div>
        </td>
      </tr>
    </table>
    </fieldset>

  </stripes:form>

  <c:set var="tSatznummer">
    <fmt:message key="fibu.buchungssatz.satznr" />
  </c:set>
  <c:set var="tBetrag">
    <fmt:message key="fibu.common.betrag" />
  </c:set>
  <c:set var="tBeleg">
    <fmt:message key="fibu.buchungssatz.beleg" />
  </c:set>
  <c:set var="tKost1">
    <fmt:message key="fibu.kost1" />
  </c:set>
  <c:set var="tKost2">
    <fmt:message key="fibu.kost2" />
  </c:set>
  <c:set var="tKonto">
    <fmt:message key="fibu.buchungssatz.konto" />
  </c:set>
  <c:set var="tGegenKonto">
    <fmt:message key="fibu.buchungssatz.gegenKonto" />
  </c:set>
  <c:set var="tSH">
    <fmt:message key="fibu.buchungssatz.sh" />
  </c:set>
  <c:set var="tText">
    <fmt:message key="fibu.buchungssatz.text" />
  </c:set>
  <c:set var="tComment">
    <fmt:message key="comment" />
  </c:set>
  <c:set var="currencyFormat">
    <fmt:message key="currencyFormat" />
  </c:set>

  <fieldset><display:table class="dataTable" name="actionBean.list" export="false" id="row"
    requestURI="/secure/fibu/BuchungssatzList.action" defaultsort="1" pagesize="${actionBean.pageSize}">
    <c:set var="style">
      <c:choose>
        <c:when test="${row.deleted eq true}">
        text-decoration: line-through;
        </c:when>
        <c:otherwise></c:otherwise>
      </c:choose>
    </c:set>
    <display:column sortable="true" title="${tSatznummer}" sortProperty="formattedSatzNummer" style="${style}">
      <stripes:link href="/secure/fibu/BuchungssatzEdit.action" event="preEdit">
        <stripes:param name="id" value="${row.id}" />
        <pf:image src="${pointerImage}" />
      </stripes:link>
      ${row.formattedSatzNummer}
    </display:column>
    <display:column sortable="true" title="${tBetrag}" style="text-align: right;${style}">
      <c:choose>
        <c:when test="${row.betrag < 0}">
          <span style="color: red;"><pf:currency value="${row.betrag}" /></span>
        </c:when>
        <c:otherwise>
          <pf:currency value="${row.betrag}" />
        </c:otherwise>
      </c:choose>
    </display:column>
    <display:column sortable="true" title="${tBeleg}" property="beleg" style="${style}" />
    <display:column sortable="true" sortProperty="kost1.shortDisplayName" title="${tKost1}" style="${style}">
      <span title="${row.kost1.description}">${row.kost1.shortDisplayName}</span>
    </display:column>
    <display:column sortable="true" sortProperty="kost2.shortDisplayName" title="${tKost2}" style="${style}">
      <span title="${row.kost2.toolTip}">${row.kost2.shortDisplayName}</span>
    </display:column>
    <display:column sortable="true" sortProperty="konto.shortDisplayName" title="${tKonto}" style="${style}">
      <span title="${row.konto.bezeichnung}">${row.konto.shortDisplayName}</span>
    </display:column>
    <display:column sortable="true" sortProperty="gegenkonto.shortDisplayName" title="${tGegenKonto}" style="${style}">
      <span title="${row.gegenKonto.bezeichnung}">${row.gegenKonto.shortDisplayName}</span>
    </display:column>
    <display:column sortable="true" title="${tSH}" property="sh" style="${style}" />
    <display:column sortable="true" title="${tText}" property="text" style="${style}" />
    <display:column sortable="true" title="${tComment}" property="comment" style="${style}" />
  </display:table></fieldset>

  <c:if test="${actionBean.selectMode == true}">
    <div class="hint"><fmt:message key="hint.selectMode.quickselect" /></div>
  </c:if>

  </body>
</fmt:bundle>
</html>
