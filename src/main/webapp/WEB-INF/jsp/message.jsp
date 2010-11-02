<%@ page pageEncoding="utf-8" session="true" isELIgnored="false"%>
<%@ include file="base-include.jsp"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<fmt:bundle basename="${actionBean.bundleName}">
  <head>
  <title><fmt:message key="message.title" /></title>
  </head>
  <body>

  <fieldset>
      ${actionBean.message}
  </fieldset>
  </body>
</fmt:bundle>
</html>
