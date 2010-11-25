<%@ taglib uri="http://www.opensymphony.com/sitemesh/decorator" prefix="decorator"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<%@ include file="../base-include.jsp"%>
<html>
<fmt:bundle basename="${actionBean.bundleName}">
  <head>
  <link rel="shortcut icon" href='<c:url value="/favicon.ico" />' />
  <title>ProjectForge® 2010 - <decorator:title default="Successful access wherever you are!" /></title>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
  <link href='<c:url value="/styles/main.css"/>' type="text/css" rel="stylesheet" />
  <link href='<c:url value="/styles/calendar.css"/>' type="text/css" rel="stylesheet" />
  <link href='<c:url value="/styles/tooltip.css"/>' type="text/css" rel="stylesheet" />
  <link href='<c:url value="/styles/jquery.autocomplete.css"/>' type="text/css" rel="stylesheet" />
  <link rel="apple-touch-icon" href='<c:url value="/images/appleTouchIcon.png"/>' />
  <script type="text/javascript" src="<c:url value='/scripts/dtree.js'/>"></script>
  <script type="text/javascript">
  /* <![CDATA[ */
  function submitEvent (event) {
    document.getElementsByName("eventKey")[0].value = event;
    return document.forms[0].submit();
  }

  function submitSelectedEvent (event, selectedValue) {
    document.getElementsByName("eventKey")[0].value = event;
    document.getElementsByName("selectedValue")[0].value = selectedValue;
    return document.forms[0].submit();
  }
  
  function submitButton (button) {
  /* Is equivalent (for stripes) as clicking on a submit button with the name button. */
    var action = document.forms[0].action + '?' + button + '=';
    /* TODO: Check wether action already contains '?' or not. */
    document.forms[0].action = action;
    return document.forms[0].submit();
  }
  
  function disableButtons() {
    $(":submit").attr("disabled", true);
  }
  
  function MM_openBrWindow(theURL, winName, features) { //v2.0
    var w = window.open(theURL, winName, features);
    w.focus();
  }
  
  function showDeleteQuestionDialog() {
    return window.confirm("<fmt:message key='question.markAsDeletedQuestion'/>");
  }
  /* ]]> */
</script>
  <script src="<c:url value='/scripts/jquery-1.3.2.min.js'/>" type="text/javascript" charset="utf-8"></script>
  <script src="<c:url value='/scripts/jquery.dimensions.min.js'/>" type="text/javascript" charset="utf-8"></script>
  <script src="<c:url value='/scripts/jquery.tooltip.min.js'/>" type="text/javascript" charset="utf-8"></script>
  <script src="<c:url value='/scripts/jquery.autocomplete.js'/>" type="text/javascript" charset="utf-8"></script>
  <script type="text/javascript" charset="utf-8">
  /* <![CDATA[ */
    $(document).ready(function(){
      $(':text[readonly]').addClass("readOnly");
      $(':input[readonly]').addClass("readOnly");
   
      // Mache alle Listen klickbar, verwende alle Links außer den Explorerbuttons
      $(".dataTable td:not(.notrlink)").click( function() {
        window.location.href = $(this).parent().find("a:not(.explore):first").attr("href");
      });
      // Kleb zwei Spans um alle &lt;a&gt; im contentMenu und mache schicke Knoepfe draus
      $("#contentMenu a").wrap("<span class='buttonizeL'><span class='buttonizeR'></span></span>");
      // Enable all title attributes as tooltip:
      $(':*[title]').tooltip({
        track: true,
        delay: 1000,
        showURL: false,
        opacity: 1,
        showBody: " - ",
        top: 5,
        left: 5
      });
    });
  
    /* ]]> */
</script>
  <decorator:head />
  </head>
  <c:if test="${actionBean.developmentSystem == true}">
    <c:set var="devStyle" value='style="background-color: #eeaaaa;"' />
  </c:if>
  <body onload="<decorator:getProperty property='body.onload' />" class="nüschte <decorator:getProperty property='body.class' />">
  <div id="container"><!-- left --> <c:url var="imagePath" value="/images/dtree/" /> <c:if test='${fn:contains(imagePath, ";")}'>
    <c:set var="imagePath" value='${fn:substringBefore(imagePath, ";")}' />
  </c:if>
  <div id="navigation" ${devStyle}>
  	<div class="navcontainer clearfix" >
  <c:if test="${actionBean.developmentSystem == true}">
    <span style="font-weight: bold; font-size: 140%">Developmentsystem!</span>
  </c:if>
	<div id="mainmenu">
  <script type="text/javascript">
      dtree = new dTree('dtree', '${imagePath}' );
      dtree.icon.root = "<c:url value='/images/dtree/micromata_icon.png' />";
      dtree.config.folderLinks = false;
      dtree.add(0,-1,'ProjectForge® 2010');
      <c:forEach var="menuEntry" items="${actionBean.menu.menuEntries4dTree}">
        <c:set var="dtId" value="${menuEntry.orderNumber}" />
        <c:set var="dtPId" value="0" />
        <c:if test="${menuEntry.hasParent == true}">
          <c:set var="dtPId" value="${menuEntry.parent.orderNumber}"/>
        </c:if>
        <fmt:message var="dtLabel" key="${menuEntry.label}"/>
        <c:set var="dtLabelSuffix" value="${menuEntry.htmlSuffixString}" />
        <c:url var="dtUrl" value="/${menuEntry.url}" />
        <c:set var="dtTitle" value="" />
        <c:set var="dtTarget" value="" />
        <c:if test="${menuEntry.newWindow == true}">
          <c:url var="dtTarget" value="pforge2" />
        </c:if>
        <c:set var="dtIcon" value="" />
        <c:if test="${not empty menuEntry.icon}">
          <c:url var="dtIcon" value="/images/${menuEntry.icon}" />
        </c:if>
        <c:set var="dtIconOpen" value="" />
        <c:if test="${not empty menuEntry.iconOpen}">
          <c:url var="dtIconOpen" value="/images/${menuEntry.iconOpen}" />
        </c:if>
        <c:set var="dtOpen" value="" />
        dtree.add(${dtId},${dtPId},'${dtLabel}${dtLabelSuffix}','${dtUrl}','${dtTitle}','${dtTarget}','${dtIcon}','${dtIconOpen}','${dtOpen}');
      </c:forEach>

      document.write(dtree);
    </script> <!-- DIV .dtree END -->
    
    </div>
    <div style="display:none"> <!--TODO: Noch aufräumen-->
  <p><c:choose>
    <c:when test="${actionBean.context.loggedIn == true}">
      <fmt:message key="loggedInUserInfo" />:<br />
      ${actionBean.context.user.userDisplayname}
    </c:when>
    <c:otherwise>
      <fmt:message key="notLoggedIn" />
    </c:otherwise>
  </c:choose></p>
  <a href='<c:url value="/wa/feedback" />'><pf:image src="/images/comment.png" tooltip="feedback.link.tooltip" /></a> <a href="<c:url value='/secure/doc/News.html' />" target="_blank"><span style="color: grey;">V.&nbsp;${actionBean.appVersion},&nbsp;${actionBean.appReleaseTimestamp}</span></a>
  
  </div>
  
  </div>
  </div>
  <!-- DIV #navigation END -->

  <div id="content">
  	<c:if test="${actionBean.context.loggedIn == true && not empty actionBean.context.alertMessage}">
    		<div style="color: red; font-size: large; line-height: 25px; font-weight: bold; margin: 3px;">${actionBean.context.alertMessage}</div>
  	</c:if> 
  	<decorator:body />
  </div>
  <!-- DIV content END -->
  <div id="footer"></div>
  </div>
  <!-- DIV container END -->
  </body>
</fmt:bundle>
</html>
