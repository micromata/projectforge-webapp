<%@ page pageEncoding="utf-8" session="true" isELIgnored="false"%>
<%@ include file="base-include.jsp"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<fmt:bundle basename="I18nResources">
  <head>
  <title><fmt:message key="login.title" /></title>
  </head>
  <body class="loginpage">
  <c:if test="${not empty param.login_error}">
    <div class="warn"><fmt:message key="login.error.loginFailed" /></div>
  </c:if>
   	 
  	<stripes:form action="/secure/Login.action" method="post" focus="username" class="loginform">
  		<fieldset>
 			<legend><fmt:message key="login.title" /></legend>
     	
	     	<h3><fmt:message key="login.welcome" /></h3>
	        <div class="clearfix">
	        	<label for="loginname"><fmt:message key="username" /></label>
	        	<stripes:text class="text full" name="username" id="loginname"/>
	        </div>
	        <div class="clearfix">
	        	<label for="loginpasswd"><fmt:message key="password" /></label>
	        	<input id="loginpasswd" type="password" class="text full" name="password" />
	        </div>	
	        <div class="stay">
	        	<input type="checkbox" name="stayLoggedIn" value="true" />&nbsp;<fmt:message key="login.stayLoggedIn" />
        		<a href="/secure/doc/Handbuch.html#label_stay-logged-in" ><pf:help key="login.stayLoggedIn.tooltip" image="help.png" /></a>
        	</div>
        </fieldset>
        
        <fieldset class="buttons">
        	
        	<input class="SubmitClass" type="submit" tabindex="3" value='<fmt:message key="login"/>' />
        </fieldset>
  </stripes:form>
  </body>
</fmt:bundle>
</html>
