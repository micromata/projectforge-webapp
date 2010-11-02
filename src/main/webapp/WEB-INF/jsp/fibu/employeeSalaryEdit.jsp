®<%@ page pageEncoding="utf-8" session="true" isELIgnored="false"%>
<%@ include file="../base-include.jsp"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<fmt:bundle basename="${actionBean.bundleName}">
  <head>
  <title><c:choose>
    <c:when test="${actionBean.employeeSalary.id >= 0}">
      <fmt:message key="fibu.employee.salary.edit.title" />
    </c:when>
    <c:otherwise>
      <fmt:message key="fibu.employee.salary.add.title" />
    </c:otherwise>
  </c:choose></title>
  </head>
  <body>

  <stripes:errors />

  <stripes:form method="post" action="/secure/fibu/EmployeeSalaryEdit.action">
    <c:if test="${actionBean.employeeSalary.id >= 0}">
      <stripes:hidden name="employeeSalary.id" />
    </c:if>
    <stripes:hidden name="employeeId" />
    <stripes:hidden name="eventKey" />

    <fieldset><legend><fmt:message key="label.data" /></legend>
    <table class="form">
      <tr>
        <th><fmt:message key="fibu.employee" /></th>
        <td><pf:employee employeeId="${actionBean.employeeSalary.employeeId}" select="employeeId" nullable="true" /></td>
      </tr>
      <tr>
        <th><fmt:message key="calendar.month" /></th>
        <td><stripes:text name="employeeSalary.year" class="stdtext" style="width:4em;" />&nbsp;<stripes:select
          name="employeeSalary.month">
          <stripes:options-collection collection="${actionBean.monthList}" label="label" value="value" />
        </stripes:select></td>
      </tr>
      <tr>
        <th><fmt:message key="fibu.employee.salary.type" /></th>
        <td><stripes:select name="employeeSalary.type">
          <stripes:options-collection collection="${actionBean.typeList}" label="label" value="value" />
        </stripes:select></td>
      </tr>
      <tr>
        <th><fmt:message key="fibu.employee.salary.bruttoMitAgAnteil" /></th>
        <td style="white-space: nowrap;"><stripes:text class="stdtext" name="employeeSalary.bruttoMitAgAnteil" style="width:8em;"
          formatType="currency" formatPattern="decimal" /></td>
      </tr>
      <tr>
        <th><fmt:message key="comment" /></th>
        <td><stripes:textarea class="stdtext" name="employeeSalary.comment" style="height:20ex;" /></td>
      </tr>
      <tr>
        <td class="buttons" colspan="2"><jsp:include page="../include/editDOButtons.jsp" /></td>
      </tr>
    </table>
    </fieldset>

    <jsp:include page="../include/history.jsp">
      <jsp:param name="requestURI" value="/secure/fibu/EmployeeSalaryEdit.action" />
    </jsp:include>

  </stripes:form>
  </body>
</fmt:bundle>
</html>
