/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2010 Kai Reinhard (k.reinhard@me.com)
//
// ProjectForge is dual-licensed.
//
// This community edition is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License as published
// by the Free Software Foundation; version 3 of the License.
//
// This community edition is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
// Public License for more details.
//
// You should have received a copy of the GNU General Public License along
// with this program; if not, see http://www.gnu.org/licenses/.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.web.admin;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.io.IOUtils;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.Version;
import org.projectforge.admin.UpdatePreCheckStatus;
import org.projectforge.admin.UpdateRunningStatus;
import org.projectforge.admin.UpdateScript;
import org.projectforge.common.StringHelper;
import org.projectforge.core.Configuration;
import org.projectforge.core.InternalErrorException;
import org.projectforge.database.DatabaseUpdateDao;
import org.projectforge.scripting.GroovyExecutor;
import org.projectforge.scripting.GroovyResult;
import org.projectforge.web.wicket.AbstractSecuredPage;
import org.projectforge.web.wicket.DownloadUtils;
import org.projectforge.xml.stream.AliasMap;
import org.projectforge.xml.stream.XmlHelper;
import org.projectforge.xml.stream.XmlObjectReader;
import org.projectforge.xml.stream.XmlObjectWriter;

public class UpdatePage extends AbstractSecuredPage
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(UpdatePage.class);

  public static final String DOWNLOAD_BASE_URL = "http://www.projectforge.org/downloads/";

  public static final String UPDATE_URL = "https://www.projectforge.org/downloads/update-scripts.xml.gz";

  @SpringBean(name = "groovyExecutor")
  private GroovyExecutor groovyExecutor;

  @SpringBean(name = "databaseUpdateDao")
  private DatabaseUpdateDao databaseUpdateDao;

  private UpdateForm form;

  protected List<UpdateScript> updateScripts;

  public UpdatePage(PageParameters parameters)
  {
    super(parameters);
    // Add simple upload form, which is hooked up to its feedback panel by
    // virtue of that panel being nested in the form.
    form = new UpdateForm(this);
    body.add(form);
    form.init();
  }

  protected void upload()
  {
    accessChecker.checkIsUserMemberOfAdminGroup();
    accessChecker.checkDemoUser();
    log.info("upload");
    final FileUpload upload = form.fileUploadField.getFileUpload();
    if (upload != null) {
      try {
        InputStream is = upload.getInputStream();
        if (form.fileUploadField.getFileUpload().getClientFileName().endsWith(".gz") == true) {
          is = new GZIPInputStream(is);
        }
        readUpdateFile(is);
      } catch (Exception ex) {
        log.error(ex.getMessage(), ex);
        error("Unsupported update script format (see log files for details).");
      }
    }
  }

  protected void downloadUpdateScript(final UpdateScript updateScript)
  {
    final String filename = "update-script-" + updateScript.getVersion() + ".xml";
    final XmlObjectWriter writer = new XmlObjectWriter();
    final String script = writer.writeToXml(updateScript, true);
    final StringBuffer buf = new StringBuffer();
    buf.append(XmlHelper.XML_HEADER) //
        .append("\n<projectforge-update>") //
        .append(script) //
        .append("\n</projectforge-update>");
    DownloadUtils.setDownloadTarget(buf.toString().getBytes(), filename);
  }

  @SuppressWarnings("unchecked")
  private void readUpdateFile(final InputStream is)
  {
    final XmlObjectReader reader = new XmlObjectReader();
    reader.initialize(UpdateScript.class);
    final AliasMap aliasMap = new AliasMap();
    aliasMap.put(ArrayList.class, "projectforge-update");
    reader.setAliasMap(aliasMap);
    reader.initialize(UpdateScript.class);
    String xml = null;
    try {
      xml = IOUtils.toString(is);
    } catch (IOException ex) {
      log.error(ex.getMessage(), ex);
      error("Unsupported update script format (see log files for details).");
    }
    updateScripts = (List<UpdateScript>) reader.read(xml); // Read all scripts from xml.
    Collections.sort(updateScripts, new Comparator<UpdateScript>() {
      public int compare(UpdateScript o1, UpdateScript o2)
      {
        return StringHelper.compareTo(o1.getVersion(), o2.getVersion());
      }
    });
    updateScriptStatus();
  }

  protected void updateScriptStatus()
  {
    for (final UpdateScript updateScript : updateScripts) {
      updateScript.setVisible(false);
      runPreCheck(updateScript);
      if (form.isShowOldUpdateScripts() == true || updateScript.getPreCheckStatus() != UpdatePreCheckStatus.ALREADY_UPDATED) {
        if (updateScript.isExperimental() == false || getWicketApplication().isDevelopmentSystem() == true) {
          updateScript.setVisible(true);
        }
      }
    }
    form.updateScriptRows();
  }

  protected void update(final UpdateScript updateScript)
  {
    accessChecker.checkIsUserMemberOfAdminGroup();
    accessChecker.checkDemoUser();
    log.info("Updating script " + updateScript.getVersion());
    runPreCheck(updateScript);
    if (UpdatePreCheckStatus.OK != updateScript.getPreCheckStatus()) {
      log.error("Pre-check failed. Aborting.");
      return;
    }
    final GroovyResult result = execute(updateScript.getScript());
    updateScript.setRunningResult(result);
    if (result != null) {
      updateScript.setRunningStatus(((UpdateRunningStatus) result.getResult()));
    }
    runPreCheck(updateScript);
    updateScriptStatus();
  }

  protected void runPreCheck(final UpdateScript updateScript)
  {
    final GroovyResult result = execute(updateScript.getPreCheck());
    updateScript.setPreCheckResult(result);
    updateScript.setPreCheckStatus(((UpdatePreCheckStatus) result.getResult()));
  }

  protected void checkForUpdates()
  {
    accessChecker.checkIsUserMemberOfAdminGroup();
    accessChecker.checkDemoUser();
    log.info("Check for updates");

    URL url;
    URLConnection urlc;
    InputStream is = null;
    String version = Version.NUMBER;
    try {
      url = new URL(UPDATE_URL.replace("VERSION", version));
    } catch (MalformedURLException ex) {
      log.error(ex.getMessage(), ex);
      throw new InternalErrorException(ex.getMessage());
    }
    try {
      urlc = url.openConnection();
      ((HttpsURLConnection) urlc).setSSLSocketFactory(Configuration.getInstance().getProjectForgesSSLSocketFactory());
      urlc.connect();
      is = new GZIPInputStream(urlc.getInputStream());
      if (is != null) {
        readUpdateFile(is);
      }
    } catch (IOException ex) {
      log.error(ex.getMessage(), ex);
      error("IOException: " + ex.getMessage());
    } catch (Exception ex) {
      log.error(ex.getMessage(), ex);
      error("Unsupported update script format (see log files for details).");
    }
  }

  protected GroovyResult execute(final String script)
  {
    final Map<String, Object> scriptVariables = new HashMap<String, Object>();
    scriptVariables.put("dao", databaseUpdateDao);
    scriptVariables.put("log", log);
    final StringBuffer buf = new StringBuffer();
    buf.append("import org.projectforge.admin.*;\n")//
        .append("import org.projectforge.database.*;\n\n")//
        .append(script);
    final GroovyResult groovyResult = groovyExecutor.execute(buf.toString(), scriptVariables);
    if (groovyResult == null) {
      form.addError("exception.groovyError", "See error log for details.");
    } else if (groovyResult.hasException() == true) {
      form.addError("exception.groovyError", String.valueOf(groovyResult.getException()));
    }
    return groovyResult;
  }

  @Override
  protected String getTitle()
  {
    return getString("system.update.title");
  }
}
