<%@ page pageEncoding="utf-8" session="true" isELIgnored="false"%>
<%@ include file="../base-include.jsp"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<fmt:bundle basename="${actionBean.bundleName}">
  <head>
  <title><fmt:message key="administration.title" /></title>
  </head>
  <body>

  <stripes:errors />

  <stripes:form method="post" action="/secure/admin/Admin.action" focus="">

    <fieldset><legend style="display: inline;">System checks and functionality_</legend>
    <table class="form">
      <tr>
        <td class="buttons" style="direction: ltr;"><stripes:submit name="checkSystemIntegrity" value="Check system integrity_" /> <stripes:submit
          name="checkI18nProperties" value="Check i18n properties_" /> <stripes:submit name="refreshCaches" value="Refresh all caches_" /> <c:if
          test="${actionBean.mebMailAccountConfigured == true}">
          <stripes:submit name="checkUnseenMebMails" value="Check for new MEB mails_" />
          <stripes:submit name="importAllMebMails" value="Import all MEB mails_" title="Imports all MEB mails, ignores the already imported messages." />
        </c:if><br />
        <stripes:submit name="rereadConfiguration" value="Reread configuration_" /> <stripes:submit name="exportConfiguration"
          value="Export configuration_" /></td>
      </tr>
    </table>
    </fieldset>
    <fieldset><legend style="display: inline;">Data base actions_</legend>
    <table class="form">
      <tr>
        <td class="buttons" style="direction: ltr;"><stripes:submit name="updateUserPrefs" value="Update all user prefs_" /> <stripes:submit
          name="createMissingDatabaseIndices" value="Create missing data base indices_" /> <c:if test="${actionBean.developmentSystem == true}">
          <stripes:submit name="dump" value="Dump database_" />
        </c:if><stripes:submit name="schemaExport" value="Schema export_" /></td>
      </tr>
    </table>
    </fieldset>
    <fieldset><legend style="display: inline;">Data base search indices_</legend>
    <table class="form">
      <tr>
        <td class="buttons" style="direction: ltr;"><stripes:text class="stdtext" name="reindexNewestNEntries" style="width:5em;" /> newest
        entries and/or from <stripes:text class="date" name="reindexFromDate" /> (date) <stripes:submit name="reindex"
          value="Rebuild db search indices_" /> (Based on the time of last modification of the entries.)</td>
      </tr>
    </table>
    </fieldset>
    <fieldset><legend style="display: inline;">Format log entries_</legend>
    <table class="form">
      <tr>
        <td><stripes:text class="stdtext" name="logEntries" style="width:100%" />${actionBean.formattedLogEntries}</td>
      </tr>
      <tr>
        <td class="buttons" style="direction: ltr;"><stripes:submit name="formatLogEntries" value="Format log entries_" /></td>
      </tr>
    </table>
    </fieldset>
    <fieldset><legend style="display: inline;">Alert message_</legend>
    <table class="form">
      <tr>
        <td><stripes:text class="stdtext" name="alertMessage" style="width:100%" /></td>
      </tr>
      <tr>
        <td>For copy &amp; paste_:<br />
        <b>Achtung: ProjectForge ist um 13:00 Uhr für ca. 5 Minuten nicht erreichbar! Es wird das neue Release ${actionBean.appVersion}
        eingespielt.</b><br />
        <b>Achtung: ProjectForge ist um 13:00 Uhr für ca. 5 Minuten nicht erreichbar! Es werden Wartungsarbeiten vorgenommen.</b><br />
        <b>Achtung: ProjectForge ist um 13:00 Uhr für ca. 5 Minuten nicht erreichbar! Es wird der Patch ${actionBean.appVersion}p1 eingespielt.</b></td>
      </tr>
      <tr>
        <td class="buttons" style="direction: ltr;"><stripes:submit name="setAlertMessage" value="Set system alert message_" /></td>
      </tr>
    </table>
    </fieldset>
  </stripes:form>
  Text strings on this page ending with an underscore '*_' are not localized (OK).
  </body>
</fmt:bundle>
</html>
