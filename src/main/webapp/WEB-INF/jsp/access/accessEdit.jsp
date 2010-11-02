<%@ page pageEncoding="utf-8" session="true" isELIgnored="false"%>
<%@ include file="../base-include.jsp"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<fmt:bundle basename="${actionBean.bundleName}">
  <head>
  <title><c:choose>
    <c:when test="${actionBean.access.id >= 0}">
      <fmt:message key="access.edit.title" />
    </c:when>
    <c:otherwise>
      <fmt:message key="access.add.title" />
    </c:otherwise>
  </c:choose></title>
  </head>
  <body>

  <stripes:errors />

  <fmt:message key="access.recursive.help" var="recursiveHelpText" />
  <stripes:form method="post" action="/secure/access/AccessEdit.action" focus="">
    <stripes:hidden name="access.id" />
    <stripes:hidden name="groupId" />
    <stripes:hidden name="taskId" />
    <stripes:hidden name="eventKey" />

    <fieldset><legend><fmt:message key="label.data" /></legend>
    <table class="form">
      <tr>
        <th><fmt:message key="task" /></th>
        <td><pf:task taskId="${actionBean.taskId}" showPath="true" select="taskId" nullable="false" /></td>
      </tr>
      <tr>
        <th><fmt:message key="group" /></th>
        <td><pf:group groupId="${actionBean.groupId}" select="groupId" nullable="false" /></td>
      </tr>
      <tr>
        <th title="${recursiveHelpText}" style="font-style: italic;"><fmt:message key="recursive" /></th>
        <td><stripes:checkbox name="access.recursive" /></td>
      </tr>
      <tr>
        <th><fmt:message key="access.accessTable" /></th>
        <td>
        <table>
          <tr>
            <th></th>
            <th><fmt:message key="access.type.select" /></th>
            <th><fmt:message key="access.type.insert" /></th>
            <th><fmt:message key="access.type.update" /></th>
            <th><fmt:message key="access.type.delete" /></th>
          </tr>
          <tr>
            <th><fmt:message key="access.type.TASK_ACCESS_MANAGEMENT" /></th>
            <td><stripes:checkbox name="accessManagementEntry.accessSelect" /></td>
            <td><stripes:checkbox name="accessManagementEntry.accessInsert" /></td>
            <td><stripes:checkbox name="accessManagementEntry.accessUpdate" /></td>
            <td><stripes:checkbox name="accessManagementEntry.accessDelete" /></td>
          </tr>
          <tr>
            <th><fmt:message key="access.type.TASKS" /></th>
            <td><stripes:checkbox name="tasksEntry.accessSelect" /></td>
            <td><stripes:checkbox name="tasksEntry.accessInsert" /></td>
            <td><stripes:checkbox name="tasksEntry.accessUpdate" /></td>
            <td><stripes:checkbox name="tasksEntry.accessDelete" /></td>
          </tr>
          <tr>
            <th><fmt:message key="access.type.TIMESHEETS" /></th>
            <td><stripes:checkbox name="timesheetsEntry.accessSelect" /></td>
            <td><stripes:checkbox name="timesheetsEntry.accessInsert" /></td>
            <td><stripes:checkbox name="timesheetsEntry.accessUpdate" /></td>
            <td><stripes:checkbox name="timesheetsEntry.accessDelete" /></td>
          </tr>
          <tr>
            <th><fmt:message key="access.type.OWN_TIMESHEETS" /></th>
            <td><stripes:checkbox name="ownTimesheetsEntry.accessSelect" /></td>
            <td><stripes:checkbox name="ownTimesheetsEntry.accessInsert" /></td>
            <td><stripes:checkbox name="ownTimesheetsEntry.accessUpdate" /></td>
            <td><stripes:checkbox name="ownTimesheetsEntry.accessDelete" /></td>
          </tr>
        </table>
        </td>
      </tr>
      <tr>
        <td class="buttons" colspan="2"><jsp:include page="../include/editDOButtons.jsp" /></td>
      </tr>
      <tr>
        <th><fmt:message key="access.templates" /></th>
        <td><stripes:submit name="clear" /><stripes:submit name="guest" /><stripes:submit name="employee" /><stripes:submit name="leader" /><stripes:submit
          name="administrator" /></td>
      </tr>
    </table>
    </fieldset>

    <jsp:include page="../include/history.jsp" />

  </stripes:form>
  </body>
</fmt:bundle>
</html>
