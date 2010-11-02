<%@ page pageEncoding="utf-8" session="true" isELIgnored="false"%>
<%@ include file="../base-include.jsp"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<fmt:bundle basename="${actionBean.bundleName}">
  <head>
  <title><fmt:message key="user.changePassword.title" /></title>
  </head>
  <body>

  <stripes:errors />

  <stripes:form method="post"
    action="/secure/user/ChangePassword.action" focus="oldPassword">

    <fieldset><legend><fmt:message key="label.data" /></legend>
    <table class="form">
      <tr>
        <th><fmt:message key="/secure/user/ChangePassword.action.oldPassword" /></th>
        <td><stripes:password class="stdtext" name="oldPassword" /></td>
      </tr>
      <tr>
        <th><fmt:message key="/secure/user/ChangePassword.action.newPassword" /></th>
        <td><stripes:password class="stdtext" name="newPassword" /></td>
      </tr>
      <tr>
        <th><fmt:message key="passwordRepeat" /></th>
        <td><stripes:password class="stdtext" name="passwordRepeat" /></td>
      </tr>
      <tr>
        <td  colspan="2" class="buttons">
        <stripes:submit name="change" />
        <stripes:submit name="cancel" />
        </td>
      </tr>
    </table>
    </fieldset>
  </stripes:form>
  </body>
</fmt:bundle>
</html>
