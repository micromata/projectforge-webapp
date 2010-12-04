<%@ page language="java" isErrorPage="true" pageEncoding="utf-8" session="true" isELIgnored="false"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<fmt:bundle basename="I18nResources">
  <head>
  <title><fmt:message key="errorpage.title" /></title>
  </head>
  <body>
  <%
    if (exception != null) {
        org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger("org.projectforge.web.ErrorPage");
        String errorMessage = exception.getMessage();
        if (exception instanceof ServletException == true) {
          log.error(errorMessage, ((ServletException) exception).getRootCause());
        } else {
          log.error(errorMessage, exception);
        }
      }
  %>
  <h1 class="errorpage"><fmt:message key="errorpage.title" /></h1>

  </body>
</fmt:bundle>
</html>
