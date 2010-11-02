<%@ page pageEncoding="utf-8" session="true" isELIgnored="false"%>
<%@ include file="../base-include.jsp"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<fmt:bundle basename="${actionBean.bundleName}">
  <head>
  <title><c:choose>
    <c:when test="${actionBean.selectMode == true}">
      <fmt:message key="user.list.select.title" />
    </c:when>
    <c:otherwise>
      <fmt:message key="user.list.title" />
    </c:otherwise>
  </c:choose></title>
  </head>
  <body>

  <div id="contentMenu"><c:if test="${actionBean.selectMode == false}">
    <stripes:link href="/secure/user/UserEdit.action" event="preEdit">
      <fmt:message key="user.menu.add" />
    </stripes:link>
  </c:if></div>

  <stripes:form method="post" action="/secure/user/UserList.action" focus="filter.searchString">
    <stripes:hidden name="flowKey" />
    <stripes:hidden name="eventKey" />
    <stripes:hidden name="selectedValue" />

    <fieldset><legend><fmt:message key="label.filterSettings" /></legend>
    <table class="form">
      <tr>
        <th><fmt:message key="searchString" /></th>
        <td><stripes:text class="stdtext" name="filter.searchString" title="${actionBean.searchToolTip}" />&nbsp;<pf:help
          key="tooltip.lucene.link" /><jsp:include page="../include/showLuceneSearchInfo.jsp" /></td>
      </tr>
      <tr>
        <th><fmt:message key="label.options" /></th>
        <td><stripes:checkbox name="filter.deleted" /> <fmt:message key="deleted" /></td>
      </tr>
      <tr>
        <td colspan="2" class="buttons"><stripes:submit name="search" /> <stripes:submit name="reset" /></td>
      </tr>
    </table>
    </fieldset>

  </stripes:form>

  <c:set var="tUsername">
    <fmt:message key="user.username" />
  </c:set>
  <c:set var="tName">
    <fmt:message key="name" />
  </c:set>
  <c:set var="tFirstName">
    <fmt:message key="firstName" />
  </c:set>
  <c:set var="tPersonalPhoneIdentifiers">
    <fmt:message key="user.personalPhoneIdentifiers" />
  </c:set>
  <c:set var="tDescription">
    <fmt:message key="description" />
  </c:set>
  <c:set var="tAssignedGroups">
    <fmt:message key="user.assignedGroups" />
  </c:set>
  <fieldset><legend><fmt:message key="label.resultset" /></legend> <display:table class="dataTable" name="actionBean.list"
    export="false" id="row" requestURI="/secure/user/UserList.action" pagesize="1000">
    <c:set var="style">
      <c:choose>
        <c:when test="${row[6] eq true}">
        text-decoration: line-through;
        </c:when>
        <c:otherwise></c:otherwise>
      </c:choose>
    </c:set>
    <display:column sortable="true" title="${tUsername}" style="${style}">
      <!-- ${row[1]} (for sort) -->
      <c:choose>
        <c:when test="${actionBean.selectMode == true}">
          <a href="javascript:submitSelectedEvent('select', ${row[0]})"><pf:image src="${pointerImage}" /></a>
        </c:when>
        <c:otherwise>
          <stripes:link href="/wa/userList?wicket:bookmarkablePage=:org.projectforge.web.user.UserEditPage" >
            <stripes:param name="id" value="${row[0]}" />
            <pf:image src="${pointerImage}" />
          </stripes:link>
        </c:otherwise>
      </c:choose>
      ${row[1]}
    </display:column>
    <display:column sortable="true" title="${tName}" style="${style}">
      ${row[2]}
    </display:column>
    <display:column sortable="true" title="${tFirstName}" style="${style}">
      ${row[3]}
    </display:column>
    <display:column sortable="true" title="${tPersonalPhoneIdentifiers}" style="${style}">
      ${row[4]}
    </display:column>
    <display:column sortable="true" title="${tDescription}" style="${style}">
      ${row[5]}
    </display:column>
    <display:column sortable="true" title="${tAssignedGroups}" style="${style}">
      ${row[6]}
    </display:column>
  </display:table></fieldset>

  <c:if test="${actionBean.selectMode == true}">
    <div class="hint"><fmt:message key="hint.selectMode.quickselect" /></div>
  </c:if>

  </body>
</fmt:bundle>
</html>
