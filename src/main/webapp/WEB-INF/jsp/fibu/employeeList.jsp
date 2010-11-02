<%@ page pageEncoding="utf-8" session="true" isELIgnored="false"%>
<%@ include file="../base-include.jsp"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<fmt:bundle basename="${actionBean.bundleName}">
  <head>
  <title><fmt:message key="fibu.employee.list.title" /></title>
  </head>
  <body>

  <div id="contentMenu"><stripes:link href="/secure/fibu/EmployeeEdit.action" event="preEdit">
    <fmt:message key="fibu.employee.menu.add" />
  </stripes:link></div>

  <stripes:form method="post" action="/secure/fibu/EmployeeList.action" focus="actionFilter.searchString">
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
        <td><stripes:checkbox name="actionFilter.showOnlyActiveEntries" onchange="javascript:submit();" /> <fmt:message
          key="label.onlyActiveEntries" />&nbsp;<stripes:checkbox name="actionFilter.deleted" /> <fmt:message key="deleted" /></td>
      </tr>
      <tr>
        <td colspan="2" class="buttons"><stripes:submit name="search" /> <stripes:submit name="reset" /></td>
      </tr>
    </table>
    </fieldset>

  </stripes:form>

  <fmt:message key="name" var="tName" />
  <fmt:message key="firstName" var="tFirstName" />
  <fmt:message key="status" var="tStatus" />
  <fmt:message key="address.positionText" var="tPosition" />
  <fmt:message key="address.division" var="tAbteilung" />
  <fmt:message key="fibu.employee.eintrittsdatum" var="tEintrittsDatum" />
  <fmt:message key="fibu.employee.austrittsdatum" var="tAustrittsDatum" />
  <fmt:message key="fibu.kost1" var="tKost1" />
  <fmt:message key="comment" var="tComment" />

  <fieldset><display:table class="dataTable" name="actionBean.list" export="false" id="row" requestURI="/secure/fibu/EmployeeList.action"
    defaultsort="1" pagesize="1000">
    <display:column sortable="true" sortProperty="user.lastname" title="${tName}">
      <c:choose>
        <c:when test="${actionBean.selectMode == true}">
          <a href="javascript:submitSelectedEvent('select', ${row.id})"><pf:image src="${pointerImage}" /></a>
        </c:when>
        <c:otherwise>
          <stripes:link href="/secure/fibu/EmployeeEdit.action" event="preEdit">
            <stripes:param name="id" value="${row.id}" />
            <pf:image src="${pointerImage}" />
          </stripes:link>
        </c:otherwise>
      </c:choose>
      ${row.user.lastname}
    </display:column>
    <display:column sortable="true" title="${tFirstName}" property="user.firstname" />
    <display:column sortable="true" title="${tStatus}" property="status" />
    <display:column sortable="true" sortProperty="kost1.nummer" title="${tKost1}">
      <pf:kost1 kost1Id="${row.kost1.id}" nullable="true" />
    </display:column>
    <display:column sortable="true" title="${tPosition}" property="position" />
    <display:column sortable="true" title="${tAbteilung}" property="abteilung" />
    <display:column sortable="true" title="${tEintrittsDatum}" style="white-space: nowrap;${style}" sortProperty="eintrittsDatum"
      defaultorder="descending">
      <pf:date date="${row.eintrittsDatum}" type="date" />
    </display:column>
    <display:column sortable="true" title="${tAustrittsDatum}" style="white-space: nowrap;${style}" sortProperty="austrittsDatum"
      defaultorder="descending">
      <pf:date date="${row.austrittsDatum}" type="date" />
    </display:column>
    <display:column sortable="true" title="${tComment}" property="comment" />
  </display:table></fieldset>

  </body>
</fmt:bundle>
</html>
