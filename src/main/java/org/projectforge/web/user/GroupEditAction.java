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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.DontValidate;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.validation.Validate;
import net.sourceforge.stripes.validation.ValidateNestedProperties;

import org.apache.log4j.Logger;
import org.projectforge.common.KeyValueBean;
import org.projectforge.user.GroupDO;
import org.projectforge.user.GroupDao;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserDao;
import org.projectforge.web.common.TwoListHelper;
import org.projectforge.web.core.BaseAction;
import org.projectforge.web.core.BaseEditAction;
import org.projectforge.web.core.BaseEditActionBean;
import org.projectforge.web.core.FlowScope;


/**
 */
@UrlBinding("/secure/user/GroupEdit.action")
@BaseAction(jspUrl = "/WEB-INF/jsp/user/groupEdit.jsp")
@BaseEditAction(listAction = GroupListAction.class, flowSope = true)
public class GroupEditAction extends BaseEditActionBean<GroupDao, GroupDO>
{
  private static final Logger log = Logger.getLogger(GroupEditAction.class);

  private List<Integer> selectedItemsToAssign;

  private List<Integer> selectedItemsToUnassign;
  
  private GroupDao groupDao;

  public void setGroupDao(GroupDao groupDao)
  {
    this.baseDao = groupDao;
    this.groupDao = groupDao;
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }

  @Override
  protected GroupDO createDataInstance()
  {
    return new GroupDO();
  }

  private TwoListHelper<Integer, String> users;

  private UserDao userDao;

  public void setUserDao(UserDao userDao)
  {
    this.userDao = userDao;
  }

  @Override
  public Resolution create()
  {
    log.debug("save: " + getData());
    restoreFlowScope();
    Set<Integer>assignedUsers = getAssignedUserIds();
    groupDao.save(getData(), assignedUsers);
    return new ForwardResolution(GroupListAction.class);
  }

  @Override
  public Resolution update()
  {
    log.debug("update: " + getData());
    restoreFlowScope();
    Set<Integer>assignedUsers = getAssignedUserIds();
    groupDao.update(getData(), assignedUsers);
    return new ForwardResolution(GroupListAction.class);
  }

  @Override
  @DefaultHandler
  @DontValidate
  public Resolution execute()
  {
    restoreFlowScope();
    if ("assign".equals(getEventKey())) {
      this.users.assign(selectedItemsToAssign);
    } else if ("unassign".equals(getEventKey())) {
      this.users.unassign(selectedItemsToUnassign);
    } else {
      log.info("Oups, unknown action.");
    }
    return getInputPage();
  }

  @ValidateNestedProperties( { @Validate(field = "name", required = true, maxlength = 255),
      @Validate(field = "organization", maxlength = 255), @Validate(field = "description", maxlength = 1000)})
  public GroupDO getGroup()
  {
    return getData();
  }

  public void setGroup(GroupDO data)
  {
    setData(data);
  }

  public TwoListHelper<Integer, String> getUsers()
  {
    if (this.users == null) {
      List<Integer> assignedUsers = new ArrayList<Integer>();
      if (getData().getAssignedUsers() != null) {
        for (PFUserDO user : getData().getAssignedUsers()) {
          assignedUsers.add(user.getId());
        }
      }
      List<KeyValueBean<Integer, String>> fullList = new ArrayList<KeyValueBean<Integer, String>>();
      List<PFUserDO> result = (List<PFUserDO>) userDao.getList(userDao.getDefaultFilter());
      for (PFUserDO user : result) {
        fullList.add(new KeyValueBean<Integer, String>(user.getId(), user.getUserDisplayname()));
      }
      this.users = new TwoListHelper<Integer, String>(fullList, assignedUsers);
      this.users.sortLists();
    }
    return this.users;
  }
  
  public List<Integer> getSelectedItemsToAssign()
  {
    return selectedItemsToAssign;
  }
  
  public void setSelectedItemsToAssign(List<Integer> selectedItemsToAssign)
  {
    this.selectedItemsToAssign = selectedItemsToAssign;
  }
  
  public List<Integer> getSelectedItemsToUnassign()
  {
    return selectedItemsToUnassign;
  }
  
  public void setSelectedItemsToUnassign(List<Integer> selectedItemsToUnassign)
  {
    this.selectedItemsToUnassign = selectedItemsToUnassign;
  }

  @Override
  protected void storeFlowScope()
  {
    storeFlowScopeObject(getFlowKey("twoList"), getUsers());
  }

  @Override
  @SuppressWarnings("unchecked")
  protected FlowScope restoreFlowScope()
  {
    FlowScope scope = getRequiredFlowScope();
    if (scope != null) {
      log.debug("FlowScope restored: " + scope);
      this.users = (TwoListHelper<Integer, String>) getFlowScopeObject(getFlowKey("twoList"), false);
    }
    return scope;
  }

  private Set<Integer> getAssignedUserIds()
  {
    Set<Integer> assignedUsers = new HashSet<Integer>();
    for (KeyValueBean<Integer, String> entry : getUsers().getAssignedItems()) {
      assignedUsers.add(entry.getKey());
    }
    return assignedUsers;
  }
}
