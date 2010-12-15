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

package org.projectforge.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ClassUtils;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.projectforge.common.NumberHelper;
import org.projectforge.task.TaskNode;
import org.projectforge.task.TaskTree;
import org.projectforge.timesheet.TimesheetDO;
import org.projectforge.user.UserGroupCache;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import de.micromata.hibernate.history.HistoryEntry;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
public class SearchDao extends HibernateDaoSupport
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SearchDao.class);

  private TaskTree taskTree;

  private UserGroupCache userGroupCache;

  @SuppressWarnings("unchecked")
  public List<SearchResultData> getHistoryEntries(final SearchFilter filter, final Class clazz, final BaseDao baseDao)
  {
    final Session session = getSession();
    if (filter == null) {
      log.info("Filter or rows in filter is null (may be Search as redirect after login): " + filter);
      return null;
    }
    log.debug("Searching in " + clazz);
    if (baseDao.hasSelectAccess(false) == false || baseDao.hasHistoryAccess(false) == false) {
      // User has in general no access to history entries of the given object type (clazz).
      return null;
    }
    // First get all history entries matching the filter and the given class.
    String className = ClassUtils.getShortClassName(clazz);
    Criteria crit = session.createCriteria(HistoryEntry.class);
    crit.add(Restrictions.eq("className", className));
    if (filter.getModifiedStartTime() != null && filter.getModifiedStopTime() != null) {
      crit.add(Restrictions.between("timestamp", filter.getModifiedStartTime(), filter.getModifiedStopTime()));
    } else if (filter.getModifiedStartTime() != null) {
      crit.add(Restrictions.ge("timestamp", filter.getModifiedStartTime()));
    } else if (filter.getModifiedStopTime() != null) {
      crit.add(Restrictions.le("timestamp", filter.getModifiedStopTime()));
    }
    if (filter.getModifiedByUserId() != null) {
      crit = crit.add(Restrictions.eq("userName", filter.getModifiedByUserId().toString()));
    }
    crit = crit.setCacheable(true);
    crit.setCacheRegion("historyItemCache");
    crit.setProjection(Projections.distinct(Projections.property("id")));
    crit.addOrder(Order.desc("id"));
    int maxRows = filter.getMaxRows() != null ? filter.getMaxRows() : 3;
    crit.setMaxResults(maxRows + 1); // Get one more entry to evaluate wether more entries does exist or not.
    final List<Integer> historyIds = crit.list();
    final List<SearchResultData> result = new ArrayList<SearchResultData>();
    crit = session.createCriteria(HistoryEntry.class);
    crit.add(Restrictions.in("id", historyIds));
    crit.addOrder(Order.desc("id"));
    final List<HistoryEntry> historyList = crit.list();
    if (historyList.size() == 0) {
      return result;
    }
    // Now get all ids, referred in the history entries (and store all history entries in a map for faster access):
    Set<Integer> ids = new HashSet<Integer>();
    for (HistoryEntry entry : historyList) {
      ids.add(entry.getEntityId());
    }
    crit = session.createCriteria(clazz);
    crit.add(Restrictions.in("id", ids));
    if (filter.getTask() != null && (clazz.equals(TimesheetDO.class) == true)) {
      TaskNode node = taskTree.getTaskNodeById(filter.getTask().getId());
      List<Integer> taskIds = node.getDescendantIds();
      taskIds.add(node.getId());
      crit.add(Restrictions.in("task.id", taskIds));
    }
    List<ExtendedBaseDO> objects = crit.list();
    // Put all found objects in a map for faster access:
    Map<Serializable, ExtendedBaseDO> map = new HashMap<Serializable, ExtendedBaseDO>();
    for (ExtendedBaseDO obj : objects) {
      if (baseDao.hasSelectAccess(obj, false) == false || baseDao.hasHistoryAccess(obj, false) == false) {
        if (log.isDebugEnabled() == true) {
          log.debug("Ignoring object (no acces): " + clazz + ", " + obj.getId());
        }
        continue;
      }
      if (map.containsKey(obj.getId()) == false) {
        map.put(obj.getId(), obj);
      }
    }
    int counter = 0;
    // Now put the stuff together:
    for (HistoryEntry entry : historyList) {
      ExtendedBaseDO<Integer> obj = map.get(entry.getEntityId());
      if (obj == null) {
        // No access (see above).
        continue;
      }
      SearchResultData data = new SearchResultData();
      Integer userId = NumberHelper.parseInteger(entry.getUserName());
      if (userId != null) {
        data.modifiedByUser = userGroupCache.getUser(userId);
      }
      data.dataObject = obj;
      data.historyEntry = entry;
      data.propertyChanges = baseDao.convert(entry, session);
      result.add(data);
      if (++counter > maxRows) {
        result.add(new SearchResultData()); // Add null entry for gui for displaying 'more entries'.
        break;
      }
    }
    return result;
  }

  public void setUserGroupCache(UserGroupCache userGroupCache)
  {
    this.userGroupCache = userGroupCache;
  }

  public void setTaskTree(TaskTree taskTree)
  {
    this.taskTree = taskTree;
  }
}
