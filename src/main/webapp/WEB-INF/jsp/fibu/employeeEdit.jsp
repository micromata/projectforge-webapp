®<%@ page pageEncoding="utf-8" session="true" isELIgnored="false"%>
<%@ include file="../base-include.jsp"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<fmt:bundle basename="${actionBean.bundleName}">
  <head>
  <title><c:choose>
    <c:when test="${actionBean.employee.id >= 0}">
      <fmt:message key="fibu.employee.edit.title" />
    </c:when>
    <c:otherwise>
      <fmt:message key="fibu.employee.add.title" />
    </c:otherwise>
  </c:choose></title>
  </head>
  <body>

  <stripes:errors />

  <stripes:form method="post" action="/secure/fibu/EmployeeEdit.action">
    <c:if test="${actionBean.employee.id >= 0}">
      <stripes:hidden name="employee.id" />
    </c:if>
    <stripes:hidden name="userId" />
    <stripes:hidden name="kost1Id" />
    <stripes:hidden name="eventKey" />

    <fieldset><legend><fmt:message key="label.data" /></legend>
    <table class="form">
      <tr>
        <th><fmt:message key="fibu.employee.user" /></th>
        <td><pf:user userId="${actionBean.userId}" select="userId" nullable="true" /></td>
        <th><fmt:message key="fibu.kost1" /></th>
        <td><pf:kost1 kost1Id="${actionBean.kost1Id}" nullable="true" select="kost1Id" /></td>
      </tr>
      <tr>
        <th><fmt:message key="status" /></th>
        <td colspan="3"><stripes:select name="employee.status">
          <stripes:options-collection collection="${actionBean.statusList}" label="label" value="value" />
        </stripes:select></td>
      </tr>
      <tr>
        <th><fmt:message key="address.division" /></th>
        <td><stripes:text class="stdtext" name="employee.abteilung" /></td>
        <th><fmt:message key="address.positionText" /></th>
        <td><stripes:text class="stdtext" name="employee.position" /></td>
      </tr>
      <tr>
        <th><fmt:message key="fibu.employee.wochenstunden" /></th>
        <td><stripes:text class="date" name="employee.wochenstunden" /></td>
        <th><fmt:message key="fibu.employee.urlaubstage" /></th>
        <td><stripes:text class="date" name="employee.urlaubstage" /></td>
      </tr>
      <tr>
        <th><fmt:message key="fibu.employee.eintrittsdatum" /></th>
        <td><stripes:text class="date" name="eintrittsDatum" /><pf:selectDate select="eintrittsDatum" /></td>
        <th><fmt:message key="fibu.employee.austrittsdatum" /></th>
        <td><stripes:text class="date" name="austrittsDatum" /><pf:selectDate select="austrittsDatum" /></td>
      </tr>
      <tr>
        <th><fmt:message key="comment" /></th>
        <td colspan="3"><stripes:textarea class="stdtext" name="employee.comment" style="height:20ex;" /></td>
      </tr>
      <tr>
        <td class="buttons" colspan="4"><jsp:include page="../include/editDOButtons.jsp" /></td>
      </tr>
    </table>
    </fieldset>

    <jsp:include page="../include/history.jsp">
      <jsp:param name="requestURI" value="/secure/fibu/EmployeeEdit.action" />
    </jsp:include>

  </stripes:form>
  </body>
</fmt:bundle>
</html>
