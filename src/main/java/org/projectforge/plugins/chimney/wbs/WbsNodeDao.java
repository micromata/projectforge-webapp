/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.wbs;

import java.util.List;

import org.hibernate.criterion.Restrictions;
import org.projectforge.core.QueryFilter;
import org.projectforge.task.TaskDO;

public class WbsNodeDao extends AbstractWBSNodeDao<AbstractWbsNodeDO>
{

  public WbsNodeDao()
  {
    super(AbstractWbsNodeDO.class);
    userRightId = WbsNodeRight.USER_RIGHT_ID;
  }

  public AbstractWbsNodeDO getByTaskDO(final TaskDO taskDO) {
    final QueryFilter queryFilter = new QueryFilter();
    queryFilter.add(Restrictions.eq("taskDo", taskDO));
    final List<AbstractWbsNodeDO> list = getList(queryFilter);
    if (list.isEmpty())
      return null;
    return list.get(0);
  }

  @Override
  public AbstractWbsNodeDO newInstance()
  {
    throw new UnsupportedOperationException("Instantiation of WbsNodeDO not possible.");
  }


}
