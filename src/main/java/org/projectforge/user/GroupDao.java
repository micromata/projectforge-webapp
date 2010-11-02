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

package org.projectforge.user;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.hibernate.FetchMode;
import org.hibernate.LockMode;
import org.hibernate.criterion.Order;
import org.projectforge.access.AccessException;
import org.projectforge.access.AccessType;
import org.projectforge.access.OperationType;
import org.projectforge.core.BaseDao;
import org.projectforge.core.BaseSearchFilter;
import org.projectforge.core.QueryFilter;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import de.micromata.user.ContextHolder;

/**
 * 
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class GroupDao extends BaseDao<GroupDO>
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(GroupDao.class);

  private static final String[] ADDITIONAL_SEARCH_FIELDS = new String[] { "assignedUsers.username", "assignedUsers.firstname",
      "assignedUsers.lastname"};

  private UserDao userDao;

  public void setUserDao(UserDao userDao)
  {
    this.userDao = userDao;
  }

  public GroupDao()
  {
    super(GroupDO.class);
  }

  public QueryFilter getDefaultFilter()
  {
    QueryFilter queryFilter = new QueryFilter();
    return queryFilter;
  }

  @Override
  public List<GroupDO> getList(BaseSearchFilter filter)
  {
    QueryFilter queryFilter = new QueryFilter(filter);
    queryFilter.addOrder(Order.asc("name"));
    queryFilter.setFetchMode("assignedUsers", FetchMode.JOIN);
    return getList(queryFilter);
  }

  /**
   * Please note: Any existing assigned user in group object are ignored!
   * @param group
   * @param assignedUserIds Full list of all users which have to assigned to this group.
   * @return
   */
  @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW, isolation = Isolation.REPEATABLE_READ)
  public Serializable save(GroupDO group, Set<Integer> assignedUserIds) throws AccessException
  {
    Set<PFUserDO> assignedUsers = new HashSet<PFUserDO>();
    for (Integer id : assignedUserIds) {
      PFUserDO user = userDao.internalGetById(id);
      if (user == null) {
        throw new RuntimeException("User '" + id + "' not found. Could not add this unknown user to new group: " + group.getName());
      }
      assignedUsers.add(user);
    }
    group.setAssignedUsers(assignedUsers);
    return super.save(group);
  }

  @Override
  @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW, isolation = Isolation.REPEATABLE_READ)
  public Serializable internalSave(GroupDO obj)
  {
    Serializable id = super.internalSave(obj);
    if (id == null) {
      log.error("Group not inserted (why?): " + obj);
      return null;
    }
    GroupDO group = internalGetById(id);
    Collection<GroupDO> groupList = new ArrayList<GroupDO>();
    groupList.add(group);
    if (group.getAssignedUsers() != null) {
      // Create history entry of PFUserDO for all assigned users:
      for (PFUserDO user : group.getAssignedUsers()) {
        createHistoryEntry(user, null, groupList);
      }
    }
    return id;
  }

  /**
   * Please note: Any existing assigned user in group object are ignored.
   * @param group
   * @param assignedUserIds Full list of all users which have to assigned to this group.
   * @throws AccessException
   */
  @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW, isolation = Isolation.REPEATABLE_READ)
  public void update(GroupDO group, Set<Integer> assignedUserIds) throws AccessException
  {
    GroupDO orig = internalGetById(group.getId());
    Validate.notNull(orig);
    Set<PFUserDO> origAssignedUsers = orig.getAssignedUsers();

    Set<PFUserDO> assignedUsers = new HashSet<PFUserDO>();
    Collection<PFUserDO> assignedList = new ArrayList<PFUserDO>(); // List of new assigned users.
    Collection<PFUserDO> unassignedList = new ArrayList<PFUserDO>(); // List of unassigned users.
    for (Integer id : assignedUserIds) {
      PFUserDO user = userDao.internalGetById(id);
      Validate.notNull(user);
      assignedUsers.add(user);
      if (origAssignedUsers.contains(user) == false) {
        assignedList.add(user);
      }
    }
    for (PFUserDO user : origAssignedUsers) {
      if (assignedUserIds.contains(user.getId()) == false) {
        unassignedList.add(user);
      }
    }
    group.setAssignedUsers(assignedUsers);
    super.update(group);
    Collection<GroupDO> groupList = new ArrayList<GroupDO>();
    groupList.add(group);
    // Create history entry of PFUserDO for all new assigned users:
    for (PFUserDO user : assignedList) {
      createHistoryEntry(user, null, groupList);
    }
    // Create history entry of PFUserDO for all unassigned users:
    for (PFUserDO user : unassignedList) {
      createHistoryEntry(user, groupList, null);
    }
  }

  /**
   * Assigns groups to and unassigns groups from given user.
   * @param user
   * @param groupIdsToAssign Groups to assign (nullable).
   * @param groupIdsToUnassign Groups to unassign (nullable).
   * @throws AccessException
   */
  @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW, isolation = Isolation.REPEATABLE_READ)
  public void assignGroups(final PFUserDO user, Set<Integer> groupIdsToAssign, Set<Integer> groupIdsToUnassign) throws AccessException
  {
    getHibernateTemplate().refresh(user, LockMode.READ);
    final List<GroupDO> assignedGroups = new ArrayList<GroupDO>();
    if (groupIdsToAssign != null) {
      for (final Integer groupId : groupIdsToAssign) {
        GroupDO group = (GroupDO) getHibernateTemplate().get(clazz, groupId, LockMode.UPGRADE);
        Set<PFUserDO> assignedUsers = group.getAssignedUsers();
        if (assignedUsers == null) {
          assignedUsers = new HashSet<PFUserDO>();
          group.setAssignedUsers(assignedUsers);
        }
        if (assignedUsers.contains(user) == false) {
          log.info("Assigning user '" + user.getUsername() + "' to group '" + group.getName() + "'.");
          assignedUsers.add(user);
          assignedGroups.add(group);
          group.setLastUpdate(); // Needed, otherwise GroupDO is not detected for hibernate history!
        } else {
          log.info("User '" + user.getUsername() + "' already assigned to group '" + group.getName() + "'.");
        }
      }
    }
    final List<GroupDO> unassignedGroups = new ArrayList<GroupDO>();
    if (groupIdsToUnassign != null) {
      for (final Integer groupId : groupIdsToUnassign) {
        GroupDO group = (GroupDO) getHibernateTemplate().get(clazz, groupId, LockMode.UPGRADE);
        Set<PFUserDO> assignedUsers = group.getAssignedUsers();
        if (assignedUsers != null && assignedUsers.contains(user) == true) {
          log.info("Unassigning user '" + user.getUsername() + "' from group '" + group.getName() + "'.");
          assignedUsers.remove(user);
          unassignedGroups.add(group);
          group.setLastUpdate(); // Needed, otherwise GroupDO is not detected for hibernate history!
        } else {
          log.info("User '" + user.getUsername() + "' is not assigned to group '" + group.getName() + "' (can't unassign).");
        }
      }
    }
    getSession().flush();
    createHistoryEntry(user, unassignedGroups, assignedGroups);
    userGroupCache.setExpired();
  }

  private void createHistoryEntry(PFUserDO user, Collection<GroupDO> unassignedList, Collection<GroupDO> assignedList)
  {
    if (unassignedList != null && unassignedList.size() == 0) {
      unassignedList = null;
    }
    if (assignedList != null && assignedList.size() == 0) {
      assignedList = null;
    }
    if (unassignedList == null && assignedList == null) {
      return;
    }
    createHistoryEntry(user, user.getId(), "assignedGroups", Collection.class, unassignedList, assignedList);
  }

  /**
   * Internal load of all tasks without checking any access.
   * @return
   */
  @SuppressWarnings("unchecked")
  @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
  List<GroupDO> loadAll()
  {
    List<GroupDO> list = getHibernateTemplate().find("from GroupDO t join fetch t.assignedUsers");
    return list;
  }

  @Override
  protected void afterSaveOrModify(GroupDO group)
  {
    super.afterSaveOrModify(group);
    userGroupCache.setExpired();
  }

  @Override
  protected String[] getAdditionalSearchFields()
  {
    return ADDITIONAL_SEARCH_FIELDS;
  }

  /**
   * return Always true, no generic select access needed for group objects.
   * @see org.projectforge.core.BaseDao#hasSelectAccess()
   */
  @Override
  public boolean hasSelectAccess(boolean throwException)
  {
    return true;
  }

  /**
   * @return false, if no admin user and the context user is not member of the group. Also deleted groups are only visible for admin users.
   * @see org.projectforge.core.BaseDao#hasSelectAccess(org.projectforge.core.BaseDO, boolean)
   */
  @Override
  public boolean hasSelectAccess(GroupDO obj, boolean throwException)
  {
    Validate.notNull(obj);
    boolean result = accessChecker.isUserMemberOfAdminGroup();
    if (result == false && obj.isDeleted() == false) {
      PFUserDO user = (PFUserDO) ContextHolder.getUserInfo();
      Validate.notNull(user);
      result = userGroupCache.isUserMemberOfGroup(user.getId(), obj.getId());
    }
    if (throwException == true && result == false) {
      throw new AccessException(AccessType.GROUP, OperationType.SELECT);
    }
    return result;
  }

  /**
   * @see org.projectforge.core.BaseDao#hasAccess(Object, OperationType)
   */
  @Override
  public boolean hasAccess(GroupDO obj, GroupDO oldObj, OperationType operationType, boolean throwException)
  {
    return accessChecker.isUserMemberOfAdminGroup(throwException);
  }

  @Override
  public boolean hasHistoryAccess(boolean throwException)
  {
    return accessChecker.isUserMemberOfAdminGroup(throwException);
  }

  @Override
  public GroupDO newInstance()
  {
    return new GroupDO();
  }
}
