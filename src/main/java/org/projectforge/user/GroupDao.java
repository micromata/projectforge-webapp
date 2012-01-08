/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2012 Kai Reinhard (k.reinhard@micromata.com)
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
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
    this.supportAfterUpdate = true;
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
   * Does a group with the given name already exists? Works also for existing users (if group name was modified).
   * @param username
   * @return
   */
  @SuppressWarnings("unchecked")
  public boolean doesGroupnameAlreadyExist(final GroupDO group)
  {
    Validate.notNull(group);
    List<GroupDO> list = null;
    if (group.getId() == null) {
      // New group
      list = getHibernateTemplate().find("from GroupDO g where g.name = ?", group.getName());
    } else {
      // group already exists. Check maybe changed name:
      list = getHibernateTemplate().find("from GroupDO g where g.name = ? and pk <> ?", new Object[] { group.getName(), group.getId()});
    }
    if (CollectionUtils.isNotEmpty(list) == true) {
      return true;
    }
    return false;
  }

  /**
   * Please note: Any existing assigned user in group object is ignored!
   * @param group
   * @param assignedUserIds Full list of all users which have to assigned to this group.
   * @return
   */
  public void setAssignedUsers(GroupDO group, Set<Integer> assignedUserIds) throws AccessException
  {
    final Set<PFUserDO> assignedUsers = group.getAssignedUsers();
    if (assignedUsers != null) {
      final Iterator<PFUserDO> it = assignedUsers.iterator();
      while (it.hasNext() == true) {
        final PFUserDO user = it.next();
        if (assignedUserIds.contains(user.getId()) == false) {
          it.remove();
        }
      }
    }
    for (Integer id : assignedUserIds) {
      final PFUserDO user = userDao.internalGetById(id);
      if (user == null) {
        throw new RuntimeException("User '" + id + "' not found. Could not add this unknown user to new group: " + group.getName());
      }
      if (assignedUsers == null || assignedUsers.contains(user) == false) {
        group.addUser(user);
      }
    }
  }

  /**
   * Creates for every user an history entry if the user is part of this new group.
   * @param group
   * @see org.projectforge.core.BaseDao#afterSave(org.projectforge.core.ExtendedBaseDO)
   */
  @Override
  public void afterSave(final GroupDO group)
  {
    final Collection<GroupDO> groupList = new ArrayList<GroupDO>();
    groupList.add(group);
    if (group.getAssignedUsers() != null) {
      // Create history entry of PFUserDO for all assigned users:
      for (final PFUserDO user : group.getAssignedUsers()) {
        createHistoryEntry(user, null, groupList);
      }
    }
  }

  /**
   * Creates for every user an history if the user is assigned or unassigned from this updated group.
   * @param group
   * @param dbGroup
   * @see org.projectforge.core.BaseDao#afterUpdate(GroupDO, GroupDO)
   */
  @Override
  protected void afterUpdate(final GroupDO group, final GroupDO dbGroup)
  {
    final Set<PFUserDO> origAssignedUsers = dbGroup.getAssignedUsers();
    final Set<PFUserDO> assignedUsers = group.getAssignedUsers();
    final Collection<PFUserDO> assignedList = new ArrayList<PFUserDO>(); // List of new assigned users.
    final Collection<PFUserDO> unassignedList = new ArrayList<PFUserDO>(); // List of unassigned users.
    for (final PFUserDO user : group.getAssignedUsers()) {
      if (origAssignedUsers.contains(user) == false) {
        assignedList.add(user);
      }
    }
    for (final PFUserDO user : dbGroup.getAssignedUsers()) {
      if (assignedUsers.contains(user) == false) {
        unassignedList.add(user);
      }
    }
    final Collection<GroupDO> groupList = new ArrayList<GroupDO>();
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
        GroupDO group = (GroupDO) getHibernateTemplate().get(clazz, groupId, LockMode.PESSIMISTIC_WRITE);
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
        GroupDO group = (GroupDO) getHibernateTemplate().get(clazz, groupId, LockMode.PESSIMISTIC_WRITE);
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
  public boolean hasSelectAccess(final PFUserDO user, final boolean throwException)
  {
    return true;
  }

  /**
   * @return false, if no admin user and the context user is not member of the group. Also deleted groups are only visible for admin users.
   * @see org.projectforge.core.BaseDao#hasSelectAccess(org.projectforge.core.BaseDO, boolean)
   */
  @Override
  public boolean hasSelectAccess(final PFUserDO user, final GroupDO obj, final boolean throwException)
  {
    Validate.notNull(obj);
    boolean result = accessChecker.isUserMemberOfAdminGroup(user)
        || accessChecker.isUserMemberOfGroup(user, ProjectForgeGroup.FINANCE_GROUP, ProjectForgeGroup.CONTROLLING_GROUP);
    if (result == false && obj.isDeleted() == false) {
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
  public boolean hasAccess(final PFUserDO user, final GroupDO obj, final GroupDO oldObj, final OperationType operationType,
      final boolean throwException)
  {
    return accessChecker.isUserMemberOfAdminGroup(user, throwException);
  }

  @Override
  public boolean hasHistoryAccess(final PFUserDO user, final boolean throwException)
  {
    return accessChecker.isUserMemberOfAdminGroup(user, throwException);
  }

  @Override
  public GroupDO newInstance()
  {
    return new GroupDO();
  }
}
