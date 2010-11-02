<%@ page pageEncoding="utf-8" session="true" isELIgnored="false"%>
<%@ include file="../base-include.jsp"%>
<fmt:bundle basename="${actionBean.bundleName}">
  <c:if test="${not empty actionBean.actionFilter.searchString}">
    <c:set var="luceneExpression" value="${actionBean.luceneSearchString}" />
    <c:if test="${actionBean.actionFilter.searchString ne luceneExpression}">
      <br />
      <span style="color:#666666;"><fmt:message key="search.lucene.expression" />&nbsp;<b> ${luceneExpression}</b></span>
    </c:if>
  </c:if>
</fmt:bundle>