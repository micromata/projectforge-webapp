¸®<%@ page pageEncoding="utf-8" session="true" isELIgnored="false"%>
<%@ include file="../base-include.jsp"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<fmt:bundle basename="${actionBean.bundleName}">
  <head>
  <title><fmt:message key="fibu.datev.import" /></title>
  </head>
  <body>

  <stripes:errors />

  <stripes:form method="post" action="/secure/fibu/datevImport.action">
    <stripes:hidden name="selectedValue" />
    <stripes:hidden name="eventKey" />
    <fieldset><legend style="display: inline;"><fmt:message key="upload" /></legend>
    <table class="form">
      <tr>
        <th><fmt:message key="fibu.datev.upload" /></th>
        <td><stripes:file name="uploadFile" /></td>
      </tr>
      <tr>
        <th>&nbsp;</th>
        <td><stripes:radio name="listType" value="all" onclick="javascript:submit();" /><fmt:message key="filter.all" />&nbsp; <stripes:radio
          name="listType" value="modified" onclick="javascript:submit();" /><fmt:message key="modified" />&nbsp; <stripes:radio
          name="listType" value="faulty" onclick="javascript:submit();" /><fmt:message key="filter.faulty" /></td>
      </tr>
      <tr>
        <td colspan="2" class="buttons" style="direction: ltr;"><stripes:submit name="importKontenplan" value="Import Kontenplan_" /><stripes:submit
          name="importBuchungsdaten" value="Import Buchungsdaten_" /><c:if test="${not empty actionBean.storage}">
          <stripes:submit name="clear" />
        </c:if></td>
      </tr>
    </table>
    </fieldset>
    <c:if test="${not empty actionBean.storage}">
      <h1>Import storage_: ${actionBean.storage.filename}</h1>
      <div id="infolayer">
      <c:choose>
      <c:when test="${actionBean.errorProperties != null}">
      <table>
        <tr>
          <th>Key_</th>
          <th>Error values_</th>
        </tr>
        <c:forEach var="prop" items="${actionBean.errorProperties}">
        <tr>
          <td>${prop.key}</td>
          <td>
            <c:forEach var="val" items="${prop.value}">
              ${val},
            </c:forEach>
          </td>
        </tr>
        </c:forEach>
      </table>
      </c:when>
      <c:when test="${actionBean.bwa != null}">
        <pre>${actionBean.bwa}</pre>
      </c:when>
      </c:choose>
      </div>
      <c:forEach var="sheet" items="${actionBean.storage.sheets}">
        <fieldset class="section"><legend>Sheet_: ${sheet.name}&nbsp;<c:choose>
          <c:when test="${sheet.reconciled == true}">(<fmt:message key="common.import.status.${sheet.status.key}" />
            <c:if test="${sheet.numberOfCommittedElements >= 0}">: #${sheet.numberOfCommittedElements}</c:if>)</c:when>
          <c:otherwise>(<fmt:message key="common.import.status.notReconciled" />)</c:otherwise>
        </c:choose></legend> <c:choose>
          <c:when test="${sheet.open}">
            <a onclick="javascript:submitSelectedEvent('close', '${sheet.name}')" href="#"> <pf:image src="/images/zoom_out.png" /> </a>
          </c:when>
          <c:otherwise>
            <a onclick="javascript:submitSelectedEvent('open', '${sheet.name}')" href="#"> <pf:image src="/images/zoom_in.png" /> </a>
          </c:otherwise>
        </c:choose> Total_=${sheet.totalNumberOfElements} <c:if test="${sheet.numberOfNewElements gt 0}">
          | New_=<span style="color: red;">${sheet.numberOfNewElements}</span>
        </c:if> <c:if test="${sheet.numberOfModifiedElements gt 0}">
          | Modified_=<span style="color: red;">${sheet.numberOfModifiedElements}</span>
        </c:if> <c:if test="${sheet.numberOfUnmodifiedElements gt 0}">
          | Unmodified_=${sheet.numberOfUnmodifiedElements}
        </c:if> <c:if test="${sheet.numberOfFaultyElements gt 0}">
          <span style="color: red; font-weight: bold;"> | Faulty_=${sheet.numberOfFaultyElements}</span>
        </c:if> <c:choose>
          <c:when test="${sheet.reconciled == false or sheet.status == 'IMPORTED' or sheet.status == 'NOTHING_TODO' or sheet.status == 'HAS_ERRORS'}">
            <a onclick="javascript:submitSelectedEvent('reconcile', '${sheet.name}')" href="#"> Verproben_ </a>
          </c:when>
          <c:when test="${sheet.reconciled == true}">
            <a onclick="javascript:submitSelectedEvent('commit', '${sheet.name}')" href="#"> Commit_ </a>
            <a onclick="javascript:submitSelectedEvent('selectAll', '${sheet.name}')" href="#"> Select all_ </a>
            <a onclick="javascript:submitSelectedEvent('deselectAll', '${sheet.name}')" href="#"> Unselect all_ </a>
            <c:if test="${actionBean.storageType eq 'BUCHUNGSSAETZE'}"></c:if>
          </c:when>
        </c:choose>
        <c:if test="${sheet.faulty == true}">
          <a onclick="javascript:submitSelectedEvent('showErrorSummary', '${sheet.name}')" href="#"> show error summary_ </a>
        </c:if>
        <c:if test="${actionBean.storageType eq 'BUCHUNGSSAETZE'}">
         <a onclick="javascript:submitSelectedEvent('showBWA', '${sheet.name}')" href="#"> show BWA_ </a>
         </c:if>
         <c:if test="${sheet.open}">
          <%
            /* Please regard search.jsp for changes and vice versa. */
          %>
          <table class="dataTable">
            <tbody>
              <c:set var="trStyle" value="even" />
              <tr>
                <th>&nbsp;</th>
                <c:choose>
                  <c:when test="${actionBean.storageType eq 'KONTENPLAN'}">
                    <th><fmt:message key="fibu.konto.nummer" /></th>
                    <th><fmt:message key="fibu.konto.bezeichnung" /></th>
                  </c:when>
                  <c:when test="${actionBean.storageType eq 'BUCHUNGSSAETZE'}">
                    <th><!-- fmt:message key="fibu.buchungssatz.satznr" /--></th>
                    <th><fmt:message key="date" /></th>
                    <th><fmt:message key="fibu.common.betrag" /></th>
                    <th><fmt:message key="fibu.buchungssatz.text" /></th>
                    <th><fmt:message key="fibu.buchungssatz.konto" /></th>
                    <th><fmt:message key="fibu.buchungssatz.gegenKonto" /></th>
                    <th><fmt:message key="fibu.kost1" /></th>
                    <th><fmt:message key="fibu.kost2" /></th>
                  </c:when>
                </c:choose>
                <th><fmt:message key="modifications" /></th>
                <th><fmt:message key="errors" /></th>
              </tr>
              <c:forEach var="element" items="${sheet.elements}">
                <c:set var="obj" value="${element.value}" />
                <c:set var="dbObj" value="${element.oldValue}" />
                <c:if test="${actionBean.listType eq 'all' or (actionBean.listType eq 'faulty' and element.faulty) or (actionBean.listType eq 'modified' and (element.new or element.modified or element.faulty))}">
                  <c:choose>
                    <c:when test="${trStyle eq 'even'}">
                      <c:set var="trStyle" value="odd" />
                    </c:when>
                    <c:otherwise>
                      <c:set var="trStyle" value="even" />
                    </c:otherwise>
                  </c:choose>
                  <c:set var="style">
                    <c:choose>
                      <c:when test="${element.faulty}">color: red;</c:when>
                      <c:when test="${dbObj != null and dbObj.deleted == true}">text-decoration: line-through;</c:when>
                      <c:otherwise></c:otherwise>
                    </c:choose>
                  </c:set>
                  <tr class="${trStyle}">
                    <td><a href="#"><c:if test="${sheet.status == 'RECONCILED'}"><stripes:checkbox name="selectedItems[${element.index}]"></stripes:checkbox></c:if><c:choose>
                      <c:when test="${element.new}">
                        <pf:image src="/images/add.png" />
                      </c:when>
                      <c:when test="${element.modified}">
                        <pf:image src="/images/pencil.png" />
                      </c:when>
                      <c:otherwise>
                        <pf:image src="/images/page_white.png" />
                      </c:otherwise>
                    </c:choose></a></td>
                    <c:choose>
                      <c:when test="${fn:contains(element.value.class.name, 'org.projectforge.fibu.KontoDO')}">
                        <td style="white-space: nowrap; text-align: right;${style}">${obj.nummer}</td>
                        <td style="${style}">${obj.bezeichnung}</td>
                      </c:when>
                      <c:otherwise>
                        <td style="white-space: nowrap; text-align: right;${style}">${obj.satznr}</td>
                        <td style="${style}"><pf:date date="${obj.datum}" type="date" /></td>
                        <td style="white-space: nowrap; text-align: right;${style}"><pf:currency value="${obj.betrag}" /></td>
                        <td style="${style}">${obj.text}</td>
                        <td style="${style}"><c:if test="${obj.konto != null}">${obj.konto.nummer}</c:if></td>
                        <td style="${style}"><c:if test="${obj.gegenKonto != null}">${obj.gegenKonto.nummer}</c:if></td>
                        <td style="${style}"><c:if test="${obj.kost1 != null}">${obj.kost1.shortDisplayName}</c:if></td>
                        <td style="${style}"><c:if test="${obj.kost2 != null}">${obj.kost2.shortDisplayName}</c:if></td>
                      </c:otherwise>
                    </c:choose>
                    <td style="${style}"><c:if test="${dbObj != null}">
                      <c:forEach items="${element.propertyChanges}" var="delta">
                          ${delta.propertyName}=<a title="${delta.oldValue}">${delta.newValue}</a>;
                        </c:forEach>
                    </c:if>
                    <td><c:if test="${element.faulty}">
                      <span style="color: red; font-weight: bold;${style}">${element.errorProperties}</span>
                    </c:if></td>
                  </tr>
                </c:if>
              </c:forEach>
            </tbody>
          </table>
        </c:if></fieldset>
      </c:forEach>
    </c:if>
  </stripes:form>
  </body>
</fmt:bundle>
</html>
