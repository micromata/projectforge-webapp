/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.teamcal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.projectforge.common.AbstractCache;
import org.projectforge.common.StringHelper;
import org.projectforge.user.GroupDO;
import org.projectforge.user.Login;
import org.projectforge.user.ProjectForgeGroup;
import org.springframework.orm.hibernate3.HibernateTemplate;

/**
 * The teamcal group relations will be cached with this class.
 * 
 * @author Maximilian Lauterbach (m.lauterbach@micromata.de)
 *
 */
public class TeamCalGroupCache extends AbstractCache
{
  private static Logger log = Logger.getLogger(TeamCalGroupCache.class);

  /** The key is the teamCal id and the value is a list of assigned groups. */
  private Map<Integer, Set<Integer>> teamCalGroupIdMap;

  /** group id + group */
  private Map<Integer, GroupDO> groupMap;

  /** teamCal id + teamCal */
  private Map<Integer, TeamCalDO> teamCalMap;

  private HibernateTemplate hibernateTemplate;

  public void setHibernateTemplate(final HibernateTemplate hibernateTemplate)
  {
    this.hibernateTemplate = hibernateTemplate;
  }

  /**
   * @see org.projectforge.common.AbstractCache#refresh()
   */
  @Override
  protected void refresh()
  {
    log.info("Initializing TeamCalGroupCache ...");
    // This method must not be synchronized because it works with a new copy of maps.

    final Map<Integer, TeamCalDO> tMap = new HashMap<Integer, TeamCalDO>();
    // Could not autowire UserDao because of cyclic reference with AccessChecker.
    //    TeamCalDO t = new TeamCalDO();
    //        final List<TeamCalDO> teamCals = t.get;
    //        for (final TeamCalDO teamCal : teamCals) {
    //          tMap.put(teamCal.getId(), teamCal);
    //        }
    final List<GroupDO> groups = Login.getInstance().getAllGroups();
    final Map<Integer, GroupDO> gMap = new HashMap<Integer, GroupDO>();
    final Map<Integer, Set<Integer>> tgIdMap = new HashMap<Integer, Set<Integer>>();
    for (final GroupDO group : groups) {
      gMap.put(group.getId(), group);
      //      if (group.getAssignedUsers() != null) {
      //        for (final PFUserDO user : group.getAssignedUsers()) {
      //          if (user != null) {
      //            final Set<Integer> groupIdSet = ensureAndGetTeamCalGroupIdMap(tgIdMap, user.getId());
      //            groupIdSet.add(group.getId());
      //          }
      //        }
      //      }
    }
    this.teamCalMap = tMap;
    this.groupMap = gMap;
    this.teamCalGroupIdMap = tgIdMap;

    //    final Map<Integer, List<UserRightDO>> rMap = new HashMap<Integer, List<UserRightDO>>();
    //    List<UserRightDO> rights;
    //    try {
    //      rights = hibernateTemplate.find("from UserRightDO t order by user.id, right_id");
    //    } catch (final Exception ex) {
    //      log.fatal("******* Exception while getting user rights from data-base (only OK for migration from older versions): "
    //          + ex.getMessage());
    //      rights = new ArrayList<UserRightDO>();
    //    }
    //    List<UserRightDO> list = null;
    //    Integer userId = null;
    //    for (final UserRightDO right : rights) {
    //      if (right.getUserId() == null) {
    //        log.warn("Oups, userId = null: " + right);
    //        continue;
    //      }
    //      if (right.getUserId().equals(userId) == false) {
    //        list = new ArrayList<UserRightDO>();
    //        userId = right.getUserId();
    //        if (userId != null) {
    //          rMap.put(userId, list);
    //        }
    //      }
    //      if (UserRights.instance().getRight(right.getRightId()).isAvailable(this, right.getUser()) == true) {
    //        list.add(right);
    //      }
    //    }
    //    this.rightMap = rMap;
    log.info("Initializing of TeamCalGroupCache done.");
  }

  public List<Integer> getAssignedGroups(final TeamCalDO teamcal) {
    final List<Integer> assigned = new ArrayList<Integer>();
    if (teamcal.getId() != null) {
      final Set<Integer> groups = teamCalGroupIdMap.get(teamcal);
      final Iterator<Integer> it = groups.iterator();
      while (it.hasNext()){
        assigned.add(it.next());
      }
    }
    return assigned;
  }

  /**
   * Checks if the given user is at least member of one of the given groups.
   * @param teamCal
   * @param groups
   */
  public boolean isUserMemberOfGroup(final TeamCalDO teamCal, final ProjectForgeGroup... groups)
  {
    return false;
  }

  public GroupDO getGroup(final Integer groupId)
  {
    checkRefresh();
    return getGroupMap().get(groupId);
  }

  public String getGroupname(final Integer groupId)
  {
    checkRefresh();
    final GroupDO group = getGroup(groupId);
    return group == null ? null : group.getName();
  }

  private Map<Integer, GroupDO> getGroupMap()
  {
    checkRefresh();
    return groupMap;
  }

  public String getGroupnames(final Integer teamCalId)
  {
    checkRefresh();
    final Set<Integer> groupSet = getTeamCalGroupIdMap().get(teamCalId);
    if (groupSet == null) {
      return "";
    }
    final List<String> list = new ArrayList<String>();
    for (final Integer groupId : groupSet) {
      final GroupDO group = getGroup(groupId);
      if (group != null) {
        list.add(group.getName());
      } else {
        log.error("Group with id " + groupId + " not found.");
      }
    }
    return StringHelper.listToString(list, "; ", true);
  }

  /**
   * Returns a collection of group id's to which teamcal is assigned to.
   * @param teamCal
   * @return collection if found, otherwise null.
   */
  public Collection<Integer> getTeamCalGroups(final TeamCalDO teamCal)
  {
    checkRefresh();
    return getTeamCalGroupIdMap().get(teamCal.getId());
  }

  public TeamCalDO getTeamCal(final Integer TeamCalId)
  {
    if (TeamCalId == null) {
      return null;
    }
    return getTeamCalMap() != null ? teamCalMap.get(TeamCalId) : null;
  }

  private Map<Integer, TeamCalDO> getTeamCalMap()
  {
    checkRefresh();
    return teamCalMap;
  }

  public TeamCalDO getTeamCal(final String teamCalTitle)
  {
    if (teamCalTitle == null) {
      return null;
    }
    for (final TeamCalDO teamCal : getTeamCalMap().values()) {
      if (teamCalTitle.equals(teamCal.getTitle()) == true) {
        return teamCal;
      }
    }
    return null;
  }

  /**
   * @return all teamcals (also deleted teamcals).
   */
  public Collection<TeamCalDO> getAllTeamCals()
  {
    return getTeamCalMap().values();
  }

  void updateTeamCal(final TeamCalDO teamCal)
  {
    getTeamCalMap().put(teamCal.getId(), teamCal);
  }

  private Map<Integer, Set<Integer>> getTeamCalGroupIdMap()
  {
    checkRefresh();
    return teamCalGroupIdMap;
  }

  private static Set<Integer> ensureAndGetTeamCalGroupIdMap(final Map<Integer, Set<Integer>> tgIdMap, final Integer teamCalId)
  {
    Set<Integer> set = tgIdMap.get(teamCalId);
    if (set == null) {
      set = new HashSet<Integer>();
      tgIdMap.put(teamCalId, set);
    }
    return set;
  }

  public boolean isTeamCalMemberOfGroup(final Integer teamCalId, final Integer groupId)
  {
    if (groupId == null) {
      return false;
    }
    checkRefresh();
    final Set<Integer> groupSet = getTeamCalGroupIdMap().get(teamCalId);
    return (groupSet != null) ? groupSet.contains(groupId) : false;
  }

}
