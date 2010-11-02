<%@ page pageEncoding="utf-8" session="true" isELIgnored="false"%>
<%@ include file="../base-include.jsp"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<fmt:bundle basename="${actionBean.bundleName}">
  <head>
  <title><c:choose>
    <c:when test="${actionBean.selectMode == true}">
      <fmt:message key="task.tree.select.title" />
    </c:when>
    <c:otherwise>
      <fmt:message key="task.tree.title" />
    </c:otherwise>
  </c:choose></title>
  </head>
  <c:choose>
    <c:when test="${not empty actionBean.selectedValue}">
      <c:set var="bodyOnload" value="javascript:self.location.href='#clickedEntry'" />
    </c:when>
    <c:otherwise>
      <c:set var="bodyOnload" value="" />
    </c:otherwise>
  </c:choose>
  <body onload="${bodyOnload}">
  <c:set var="count" value="0" />
  <c:set var="style" />

  <c:set var="actionUrl" value="/secure/task/TaskTree.action" />
  <c:set var="spacerImage" value="/images/spacer.gif" />
  <c:set var="leafImage" value="/images/leaf.gif" />
  <c:set var="folderImage" value="/images/folder.gif" />
  <c:set var="openFolderImage" value="/images/folder_open.gif" />
  <c:set var="explosionImage" value="/images/explosion.gif" />
  <c:set var="iconWidth" value="19" />
  <c:set var="iconHeight" value="15" />

  <div id="contentMenu"></div>

  <stripes:form name="form" method="post" action="/secure/task/TaskTree.action">
    <stripes:hidden name="selectedValue" />
    <stripes:hidden name="eventKey" />
    <stripes:hidden name="flowKey" />
    <fieldset><legend><fmt:message key="label.filterSettings" /></legend>
    <table class="form">
      <tr>
        <th><fmt:message key="searchString" /></th>
        <td><stripes:text class="stdtext" name="actionFilter.searchString" title="${actionBean.searchToolTip}" />&nbsp;<pf:help
          key="tooltip.lucene.link" /><jsp:include page="../include/showLuceneSearchInfo.jsp" /></td>
      </tr>
      <tr>
        <th><fmt:message key="task.status" /></th>
        <td><stripes:checkbox name="actionFilter.notOpened" /> <fmt:message key="task.status.notOpened" /><stripes:checkbox
          name="actionFilter.opened" /> <fmt:message key="task.status.opened" /><stripes:checkbox name="actionFilter.closed" /> <fmt:message
          key="task.status.closed" /><stripes:checkbox name="actionFilter.deleted" /> <fmt:message key="task.deleted" /></td>
      </tr>
      <tr class="hRuler">
        <td colspan="4"></td>
      </tr>
      <tr>
        <td colspan="2" class="buttons"><stripes:submit name="search" /> <stripes:submit name="reset" /></td>
      </tr>
    </table>
    </fieldset>

    <fmt:message var="tTask" key="task" />
    <fmt:message var="tShortDescription" key="shortDescription" />
    <fmt:message var="tProtection" key="task.protectTimesheetsUntil.short" />
    <fmt:message var="tReference" key="task.reference" />
    <fmt:message var="tPriority" key="priority" />
    <fmt:message var="tStatus" key="task.status" />
    <c:choose>
      <c:when test="${actionBean.searchMode == true}">
        <%
          /* Task list view: */
        %>
        <fieldset><legend><fmt:message key="label.resultset" /></legend> <display:table class="dataTable" name="actionBean.list"
          export="false" id="row" requestURI="/secure/task/TaskTree.action" pagesize="1000">
          <display:column sortable="false" title="Id">
            <c:choose>
              <c:when test="${actionBean.selectMode == true}">
                <pf:submit event="select" select="${row.id}">
                  <pf:image src="${pointerImage}" />
                </pf:submit>
              </c:when>
              <c:otherwise>
                <pf:image src="${pointerImage}" />
              </c:otherwise>
            </c:choose>
          </display:column>
          <display:column sortable="true" title='${tTask}' decorator="taskColumnDecorator" property="id" />
          <display:column sortable="true" title="${tShortDescription}" property="shortDescription" />
          <display:column sortable="true" title="${tProtection}" sortProperty="protectTimesheetsUntil">
            <pf:date date="${row.protectTimesheetsUntil}" type="date" />
          </display:column>/>
          <display:column sortable="true" title="${tReference}" property="reference" />
          <display:column sortable="true" title="${tPriority}" property="priority" decorator="taskColumnDecorator" />
          <display:column sortable="true" title="${tStatus}" property="status" decorator="taskColumnDecorator" />
        </display:table></fieldset>
      </c:when>
      <c:otherwise>
        <%
          /* Tree navigation view: */
        %>
        <table class="taskExplorer">
          <tr>
            <th class="header" style="white-space: nowrap;">${tTask}</th>
            <th class="header">${tShortDescription}</th>
            <th class="header">${tProtection}</th>
            <th class="header">${tReference}</th>
            <th class="header">${tPriority}</th>
            <th class="header">${tStatus}</th>
          </tr>
          <c:forEach var="node" items="${actionBean.taskTreeList}">
            <c:set var="count" value="${count + 1}" />
            <c:choose>
              <c:when test="${node.hashId == actionBean.preselectedValue || node.hashId == actionBean.selectedValue}">
                <c:set var="style" value="preselected" />
              </c:when>
              <c:when test="${count % 2 == 0}">
                <c:set var="style" value="even" />
              </c:when>
              <c:otherwise>
                <c:set var="style" value="odd" />
              </c:otherwise>
            </c:choose>
            <tr class="${style}">
              <td class="${style}" style="white-space: nowrap;">
              <%
                /* Icon bestimmen: */
              %> <c:if test="${node.indent > 0}">
                <pf:image src="${spacerImage}" width="${node.indent * iconWidth}" height="10" />
              </c:if> <%
   /* Explore icon anzeigen, wenn childs vorhanden: */
 %><c:if test="${node.hasChilds == true}">
                <a onclick="javascript:submitSelectedEvent('explore', '${node.hashId}')" href="#" class="explore"><pf:image
                  src="${explosionImage}" width="${iconWidth}" height="${iconHeight}" /></a>
              </c:if><c:choose>
                <c:when test="${node.folder == true}">
                  <a class="explore" onclick="javascript:submitSelectedEvent('open', '${node.hashId}')" href="#"><pf:image src="${folderImage}"
                    width="${iconWidth}" height="${iconHeight}" /></a>
                </c:when>
                <c:when test="${node.openFolder == true}">
                  <a class="explore" onclick="javascript:submitSelectedEvent('close', '${node.hashId}')"><pf:image src="${openFolderImage}"
                    width="${iconWidth}" height="${iconHeight}" /></a>
                </c:when>
                <c:otherwise>
                  <pf:image src="${spacerImage}" width="${iconWidth}" height="${iconHeight}" />
                  <pf:image src="${leafImage}" width="${iconWidth}" height="${iconHeight}" />
                </c:otherwise>
              </c:choose> <pf:image src="${leafImage}" width="4" height="1" /> <c:choose>
                <c:when test="${actionBean.selectMode == true}">
                  <pf:submit event="select" select="${node.hashId}">
                    <pf:task taskId="${node.id}" showPathAsTooltip="false" enableLinks="false" />
                  </pf:submit>
                </c:when>
                <c:otherwise>
                  <pf:task taskId="${node.id}" showPathAsTooltip="false" enableLinks="false" />
                </c:otherwise>
              </c:choose></td>
              <td class="${style}"><c:choose>
                <c:when test="${node.hashId == actionBean.selectedValue}">
                  <a name="clickedEntry">${node.shortDescription}&nbsp;</a>
                </c:when>
                <c:otherwise>${node.shortDescription}</c:otherwise>
              </c:choose></td>
              <td class="${style}"><pf:date date="${node.taskNode.task.protectTimesheetsUntil}" type="date" /></td>
              <td class="${style}"><c:out value="${node.taskNode.task.reference}" /></td>
              <td class="${style}"><pf:priority priority="${node.priority}" /></td>
              <td class="${style}"><pf:taskStatus status="${node.status}" /></td>
            </tr>
          </c:forEach>
          <c:if test="${actionBean.showRootNode == true}">
            <tr>
              <td colspan="3"><c:choose>
                <c:when test="${actionBean.selectMode == true}">
                  <pf:submit event="select" select="${actionBean.rootNodeId}">
                    <pf:task taskId="${actionBean.rootNodeId}" showPathAsTooltip="false" enableLinks="false" />
                  </pf:submit>
                </c:when>
                <c:otherwise>
                  <pf:task taskId="${actionBean.rootNodeId}" showPathAsTooltip="false" enableLinks="false" />
                </c:otherwise>
              </c:choose></td>
              <td><pf:date date="${actionBean.rootNode.task.protectTimesheetsUntil}" type="date" /></td>
              <td colspan="4">&nbsp;</td>
            </tr>
          </c:if>
        </table>
      </c:otherwise>
    </c:choose>
  </stripes:form>
  </body>
</fmt:bundle>
</html>
