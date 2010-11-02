<%@ page pageEncoding="utf-8" session="true" isELIgnored="false"%>
<%@ include file="../base-include.jsp"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<fmt:bundle basename="${actionBean.bundleName}">
  <head>
  <title><fmt:message key="fibu.konto.list.title" /></title>
  </head>
  <body>

  <div id="contentMenu"><stripes:link href="/secure/fibu/KontoEdit.action" event="preEdit">
    <fmt:message key="fibu.konto.menu.add" />
  </stripes:link></div>

  <stripes:form method="post" action="/secure/fibu/KontoList.action" focus="actionFilter.searchString">

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

  <c:set var="tNummer">
    <fmt:message key="fibu.konto.nummer" />
  </c:set>
  <c:set var="tBezeichnung">
    <fmt:message key="fibu.konto.bezeichnung" />
  </c:set>
  <c:set var="tDescription">
    <fmt:message key="description" />
  </c:set>
  <fieldset><display:table class="dataTable" name="actionBean.list" export="false" id="row"
    requestURI="/secure/fibu/KontoList.action" defaultsort="1" pagesize="1000">
    <display:column sortable="true" sortProperty="nummer" title="${tNummer}" style="text-align: right; width: 10%;">
      <stripes:link href="/secure/fibu/KontoEdit.action" event="preEdit">
        <stripes:param name="id" value="${row.id}" />
        <pf:image src="${pointerImage}" />
      </stripes:link>
      ${row.nummer}
    </display:column>
    <display:column sortable="true" title="${tBezeichnung}" property="bezeichnung" />
    <display:column sortable="true" title="${tDescription}" property="description" />
  </display:table></fieldset>

  </body>
</fmt:bundle>
</html>
