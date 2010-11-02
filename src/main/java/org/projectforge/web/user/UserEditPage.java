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

package org.projectforge.web.user;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.wicket.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.core.Configuration;
import org.projectforge.user.GroupDao;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserDao;
import org.projectforge.user.UserRightDao;
import org.projectforge.user.UserRightVO;
import org.projectforge.web.wicket.AbstractBasePage;
import org.projectforge.web.wicket.AbstractEditPage;
import org.projectforge.web.wicket.EditPage;

@EditPage(defaultReturnPage = UserListPage.class)
public class UserEditPage extends AbstractEditPage<PFUserDO, UserEditForm, UserDao>
{
  private static final long serialVersionUID = 4636922408954211544L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(UserEditPage.class);

  @SpringBean(name = "userDao")
  private UserDao userDao;

  @SpringBean(name = "groupDao")
  private GroupDao groupDao;

  @SpringBean(name = "userRightDao")
  private UserRightDao userRightDao;

  public UserEditPage(final PageParameters parameters)
  {
    super(parameters, "user");
    super.init();
    if (isNew() == true) {
      getData().setTimeZone(Configuration.getInstance().getDefaultTimeZone());
    }
  }

  @Override
  protected AbstractBasePage onSaveOrUpdate()
  {
    if (StringUtils.isNotEmpty(form.getEncryptedPassword()) == true) {
      getData().setPassword(form.getEncryptedPassword());
    }
    return super.onSaveOrUpdate();
  }

  @Override
  protected AbstractBasePage afterSaveOrUpdate()
  {
    final Set<Integer> assignedGroupIds = new HashSet<Integer>();
    for (Integer groupId : form.groups.getValuesToAssign()) {
      assignedGroupIds.add(groupId);
    }
    final Set<Integer> unassignedGroupIds = new HashSet<Integer>();
    for (Integer groupId : form.groups.getValuesToUnassign()) {
      unassignedGroupIds.add(groupId);
    }
    groupDao.assignGroups(getData(), assignedGroupIds, unassignedGroupIds);

    final List<UserRightVO> list = form.rightsData.getRights();
    userRightDao.updateUserRights(getData(), list);

    return super.afterSaveOrUpdate();
  }

  @Override
  protected UserDao getBaseDao()
  {
    return userDao;
  }

  @Override
  protected UserEditForm newEditForm(AbstractEditPage< ? , ? , ? > parentPage, PFUserDO data)
  {
    return new UserEditForm(this, data);
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }
}
