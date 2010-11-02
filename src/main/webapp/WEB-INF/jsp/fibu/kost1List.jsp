<%@ page pageEncoding="utf-8" session="true" isELIgnored="false"%>
<%@ include file="../base-include.jsp"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<fmt:bundle basename="${actionBean.bundleName}">
  <head>
  <title><c:choose>
    <c:when test="${actionBean.selectMode == true}">
      <fmt:message key="fibu.kost1.list.select.title" />
    </c:when>
    <c:otherwise>
      <fmt:message key="fibu.kost1.list.title" />
    </c:otherwise>
  </c:choose></title>
  </head>
  <body>

  <div id="contentMenu"><c:if test="${actionBean.selectMode == false}">
    <stripes:link href="/secure/fibu/Kost1Edit.action" event="preEdit">
      <fmt:message key="fibu.kost1.menu.add" />
    </stripes:link>
  </c:if></div>

  <stripes:form method="post" action="/secure/fibu/Kost1List.action" focus="actionFilter.searchString">
    <stripes:hidden name="flowKey" />
    <stripes:hidden name="eventKey" />
    <stripes:hidden name="selectedValue" />

    <fieldset><legend><fmt:message key="label.filterSettings" /></legend>
    <table class="form">
      <tr>
        <th><fmt:message key="searchString" /></th>
        <td><stripes:text class="stdtext" name="actionFilter.searchString" title="${actionBean.searchToolTip}" />&nbsp;<pf:help
          key="tooltip.lucene.link" /><jsp:include page="../include/showLuceneSearchInfo.jsp" /></td>
      </tr>
      <tr>
        <th><fmt:message key="label.options" /></th>
        <td><stripes:select name="actionFilter.listType" onclick="javascript:submit();">
          <stripes:options-collection collection="${actionBean.filterTypeList}" label="label" value="value" />
        </stripes:select></td>
      </tr>
      <tr>
        <td colspan="2" class="buttons"><stripes:submit name="search" /> <stripes:submit name="reset" /></td>
      </tr>
    </table>
    </fieldset>

  </stripes:form>

  <fmt:message var="tNummer" key="fibu.kost1" />
  <fmt:message var="tDescription" key="description" />
  <fmt:message var="tStatus" key="status" />
  <fieldset><display:table class="dataTable" name="actionBean.list" export="false" id="row"
    requestURI="/secure/fibu/Kost1List.action" defaultsort="1" pagesize="1000">
    <c:set var="style">
      <c:choose>
        <c:when test="${row.deleted eq true or row.kostentraegerStatus eq 'ENDED'}">
        text-decoration: line-through;
        </c:when>
        <c:otherwise></c:otherwise>
      </c:choose>
    </c:set>
    <display:column sortable="true" sortProperty="shortDisplayName" title="${tNummer}" style="${style}" class="row-link">
      <c:choose>
        <c:when test="${actionBean.selectMode == true}">
          <a href="javascript:submitSelectedEvent('select', ${row.id})"><pf:image src="${pointerImage}" /></a>
        </c:when>
        <c:otherwise>
          <stripes:link href="/secure/fibu/Kost1Edit.action" event="preEdit">
            <stripes:param name="id" value="${row.id}" />
            <pf:image src="${pointerImage}" />
          </stripes:link>
        </c:otherwise>
      </c:choose>
      ${row.shortDisplayName}
    </display:column>
    <display:column sortable="true" title="${tDescription}" property="description" style="${style}" />
    <display:column sortable="true" title="${tStatus}" property="kostentraegerStatus" style="${style}" />
  </display:table></fieldset>

  <c:if test="${actionBean.selectMode == true}">
    <div class="hint"><fmt:message key="hint.selectMode.quickselect" /></div>
  </c:if>

  </body>
</fmt:bundle>
</html>
