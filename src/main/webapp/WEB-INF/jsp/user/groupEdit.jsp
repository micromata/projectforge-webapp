<%@ page pageEncoding="utf-8" session="true" isELIgnored="false"%>
<%@ include file="../base-include.jsp"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<fmt:bundle basename="${actionBean.bundleName}">
  <head>
  <title><c:choose>
    <c:when test="${actionBean.group.id >= 0}">
      <fmt:message key="group.edit.title" />
    </c:when>
    <c:otherwise>
      <fmt:message key="group.add.title" />
    </c:otherwise>
  </c:choose></title>
  </head>
  <body>

  <stripes:errors />

  <stripes:form method="post" action="/secure/user/GroupEdit.action" focus="">
    <stripes:hidden name="group.id" />
    <stripes:hidden name="flowKey" />
    <stripes:hidden name="eventKey" />

    <fieldset><legend><fmt:message key="label.data" /></legend>
    <table class="form">
      <tr>
        <th><fmt:message key="name" /></th>
        <td><stripes:text class="stdtext" name="group.name" /></td>
      </tr>
      <tr>
        <th><fmt:message key="organization" /></th>
        <td><stripes:text class="stdtext" name="group.organization" /></td>
      </tr>
      <tr>
        <th><fmt:message key="group.assignedUsers" /></th>
        <td>
        <table>
          <tr>
            <td><fmt:message key="group.assignedUsers" /></td>
            <td></td>
            <td><fmt:message key="group.unassignedUsers" /></td>
          </tr>
          <tr>
            <td><stripes:select class="stdtext" multiple="multiple" size="20" name="selectedItemsToUnassign">
              <stripes:options-collection collection="${actionBean.users.assignedItems}" label="value" value="key" />
            </stripes:select></td>
            <td><a onclick='javascript:submitEvent("assign")' href="#"><pf:image src="${buttonLeftImage}" tooltip="tooltip.assign" /></a><br />
            <a onclick='javascript:submitEvent("unassign")' href="#"><pf:image src="${buttonRightImage}" tooltip="tooltip.unassign" /></a></td>
            <td><stripes:select class="stdtext" multiple="multiple" size="20" name="selectedItemsToAssign">
              <stripes:options-collection collection="${actionBean.users.unassignedItems}" label="value" value="key" />
            </stripes:select></td>
          </tr>
        </table>
        </td>
      </tr>
      <tr>
        <th><fmt:message key="description" /></th>
        <td><stripes:textarea class="stdtext" name="group.description" rows="8" cols="80" /></td>
      </tr>
      <tr>
        <td class="buttons" colspan="2"><jsp:include page="../include/editDOButtons.jsp" /></td>
      </tr>
    </table>
    </fieldset>

    <jsp:include page="../include/history.jsp">
      <jsp:param name="requestURI" value="/secure/user/GroupEdit.action" />
    </jsp:include>

  </stripes:form>
  </body>
</fmt:bundle>
</html>
