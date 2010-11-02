<%@ page pageEncoding="utf-8" session="true" isELIgnored="false"%>
<%@ include file="../base-include.jsp"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<fmt:bundle basename="${actionBean.bundleName}">
  <head>
  <title><c:choose>
    <c:when test="${actionBean.address.id >= 0}">
      <fmt:message key="address.edit.title" />
    </c:when>
    <c:otherwise>
      <fmt:message key="address.add.title" />
    </c:otherwise>
  </c:choose></title>
  <c:set var="action" value="/secure/address/AddressEdit.action" />
  <script type="text/javascript">
<!--
$(document).ready(function(){
$(":submit[name='update']").bind("click", function(e) {
      if (document.forms[0].elements["address.name"].value == '${actionBean.address.name}' && document.forms[0].elements["address.firstName"].value == '${actionBean.address.firstName}') {
        return true;
      }
      return window.confirm('<fmt:message key="address.question.changeName" /> (${actionBean.address.firstName}' + ' ' + '${actionBean.address.name})');
    });
    });
    -->
    </script>
  </head>
  <body>

  <stripes:errors />

  <stripes-d:form method="post" action="/secure/address/AddressEdit.action" focus="" autocomplete="off">
    <stripes:hidden name="address.id" />
    <stripes:hidden name="taskId" />
    <stripes:hidden name="eventKey" />

    <fieldset><legend><fmt:message key="label.data" /></legend>
    <table class="form">
      <tr>
        <th><fmt:message key="task" /></th>
        <td colspan="5"><pf:task taskId="${actionBean.data.taskId}" showPath="true" /></td>
      </tr>
      <tr>
        <th><fmt:message key="name" /> / <fmt:message key="address.form" /></th>
        <td colspan="3"><stripes:text class="stdtext" name="address.name" style="width:20em" tabindex="1" /> / <stripes:select
          name="address.form" style="width:10em" tabindex="2">
          <stripes:options-collection collection="${actionBean.addressFormList}" label="label" value="value" />
        </stripes:select> <stripes:checkbox name="personalAddress.favoriteCard" /><pf:info key="address.tooltip.vCardList" /></td>
        <th><fmt:message key="address.businessPhone" /></th>
        <td colspan="3"><stripes:text class="shorttext" name="address.businessPhone" tabindex="9" /> <stripes:checkbox
          name="personalAddress.favoriteBusinessPhone" /><pf:info key="address.tooltip.phonelist" /></td>
      </tr>
      <tr>
        <th><fmt:message key="firstName" /> / <fmt:message key="address.title" /></th>
        <td colspan="3"><stripes:text class="stdtext" name="address.firstName" style="width:20em" tabindex="3" /> / <stripes:text
          class="stdtext" name="address.title" style="width:10em;" tabindex="4" /></td>
        <th><fmt:message key="address.fax" /></th>
        <td colspan="3"><stripes:text class="shorttext" name="address.fax" tabindex="10" /> <stripes:checkbox name="personalAddress.favoriteFax" /><pf:info
          key="address.tooltip.phonelist" /></td>
      </tr>
      <tr>
        <th><fmt:message key="organization" /></th>
        <td colspan="3"><stripes:text class="stdtext" name="address.organization" tabindex="5" id="organizationAjax" /></td>
        <th><fmt:message key="address.mobilePhone" /></th>
        <td colspan="3"><stripes:text class="shorttext" name="address.mobilePhone" tabindex="11" /> <stripes:checkbox
          name="personalAddress.favoriteMobilePhone" /><pf:info key="address.tooltip.phonelist" /></td>
      </tr>
      <tr>
        <th><fmt:message key="address.division" /></th>
        <td colspan="3"><stripes:text class="stdtext" name="address.division" tabindex="6" /></td>
        <th><fmt:message key="address.positionText" /></th>
        <td colspan="3"><stripes:text class="stdtext" name="address.positionText" tabindex="12" /></td>
      </tr>
      <tr>
        <th><fmt:message key="email" /></th>
        <td colspan="3"><stripes:text class="stdtext" name="address.email" tabindex="7" /></td>
        <th><fmt:message key="address.website" /></th>
        <td colspan="3"><stripes:text class="stdtext" name="address.website" tabindex="13" /></td>
      </tr>
      <tr>
        <th><fmt:message key="address.contactStatus" /></th>
        <td colspan="3"><stripes:select name="address.contactStatus" tabindex="8">
          <stripes:options-collection collection="${actionBean.contactStatusList}" label="label" value="value" />
        </stripes:select></td>
        <th><fmt:message key="address.imageBroschure" /></th>
        <td colspan="3"><stripes:checkbox name="address.imageBroschure" tabindex="14" /></td>
      </tr>
      <tr class="hRuler">
        <td colspan="8"></td>
      </tr>

      <tr>
        <th><fmt:message key="address.addressStatus" /></th>
        <td colspan="3"><stripes:select name="address.addressStatus" tabindex="15">
          <stripes:options-collection collection="${actionBean.addressStatusList}" label="label" value="value" />
        </stripes:select></td>
      </tr>
      <tr>
        <th><fmt:message key="address.addressText" /></th>
        <td colspan="3"><stripes:text class="stdtext" name="address.addressText" tabindex="16" id="addressTextAjax" /></td>
        <th><fmt:message key="address.postalAddressText" /></th>
        <td colspan="3"><stripes:text class="stdtext" name="address.postalAddressText" tabindex="21" /></td>
      </tr>
      <tr>
        <th><fmt:message key="address.zipCode" /> / <fmt:message key="address.city" /></th>
        <td colspan="3"><stripes:text class="text" name="address.zipCode" size="5" tabindex="17" />&nbsp;<stripes:text class="shorttext"
          name="address.city" tabindex="18" /></td>
        <th><fmt:message key="address.zipCode" /> / <fmt:message key="address.city" /></th>
        <td colspan="3"><stripes:text class="text" name="address.postalZipCode" size="5" tabindex="22" />&nbsp;<stripes:text class="shorttext"
          name="address.postalCity" tabindex="23" /></td>
      </tr>
      <tr>
        <th><fmt:message key="address.country" /></th>
        <td><stripes:text class="shorttext" name="address.country" tabindex="19" /></td>
        <th><fmt:message key="address.state" /></th>
        <td><stripes:text class="shorttext" name="address.state" tabindex="20" /></td>
        <th><fmt:message key="address.country" /></th>
        <td><stripes:text class="shorttext" name="address.postalCountry" tabindex="24" /></td>
        <th><fmt:message key="address.state" /></th>
        <td><stripes:text class="shorttext" name="address.postalState" tabindex="25" /></td>
      </tr>
      <tr class="hRuler">
        <td colspan="8"></td>
      </tr>
      <tr>
        <th><fmt:message key="address.privateAddressText" /></th>
        <td colspan="3"><stripes:text class="stdtext" name="address.privateAddressText" tabindex="26" /></td>
        <th><fmt:message key="address.privatePhone" /></th>
        <td colspan="3"><stripes:text class="shorttext" name="address.privatePhone" tabindex="31" /><stripes:checkbox
          name="personalAddress.favoritePrivatePhone" /><pf:info key="address.tooltip.phonelist" /></td>
      </tr>
      <tr>
        <th><fmt:message key="address.zipCode" /> / <fmt:message key="address.city" /></th>
        <td colspan="3"><stripes:text class="text" name="address.privateZipCode" size="5" tabindex="27" />&nbsp;<stripes:text class="shorttext"
          name="address.privateCity" tabindex="28" /></td>
        <th><fmt:message key="address.privateMobilePhone" /></th>
        <td colspan="3"><stripes:text class="shorttext" name="address.privateMobilePhone" tabindex="32" /><stripes:checkbox
          name="personalAddress.favoritePrivateMobilePhone" /><pf:info key="address.tooltip.phonelist" /></td>
      </tr>
      <tr>
        <th><fmt:message key="address.country" /></th>
        <td><stripes:text class="shorttext" name="address.privateCountry" tabindex="29" /></td>
        <th><fmt:message key="address.state" /></th>
        <td><stripes:text class="shorttext" name="address.privateState" tabindex="30" /></td>
        <th><fmt:message key="address.privateEmail" /></th>
        <td colspan="3"><stripes:text class="stdtext" name="address.privateEmail" tabindex="33" /></td>
      </tr>
      <tr class="hRuler">
        <td colspan="8"></td>
      </tr>
      <tr>
        <th><fmt:message key="address.birthday" /></th>
        <td colspan="7"><stripes:text class="date" name="address.birthday" tabindex="34" />&nbsp;<pf:image src="/images/cake.png" />&nbsp;<span
          style="font-size: x-small; color: white;"><pf:date date="${actionBean.address.birthday}" type="utc" /></span></td>
      </tr>
      <tr class="hRuler">
        <td colspan="8"></td>
      </tr>
      <tr>
        <th colspan="4"><fmt:message key="comment" /></th>
        <th colspan="4"><fmt:message key="address.publicKey" /></th>
      </tr>
      <tr>
        <td colspan="4"><stripes:textarea class="stdtext" name="address.comment" style="height:20ex;" tabindex="35" /></td>
        <td colspan="4"><stripes:textarea class="stdtext" name="address.publicKey" style="height:20ex;" tabindex="36" /></td>
      </tr>
      <tr>
        <th colspan="8"><fmt:message key="address.fingerprint" /></th>
      </tr>
      <tr>
        <td colspan="8"><stripes:text class="stdtext" style="width:100%;" name="address.fingerprint" tabindex="37" /></td>
      </tr>
      <tr class="hRuler">
        <td colspan="8"></td>
      </tr>
      <tr>
        <td class="buttons" colspan="8"><jsp:include page="../include/editDOButtons.jsp" /></td>
      </tr>
    </table>
    </fieldset>

    <jsp:include page="../include/history.jsp">
      <jsp:param name="requestURI" value="/secure/address/AddressEdit.action" />
    </jsp:include>

    <script type="text/javascript">  
    $(document).ready(function() {
      var z = $("#organizationAjax");
      z.autocomplete("<c:url value='${action}' />", "organizationAutocomplete",{matchContains:1, minChars:2, selectOnly:true});
      var z = $("#addressTextAjax");
      z.autocomplete("<c:url value='${action}' />", "addressTextAutocomplete",{matchContains:1, minChars:2, selectOnly:true});
    });
</script>

  </stripes-d:form>
  </body>
</fmt:bundle>
</html>
