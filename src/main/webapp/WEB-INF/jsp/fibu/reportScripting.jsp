<%@ page pageEncoding="utf-8" session="true" isELIgnored="false"%>
<%@ include file="../base-include.jsp"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<fmt:bundle basename="${actionBean.bundleName}">
  <head>
  <title><fmt:message key="fibu.reporting.scripting" /></title>
  </head>
  <body>

  <stripes:errors />

  <stripes:form method="post" action="/secure/fibu/ReportScripting.action">

    <c:if test="${actionBean.reportStorage != null and actionBean.reportStorage.root.load == true}">
      <!--  If update h3, please update also report.jsp. -->
      <c:set var="report" value="${actionBean.reportStorage.currentReport}" />
      <h3><c:forEach var="ancestor" items="${report.path}">${ancestor.id} -&gt;</c:forEach> ${report.id}</h3>
    </c:if>
    <fieldset>
    <table class="form">
      <tbody>
        <tr>
          <th><fmt:message key="file" /> (*.xsl, *.jrxml)</th>
          <td><c:out value="${actionBean.filename}" /> <stripes:file name="uploadFile" />&nbsp;<span class="buttons"
            style="direction: ltr;"><stripes:submit name="upload" /></span></td>
        </tr>
        <tr>
          <th><fmt:message key="label.groovyScript" /></th>
          <td><stripes:textarea class="stdtext" name="groovyScript" rows="40" cols="80" /></td>
        </tr>
        <tr>
          <td colspan="2" class="buttons" style="direction: ltr;"><stripes:submit name="execute" /></td>
        </tr>
        <c:if test="${not empty actionBean.groovyResult}">
          <tr>
            <th>Groovy result</th>
            <td>${actionBean.groovyResultAsHtmlString}<c:if
              test="${not empty actionBean.groovyResult.result and not empty actionBean.groovyResult.output}">
              <br />
            </c:if><c:out value="${actionBean.groovyResult.output}" /></td>
          </tr>
        </c:if>
      </tbody>
    </table>
    <c:if test="${actionBean.exception == true}">
      <table>
        <tbody>
          <c:forEach items="${actionBean.lines}" var="row" varStatus="loop">
            <tr>
              <td style="text-align: right; font-size: smaller">${loop.index + 1}</td>
              <td style="font-size: smaller">${row}</td>
            </tr>
          </c:forEach>
        </tbody>
      </table>
    </c:if></fieldset>
    <h3><fmt:message key="label.help" /></h3>
    <table>
      <tbody>
        <tr>
          <th>JavaDocApi:</th>
          <td><a href="<c:url value='/secure/doc/reporting-javadoc.html'/>" target="_blank">JavaDoc</a></td>
        </tr>
        <tr>
          <th>Available script variables</th>
          <td>scriptResult<c:forEach var="variable" items="${actionBean.scriptVariables}">
            , ${variable.label}</c:forEach></td>
        </tr>
        <tr>
          <th>Bwa-Zeilen als Parameter</th>
          <td><c:forEach var="zeile" items="${actionBean.definedBwaZeilen}" varStatus="loop">
            <c:if test="${loop.index > 0}">, </c:if>z${zeile.id}, ${zeile.key}</c:forEach><c:forEach var="value"
            items="${actionBean.additionalBwaValues}">, ${value}</c:forEach></td>
        </tr>
      </tbody>
    </table>
  </stripes:form>
  </body>
</fmt:bundle>
</html>
