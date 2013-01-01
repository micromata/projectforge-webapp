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

package org.projectforge.humanresources;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.hibernate.criterion.Order;
import org.projectforge.calendar.DayHolder;
import org.projectforge.common.DateHolder;
import org.projectforge.common.NumberHelper;
import org.projectforge.core.BaseSearchFilter;
import org.projectforge.core.IDao;
import org.projectforge.core.QueryFilter;
import org.projectforge.fibu.KundeDO;
import org.projectforge.fibu.ProjektDO;
import org.projectforge.task.TaskTree;
import org.projectforge.timesheet.TimesheetDO;
import org.projectforge.timesheet.TimesheetDao;
import org.projectforge.timesheet.TimesheetFilter;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserDao;
import org.projectforge.user.UserGroupCache;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


/**
 * 
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class HRDao extends HibernateDaoSupport implements IDao<HRViewData>
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(HRDao.class);

  private TimesheetDao timesheetDao;

  private HRPlanningDao hrPlanningDao;

  private UserGroupCache userGroupCache;

  private TaskTree taskTree;

  private UserDao userDao;

  /**
   * Rows contains the users and the last row contains the total sums. Columns of each rows are the man days of the projects (see
   * getProjectNames)
   */
  @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
  public HRViewData getResources(final HRFilter filter)
  {
    final HRViewData data = new HRViewData(filter);
    if (filter.getStartTime() == null) {
      final DayHolder day = new DayHolder();
      day.setBeginOfWeek();
      filter.setStartTime(day.getDate());
    }
    if (filter.getStopTime() == null) {
      final DayHolder day = new DayHolder(filter.getStartTime());
      day.setEndOfWeek();
      filter.setStopTime(day.getDate());
    }
    if (filter.isShowBookedTimesheets() == true) {
      final TimesheetFilter tsFilter = new TimesheetFilter();
      tsFilter.setStartTime(filter.getStartTime());
      tsFilter.setStopTime(filter.getStopTime());
      final List<TimesheetDO> sheets = timesheetDao.getList(tsFilter);
      for (final TimesheetDO sheet : sheets) {
        final PFUserDO user = userGroupCache.getUser(sheet.getUserId());
        if (user == null) {
          log.error("Oups, user of time sheet is null or unknown? Ignoring entry: " + sheet);
          continue;
        }
        final ProjektDO projekt = taskTree.getProjekt(sheet.getTaskId());
        final Object targetObject = getTargetObject(filter, projekt);
        if (targetObject == null) {
          data.addTimesheet(sheet, user);
        } else if (targetObject instanceof ProjektDO) {
          data.addTimesheet(sheet, user, (ProjektDO) targetObject);
        } else if (targetObject instanceof KundeDO) {
          data.addTimesheet(sheet, user, (KundeDO) targetObject);
        } else {
          log.error("Target object of type " + targetObject + " not supported.");
          data.addTimesheet(sheet, user);
        }
      }
    }
    if (filter.isShowPlanning() == true) {
      final HRPlanningFilter hrFilter = new HRPlanningFilter();
      final DateHolder date = new DateHolder(filter.getStartTime());
      hrFilter.setStartTime(date.getSQLDate()); // Considers the user's time zone.
      date.setDate(filter.getStopTime());
      hrFilter.setStopTime(date.getSQLDate()); // Considers the user's time zone.
      final List<HRPlanningDO> plannings = hrPlanningDao.getList(hrFilter);
      for (final HRPlanningDO planning : plannings) {
        if (planning.getEntries() == null) {
          continue;
        }
        for (final HRPlanningEntryDO entry : planning.getEntries()) {
          if (entry.isDeleted() == true) {
            continue;
          }
          final PFUserDO user = userGroupCache.getUser(planning.getUserId());
          final ProjektDO projekt = entry.getProjekt();
          final Object targetObject = getTargetObject(filter, projekt);
          if (targetObject == null) {
            data.addHRPlanningEntry(entry, user);
          } else if (targetObject instanceof ProjektDO) {
            data.addHRPlanningEntry(entry, user, (ProjektDO) targetObject);
          } else if (targetObject instanceof KundeDO) {
            data.addHRPlanningEntry(entry, user, (KundeDO) targetObject);
          } else {
            log.error("Target object of type " + targetObject + " not supported.");
            data.addHRPlanningEntry(entry, user);
          }
        }
      }
    }
    if (filter.isOnlyMyProjects() == true) {
      // remove all user entries which have no planning or booking on my projects.
      final List<HRViewUserData> list = data.getUserDatas();
      if (list != null) {
        final Iterator<HRViewUserData> it = list.iterator();
        while (it.hasNext() == true) {
          final HRViewUserData entry = it.next();
          boolean hasEntries = false;
          if (entry.entries != null) {
            for (final HRViewUserEntryData entryData : entry.entries) {
              if (entryData.projekt != null || entryData.kunde != null) {
                hasEntries = true;
                break;
              }
            }
          }
          if (hasEntries == false) {
            it.remove();
          }
        }
      }
    }
    return data;
  }

  /**
   * Returns a list of all users which are accessible by the current logged in user and not planned in the given HRViewData object.
   * @return Result list (may be empty but never null).
   */
  @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
  public List<PFUserDO> getUnplannedResources(final HRViewData data)
  {
    final List<PFUserDO> users = new ArrayList<PFUserDO>();
    final QueryFilter queryFilter = new QueryFilter(new BaseSearchFilter());
    queryFilter.addOrder(Order.asc("firstname")).addOrder(Order.asc("lastname"));
    final List<PFUserDO> allUsers = userDao.getList(queryFilter);
    if (allUsers != null) {
      for (final PFUserDO user : allUsers) {
        final HRViewUserData userData = data.getUserData(user);
        if (userData == null || NumberHelper.isNotZero(userData.getPlannedDaysSum()) == false) {
          users.add(user);
        }
      }
    }
    return users;
  }

  /**
   * Return the target object (ProjektDO, KundeDO or null) to which the entry (time sheet or planning) should be assigned to. The results
   * depends on the filter settings.
   * @param filter
   * @param projekt
   * @return
   */
  private Object getTargetObject(final HRFilter filter, final ProjektDO projekt)
  {
    if (projekt == null) {
      return null;
    }
    final KundeDO kunde = projekt.getKunde();
    if (filter.isOnlyMyProjects() == true) {
      if (isMyProject(projekt) == true) {
        if (filter.isAllProjectsGroupedByCustomer() == true) {
          return kunde;
        } else {
          return projekt;
        }
      } else {
        return null;
      }
    } else if (filter.isAllProjectsGroupedByCustomer() == true) {
      return kunde;
    } else if (filter.isOtherProjectsGroupedByCustomer() == true) {
      if (isMyProject(projekt) == true) {
        return projekt;
      } else {
        return kunde;
      }
    } else {
      // Show all projects
      return projekt;
    }
  }

  private boolean isMyProject(final ProjektDO projekt)
  {
    return (projekt != null && projekt.getProjektManagerGroup() != null && userGroupCache.isLoggedInUserMemberOfGroup(projekt
        .getProjektManagerGroupId()) == true);
  }

  public void setTimesheetDao(TimesheetDao timesheetDao)
  {
    this.timesheetDao = timesheetDao;
  }

  public void setHrPlanningDao(HRPlanningDao hrPlanningDao)
  {
    this.hrPlanningDao = hrPlanningDao;
  }

  public void setUserGroupCache(UserGroupCache userGroupCache)
  {
    this.userGroupCache = userGroupCache;
  }

  public void setTaskTree(TaskTree taskTree)
  {
    this.taskTree = taskTree;
  }

  public void setUserDao(UserDao userDao)
  {
    this.userDao = userDao;
  }

  /**
   * Throws UnsupportedOperationException.
   * @see org.projectforge.core.IDao#getList(org.projectforge.core.BaseSearchFilter)
   */
  public List<HRViewData> getList(BaseSearchFilter filter)
  {
    throw new UnsupportedOperationException();
  }

  public String[] getSearchFields()
  {
    return null;
  }

  /**
   * @return false.
   * @see org.projectforge.core.IDao#isHistorizable()
   */
  public boolean isHistorizable()
  {
    return false;
  }

  /**
   * @return true.
   * @see org.projectforge.core.IDao#hasInsertAccess()
   */
  public boolean hasInsertAccess(final PFUserDO user)
  {
    return hrPlanningDao.hasInsertAccess(user);
  }
}
