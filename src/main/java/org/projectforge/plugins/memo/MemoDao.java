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

package org.projectforge.plugins.memo;

import org.projectforge.core.BaseDao;
import org.projectforge.plugins.todo.ToDoDO;
import org.projectforge.user.PFUserContext;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserDao;
import org.projectforge.user.UserRightId;

/**
 * This is the base data access object class. Most functionality such as access checking, select, insert, update, save, delete etc. is implemented by the super class.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class MemoDao extends BaseDao<MemoDO>
{
  public static final UserRightId USER_RIGHT_ID = new UserRightId("PLUGIN_MEMO", "plugin20", "plugins.memo.memo");;

  private UserDao userDao;

  public MemoDao()
  {
    super(MemoDO.class);
    userRightId = USER_RIGHT_ID;
  }
  
  @Override
  protected void onSaveOrModify(MemoDO obj)
  {
    super.onSaveOrModify(obj);
    obj.setOwner(PFUserContext.getUser()); // Set always the logged-in user as owner.
  }

  public void setAssignee(final ToDoDO todo, final Integer userId)
  {
    final PFUserDO user = userDao.getOrLoad(userId);
    todo.setAssignee(user);
  }

  public void setReporter(final ToDoDO todo, final Integer userId)
  {
    final PFUserDO user = userDao.getOrLoad(userId);
    todo.setReporter(user);
  }

  @Override
  public MemoDO newInstance()
  {
    return new MemoDO();
  }

  public void setUserDao(final UserDao userDao)
  {
    this.userDao = userDao;
  }
}
