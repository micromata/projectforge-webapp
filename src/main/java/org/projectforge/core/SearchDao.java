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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.PredicateUtils;
import org.apache.commons.lang.ClassUtils;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Order;
import org.projectforge.access.AccessDao;
import org.projectforge.access.GroupTaskAccessDO;
import org.projectforge.address.AddressDO;
import org.projectforge.address.AddressDao;
import org.projectforge.book.BookDO;
import org.projectforge.book.BookDao;
import org.projectforge.common.NumberHelper;
import org.projectforge.fibu.KontoDO;
import org.projectforge.fibu.KontoDao;
import org.projectforge.fibu.KundeDO;
import org.projectforge.fibu.KundeDao;
import org.projectforge.fibu.ProjektDO;
import org.projectforge.fibu.ProjektDao;
import org.projectforge.fibu.RechnungDO;
import org.projectforge.fibu.RechnungDao;
import org.projectforge.fibu.kost.BuchungssatzDO;
import org.projectforge.fibu.kost.BuchungssatzDao;
import org.projectforge.fibu.kost.Kost1DO;
import org.projectforge.fibu.kost.Kost1Dao;
import org.projectforge.fibu.kost.Kost2ArtDO;
import org.projectforge.fibu.kost.Kost2ArtDao;
import org.projectforge.fibu.kost.Kost2DO;
import org.projectforge.fibu.kost.Kost2Dao;
import org.projectforge.task.TaskDO;
import org.projectforge.task.TaskDao;
import org.projectforge.task.TaskNode;
import org.projectforge.task.TaskTree;
import org.projectforge.timesheet.TimesheetDO;
import org.projectforge.timesheet.TimesheetDao;
import org.projectforge.user.GroupDO;
import org.projectforge.user.GroupDao;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserDao;
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

  private TimesheetDao timesheetDao;

  private TaskDao taskDao;

  private BookDao bookDao;

  private AddressDao addressDao;

  private UserDao userDao;

  private GroupDao groupDao;

  private AccessDao accessDao;

  private RechnungDao rechnungDao;

  private BuchungssatzDao buchungssatzDao;

  private Kost1Dao kost1Dao;

  private Kost2Dao kost2Dao;

  private Kost2ArtDao kost2ArtDao;

  private KontoDao kontoDao;

  private KundeDao kundeDao;

  private ProjektDao projektDao;

  public List<List<SearchResultData>> getResultLists(SearchFilter filter)
  {
    List<List<SearchResultData>> result = new ArrayList<List<SearchResultData>>();
    addList(result, getTimesheetList(filter), filter, SearchArea.TIMESHEET);
    addList(result, getAddressList(filter), filter, SearchArea.ADDRESS);
    addList(result, getTaskList(filter), filter, SearchArea.TASK);
    addList(result, getBookList(filter), filter, SearchArea.BOOK);
    addList(result, getRechnungList(filter), filter, SearchArea.RECHNUNG);
    addList(result, getUserList(filter), filter, SearchArea.USER);
    addList(result, getGroupList(filter), filter, SearchArea.GROUP);
    addList(result, getAccessList(filter), filter, SearchArea.ACCESS);
    addList(result, getKost2ArtList(filter), filter, SearchArea.KOST2ART);
    addList(result, getKundeList(filter), filter, SearchArea.KUNDE);
    addList(result, getProjektList(filter), filter, SearchArea.PROJEKT);
    addList(result, getKontoList(filter), filter, SearchArea.KONTO);
    addList(result, getKost1List(filter), filter, SearchArea.KOST1);
    addList(result, getKost2List(filter), filter, SearchArea.KOST2);
    addList(result, getBuchungssatzList(filter), filter, SearchArea.BUCHUNGSSATZ);
    return result;
  }

  @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
  public boolean hasUserAccess(SearchArea area)
  {
    if (area == SearchArea.ALL) {
      return true;
    } else if (area == SearchArea.ACCESS) {
      return accessDao.hasHistoryAccess(false);
    } else if (area == SearchArea.ADDRESS) {
      return addressDao.hasHistoryAccess(false);
    } else if (area == SearchArea.BOOK) {
      return bookDao.hasHistoryAccess(false);
    } else if (area == SearchArea.GROUP) {
      return groupDao.hasHistoryAccess(false);
    } else if (area == SearchArea.BUCHUNGSSATZ) {
      return buchungssatzDao.hasHistoryAccess(false);
    } else if (area == SearchArea.KOST1) {
      return kost1Dao.hasHistoryAccess(false);
    } else if (area == SearchArea.KOST2) {
      return kost2Dao.hasHistoryAccess(false);
    } else if (area == SearchArea.KOST2ART) {
      return kost2ArtDao.hasHistoryAccess(false);
    } else if (area == SearchArea.KONTO) {
      return kontoDao.hasHistoryAccess(false);
    } else if (area == SearchArea.KUNDE) {
      return kundeDao.hasHistoryAccess(false);
    } else if (area == SearchArea.PROJEKT) {
      return projektDao.hasHistoryAccess(false);
    } else if (area == SearchArea.RECHNUNG) {
      return rechnungDao.hasHistoryAccess(false);
    } else if (area == SearchArea.TASK) {
      return taskDao.hasHistoryAccess(false);
    } else if (area == SearchArea.TIMESHEET) {
      return timesheetDao.hasHistoryAccess(false);
    } else if (area == SearchArea.USER) {
      return userDao.hasHistoryAccess(false);
    }
    throw new UnsupportedOperationException("SearchArea not supported: " + area);
  }

  private void addList(List<List<SearchResultData>> lists, List<SearchResultData> list, SearchFilter filter, SearchArea area)
  {
    if ((filter.getArea() == SearchArea.ALL || filter.getArea() == area) && list != null && list.size() > 0) {
      lists.add(list);
    }
  }

  public List<SearchResultData> getAddressList(SearchFilter filter)
  {
    if (filter.getArea() != SearchArea.ALL && filter.getArea() != SearchArea.ADDRESS) {
      return null;
    }
    Session session = getSession();
    List<SearchResultData> list = getHistoryEntries(session, filter, AddressDO.class, addressDao);
    return list;
  }

  public List<SearchResultData> getTimesheetList(SearchFilter filter)
  {
    if (filter.getArea() != SearchArea.ALL && filter.getArea() != SearchArea.TIMESHEET) {
      return null;
    }
    Session session = getSession();
    List<SearchResultData> list = getHistoryEntries(session, filter, TimesheetDO.class, timesheetDao);
    return list;
  }

  public List<SearchResultData> getUserList(SearchFilter filter)
  {
    if (filter.getArea() != SearchArea.ALL && filter.getArea() != SearchArea.USER) {
      return null;
    }
    Session session = getSession();
    List<SearchResultData> list = getHistoryEntries(session, filter, PFUserDO.class, userDao);
    return list;
  }

  public List<SearchResultData> getGroupList(SearchFilter filter)
  {
    if (filter.getArea() != SearchArea.ALL && filter.getArea() != SearchArea.GROUP) {
      return null;
    }
    Session session = getSession();
    List<SearchResultData> list = getHistoryEntries(session, filter, GroupDO.class, groupDao);
    return list;
  }

  public List<SearchResultData> getAccessList(SearchFilter filter)
  {
    if (filter.getArea() != SearchArea.ALL && filter.getArea() != SearchArea.ACCESS) {
      return null;
    }
    Session session = getSession();
    List<SearchResultData> list = getHistoryEntries(session, filter, GroupTaskAccessDO.class, accessDao);
    return list;
  }

  public List<SearchResultData> getTaskList(SearchFilter filter)
  {
    if (filter.getArea() != SearchArea.ALL && filter.getArea() != SearchArea.TASK) {
      return null;
    }
    Session session = getSession();
    List<SearchResultData> list = getHistoryEntries(session, filter, TaskDO.class, taskDao);
    return list;
  }

  public List<SearchResultData> getBookList(SearchFilter filter)
  {
    if (filter.getArea() != SearchArea.ALL && filter.getArea() != SearchArea.BOOK) {
      return null;
    }
    Session session = getSession();
    List<SearchResultData> list = getHistoryEntries(session, filter, BookDO.class, bookDao);
    return list;
  }

  public List<SearchResultData> getRechnungList(SearchFilter filter)
  {
    if (filter.getArea() != SearchArea.ALL && filter.getArea() != SearchArea.RECHNUNG) {
      return null;
    }
    Session session = getSession();
    List<SearchResultData> list = getHistoryEntries(session, filter, RechnungDO.class, rechnungDao);
    return list;
  }

  public List<SearchResultData> getBuchungssatzList(SearchFilter filter)
  {
    if (filter.getArea() != SearchArea.ALL && filter.getArea() != SearchArea.BUCHUNGSSATZ) {
      return null;
    }
    Session session = getSession();
    List<SearchResultData> list = getHistoryEntries(session, filter, BuchungssatzDO.class, buchungssatzDao);
    return list;
  }

  public List<SearchResultData> getKost1List(SearchFilter filter)
  {
    if (filter.getArea() != SearchArea.ALL && filter.getArea() != SearchArea.KOST1) {
      return null;
    }
    Session session = getSession();
    List<SearchResultData> list = getHistoryEntries(session, filter, Kost1DO.class, kost1Dao);
    return list;
  }

  public List<SearchResultData> getKost2ArtList(SearchFilter filter)
  {
    if (filter.getArea() != SearchArea.ALL && filter.getArea() != SearchArea.KOST2ART) {
      return null;
    }
    Session session = getSession();
    List<SearchResultData> list = getHistoryEntries(session, filter, Kost2ArtDO.class, kost2ArtDao);
    return list;
  }

  public List<SearchResultData> getKost2List(SearchFilter filter)
  {
    if (filter.getArea() != SearchArea.ALL && filter.getArea() != SearchArea.KOST2) {
      return null;
    }
    Session session = getSession();
    List<SearchResultData> list = getHistoryEntries(session, filter, Kost2DO.class, kost2Dao);
    return list;
  }

  public List<SearchResultData> getKontoList(SearchFilter filter)
  {
    if (filter.getArea() != SearchArea.ALL && filter.getArea() != SearchArea.KONTO) {
      return null;
    }
    Session session = getSession();
    List<SearchResultData> list = getHistoryEntries(session, filter, KontoDO.class, kontoDao);
    return list;
  }

  public List<SearchResultData> getKundeList(SearchFilter filter)
  {
    if (filter.getArea() != SearchArea.ALL && filter.getArea() != SearchArea.KUNDE) {
      return null;
    }
    Session session = getSession();
    List<SearchResultData> list = getHistoryEntries(session, filter, KundeDO.class, kundeDao);
    return list;
  }

  public List<SearchResultData> getProjektList(SearchFilter filter)
  {
    if (filter.getArea() != SearchArea.ALL && filter.getArea() != SearchArea.PROJEKT) {
      return null;
    }
    Session session = getSession();
    List<SearchResultData> list = getHistoryEntries(session, filter, ProjektDO.class, projektDao);
    return list;
  }

  @SuppressWarnings("unchecked")
  private List<SearchResultData> getHistoryEntries(Session session, SearchFilter filter, Class clazz, BaseDao baseDao)
  {
    if (filter == null || filter.getMaxRows() == null) {
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
    crit.add(Expression.eq("className", className));
    if (filter.getStartTime() != null && filter.getStopTime() != null) {
      crit.add(Expression.between("timestamp", filter.getStartTime(), filter.getStopTime()));
    } else if (filter.getStartTime() != null) {
      crit.add(Expression.ge("timestamp", filter.getStartTime()));
    } else if (filter.getStopTime() != null) {
      crit.add(Expression.le("timestamp", filter.getStopTime()));
    }
    if (filter.getModifiedByUserId() != null) {
      crit = crit.add(Expression.eq("userName", filter.getModifiedByUserId().toString()));
    }
    crit = crit.setCacheable(true);
    crit.setCacheRegion("historyItemCache");
    crit.addOrder(Order.desc("timestamp"));
    List<SearchResultData> result = new ArrayList<SearchResultData>();
    List<HistoryEntry> historyList = crit.list();
    if (historyList.size() == 0) {
      return result;
    }
    HistoryEntry[] history = (HistoryEntry[]) CollectionUtils.select(historyList, PredicateUtils.uniquePredicate()).toArray(
        new HistoryEntry[0]);
    // Now get all ids, referred in the history entries (and store all history entries in a map for faster access):
    Set<Integer> ids = new HashSet<Integer>();
    for (HistoryEntry entry : history) {
      ids.add(entry.getEntityId());
    }
    crit = session.createCriteria(clazz);
    crit.add(Expression.in("id", ids));
    if (filter.getTaskId() != null && (clazz.equals(TimesheetDO.class) == true)) {
      TaskNode node = taskTree.getTaskNodeById(filter.getTaskId());
      List<Integer> taskIds = node.getDescendantIds();
      taskIds.add(node.getId());
      crit.add(Expression.in("task.id", taskIds));
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
    for (HistoryEntry entry : history) {
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
      if (++counter >= filter.getMaxRows()) {
        result.add(new SearchResultData()); // Add null entry for gui for displaying 'more entries'.
        break;
      }
    }
    return result;
  }

  public void setTimesheetDao(TimesheetDao timesheetDao)
  {
    this.timesheetDao = timesheetDao;
  }

  public void setAddressDao(AddressDao addressDao)
  {
    this.addressDao = addressDao;
  }

  public void setUserGroupCache(UserGroupCache userGroupCache)
  {
    this.userGroupCache = userGroupCache;
  }

  public void setTaskTree(TaskTree taskTree)
  {
    this.taskTree = taskTree;
  }

  public void setTaskDao(TaskDao taskDao)
  {
    this.taskDao = taskDao;
  }

  public void setBookDao(BookDao bookDao)
  {
    this.bookDao = bookDao;
  }

  public void setUserDao(UserDao userDao)
  {
    this.userDao = userDao;
  }

  public void setGroupDao(GroupDao groupDao)
  {
    this.groupDao = groupDao;
  }

  public void setAccessDao(AccessDao accessDao)
  {
    this.accessDao = accessDao;
  }

  public void setRechnungDao(RechnungDao rechnungDao)
  {
    this.rechnungDao = rechnungDao;
  }

  public void setKost2ArtDao(Kost2ArtDao kost2ArtDao)
  {
    this.kost2ArtDao = kost2ArtDao;
  }

  public void setKundeDao(KundeDao kundeDao)
  {
    this.kundeDao = kundeDao;
  }

  public void setKontoDao(KontoDao kontoDao)
  {
    this.kontoDao = kontoDao;
  }

  public void setBuchungssatzDao(BuchungssatzDao buchungssatzDao)
  {
    this.buchungssatzDao = buchungssatzDao;
  }

  public void setKost1Dao(Kost1Dao kost1Dao)
  {
    this.kost1Dao = kost1Dao;
  }

  public void setKost2Dao(Kost2Dao kost2Dao)
  {
    this.kost2Dao = kost2Dao;
  }

  public void setProjektDao(ProjektDao projektDao)
  {
    this.projektDao = projektDao;
  }
}
