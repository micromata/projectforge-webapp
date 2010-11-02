<%@ page pageEncoding="utf-8" session="true" isELIgnored="false"%>
<%@ include file="../base-include.jsp"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<fmt:bundle basename="${actionBean.bundleName}">
  <head>
  <title><fmt:message key="address.list.title" /></title>
  <style type="text/css">
#divZoom {
	position: fixed;
	left: 0px;
	top: 50px;
	width: 100%;
	text-align: center;
	line-height: 1.4em;
	font-family: arial black, arial, helvetica, sans-serif;
}

.zoomText {
	text-align: center;
	/*background-color: #FFFFFF;*/
	padding-left: 10px;
	padding-right: 10px;
}
</style>

  <c:set var="action" value="/secure/address/AddressList.action" />

  <script type="text/javascript">
<!--
$(document).ready(function() {
  var z = $("#searchAjax");
  var recent = ${actionBean.recentSearchTerms};
  z.autocomplete("<c:url value='${action}' />", "searchAutocomplete",{matchContains:1, minChars:2, formatItem:formatItem, onItemSelect:selectItem, autoSubmit:true, favoriteEntries:recent});
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
  var sValue = "id:" + li.extra[0];
}

// The colors to fade step by step:
var zColor = new Array('#C0C0C0', '#A0A0A0', '#808080', '#606060', '#404040', '#202020', '#000000');
var timeout1, timeout2;

function zoom(text) {
  this.obj = document.getElementById('divZoom');
  this.css = this.obj.style;
  this.css.color = zColor[0];
  this.obj.innerHTML='<span class="zoomText">' + text + '</span>';
  this.css.visibility = 'visible';
  increaseFontSize(10);
}

function increaseFontSize(size)
{
  this.css.fontSize = size + "px";
  size += 5;
  if (size <= 90) {
    timeout1 = window.setTimeout('increaseFontSize(' + size + ')', 20);
  } else {
    fadeFont(1);
  }
}

function fadeFont(cnum) {
  this.css.color = zColor[cnum];
  cnum++;
  if (cnum < zColor.length) {
    timeout2 = window.setTimeout('fadeFont(' + cnum + ')', 50);
  }
}

function hide() {
  this.css.visibility = 'hidden';
  if (timeout1) window.clearTimeout(timeout1);
  if (timeout2) window.clearTimeout(timeout2);
}

function setOptionStatus() {
  if ($("#listTypeStandard").attr("checked") == true) {
    $("#options").attr("style", "display: block;");
  }
}

function hideOptions() {
  $("#options").hide("normal");
  document.form.submit();
}

function showOptions() {
  $("#options").show("normal");
}

//  End -->
</script>
  </head>
  <body onload="javascript:setOptionStatus();">

  <div id="contentMenu"><stripes:link href="/secure/address/AddressEdit.action" event="preEdit">
    <fmt:message key="address.menu.add" />
  </stripes:link> <c:if test="${actionBean.smsEnabled == true}">
    <stripes:link href="/wa/sendSms">
      <fmt:message key="address.sendSms.title" />
    </stripes:link>
  </c:if></div>

  <div id="divZoom"></div>

  <stripes:errors />


  <stripes:form name="form" method="post" action="${action}">
    <fieldset><legend><fmt:message key="label.filterSettings" /></legend>
    <table class="form">
      <tr>
        <th><fmt:message key="searchString" /></th>
        <td><stripes:text class="stdtext" name="filter.searchString" title="${actionBean.searchToolTip}" id="searchAjax" />&nbsp;<pf:help
          key="tooltip.autocomplete.recentSearchTerms" image="keyboard.png" />&nbsp;<pf:help key="tooltip.lucene.link" /><jsp:include
          page="../include/showLuceneSearchInfo.jsp" /></td>
      </tr>
      <tr>
        <th><fmt:message key="label.options" /></th>
        <td>
        <div id="options" style="display: none;"><fmt:message key="address.addressStatus" />:&nbsp;<stripes:checkbox name="filter.uptodate" />
        <fmt:message key="address.addressStatus.uptodate" />&nbsp; <stripes:checkbox name="filter.outdated" /> <fmt:message
          key="address.addressStatus.outdated" />&nbsp; <stripes:checkbox name="filter.leaved" /> <fmt:message key="address.addressStatus.leaved" /><br />
        <fmt:message key="address.contactStatus" />:&nbsp;<stripes:checkbox name="filter.active" /> <fmt:message key="address.contactStatus.active" />&nbsp;
        <stripes:checkbox name="filter.nonActive" /> <fmt:message key="address.contactStatus.nonActive" />&nbsp; <stripes:checkbox
          name="filter.uninteresting" /> <fmt:message key="address.contactStatus.uninteresting" />&nbsp; <stripes:checkbox
          name="filter.personaIngrata" /> <fmt:message key="address.contactStatus.personaIngrata" />&nbsp; <stripes:checkbox name="filter.departed" />
        <fmt:message key="address.contactStatus.departed" /></div>
        <br />
        <stripes:radio name="filter.listType" value="filter" id="listTypeStandard" onclick="javascript:showOptions();" />&nbsp;<fmt:message
          key="filter" /><stripes:radio name="filter.listType" value="newest" onclick="javascript:hideOptions();" />&nbsp;<fmt:message
          key="filter.newest" />&nbsp;<stripes:radio name="filter.listType" value="myFavorites" onclick="javascript:hideOptions();" />&nbsp;<fmt:message
          key="address.filter.myFavorites" />&nbsp;<stripes:radio name="filter.listType" value="deleted" onclick="javascript:hideOptions();" /> <fmt:message
          key="deleted" />,&nbsp;<jsp:include page="../include/pageSizeOption.jsp" /></td>
      </tr>
      <tr class="hRuler">
        <td colspan="2"></td>
      </tr>
      <tr>
        <c:set var="tvCardExportTip">
          <fmt:message key="address.book.vCardExport.tooltip" />
        </c:set>
        <c:set var="tExportTip">
          <fmt:message key="address.book.export.tooltip" />
        </c:set>
        <c:set var="tPhoneListExportTip">
          <fmt:message key="address.book.phoneNumber.tooltip" />
        </c:set>
        <fmt:message var="tAppleScriptTooltip" key="address.book.export.tooltip.appleScript4Notes" />
        <td colspan="2" class="buttons"><stripes:submit name="search" /> <stripes:submit name="reset" /> <stripes:submit
          name="exportFavoriteVCards" title="${tvCardExportTip}" /> <stripes:submit name="export" title="${tExportTip}" /> <stripes:submit
          name="exportFavoritePhoneList" title="${tPhoneListExportTip}" /> <a href="../../misc/AddressBookRemoveNotesOfClassWork.scpt" title="${tAppleScriptTooltip}">AppleScript</a></td>
      </tr>
    </table>
    </fieldset>
  </stripes:form>
  <c:set var="tModified">
    <fmt:message key="modified" />
  </c:set>
  <c:set var="tName">
    <fmt:message key="name" />
  </c:set>
  <c:set var="tFirstName">
    <fmt:message key="firstName" />
  </c:set>
  <c:set var="tOrganization">
    <fmt:message key="organization" />
  </c:set>
  <c:set var="tEMail">
    <fmt:message key="email" />
  </c:set>
  <c:set var="tPhoneNumbers">
    <fmt:message key="address.phoneNumbers" />
  </c:set>
  <display:table class="dataTable" name="actionBean.list" export="false" id="row" requestURI="/secure/address/AddressList.action"
    pagesize="${actionBean.pageSize}">
    <c:set var="style">
      <c:choose>
        <c:when test="${row.address.deleted eq true}">
        text-decoration: line-through;
        </c:when>
        <c:otherwise></c:otherwise>
      </c:choose>
    </c:set>
    <display:column sortable="true" title="${tModified}" sortProperty="address.lastUpdate" defaultorder="descending" style="${style}" class="row-link">
      <stripes:link href="/secure/address/AddressEdit.action" event="preEdit">
        <stripes:param name="id" value="${row.address.id}" />
        <pf:image src="${pointerImage}" />
      </stripes:link>
      <pf:date date="${row.address.lastUpdate}" type="date" />
      <stripes:link href="/secure/address/AddressView.action">
        <stripes:param name="id" value="${row.address.id}" />
      &nbsp;<pf:image src="/images/printer.png" />
      </stripes:link>
    </display:column>
    <display:column sortable="true" title="${tName}" sortProperty="address.name" style="${style}">
      <c:choose>
        <c:when test="${row.favoriteCard == true}">
          <span style="color: red; font-weight: bold;">${row.address.name}</span>
        </c:when>
        <c:otherwise>${row.address.name}</c:otherwise>
      </c:choose>
    </display:column>
    <display:column sortable="true" title="${tFirstName}" style="${style}">
      <c:choose>
        <c:when test="${row.favoriteCard == true}">
          <span style="color: red; font-weight: bold;">${row.address.firstName}</span>
        </c:when>
        <c:otherwise>${row.address.firstName}</c:otherwise>
      </c:choose>
    </display:column>
    <display:column sortable="true" title="${tOrganization}" property="address.organization" style="${style}" />
    <display:column sortable="true" title="${tEMail}" style="${style}" class="notrlink">
      <c:if test="${not empty row.address.email}">
        <a href="mailto:${row.address.email}">${row.address.email}</a>
      </c:if>
      <c:if test="${not empty row.address.privateEmail}">
        <c:if test="${not empty row.address.email}">
          <br />
        </c:if>
        <a href="mailto:${row.address.privateEmail}">${row.address.privateEmail}</a>
      </c:if>
    </display:column>
    <display:column sortable="false" title="${tPhoneNumbers}" style="white-space:nowrap;${style}" class="notrlink">
      <c:set var="count" value="0" />
      <c:if test='${not empty row.address.businessPhone and row.address.businessPhone != ""}'>
        <c:set var="count" value="${count + 1}" />
        <stripes:link href="/secure/address/PhoneCall.action" event="init" onmouseover="zoom('${row.address.businessPhone}'); return false;"
          onmouseout="hide()">
          <stripes:param name="addressId" value="${row.address.id}" />
          <stripes:param name="phoneType" value="business" />
          <pf:image src="/images/telephone.png" tooltip="address.businessPhone" />&nbsp;<c:choose>
            <c:when test="${row.favoriteBusinessPhone == true}">
              <span style="color: red; font-weight: bold;">${row.address.businessPhone}</span>
            </c:when>
            <c:otherwise>${row.address.businessPhone}</c:otherwise>
          </c:choose>
        </stripes:link>
      </c:if>
      <c:if test='${not empty row.address.mobilePhone and row.address.mobilePhone != ""}'>
        <c:if test="${count > 0}">
          <br />
        </c:if>
        <c:set var="count" value="${count + 1}" />
        <stripes:link href="/secure/address/PhoneCall.action" onmouseover="zoom('${row.address.mobilePhone}'); return false;" onmouseout="hide()">
          <stripes:param name="addressId" value="${row.address.id}" />
          <stripes:param name="phoneType" value="mobile" />
          <pf:image src="/images/phone.png" tooltip="address.mobilePhone" />&nbsp;<c:choose>
            <c:when test="${row.favoriteMobilePhone == true}">
              <span style="color: red; font-weight: bold;">${row.address.mobilePhone}</span>
            </c:when>
            <c:otherwise>${row.address.mobilePhone}</c:otherwise>
          </c:choose>
        </stripes:link>
        <c:if test="${actionBean.smsEnabled == true}">
          <stripes:link href="/wa/sendSms">
            <stripes:param name="addressId" value="${row.address.id}" />
            <stripes:param name="phoneType" value="mobile" />
            <pf:image src="/images/sms.png" tooltip="address.tooltip.writeSMS" />
          </stripes:link>
        </c:if>
      </c:if>
      <c:if test='${not empty row.address.privatePhone and row.address.privatePhone != ""}'>
        <c:if test="${count > 0}">
          <br />
        </c:if>
        <stripes:link href="/secure/address/PhoneCall.action" onmouseover="zoom('${row.address.privatePhone}'); return false;" onmouseout="hide()">
          <stripes:param name="addressId" value="${row.address.id}" />
          <stripes:param name="phoneType" value="private" />
          <pf:image src="/images/house.png" tooltip="address.privatePhone" />&nbsp;<c:choose>
            <c:when test="${row.favoritePrivatePhone == true}">
              <span style="color: red; font-weight: bold;">${row.address.privatePhone}</span>
            </c:when>
            <c:otherwise>${row.address.privatePhone}</c:otherwise>
          </c:choose>
        </stripes:link>
      </c:if>
      <c:if test='${not empty row.address.privateMobilePhone and row.address.privateMobilePhone != ""}'>
        <c:if test="${count > 0}">
          <br />
        </c:if>
        <c:set var="count" value="${count + 1}" />
        <stripes:link href="/secure/address/PhoneCall.action" onmouseover="zoom('${row.address.privateMobilePhone}'); return false;"
          onmouseout="hide()">
          <stripes:param name="addressId" value="${row.address.id}" />
          <stripes:param name="phoneType" value="privateMobile" />
          <pf:image src="/images/phone.png" tooltip="address.privateMobilePhone" />&nbsp;<c:choose>
            <c:when test="${row.favoritePrivateMobilePhone == true}">
              <span style="color: red; font-weight: bold;">${row.address.privateMobilePhone}</span>
            </c:when>
            <c:otherwise>${row.address.privateMobilePhone}</c:otherwise>
          </c:choose>
        </stripes:link>
        <c:if test="${actionBean.smsEnabled == true}">
          <stripes:link href="/wa/sendSms">
            <stripes:param name="addressId" value="${row.address.id}" />
            <stripes:param name="phoneType" value="privateMobile" />
            <pf:image src="/images/sms.png" tooltip="address.tooltip.writeSMS" />
          </stripes:link>
        </c:if>
      </c:if>
    </display:column>
  </display:table>

  </body>
</fmt:bundle>
</html>
