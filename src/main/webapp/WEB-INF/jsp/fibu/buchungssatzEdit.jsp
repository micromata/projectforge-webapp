®<%@ page pageEncoding="utf-8" session="true" isELIgnored="false"%>
<%@ include file="../base-include.jsp"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<fmt:bundle basename="${actionBean.bundleName}">
  <head>
  <title><c:choose>
    <c:when test="${actionBean.buchungssatz.id >= 0}">
      <fmt:message key="fibu.buchungssatz.edit.title" />
    </c:when>
    <c:otherwise>
      <fmt:message key="fibu.buchungssatz.add.title" />
    </c:otherwise>
  </c:choose></title>
  </head>
  <body>

  <stripes:errors />

  <stripes:form method="post" action="/secure/fibu/BuchungssatzEdit.action">
    <fieldset><legend><fmt:message key="label.data" /></legend>
    <table class="form">
      <tr>
        <th><fmt:message key="date" /></th>
        <td><stripes:text class="date" readonly="true" name="buchungssatz.datum" /></td>
        <th><fmt:message key="calendar.year" />-<fmt:message key="calendar.month" />-<fmt:message key="fibu.buchungssatz.satznr" /></th>
        <td><stripes:text size="4" readonly="true" name="buchungssatz.year" />-<stripes:text size="2" readonly="true"
          name="month" />-<stripes:text size="5" readonly="true" name="satznr" /></td>
      </tr>
      <tr>
        <th><fmt:message key="fibu.common.betrag" />&nbsp;<fmt:message key="fibu.buchungssatz.sh" /></th>
        <td><stripes:text class="stdtext" readonly="true" name="buchungssatz.betrag" style="width:8em;" formatType="currency"
          formatPattern="decimal" /> <stripes:text size="6" readonly="true" name="buchungssatz.sh" /></td>
        <th><fmt:message key="fibu.buchungssatz.beleg" /></th>
        <td><stripes:text class="stdtext" readonly="true" name="buchungssatz.beleg" /></td>
      </tr>
      <tr>
        <th><fmt:message key="fibu.kost1" /></th>
        <td><stripes:text class="stdtext" style="width:8em;" readonly="true" name="kost1" /></td>
        <th><fmt:message key="fibu.kost2" /></th>
        <td><stripes:text class="stdtext" style="width:8em;" readonly="true" name="kost2" /></td>
      </tr>
      <tr>
        <th><fmt:message key="fibu.buchungssatz.konto" /></th>
        <td><stripes:text class="stdtext" style="width:8em;" readonly="true" name="konto" /></td>
        <th><fmt:message key="fibu.buchungssatz.gegenKonto" /></th>
        <td><stripes:text class="stdtext" style="width:8em;" readonly="true" name="gegenKonto" /></td>
      </tr>
      <tr>
        <th><fmt:message key="fibu.buchungssatz.text" /></th>
        <td><stripes:text class="stdtext" readonly="true" name="buchungssatz.text" /></td>
        <th><fmt:message key="fibu.buchungssatz.menge" /></th>
        <td><stripes:text class="stdtext" readonly="true" name="buchungssatz.menge" /></td>
      </tr>
      <tr>
        <th><fmt:message key="comment" /></th>
        <td colspan="3"><stripes:textarea class="stdtext" readonly="true" name="buchungssatz.comment" style="height:20ex;" /></td>
      </tr>
    </table>
    </fieldset>

    <jsp:include page="../include/history.jsp">
      <jsp:param name="requestURI" value="/secure/fibu/BuchungssatzEdit.action" />
    </jsp:include>

  </stripes:form>
  </body>
</fmt:bundle>
</html>
