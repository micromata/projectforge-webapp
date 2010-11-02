<%@ page pageEncoding="utf-8" session="true" isELIgnored="false"%>
<%@ include file="../base-include.jsp"%>
<fmt:bundle basename="${actionBean.bundleName}">

  <c:choose>
    <c:when test="${actionBean.data.deleted == true}">
      <% /* No update or create button for deleted items. */ %>
    </c:when>
    <c:when test="${actionBean.data.id >= 0}">
      <stripes:submit name="update" />
    </c:when>
    <c:otherwise>
      <stripes:submit name="create" />
    </c:otherwise>
  </c:choose>

  <stripes:submit name="cancel" />
  <c:choose>
    <c:when test="${actionBean.data.id >= 0}">
      <c:choose>
        <c:when test="${actionBean.data.deleted == true}">
          <stripes:submit name="undelete" />
        </c:when>
        <c:otherwise>
          <stripes:submit name="markAsDeleted" onclick="return showDeleteQuestionDialog()" />
        </c:otherwise>
      </c:choose>
    </c:when>
    <c:otherwise>
      <stripes:submit name="reset" />
    </c:otherwise>
  </c:choose>
</fmt:bundle>
