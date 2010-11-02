<%@ page pageEncoding="utf-8" session="true" isELIgnored="false"%>
<%@ include file="base-include.jsp"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<fmt:bundle basename="I18nResources">
  <head>
  <title><fmt:message key="login.title" /></title>
  </head>
  <body>
  <c:if test="${not empty param.login_error}">
    <div class="warn"><fmt:message key="login.error.loginFailed" /></div>
  </c:if>

  <fieldset><legend><fmt:message key="login.title" /></legend> <stripes:form action="/secure/Login.action" method="post" focus="username">
    <div class="login">
    <table class="form">
      <tr>
        <th style="padding: 20px 6px;" colspan="2"><fmt:message key="login.welcome" /></th>
      </tr>
      <tr>
        <th><fmt:message key="username" /></th>
        <td><stripes:text class="shorttext" name="username" /></td>
      </tr>
      <tr>
        <th><fmt:message key="password" /></th>
        <td><input type="password" class="shorttext" name="password" /></td>
      </tr>
      <tr>
        <th>&nbsp;</th>
        <td><input type="checkbox" name="stayLoggedIn" value="true" />&nbsp;<fmt:message key="login.stayLoggedIn" />&nbsp;<a
          href="/secure/doc/Handbuch.html#label_stay-logged-in" target="_blank"><pf:help key="login.stayLoggedIn.tooltip" image="help.png" /></a></td>
      </tr>
      <tr>
        <td class="buttons" colspan="2"><input class="SubmitClass" type="submit" tabindex="3" value='<fmt:message key="login"/>' /></td>
      </tr>
      <tr>
        <td colspan="2">${actionBean.messageOfTheDay}</td>
      </tr>
    </table>
    </div>
  </stripes:form></fieldset>
  </body>
</fmt:bundle>
</html>
