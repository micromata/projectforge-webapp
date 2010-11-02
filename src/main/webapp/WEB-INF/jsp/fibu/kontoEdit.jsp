®<%@ page pageEncoding="utf-8" session="true" isELIgnored="false"%>
<%@ include file="../base-include.jsp"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<fmt:bundle basename="${actionBean.bundleName}">
  <head>
  <title><c:choose>
    <c:when test="${actionBean.konto.id >= 0}">
      <fmt:message key="fibu.konto.edit.title" />
    </c:when>
    <c:otherwise>
      <fmt:message key="fibu.konto.add.title" />
    </c:otherwise>
  </c:choose></title>
  </head>
  <body>

  <stripes:errors />

  <stripes:form method="post" action="/secure/fibu/KontoEdit.action">
    <stripes:hidden name="konto.id" />

    <fieldset><legend><fmt:message key="label.data" /></legend>
    <table class="form">
      <tr>
        <th><fmt:message key="fibu.konto.nummer" /></th>
        <td><stripes:text size="8" maxlength="8" name="konto.nummer" /></td>
      </tr>
      <tr>
        <th><fmt:message key="fibu.konto.bezeichnung" /></th>
        <td><stripes:text class="stdtext" name="konto.bezeichnung" /></td>
      </tr>
      <tr>
        <th><fmt:message key="description" /></th>
        <td><stripes:textarea class="stdtext" name="konto.description" style="height:20ex;" /></td>
      </tr>
      <tr>
        <td class="buttons" colspan="2"><jsp:include page="../include/editDOButtons.jsp" /></td>
      </tr>
    </table>
    </fieldset>

    <jsp:include page="../include/history.jsp">
      <jsp:param name="requestURI" value="/secure/fibu/KontoEdit.action" />
    </jsp:include>

  </stripes:form>
  </body>
</fmt:bundle>
</html>
