<%@ page pageEncoding="utf-8" session="true" isELIgnored="false"%>
<%@ include file="../base-include.jsp"%>
<fmt:bundle basename="${actionBean.bundleName}">
  <c:if test="${actionBean.showHistory == true}">
    <fieldset><legend><fmt:message key="label.historyOfChanges" /></legend> <c:if test="${actionBean.baseDO == true}">
      <table class="form">
        <tr>
          <th><fmt:message key="timeOfCreation" /></th>
          <td><pf:date date="${actionBean.data.created}" /></td>
        </tr>
        <tr>
          <th><fmt:message key="timeOfLastUpdate" /></th>
          <td><pf:date date="${actionBean.data.lastUpdate}" /></td>
        </tr>
      </table>
    </c:if> <c:if test="${actionBean.history != null}">
      <c:set var="tTimestamp">
        <fmt:message key="timestamp" />
      </c:set>
      <c:set var="tUser">
        <fmt:message key="user" />
      </c:set>
      <c:set var="tEntryType">
        <fmt:message key="history.entryType" />
      </c:set>
      <c:set var="tPropertyName">
        <fmt:message key="history.propertyName" />
      </c:set>
      <c:set var="tNewValue">
        <fmt:message key="history.newValue" />
      </c:set>
      <display:table class="history" name="actionBean.history" export="false" id="entry"
        requestURI='<%= request.getParameter("requestURI") %>' pagesize="1000">
        <display:column sortable="true" title="${tTimestamp}" property="timestamp" decorator="dateTimeColumnDecorator" />
        <display:column sortable="true" title="${tUser}" property="user.username" />
        <display:column sortable="true" title="${tEntryType}" property="entryType" />
        <display:column sortable="true" title="${tPropertyName}" property="propertyName" />
        <display:column sortable="true" title="${tNewValue}"><c:choose>
            <c:when test="${not empty entry.oldValue and entry.oldValue != 'null'}">
              <a title="${entry.oldValue}">${entry.newValue}</a>
            </c:when>
            <c:otherwise>${entry.newValue}</c:otherwise>
          </c:choose>
        </display:column>
      </display:table></fieldset>
  </c:if>
  <%
    /* actionBean.history */
  %>
  </c:if>
  <%
    /* actionBean.showHistory */
  %>
</fmt:bundle>
