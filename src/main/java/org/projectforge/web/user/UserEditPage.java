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

package org.projectforge.web.user;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.core.Configuration;
import org.projectforge.ldap.PFUserDOConverter;
import org.projectforge.user.GroupDao;
import org.projectforge.user.Login;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserDao;
import org.projectforge.user.UserRightDao;
import org.projectforge.user.UserRightVO;
import org.projectforge.web.wicket.AbstractEditPage;
import org.projectforge.web.wicket.AbstractSecuredBasePage;
import org.projectforge.web.wicket.EditPage;

@EditPage(defaultReturnPage = UserListPage.class)
public class UserEditPage extends AbstractEditPage<PFUserDO, UserEditForm, UserDao>
{
  private static final long serialVersionUID = 4636922408954211544L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(UserEditPage.class);

  @SpringBean(name = "groupDao")
  private GroupDao groupDao;

  @SpringBean(name = "userDao")
  private UserDao userDao;

  @SpringBean(name = "userRightDao")
  private UserRightDao userRightDao;

  protected boolean tutorialMode;

  protected List<Integer> tutorialGroupsToAdd;

  /**
   * Used by the TutorialPage.
   * @param tutorialUser
   * @param tutorialGroupsToAdd
   */
  public UserEditPage(final PFUserDO tutorialUser, final List<Integer> tutorialGroupsToAdd)
  {
    super(new PageParameters(), "user");
    this.tutorialGroupsToAdd = tutorialGroupsToAdd;
    this.tutorialMode = true;
    super.init(tutorialUser);
    myInit();
  }

  public UserEditPage(final PageParameters parameters)
  {
    super(parameters, "user");
    super.init();
    myInit();
  }

  private void myInit()
  {
    if (isNew() == true) {
      getData().setTimeZone(Configuration.getInstance().getDefaultTimeZone());
    }
  }

  @Override
  public AbstractSecuredBasePage onSaveOrUpdate()
  {
    if (StringUtils.isNotEmpty(form.getEncryptedPassword()) == true) {
      getData().setPassword(form.getEncryptedPassword());
    }
    getData().setPersonalPhoneIdentifiers(userDao.getNormalizedPersonalPhoneIdentifiers(getData()));
    if (form.ldapUserValues.isValuesEmpty() == false) {
      final String xml = PFUserDOConverter.getLdapValuesAsXml(form.ldapUserValues);
      getData().setLdapValues(xml);
    }
    return super.onSaveOrUpdate();
  }

  @Override
  public AbstractSecuredBasePage afterSaveOrUpdate()
  {
    groupDao.assignGroups(getData(), form.assignListHelper.getItemsToAssign(), form.assignListHelper.getItemsToUnassign());

    if (form.rightsData != null) {
      final List<UserRightVO> list = form.rightsData.getRights();
      userRightDao.updateUserRights(getData(), list);
    }
    if (StringUtils.isNotEmpty(form.getEncryptedPassword()) == true) {
      Login.getInstance().passwordChanged(getData(), form.password);
    }
    return super.afterSaveOrUpdate();
  }

  @Override
  protected UserDao getBaseDao()
  {
    return userDao;
  }

  @Override
  protected UserEditForm newEditForm(final AbstractEditPage< ? , ? , ? > parentPage, final PFUserDO data)
  {
    return new UserEditForm(this, data);
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }
}
