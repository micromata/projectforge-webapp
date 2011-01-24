/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2011 Kai Reinhard (k.reinhard@me.com)
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

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.PageParameters;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.core.ConfigurationDO;
import org.projectforge.core.ConfigurationDao;
import org.projectforge.core.ConfigurationParam;
import org.projectforge.core.UserException;
import org.projectforge.database.InitDatabaseDao;
import org.projectforge.user.PFUserDO;
import org.projectforge.web.UserFilter;
import org.projectforge.web.wicket.AbstractSecuredPage;
import org.projectforge.web.wicket.MessagePage;
import org.projectforge.web.wicket.MySession;

public class SetupPage extends AbstractSecuredPage
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SetupPage.class);

  @SpringBean(name = "initDatabaseDao")
  private InitDatabaseDao initDatabaseDao;

  @SpringBean(name = "configurationDao")
  private ConfigurationDao configurationDao;

  private SetupForm form;

  public SetupPage(PageParameters parameters)
  {
    super(parameters);
    checkAccess();
    form = new SetupForm(this);
    body.add(form);
    form.init();
  }

  protected void finishSetup()
  {
    log.info("Finishing the set-up...");
    checkAccess();
    PFUserDO adminUser = null;
    final String message;
    if (form.getSetupMode() == SetupTarget.TEST_DATA) {
      adminUser = initDatabaseDao.initializeEmptyDatabaseWithTestData(form.getEncryptedPassword(), form.getTimeZone());
      message = "administration.setup.message.testdata";
    } else {
      adminUser = initDatabaseDao.initializeEmptyDatabase(form.getEncryptedPassword(), form.getTimeZone());
      message = "administration.setup.message.emptyDatabase";
    }
    ((MySession) getSession()).login(adminUser, getRequest());
    UserFilter.login(((WebRequest) getRequest()).getHttpServletRequest(), adminUser);
    configurationDao.checkAndUpdateDatabaseEntries();
    if (form.getTimeZone() != null) {
      final ConfigurationDO configurationDO = getConfigurationDO(ConfigurationParam.DEFAULT_TIMEZONE);
      if (configurationDO != null) {
        configurationDO.setTimeZone(form.getTimeZone());
        configurationDao.update(configurationDO);
      }
    }
    configure(ConfigurationParam.SYSTEM_ADMIN_E_MAIL, form.getSysopEMail());
    configure(ConfigurationParam.FEEDBACK_E_MAIL, form.getFeedbackEMail());
    setResponsePage(new MessagePage(message, InitDatabaseDao.DEFAULT_ADMIN_USER));
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

  @Override
  protected String getTitle()
  {
    return getString("administration.setup.title");
  }

  private void checkAccess()
  {
    if (initDatabaseDao.isEmpty() == false) {
      throw new UserException("Couldn't call set-up page, because the data-base isn't empty anymore!");
    }
  }
}
