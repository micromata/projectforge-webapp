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

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.stripes.action.StrictBinding;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.validation.Validate;
import net.sourceforge.stripes.validation.ValidateNestedProperties;

import org.apache.log4j.Logger;
import org.projectforge.core.BaseSearchFilter;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserDao;
import org.projectforge.web.core.BaseAction;
import org.projectforge.web.core.BaseListAction;
import org.projectforge.web.core.BaseListActionBean;


@StrictBinding
@UrlBinding("/secure/user/UserList.action")
@BaseAction(jspUrl = "/WEB-INF/jsp/user/userList.jsp")
@BaseListAction(flowSope = true)
public class UserListAction extends BaseListActionBean<BaseSearchFilter, UserDao, Object[]>
{
  private static final Logger log = Logger.getLogger(UserListAction.class);

  public void setUserDao(UserDao userDao)
  {
    this.baseDao = userDao;
  }

  /**
   * Needed only for StrictBinding. If method has same signature as super.getActionFilter then stripes ignores these validate settings
   * (bug?).
   */
  @ValidateNestedProperties( { @Validate(field = "searchString"), @Validate(field = "deleted")})
  public BaseSearchFilter getFilter()
  {
    return super.getActionFilter();
  }

  /**
   * return always true.
   * @see org.projectforge.web.core.BaseListActionBean#isShowResultInstantly()
   */
  @Override
  protected boolean isShowResultInstantly()
  {
    return true;
  }

  /**
   * Quick select support.
   * @see org.projectforge.web.core.BaseListActionBean#getSingleEntryValue()
   */
  @Override
  protected String getSingleEntryValue()
  {
    if (getList().size() == 1) {
      return String.valueOf(getList().get(0)[0]); // return the pk.
    }
    return null;
  }

  @Override
  protected List<Object[]> buildList()
  {
    List<PFUserDO> list = baseDao.getList(getActionFilter());
    return convertToStringList(list);
  }

  private List<Object[]> convertToStringList(List<PFUserDO> list)
  {
    final List<Object[]> result = new ArrayList<Object[]>();
    if (list == null) {
      return result;
    }
    for (final PFUserDO user : list) {
      if (user != null) { // Paranoia
        result.add(new Object[] { user.getId(), user.getUsername(), user.getLastname(), user.getFirstname(),
            user.getPersonalPhoneIdentifiers(), user.getDescription(), baseDao.getGroupnames(user), user.isDeleted()});
      }
    }
    return result;
  }

  @Override
  protected BaseSearchFilter createFilterInstance()
  {
    return new BaseSearchFilter();
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }
}
