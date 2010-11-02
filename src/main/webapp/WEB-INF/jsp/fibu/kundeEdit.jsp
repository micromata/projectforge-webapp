®<%@ page pageEncoding="utf-8" session="true" isELIgnored="false"%>
<%@ include file="../base-include.jsp"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<fmt:bundle basename="${actionBean.bundleName}">
  <head>
  <title><c:choose>
    <c:when test="${actionBean.kunde.id >= 0}">
      <fmt:message key="fibu.kunde.edit.title" />
      <c:set var="focus" value="kunde.name" />
    </c:when>
    <c:otherwise>
      <fmt:message key="fibu.kunde.add.title" />
      <c:set var="focus" value="kunde.id" />
    </c:otherwise>
  </c:choose></title>
  </head>
  <body>

  <stripes:errors />

  <stripes:form method="post" action="/secure/fibu/KundeEdit.action" focus="${focus}">
    <c:if test="${actionBean.kunde.id >= 0}">
      <stripes:hidden name="kunde.id" />
    </c:if>

    <fieldset><legend><fmt:message key="label.data" /></legend>
    <table class="form">
      <tr>
        <th><fmt:message key="fibu.kunde.nummer" /></th>
        <td><c:choose>
          <c:when test="${actionBean.kunde.id >= 0}">
            <c:out value="${actionBean.kunde.id}" />
          </c:when>
          <c:otherwise>
            <stripes:text name="kunde.id" size="3" maxlength="3" formatType="digits" formatPattern="3" />
          </c:otherwise>
        </c:choose></td>
      </tr>
      <tr>
        <th><fmt:message key="fibu.kunde.name" /></th>
        <td><stripes:text class="stdtext" name="kunde.name" /></td>
      </tr>
    <tr>
      <th><fmt:message key="fibu.kunde.identifier" /></th>
      <td><stripes:text class="stdtext" name="kunde.identifier" /></td>
    </tr>
      <tr>
        <th><fmt:message key="fibu.kunde.division" /></th>
        <td><stripes:text class="stdtext" name="kunde.division" /></td>
      </tr>
      <tr>
        <th><fmt:message key="description" /></th>
        <td><stripes:textarea class="stdtext" name="kunde.description" style="height:20ex;" /></td>
      </tr>
      <tr>
        <th><fmt:message key="status" /></th>
        <td><stripes:select name="kunde.status">
          <stripes:options-collection collection="${actionBean.statusList}" label="label" value="value" />
        </stripes:select></td>
      </tr>
      <tr>
        <td class="buttons" colspan="2"><jsp:include page="../include/editDOButtons.jsp" /></td>
      </tr>
    </table>
    </fieldset>

    <jsp:include page="../include/history.jsp">
      <jsp:param name="requestURI" value="/secure/fibu/KundeEdit.action" />
    </jsp:include>

  </stripes:form>
  </body>
</fmt:bundle>
</html>
