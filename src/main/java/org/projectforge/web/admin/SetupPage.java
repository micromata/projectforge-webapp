/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2013 Kai Reinhard (k.reinhard@micromata.de)
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

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.zip.GZIPInputStream;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.core.Configuration;
import org.projectforge.core.ConfigurationDO;
import org.projectforge.core.ConfigurationDao;
import org.projectforge.core.ConfigurationParam;
import org.projectforge.core.HibernateSearchReindexer;
import org.projectforge.database.InitDatabaseDao;
import org.projectforge.database.XmlDump;
import org.projectforge.database.xstream.XStreamSavingConverter;
import org.projectforge.task.TaskTree;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserGroupCache;
import org.projectforge.web.LoginPage;
import org.projectforge.web.MenuItemRegistry;
import org.projectforge.web.UserFilter;
import org.projectforge.web.wicket.AbstractUnsecureBasePage;
import org.projectforge.web.wicket.MessagePage;
import org.projectforge.web.wicket.MySession;
import org.projectforge.web.wicket.WicketUtils;

public class SetupPage extends AbstractUnsecureBasePage
{
  private static final long serialVersionUID = 9174903871130640690L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SetupPage.class);

  @SpringBean(name = "initDatabaseDao")
  private InitDatabaseDao initDatabaseDao;

  @SpringBean(name = "configurationDao")
  private ConfigurationDao configurationDao;

  @SpringBean(name = "hibernateSearchReindexer")
  private HibernateSearchReindexer hibernateSearchReindexer;

  @SpringBean(name = "taskTree")
  private TaskTree taskTree;

  @SpringBean(name = "userGroupCache")
  private UserGroupCache userGroupCache;

  @SpringBean(name = "xmlDump")
  private XmlDump xmlDump;

  private final SetupForm setupForm;

  private final SetupImportForm importForm;

  public SetupPage(final PageParameters parameters)
  {
    super(parameters);
    checkAccess();
    setupForm = new SetupForm(this);
    body.add(setupForm);
    setupForm.init();
    importForm = new SetupImportForm(this);
    body.add(importForm);
    importForm.init();
    // final StringBuffer js = new StringBuffer("<script>\n") //
    // .append("$(function() {") //
    // .append("  $('input:file').uniform({\n") //
    // .append("    fileDefaultText : '- No file selected',\n") //
    // .append("    fileBtnText : 'Choose - File'\n") //
    // .append("  }); });\n") //
    // .append("alert('Hurzel');") //
    // .append("</script>\n");
    // body.add(new Label("uploadScript", js.toString()).setEscapeModelStrings(false));
  }

  protected void finishSetup()
  {
    log.info("Finishing the set-up...");
    checkAccess();
    PFUserDO adminUser = null;
    final String message;
    if (setupForm.getSetupMode() == SetupTarget.EMPTY_DATABASE) {
      adminUser = initDatabaseDao.initializeEmptyDatabase(setupForm.getAdminUsername(), setupForm.getEncryptedPassword(),
          setupForm.getTimeZone());
      message = "administration.setup.message.emptyDatabase";
    } else {
      adminUser = initDatabaseDao.initializeEmptyDatabaseWithTestData(setupForm.getAdminUsername(), setupForm.getEncryptedPassword(),
          setupForm.getTimeZone());
      message = "administration.setup.message.testdata";
      // refreshes the visibility of the costConfigured dependent menu items:
      Configuration.getInstance().setExpired(); // Force reload.
      MenuItemRegistry.instance().refresh();
    }
    ((MySession) getSession()).login(adminUser, getRequest());
    UserFilter.login(WicketUtils.getHttpServletRequest(getRequest()), adminUser);
    configurationDao.checkAndUpdateDatabaseEntries();
    if (setupForm.getTimeZone() != null) {
      final ConfigurationDO configurationDO = getConfigurationDO(ConfigurationParam.DEFAULT_TIMEZONE);
      if (configurationDO != null) {
        configurationDO.setTimeZone(setupForm.getTimeZone());
        configurationDao.update(configurationDO);
      }
    }
    configure(ConfigurationParam.SYSTEM_ADMIN_E_MAIL, setupForm.getSysopEMail());
    configure(ConfigurationParam.FEEDBACK_E_MAIL, setupForm.getFeedbackEMail());
    setResponsePage(new MessagePage(message, adminUser.getUsername()));
    log.info("Set-up finished.");
  }

  private ConfigurationDO getConfigurationDO(final ConfigurationParam param)
  {
    final ConfigurationDO configurationDO = configurationDao.getEntry(param);
    if (configurationDO == null) {
      log.error("Oups, can't find configuration parameter '" + param + "'. You can re-configure it anytime later.");
    }
    return configurationDO;
  }

  private void configure(final ConfigurationParam param, final String value)
  {
    if (StringUtils.isBlank(value) == true) {
      return;
    }
    final ConfigurationDO configurationDO = getConfigurationDO(param);
    if (configurationDO != null) {
      configurationDO.setStringValue(value);
      configurationDao.update(configurationDO);
    }
  }

  protected void upload()
  {
    checkAccess();
    log.info("Uploading data-base dump file...");
    final FileUpload fileUpload = importForm.fileUploadField.getFileUpload();
    if (fileUpload == null) {
      return;
    }
    try {
      final String clientFileName = fileUpload.getClientFileName();
      Reader reader = null;
      if (clientFileName.endsWith(".xml.gz") == true) {
        reader = new InputStreamReader(new GZIPInputStream(fileUpload.getInputStream()));
      } else if (clientFileName.endsWith(".xml") == true) {
        reader = new InputStreamReader(fileUpload.getInputStream());
      } else {
        log.info("Unsupported file suffix. Only *.xml and *.xml.gz is supported: " + clientFileName);
        error(getString("administration.setup.error.uploadfile"));
        return;
      }
      final XStreamSavingConverter converter = xmlDump.restoreDatabase(reader);
      final int counter = xmlDump.verifyDump(converter);
      configurationDao.checkAndUpdateDatabaseEntries();
      Configuration.getInstance().setExpired();
      taskTree.setExpired();
      userGroupCache.setExpired();
      new Thread() {
        @Override
        public void run()
        {
          hibernateSearchReindexer.rebuildDatabaseSearchIndices();
        }
      }.start();
      if (counter > 0) {
        ((MySession) getSession()).logout();
        setResponsePage(LoginPage.class);
      } else {
        error(getString("administration.setup.error.import"));
      }
    } catch (final Exception ex) {
      log.error(ex.getMessage(), ex);
      error(getString("administration.setup.error.import"));
    }
  }

  @Override
  protected String getTitle()
  {
    return getString("administration.setup.title");
  }

  private void checkAccess()
  {
    if (initDatabaseDao.isEmpty() == false) {
      log.error("Couldn't call set-up page, because the data-base isn't empty!");
      ((MySession) getSession()).logout();
      throw new RestartResponseException(WicketUtils.getDefaultPage());
    }
  }

  /**
   * @see org.projectforge.web.wicket.AbstractUnsecureBasePage#thisIsAnUnsecuredPage()
   */
  @Override
  protected void thisIsAnUnsecuredPage()
  {
  }
}
