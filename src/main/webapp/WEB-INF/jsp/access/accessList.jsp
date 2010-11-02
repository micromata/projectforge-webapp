<%@ page pageEncoding="utf-8" session="true" isELIgnored="false"%>
<%@ include file="../base-include.jsp"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<fmt:bundle basename="${actionBean.bundleName}">
  <head>
  <title><fmt:message key="access.list.title" /></title>
  </head>
  <body>

  <div id="contentMenu">
  <table>
    <tr>
      <td><stripes:link href="/secure/access/AccessEdit.action" event="preEdit">
        <fmt:message key="menu.addNewEntry" />
      </stripes:link></td>
    </tr>
  </table>
  </div>

  <stripes:form method="post" action="/secure/access/AccessList.action" focus="actionFilter.searchString">

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

  <c:set var="tGroup">
    <fmt:message key="group" />
  </c:set>
  <c:set var="tTask">
    <fmt:message key="task" />
  </c:set>
  <c:set var="tAccessType">
    <fmt:message key="access.type" />
  </c:set>
  <fmt:message var="tRecursive" key="recursive" />
  <fieldset><legend><fmt:message key="label.resultset" /></legend>
  <div style="text-align: right;"><fmt:message key="access.info.accessRights" /> <pf:image src="/images/database_select.png"
    tooltip="access.tooltip.selectAccess" /> <pf:image src="/images/database_insert.png" tooltip="access.tooltip.insertAccess" /> <pf:image
    src="/images/database_update.png" tooltip="access.tooltip.updateAccess" /> <pf:image src="/images/database_delete.png"
    tooltip="access.tooltip.deleteAccess" /></div>

  <display:table class="dataTable" style="width:100%;" name="actionBean.list" export="false" id="row" requestURI="/secure/access/AccessList.action"
    pagesize="1000">
    <c:set var="style">
      <c:choose>
        <c:when test="${row.deleted eq true}">
        text-decoration: line-through;
        </c:when>
        <c:otherwise></c:otherwise>
      </c:choose>
    </c:set>
    <display:column sortable="true" sortProperty="task.title" title="${tTask}" style="${style}">
      <stripes:link href="/secure/access/AccessEdit.action" event="preEdit">
        <pf:image src="${pointerImage}" />
        <stripes:param name="id" value="${row.id}" />
      </stripes:link>
      ${row.task.title}
    </display:column>
    <display:column sortable="true" title="${tGroup}" property="group.name" style="${style}" />
    <display:column sortable="true" title="${tRecursive}" property="recursive" style="${style}" />
    <display:column sortable="false" title="${tAccessType}" style="text-align: right;">
      <table style="text-align: right;">
        <c:forEach var="entry" items="${row.orderedEntries}">
          <tr>
            <td><fmt:message key='access.type.${entry.accessType}' /></td>
            <td><pf:boolean value="${entry.accessSelect}" showFalse="true" displayFormat="ticker" /></td>
            <td><pf:boolean value="${entry.accessInsert}" showFalse="true" displayFormat="ticker" /></td>
            <td><pf:boolean value="${entry.accessUpdate}" showFalse="true" displayFormat="ticker" /></td>
            <td><pf:boolean value="${entry.accessDelete}" showFalse="true" displayFormat="ticker" /></td>
          </tr>
        </c:forEach>
      </table>
    </display:column>
    <c:set var="count" value="${count + 1}" />
  </display:table></fieldset>

  </body>
</fmt:bundle>
</html>
