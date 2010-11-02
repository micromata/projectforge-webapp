<%@ page pageEncoding="utf-8" session="true" isELIgnored="false"%>
<%@ include file="../base-include.jsp"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<fmt:bundle basename="${actionBean.bundleName}">
  <head>
  <title><fmt:message key="fibu.employee.salary.list.title" /></title>
  </head>
  <body>

  <div id="contentMenu"><stripes:link href="/secure/fibu/EmployeeSalaryEdit.action" event="preEdit">
    <fmt:message key="fibu.employee.salary.menu.add" />
  </stripes:link></div>

  <stripes:form method="post" action="/secure/fibu/EmployeeSalaryList.action" focus="actionFilter.searchString">

    <stripes:errors />

    <fieldset><legend><fmt:message key="label.filterSettings" /></legend>
    <table class="form">
      <tr>
        <th><fmt:message key="searchString" /></th>
        <td><stripes:text class="stdtext" name="actionFilter.searchString" title="${actionBean.searchToolTip}" />&nbsp;<pf:help
          key="tooltip.lucene.link" /><jsp:include page="../include/showLuceneSearchInfo.jsp" /></td>
      </tr>
      <tr>
        <th><fmt:message key="label.options" /></th>
        <td><stripes:select name="year" onchange="javascript:submit();">
          <stripes:options-collection collection="${actionBean.yearList}" label="label" value="value" />
        </stripes:select>&nbsp;<stripes:select name="month" onchange="javascript:submit();">
          <stripes:options-collection collection="${actionBean.monthList}" label="label" value="value" />
        </stripes:select>&nbsp;<stripes:checkbox name="filter.deleted" /> <fmt:message key="deleted" /></td>
      </tr>
      <tr>
        <td colspan="2" class="buttons"><stripes:submit name="search" /> <stripes:submit name="exportAsXls" /> <stripes:submit
          name="reset" /></td>
      </tr>
    </table>
    </fieldset>

  </stripes:form>

  <fmt:message key="name" var="tName" />
  <fmt:message key="firstName" var="tFirstName" />
  <fmt:message key="fibu.employee.salary.type" var="tType" />
  <fmt:message key="fibu.employee.salary.bruttoMitAgAnteil" var="tBruttoMitAgAnteil" />
  <fmt:message key="comment" var="tComment" />
  <fmt:message var="currencyFormat" key="currencyFormat" />

  <fieldset><display:table class="dataTable" name="actionBean.list" export="false" id="row"
    requestURI="/secure/fibu/EmployeeSalaryList.action" defaultsort="1" pagesize="1000">
    <display:column sortable="true" sortProperty="month" title="${tName}">
      <stripes:link href="/secure/fibu/EmployeeSalaryEdit.action" event="preEdit">
        <stripes:param name="id" value="${row.id}" />
        <pf:image src="${pointerImage}" />
      </stripes:link>
      ${row.year}-${row.formattedMonth}
    </display:column>
    <display:column sortable="true" title="${tName}" property="employee.user.lastname" />
    <display:column sortable="true" title="${tFirstName}" property="employee.user.firstname" />
    <display:column sortable="true" title="${tType}" property="type" />
    <display:column sortable="true" title="${tBruttoMitAgAnteil}" property="bruttoMitAgAnteil" format="${currencyFormat}" style="text-align: right;${style}" />
    <display:column sortable="true" title="${tComment}" property="comment" />
  </display:table></fieldset>

  </body>
</fmt:bundle>
</html>
