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

import net.sourceforge.stripes.action.UrlBinding;

import org.apache.log4j.Logger;
import org.projectforge.core.BaseSearchFilter;
import org.projectforge.user.GroupDO;
import org.projectforge.user.GroupDao;
import org.projectforge.web.core.BaseAction;
import org.projectforge.web.core.BaseListActionBean;


/**
 */
@UrlBinding("/secure/user/GroupList.action")
@BaseAction(jspUrl = "/WEB-INF/jsp/user/groupList.jsp")
public class GroupListAction extends BaseListActionBean<BaseSearchFilter, GroupDao, GroupDO>
{
  private static final Logger log = Logger.getLogger(GroupListAction.class);

  public void setGroupDao(GroupDao groupDao)
  {
    this.baseDao = groupDao;
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
