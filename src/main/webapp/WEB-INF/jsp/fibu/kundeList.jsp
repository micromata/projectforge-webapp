<%@ page pageEncoding="utf-8" session="true" isELIgnored="false"%>
<%@ include file="../base-include.jsp"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<fmt:bundle basename="${actionBean.bundleName}">
  <head>
  <title><c:choose>
    <c:when test="${actionBean.selectMode == true}">
      <fmt:message key="fibu.kunde.list.select.title" />
    </c:when>
    <c:otherwise>
      <fmt:message key="fibu.kunde.list.title" />
    </c:otherwise>
  </c:choose></title>
  </head>
  <body>

  <div id="contentMenu"><c:if test="${actionBean.selectMode == false}">
    <stripes:link href="/secure/fibu/KundeEdit.action" event="preEdit">
      <fmt:message key="fibu.kunde.menu.add" />
    </stripes:link>
  </c:if></div>

  <stripes:form method="post" action="/secure/fibu/KundeList.action" focus="actionFilter.searchString">
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
        <td><stripes:checkbox name="actionFilter.deleted" /> <fmt:message key="deleted" /></td>
      </tr>
      <tr>
        <td colspan="2" class="buttons"><stripes:submit name="search" /> <stripes:submit name="reset" /></td>
      </tr>
    </table>
    </fieldset>

  </stripes:form>

  <c:set var="tName">
    <fmt:message key="name" />
  </c:set>
  <fmt:message var="tIdentifier" key="fibu.kunde.identifier" />
  <c:set var="tNummer">
    <fmt:message key="fibu.kunde.nummer" />
  </c:set>
  <c:set var="tDivision">
    <fmt:message key="fibu.kunde.division" />
  </c:set>
  <c:set var="tStatus">
    <fmt:message key="status" />
  </c:set>
  <c:set var="tDescription">
    <fmt:message key="description" />
  </c:set>
  <fieldset><display:table class="dataTable" name="actionBean.list" export="false" id="row"
    requestURI="/secure/fibu/KundeList.action" defaultsort="1" pagesize="1000">
    <c:set var="style">
      <c:choose>
        <c:when test="${row.deleted eq true}">
        text-decoration: line-through;
        </c:when>
        <c:otherwise></c:otherwise>
      </c:choose>
    </c:set>
    <display:column sortable="true" sortProperty="id" title="${tNummer}" style="${style}">
      <c:choose>
        <c:when test="${actionBean.selectMode == true}">
          <a href="javascript:submitSelectedEvent('select', ${row.id})"><pf:image src="${pointerImage}" /></a>
        </c:when>
        <c:otherwise>
          <stripes:link href="/secure/fibu/KundeEdit.action" event="preEdit">
            <stripes:param name="id" value="${row.id}" />
            <pf:image src="${pointerImage}" />
          </stripes:link>
        </c:otherwise>
      </c:choose>
      ${row.id}
    </display:column>
    <display:column sortable="true" title="${tName}" property="name" style="${style}" />
    <display:column sortable="true" title="${tIdentifier}" property="identifier" style="${style}" />
    <display:column sortable="true" title="${tDivision}" property="division" style="${style}" />
    <display:column sortable="true" title="${tStatus}" property="status" style="${style}" />
    <display:column sortable="true" title="${tDescription}" property="description" style="${style}" />
  </display:table></fieldset>

  <c:if test="${actionBean.selectMode == true}">
    <div class="hint"><fmt:message key="hint.selectMode.quickselect" /></div>
  </c:if>

  </body>
</fmt:bundle>
</html>
