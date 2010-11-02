<%@ page pageEncoding="utf-8" session="true" isELIgnored="false"%>
<%@ include file="../base-include.jsp"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<fmt:bundle basename="${actionBean.bundleName}">
  <head>
  <title><fmt:message key="address.view.title" /></title>
  </head>
  <body>
  <div id="contentMenu"><a href="javascript:history.back()">
    <fmt:message key="back" />
  </a><stripes:link href="/secure/address/AddressEdit.action" event="preEdit">
    <stripes:param name="id" value="${actionBean.address.id}" />
    <fmt:message key="update" />
  </stripes:link></div>
  <table class="form" style="width: 600px;">
    <tr>
      <th><fmt:message key="name" />:</th>
      <td><c:if test="${not empty actionBean.form}">${actionBean.form}&nbsp;</c:if> <c:if test="${not empty actionBean.address.title}">${actionBean.address.title}&nbsp;</c:if>
      ${actionBean.address.firstName}&nbsp;${actionBean.address.name}</td>
    </tr>
    <c:if test="${not empty actionBean.address.businessPhone}">
      <tr>
        <th><fmt:message key="address.businessPhone" />:</th>
        <td>${actionBean.address.businessPhone}</td>
      </tr>
    </c:if>
    <c:if test="${not empty actionBean.address.fax}">
      <tr>
        <th><fmt:message key="address.fax" />:</th>
        <td>${actionBean.address.fax}</td>
      </tr>
    </c:if>
    <c:if test="${not empty actionBean.address.mobilePhone}">
      <tr>
        <th><fmt:message key="address.mobilePhone" />:</th>
        <td>${actionBean.address.mobilePhone}</td>
      </tr>
    </c:if>
    <c:if test="${not empty actionBean.address.privatePhone}">
      <tr>
        <th><fmt:message key="address.privatePhone" />:</th>
        <td>${actionBean.address.privatePhone}</td>
      </tr>
    </c:if>
    <c:if test="${not empty actionBean.address.privateMobilePhone}">
      <tr>
        <th><fmt:message key="address.privateMobilePhone" />:</th>
        <td>${actionBean.address.privateMobilePhone}</td>
      </tr>
    </c:if>
    <c:if test="${not empty actionBean.address.organization}">
      <tr>
        <th><fmt:message key="organization" />:</th>
        <td>${actionBean.address.organization}</td>
      </tr>
    </c:if>
    <c:if test="${not empty actionBean.address.division}">
      <tr>
        <th><fmt:message key="address.division" />:</th>
        <td>${actionBean.address.division}</td>
      </tr>
    </c:if>
    <c:if test="${not empty actionBean.address.positionText}">
      <tr>
        <th><fmt:message key="address.positionText" />:</th>
        <td>${actionBean.address.positionText}</td>
      </tr>
    </c:if>
    <c:if test="${not empty actionBean.address.email}">
      <tr>
        <th><fmt:message key="email" />:</th>
        <td><a href="mailto:${actionBean.address.email}">${actionBean.address.email}</a></td>
      </tr>
    </c:if>
    <c:if test="${not empty actionBean.address.privateEmail}">
      <tr>
        <th><fmt:message key="address.privateEmail" />:</th>
        <td><a href="mailto:${actionBean.address.privateEmail}">${actionBean.address.privateEmail}</a></td>
      </tr>
    </c:if>
    <c:if test="${not empty actionBean.address.website}">
      <tr>
        <th><fmt:message key="address.website" />:</th>
        <td>${actionBean.address.website}</td>
      </tr>
    </c:if>
    <c:if test="${not empty actionBean.address.addressText}">
      <tr>
        <th><fmt:message key="address.addressText" />:</th>
        <td>${actionBean.address.addressText}<br />
        ${actionBean.address.zipCode}&nbsp;${actionBean.address.city} <c:if test="${not empty actionBean.address.country}">
          <br />
        ${actionBean.address.country}</c:if> <c:if test="${not empty actionBean.address.state}">
          <br />
        ${actionBean.address.state}</c:if></td>
      </tr>
    </c:if>
    <c:if test="${not empty actionBean.address.postalAddressText}">
      <tr>
        <th><fmt:message key="address.postalAddressText" />:</th>
        <td>${actionBean.address.postalAddressText}<br />
        ${actionBean.address.postalZipCode}&nbsp;${actionBean.address.postalCity} <c:if test="${not empty actionBean.address.postalCountry}">
          <br />
        ${actionBean.address.postalCountry}</c:if> <c:if test="${not empty actionBean.address.postalState}">
          <br />
        ${actionBean.address.postalState}</c:if></td>
      </tr>
    </c:if>
    <c:if test="${not empty actionBean.address.privateAddressText}">
      <tr>
        <th><fmt:message key="address.privateAddressText" />:</th>
        <td>${actionBean.address.privateAddressText}<br />
        ${actionBean.address.privateZipCode}&nbsp;${actionBean.address.privateCity} <c:if
          test="${not empty actionBean.address.privateCountry}">
          <br />
        ${actionBean.address.privateCountry}</c:if> <c:if test="${not empty actionBean.address.privateState}">
          <br />
        ${actionBean.address.privateState}</c:if></td>
      </tr>
    </c:if>
    <c:if test="${not empty actionBean.address.birthday}">
      <tr>
        <th><fmt:message key="address.birthday" />:</th>
        <td><pf:date date="${actionBean.address.birthday}" type="date" /></td>
      </tr>
    </c:if>
    <c:if test="${not empty actionBean.address.comment}">
      <tr>
        <th><fmt:message key="comment" />:</th>
        <td><pre>${actionBean.address.comment}</pre></td>
      </tr>
    </c:if>
    <c:if test="${not empty actionBean.address.publicKey}">
      <tr>
        <th><fmt:message key="address.publicKey" />:</th>
        <td><pre>${actionBean.address.publicKey}</pre></td>
      </tr>
    </c:if>
    <c:if test="${not empty actionBean.address.fingerprint}">
      <tr>
        <th><fmt:message key="address.fingerprint" />:</th>
        <td><pre>${actionBean.address.fingerprint}</pre></td>
      </tr>
    </c:if>

  </table>
  </body>
</fmt:bundle>
</html>
