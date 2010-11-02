<%@ page pageEncoding="utf-8" session="true" isELIgnored="false"%>
<%@ include file="../base-include.jsp"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<fmt:bundle basename="${actionBean.bundleName}">
  <head>
  <title><fmt:message key="address.phoneCall.title" /></title>
  <c:set var="action" value="/secure/address/PhoneCall.action" />
  <script type="text/javascript">
<!--
$(document).ready(function() {
  var z = $("#searchAjax");
  var recent = ${actionBean.recentSearchTerms};
  z.autocomplete("<c:url value='${action}' />", "searchAutocomplete",{matchContains:1, minChars:2, formatItem:formatItem, onItemSelect:selectItem, autoSubmit:true, favoriteEntries:recent, selectFirst:true});
  z.focus();
  z.select();
});

function formatItem(row, q) {
  if (row[1]) {
    str = row[1] + ", " + row[2] + ", " + row[3];
    return defaultFormat(str, q)
  }
  return row[0];
}

function selectItem(li) {
  document.form.submit();
}

function callNumber(addressId, phoneType) {
  document.getElementsByName("addressId")[0].value = addressId;
  document.getElementsByName("phoneType")[0].value = phoneType;
  document.getElementsByName("callNow")[0].value = "true";
  return document.forms[0].submit();
}

//  End -->
</script>
  </head>
  <body>

  <stripes:errors />

  <stripes:form method="post" action="/secure/address/PhoneCall.action">
    <stripes:hidden name="addressId" />
    <stripes:hidden name="phoneType" />
    <stripes:hidden name="callNow" />

    <fieldset><legend><fmt:message key="label.data" /></legend>
    <table class="form">
      <tr>
        <th><fmt:message key="address.phoneCall.number" />:</th>
        <td><stripes:text class="stdtext" name="phoneNumber" style="width:40em" tabindex="1" id="searchAjax" />&nbsp;<pf:help
          key="address.directCall.number.tooltip" image="keyboard.png" /></td>
      </tr>
      <tr>
        <th><fmt:message key="address.myCurrentPhoneId" />:</th>
        <td><stripes:select name="myCurrentPhoneId">
          <stripes:options-collection collection="${actionBean.telephoneIds}" label="label" value="value" />
        </stripes:select> <pf:help key="address.myCurrentPhoneId.tooltip" /></td>
      </tr>
      <c:if test="${actionBean.address != null}">
        <tr>
          <th><fmt:message key="name" />:</th>
          <td style="font-weight: bold;"><stripes:link href="/secure/address/AddressView.action" event="preEdit">
            <stripes:param name="id" value="${actionBean.address.id}" />
            <c:if test="${not empty actionBean.form}">${actionBean.form}&nbsp;</c:if>
            <c:if test="${not empty actionBean.address.title}">${actionBean.address.title}&nbsp;</c:if>
          ${actionBean.address.firstName}&nbsp;${actionBean.address.name}</stripes:link></td>
        </tr>
        <c:if test="${not empty actionBean.address.businessPhone}">
          <tr>
            <th><fmt:message key="address.businessPhone" />:</th>
            <td><a href="#" onclick="javascript:callNumber('${actionBean.addressId}', 'business')">${actionBean.address.businessPhone}</a></td>
          </tr>
        </c:if>
        <c:if test="${not empty actionBean.address.mobilePhone}">
          <tr>
            <th><fmt:message key="address.mobilePhone" />:</th>
            <td><a href="#" onclick="javascript:callNumber('${actionBean.addressId}', 'mobile')">${actionBean.address.mobilePhone}</a></td>
          </tr>
        </c:if>
        <c:if test="${not empty actionBean.address.privatePhone}">
          <tr>
            <th><fmt:message key="address.privatePhone" />:</th>
            <td><a href="#" onclick="javascript:callNumber('${actionBean.addressId}', 'private')">${actionBean.address.privatePhone}</a></td>
          </tr>
        </c:if>
        <c:if test="${not empty actionBean.address.privateMobilePhone}">
          <tr>
            <th><fmt:message key="address.privateMobilePhone" />:</th>
            <td><a href="#" onclick="javascript:callNumber('${actionBean.addressId}', 'privateMobile')">${actionBean.address.privateMobilePhone}</a></td>
          </tr>
        </c:if>
      </c:if>
      <tr class="hRuler">
        <td colspan="2"></td>
      </tr>
      <c:if test="${not empty actionBean.result}">
        <tr>
          <th><fmt:message key="label.result" />:</th>
          <td><c:if test="${actionBean.lastSuccessfulPhoneCall != null}"><pf:date date="${actionBean.lastSuccessfulPhoneCall}" type="timeOfDay" />:&nbsp;</c:if>${actionBean.result}</td>
        </tr>
      </c:if>
      <tr>
        <td class="buttons" colspan="2"><stripes:submit name="call" /></td>
      </tr>
      <tr><!--  Micromata specific! -->
        <td colspan="2"><a onclick="MM_openBrWindow('http://192.168.76.25/', 'astrisk', 'scrollbars=no,resizable=yes,width=800,height=400')" href="#"><pf:image src="/images/asteriskOperatorPanel.png" tooltipText="Asterisk Operator Panel - Nur im Intranet verfügbar." /></a></td>
      </tr>
    </table>
    </fieldset>
  </stripes:form>
  </body>
</fmt:bundle>
</html>
