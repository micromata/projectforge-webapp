/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.gantt;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.joda.time.Period;
import org.joda.time.ReadablePeriod;
import org.projectforge.plugins.chimney.resourceplanning.ResourceAssignmentDO;
import org.projectforge.plugins.chimney.resourceplanning.ResourceAssignmentDao;
import org.projectforge.plugins.chimney.wbs.AbstractWbsNodeDO;
import org.projectforge.user.PFUserDO;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly=true, propagation=Propagation.SUPPORTS)
public class ResDaoBackedPlannedResourcesProvider implements PlannedResourcesProvider
{
  private ResourceAssignmentDao resDao;

  @Deprecated
  public ResDaoBackedPlannedResourcesProvider() {}

  public ResDaoBackedPlannedResourcesProvider(final ResourceAssignmentDao resDao)
  {
    this.resDao = resDao;
  }

  @Override
  public List<ReadablePeriod> findPlannedResources(final AbstractWbsNodeDO wbsNode)
  {
    final Map<PFUserDO, Period> users = new HashMap<PFUserDO, Period>();
    for (final ResourceAssignmentDO res: resDao.getListByWbsNode(wbsNode)) {
      final PFUserDO user = res.getUser();
      if (!users.containsKey(user)) {
        users.put(user, Period.ZERO);
      }

      users.put(user, users.get(user).plus(res.getPlannedEffort()));
    }
    return new LinkedList<ReadablePeriod>(users.values());
  }
}
